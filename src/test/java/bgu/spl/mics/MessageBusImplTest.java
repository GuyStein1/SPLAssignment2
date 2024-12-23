package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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
}