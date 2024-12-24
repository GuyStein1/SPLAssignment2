package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

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
        messageBus.register(testMicroService1);
        messageBus.register(testMicroService2);
    }

    @Test
    void testSubscribeEventAndSendEvent() throws InterruptedException {
        // Arrange
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        // Act
        Future<String> future = messageBus.sendEvent(testEvent);

        // Assert
        assertNotNull(future, "sendEvent should return a non-null Future for an active subscriber.");

        // Ensure the event is delivered to the correct MicroService
        Message receivedMessage = messageBus.awaitMessage(testMicroService1);
        assertEquals(testEvent, receivedMessage, "The MicroService should receive the correct event.");

        // Complete the event and check the resolved value
        messageBus.complete(testEvent, "Result");
        assertTrue(future.isDone(), "Future should be marked as done after completing the event.");
        assertEquals("Result", future.get(), "The Future should resolve to the correct result.");
    }

    @Test
    void testSubscribeBroadcastAndSendBroadcast() throws InterruptedException {
        // Arrange
        class TestBroadcast implements Broadcast {}
        TestBroadcast testBroadcast = new TestBroadcast();

        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService2);

        // Act
        messageBus.sendBroadcast(testBroadcast);

        // Assert
        // Ensure both MicroServices receive the broadcast
        Message receivedMessage1 = messageBus.awaitMessage(testMicroService1);
        Message receivedMessage2 = messageBus.awaitMessage(testMicroService2);

        assertEquals(testBroadcast, receivedMessage1, "MicroService1 should receive the correct broadcast.");
        assertEquals(testBroadcast, receivedMessage2, "MicroService2 should receive the correct broadcast.");
    }

    @Test
    void testRegisterAndUnregister() throws InterruptedException {
        // Arrange
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        // Act
        messageBus.unregister(testMicroService1);
        Future<String> future = messageBus.sendEvent(testEvent);

        // Assert
        assertNull(future, "sendEvent should return null if no MicroService is subscribed.");

        // Test awaitMessage after unregistering
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService1),
                "awaitMessage should throw an exception if the MicroService is not registered.");
    }

    @Test
    void testAwaitMessageBlocksUntilMessageIsAvailable() throws InterruptedException {
        // Arrange
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        // Act
        Thread senderThread = new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate delay
                messageBus.sendEvent(testEvent);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        senderThread.start();

        // Assert
        long startTime = System.currentTimeMillis();
        Message receivedMessage = messageBus.awaitMessage(testMicroService1);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertEquals(testEvent, receivedMessage, "The MicroService should receive the correct event.");
        assertTrue(elapsedTime >= 100, "awaitMessage should block until a message is available.");
    }

    @Test
    void testSendEventWithNoSubscribers() {
        // Arrange
        class TestEvent implements Event<String> {}
        TestEvent testEvent = new TestEvent();

        // Act
        Future<String> future = messageBus.sendEvent(testEvent);

        // Assert
        assertNull(future, "sendEvent should return null if no MicroService is subscribed to the event.");
    }

    @Test
    void testRoundRobinEventDeliveryWithManyMicroServices() throws InterruptedException {
        // Arrange
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

        // Act
        // Send events equal to the number of MicroServices
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < numMicroServices; i++) {
            futures.add(messageBus.sendEvent(new TestEvent()));
        }

        // Assert
        // Each MicroService should receive exactly one event
        for (MicroService m : microServices) {
            Message receivedMessage = messageBus.awaitMessage(m);
            assertTrue(receivedMessage instanceof TestEvent, "Each MicroService should receive an event.");
        }

        // Ensure all futures are resolved
        for (Future<String> future : futures) {
            assertNotNull(future, "Each event should create a Future.");
            assertFalse(future.isDone(), "Futures should be unresolved initially.");
        }
    }

    @Test
    void testConcurrentMessageHandling() throws InterruptedException {
        // Arrange
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

        // Act
        // Start multiple threads to send messages concurrently
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

        // Assert
        for (MicroService m : microServices) {
            for (int i = 0; i < 5; i++) { // Check that each MicroService processes multiple messages
                Message message = messageBus.awaitMessage(m);
                assertNotNull(message, "Each MicroService should receive messages.");
            }
        }

        eventSender.join();
        broadcastSender.join();
    }
}