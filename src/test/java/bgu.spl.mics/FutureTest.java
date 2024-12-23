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
        assertTrue(future.isDone()); // Check if future is marked as done
        assertEquals("Test Result", future.get()); // Check the result is set correctly
    }

    @Test
    void testGetBlocksUntilResolved() {
        // Arrange
        Future<String> future = new Future<>();

        // Act & Assert
        Thread resolverThread = new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate delay before resolving
                future.resolve("Delayed Result");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        resolverThread.start();

        assertEquals("Delayed Result", future.get()); // `get()` should block until resolved
    }

    @Test
    void testGetWithTimeoutReturnsNullWhenNotResolved() {
        // Arrange
        Future<String> future = new Future<>();

        // Act
        String result = future.get(500, TimeUnit.MILLISECONDS);

        // Assert
        assertNull(result); // Since it wasn't resolved, expect null
    }

    @Test
    void testGetWithTimeoutReturnsResultIfResolvedBeforeTimeout() {
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
        assertEquals("Result Before Timeout", result); // Ensure correct result is returned
    }

    @Test
    void testResolveOnlyOnce() {
        // Arrange
        Future<String> future = new Future<>();
        future.resolve("First Result");

        // Act
        future.resolve("Second Result"); // Attempt to resolve again

        // Assert
        assertEquals("First Result", future.get()); // Should remain the first result
    }
}