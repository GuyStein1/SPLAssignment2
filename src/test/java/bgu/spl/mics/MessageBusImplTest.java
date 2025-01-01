package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private MicroService testMicroService1;
    private MicroService testMicroService2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance(); // Singleton instance
        // Create two test MicroServices for the tests
        testMicroService1 = new MicroService("TestMicroService1") {
            @Override
            protected void initialize() {
                // No initialization logic for testing
            }
        };
        testMicroService2 = new MicroService("TestMicroService2") {
            @Override
            protected void initialize() {
                // No initialization logic for testing
            }
        };
        // Register both MicroServices with the MessageBus
        messageBus.register(testMicroService1);
        messageBus.register(testMicroService2);
    }

    @Test
    void testSubscribeEventAndSendEvent() throws InterruptedException {
        // Arrange: Create a custom event and subscribe MicroService1 to it
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        // Act: Send the event and retrieve the Future
        Future<String> future = messageBus.sendEvent(testEvent);

        // Assert: Verify the Future is not null and the event is delivered
        assertNotNull(future, "sendEvent should return a non-null Future for an active subscriber.");
        Message receivedMessage = messageBus.awaitMessage(testMicroService1);
        assertEquals(testEvent, receivedMessage, "The MicroService should receive the correct event.");

        // Complete the event and validate the Future resolution
        messageBus.complete(testEvent, "Result");
        assertTrue(future.isDone(), "Future should be marked as done after completing the event.");
        assertEquals("Result", future.get(), "The Future should resolve to the correct result.");
    }

    @Test
    void testSubscribeBroadcastAndSendBroadcast() throws InterruptedException {
        // Arrange: Create a broadcast message and subscribe both MicroServices
        class TestBroadcast implements Broadcast {}
        TestBroadcast testBroadcast = new TestBroadcast();

        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService2);

        // Act: Send the broadcast message
        messageBus.sendBroadcast(testBroadcast);

        // Assert: Verify both MicroServices receive the broadcast
        Message receivedMessage1 = messageBus.awaitMessage(testMicroService1);
        Message receivedMessage2 = messageBus.awaitMessage(testMicroService2);

        assertEquals(testBroadcast, receivedMessage1, "MicroService1 should receive the correct broadcast.");
        assertEquals(testBroadcast, receivedMessage2, "MicroService2 should receive the correct broadcast.");
    }

    @Test
    void testRegisterAndUnregister() throws InterruptedException {
        // Arrange: Create a custom event and subscribe MicroService1 to it
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        // Act: Unregister MicroService1 and try sending an event
        messageBus.unregister(testMicroService1);
        Future<String> future = messageBus.sendEvent(testEvent);

        // Assert: Ensure the future is null and awaitMessage throws an exception
        assertNull(future, "sendEvent should return null if no MicroService is subscribed.");
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService1),
                "awaitMessage should throw an exception if the MicroService is not registered.");
    }

    @Test
    void testAwaitMessageBlocksUntilMessageIsAvailable() throws InterruptedException {
        // Arrange: Create a custom event and subscribe MicroService1 to it
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        // Act: Start a thread to send the event after a delay
        Thread senderThread = new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate a delay
                messageBus.sendEvent(testEvent);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        senderThread.start();

        // Assert: Verify awaitMessage blocks until the event is sent
        long startTime = System.currentTimeMillis();
        Message receivedMessage = messageBus.awaitMessage(testMicroService1);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertEquals(testEvent, receivedMessage, "The MicroService should receive the correct event.");
        assertTrue(elapsedTime >= 100, "awaitMessage should block until a message is available.");

        senderThread.join(); // Ensure the thread completes
    }

    @Test
    void testSendEventWithNoSubscribers() {
        // Arrange: Create a custom event
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        // Act: Send the event without any subscribers
        Future<String> future = messageBus.sendEvent(testEvent);

        // Assert: Ensure the future is null
        assertNull(future, "sendEvent should return null if no MicroService is subscribed to the event.");
    }

    @Test
    void testRoundRobinEventDeliveryWithManyMicroServices() throws InterruptedException {
        // Arrange: Create a custom event and multiple MicroServices
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        int numMicroServices = 50; // Test with 50 MicroServices
        List<MicroService> microServices = new ArrayList<>();

        // Register and subscribe all MicroServices to the event
        for (int i = 0; i < numMicroServices; i++) {
            MicroService m = new MicroService("MicroService" + i) {
                @Override
                protected void initialize() {
                    // No initialization logic
                }
            };
            microServices.add(m);
            messageBus.register(m);
            messageBus.subscribeEvent(TestEvent.class, m);
        }

        // Act: Send events equal to the number of MicroServices
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < numMicroServices; i++) {
            futures.add(messageBus.sendEvent(new TestEvent()));
        }

        // Assert: Ensure each MicroService receives one event
        for (MicroService m : microServices) {
            Message receivedMessage = messageBus.awaitMessage(m);
            assertTrue(receivedMessage instanceof TestEvent, "Each MicroService should receive an event.");
        }

        // Verify all futures are unresolved initially
        for (Future<String> future : futures) {
            assertNotNull(future, "Each event should create a Future.");
            assertFalse(future.isDone(), "Futures should be unresolved initially.");
        }
    }

    @Test
    void testConcurrentMessageHandling() throws InterruptedException {
        // Arrange: Create a custom event and broadcast
        class TestEvent implements Event<String> {}
        class TestBroadcast implements Broadcast {}

        int numMicroServices = 20;
        List<MicroService> microServices = new ArrayList<>();

        // Register and subscribe all MicroServices
        for (int i = 0; i < numMicroServices; i++) {
            MicroService m = new MicroService("MicroService" + i) {
                @Override
                protected void initialize() {
                    // No initialization logic
                }
            };
            microServices.add(m);
            messageBus.register(m);
            messageBus.subscribeEvent(TestEvent.class, m);
            messageBus.subscribeBroadcast(TestBroadcast.class, m);
        }

        // Act: Start threads to send events and broadcasts concurrently
        Thread eventSender = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                messageBus.sendEvent(new TestEvent());
            }
        });

        Thread broadcastSender = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                messageBus.sendBroadcast(new TestBroadcast());
            }
        });

        eventSender.start();
        broadcastSender.start();

        // Assert: Verify all MicroServices process multiple messages
        for (MicroService m : microServices) {
            for (int i = 0; i < 5; i++) {
                Message message = messageBus.awaitMessage(m);
                assertNotNull(message, "Each MicroService should receive messages.");
            }
        }

        eventSender.join();
        broadcastSender.join();
    }
}
