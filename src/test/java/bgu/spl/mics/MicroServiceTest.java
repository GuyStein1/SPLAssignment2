package bgu.spl.mics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MicroService class using the TestMicroService concrete implementation.
 * Includes multiple tests to validate the message loop, event handling, and broadcast handling.
 */
class MicroServiceTest {

    @Test
    void testMultipleMicroServices() throws InterruptedException {
        int numServices = 3;
        CountDownLatch latch = new CountDownLatch(numServices); // Latch to wait for initialization
        ExecutorService executor = Executors.newFixedThreadPool(numServices);

        // Create TestMicroServices
        TestMicroService ms1 = new TestMicroService("Service1", latch);
        TestMicroService ms2 = new TestMicroService("Service2", latch);
        TestMicroService ms3 = new TestMicroService("Service3", latch);

        // Start MicroServices
        executor.submit(ms1);
        executor.submit(ms2);
        executor.submit(ms3);

        // Wait for all MicroServices to initialize
        latch.await();

        // Send events to the MicroServices
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        Future<String> future1 = messageBus.sendEvent(new TestMicroService.TestEvent("Event1"));
        Future<String> future2 = messageBus.sendEvent(new TestMicroService.TestEvent("Event2"));
        Future<String> future3 = messageBus.sendEvent(new TestMicroService.TestEvent("Event3"));

        // Ensure futures are not null
        assertNotNull(future1, "Future1 should not be null; ensure a microservice is subscribed to the event.");
        assertNotNull(future2, "Future2 should not be null; ensure a microservice is subscribed to the event.");
        assertNotNull(future3, "Future3 should not be null; ensure a microservice is subscribed to the event.");

        // Validate that all events are handled by some microservice
        assertTrue(future1.get().contains("handled Event1"), "Event1 was not handled correctly.");
        assertTrue(future2.get().contains("handled Event2"), "Event2 was not handled correctly.");
        assertTrue(future3.get().contains("handled Event3"), "Event3 was not handled correctly.");

        // Send a broadcast
        TestMicroService.TestBroadcast broadcast = new TestMicroService.TestBroadcast("BroadcastMessage");
        messageBus.sendBroadcast(broadcast);

        // Terminate the MicroServices
        ms1.terminate();
        ms2.terminate();
        ms3.terminate();
        executor.shutdownNow();
    }
}