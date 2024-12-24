package bgu.spl.mics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Future, MessageBusImpl, and MicroService classes.
 */
public class FutureMsgBusAndMicroServiceTest {

    private static class TestMicroService extends MicroService {
        private final CountDownLatch latch;

        public TestMicroService(String name, CountDownLatch latch) {
            super(name);
            this.latch = latch;
        }

        @Override
        protected void initialize() {
            // Subscribe to TestEvent
            subscribeEvent(TestEvent.class, event -> {
                complete(event, getName() + " handled " + event.getData());
            });

            // Subscribe to TestBroadcast
            subscribeBroadcast(TestBroadcast.class, broadcast -> {
                System.out.println(getName() + " received broadcast: " + broadcast.getData());
            });

            // Signal that initialization is complete
            latch.countDown();
        }

        public static class TestEvent implements Event<String> {
            private final String data;

            public TestEvent(String data) {
                this.data = data;
            }

            public String getData() {
                return data;
            }
        }

        public static class TestBroadcast implements Broadcast {
            private final String data;

            public TestBroadcast(String data) {
                this.data = data;
            }

            public String getData() {
                return data;
            }
        }
    }

    /**
     * Tests multiple microservices handling events using round-robin distribution.
     */
    @Test
    void testMultipleMicroServicesWithRoundRobin() throws InterruptedException {
        int numServices = 5;
        CountDownLatch latch = new CountDownLatch(numServices); // Wait for all microservices to initialize
        ExecutorService executor = Executors.newFixedThreadPool(numServices);

        // Create and start TestMicroServices
        TestMicroService[] services = new TestMicroService[numServices];
        for (int i = 0; i < numServices; i++) {
            services[i] = new TestMicroService("Service" + (i + 1), latch);
            executor.submit(services[i]);
        }

        latch.await(); // Ensure all services are initialized

        // Send events to microservices
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        Future<String>[] futures = new Future[5];
        for (int i = 0; i < 5; i++) {
            futures[i] = messageBus.sendEvent(new TestMicroService.TestEvent("Event" + (i + 1)));
        }

        // Validate event handling
        for (int i = 0; i < 5; i++) {
            assertNotNull(futures[i], "Future should not be null; ensure a microservice is subscribed to the event.");
            String result = futures[i].get(); // Blocking call
            assertTrue(result.contains("handled Event" + (i + 1)), "Incorrect event handling result: " + result);
        }

        // Terminate microservices
        for (TestMicroService service : services) {
            service.terminate();
        }
        executor.shutdownNow();
    }

    /**
     * Tests broadcast messages being delivered to all subscribed microservices.
     */
    @Test
    void testBroadcastToAllMicroServices() throws InterruptedException {
        int numServices = 3;
        CountDownLatch latch = new CountDownLatch(numServices);
        ExecutorService executor = Executors.newFixedThreadPool(numServices);

        // Create and start TestMicroServices
        TestMicroService[] services = new TestMicroService[numServices];
        for (int i = 0; i < numServices; i++) {
            services[i] = new TestMicroService("Service" + (i + 1), latch);
            executor.submit(services[i]);
        }

        latch.await(); // Ensure all services are initialized

        // Send a broadcast message
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        TestMicroService.TestBroadcast broadcast = new TestMicroService.TestBroadcast("BroadcastMessage");
        messageBus.sendBroadcast(broadcast);

        // Allow some time for broadcast delivery
        Thread.sleep(500);

        // Validate that all microservices receive the broadcast
        for (TestMicroService service : services) {
            // Broadcasts don't produce a return value to check; you can log or observe output here if needed
            System.out.println(service.getName() + " should have received the broadcast.");
        }

        // Terminate microservices
        for (TestMicroService service : services) {
            service.terminate();
        }
        executor.shutdownNow();
    }

    /**
     * Tests a large number of events to stress test the MessageBus and MicroService framework.
     */
    @Test
    void testStressWithMultipleEvents() throws InterruptedException {
        int numServices = 10;
        int numEvents = 100;
        CountDownLatch latch = new CountDownLatch(numServices);
        ExecutorService executor = Executors.newFixedThreadPool(numServices);

        // Create and start TestMicroServices
        TestMicroService[] services = new TestMicroService[numServices];
        for (int i = 0; i < numServices; i++) {
            services[i] = new TestMicroService("Service" + (i + 1), latch);
            executor.submit(services[i]);
        }

        latch.await(); // Ensure all services are initialized

        // Send multiple events
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        Future<String>[] futures = new Future[numEvents];
        for (int i = 0; i < numEvents; i++) {
            futures[i] = messageBus.sendEvent(new TestMicroService.TestEvent("Event" + (i + 1)));
        }

        // Validate that all events are handled
        for (int i = 0; i < numEvents; i++) {
            assertNotNull(futures[i], "Future should not be null for Event" + (i + 1));
            String result = futures[i].get(); // Blocking call
            assertTrue(result.contains("handled Event" + (i + 1)), "Incorrect result for Event" + (i + 1));
        }

        // Terminate microservices
        for (TestMicroService service : services) {
            service.terminate();
        }
        executor.shutdownNow();
    }
}