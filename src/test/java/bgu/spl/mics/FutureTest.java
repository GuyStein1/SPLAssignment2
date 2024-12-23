package bgu.spl.mics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FutureTest {

    @Test
    void testResolveSetsResult() {
        // Arrange
        Future<String> future = new Future<>();

        // Act
        future.resolve("Test Result");

        // Assert
        assertTrue(future.isDone(), "Future should be marked as done after resolve.");
        assertEquals("Test Result", future.get(), "The result should match the resolved value.");
    }

    @Test
    void testGetBlocksUntilResolved() throws InterruptedException {
        // Arrange
        Future<String> future = new Future<>();

        // Act
        Thread resolverThread = new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate delay before resolving
                future.resolve("Delayed Result");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        resolverThread.start();

        long startTime = System.currentTimeMillis();
        String result = future.get(); // `get()` should block until resolved
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Assert
        assertEquals("Delayed Result", result, "The result should match the resolved value.");
        assertTrue(elapsedTime >= 100, "get() should block until resolve is called.");
    }

    @Test
    void testGetWithTimeoutReturnsNullWhenNotResolved() {
        // Arrange
        Future<String> future = new Future<>();

        // Act
        String result = future.get(500, TimeUnit.MILLISECONDS);

        // Assert
        assertNull(result, "get(timeout) should return null if Future is not resolved within the timeout.");
    }

    @Test
    void testGetWithTimeoutReturnsResultIfResolvedBeforeTimeout() throws InterruptedException {
        // Arrange
        Future<String> future = new Future<>();

        Thread resolverThread = new Thread(() -> {
            try {
                Thread.sleep(200); // Simulate delay before resolving
                future.resolve("Result Before Timeout");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        resolverThread.start();

        // Act
        String result = future.get(500, TimeUnit.MILLISECONDS);

        // Assert
        assertEquals("Result Before Timeout", result, "get(timeout) should return the resolved result if resolved before the timeout.");
    }

    @Test
    void testResolveOnlyOnce() {
        // Arrange
        Future<String> future = new Future<>();
        future.resolve("First Result");

        // Act
        future.resolve("Second Result"); // Attempt to resolve again

        // Assert
        assertEquals("First Result", future.get(), "The result should not change after the first resolve.");
    }

    @Test
    void testIsDoneReturnsFalseInitially() {
        // Arrange
        Future<String> future = new Future<>();

        // Act & Assert
        assertFalse(future.isDone(), "Future should not be marked as done before resolve.");
    }

    @Test
    void testIsDoneReturnsTrueAfterResolve() {
        // Arrange
        Future<String> future = new Future<>();

        // Act
        future.resolve("Test Result");

        // Assert
        assertTrue(future.isDone(), "Future should be marked as done after resolve.");
    }
}
