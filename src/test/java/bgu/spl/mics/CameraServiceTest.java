package bgu.spl.mics;

import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class CameraServiceTest {

    @BeforeEach
    public void resetStatisticalFolder() {
        StatisticalFolder.getInstance().reset();
    }

    @Test
    public void testCameraServiceProcessingTickBroadcasts() throws InterruptedException {
        // Setup: Camera with detected objects
        StampedDetectedObjects detection1 = new StampedDetectedObjects(1, Arrays.asList(
                new DetectedObject("Wall_1", "Wall"),
                new DetectedObject("Door_1", "Door")
        ));
        StampedDetectedObjects detection2 = new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("Wall_2", "Wall")
        ));

        Camera camera = new Camera(1, 2, Arrays.asList(detection1, detection2)); // Frequency = 2
        CameraService cameraService = new CameraService(camera);

        // Start CameraService in its own thread
        Thread cameraThread = new Thread(cameraService);
        cameraThread.start();

        Thread.sleep(50);

        // Simulate TimeService broadcasting TickBroadcast
        TimeService timeService = new TimeService(1, 5); // Tick time = 1s, duration = 5 ticks
        Thread timeThread = new Thread(timeService);
        timeThread.start();

        // Wait for TimeService to complete
        timeThread.join();

        // Assert: CameraService processed detections
        assertEquals(3, StatisticalFolder.getInstance().getNumDetectedObjects(),
                "CameraService should process and detect objects correctly.");

        // Ensure threads are terminated
        assertFalse(cameraThread.isAlive(), "CameraService thread should terminate.");
        assertFalse(timeThread.isAlive(), "TimeService thread should terminate.");

        // Cleanup
        if (cameraThread.isAlive()) cameraThread.interrupt();
        if (timeThread.isAlive()) timeThread.interrupt();
    }

    @Test
    public void testMultipleCameraServicesProcessingTicks() throws InterruptedException {
        // Setup: Multiple Cameras with detected objects
        StampedDetectedObjects camera1Detection1 = new StampedDetectedObjects(1, Arrays.asList(
                new DetectedObject("Wall_1", "Wall"),
                new DetectedObject("Door_1", "Door")
        ));
        StampedDetectedObjects camera1Detection2 = new StampedDetectedObjects(3, Arrays.asList(
                new DetectedObject("Wall_2", "Wall")
        ));
        Camera camera1 = new Camera(1, 2, Arrays.asList(camera1Detection1, camera1Detection2)); // Frequency = 2

        StampedDetectedObjects camera2Detection1 = new StampedDetectedObjects(1, Arrays.asList(
                new DetectedObject("Chair_1", "Chair"),
                new DetectedObject("Table_1", "Table")
        ));
        StampedDetectedObjects camera2Detection2 = new StampedDetectedObjects(4, Arrays.asList(
                new DetectedObject("Chair_2", "Chair")
        ));
        Camera camera2 = new Camera(2, 3, Arrays.asList(camera2Detection1, camera2Detection2)); // Frequency = 3

        // Initialize CameraServices
        CameraService cameraService1 = new CameraService(camera1);
        CameraService cameraService2 = new CameraService(camera2);

        // Start CameraServices in their own threads
        Thread cameraThread1 = new Thread(cameraService1);
        Thread cameraThread2 = new Thread(cameraService2);
        cameraThread1.start();
        cameraThread2.start();

        // Wait briefly to ensure CameraServices have started
        Thread.sleep(100);

        // Simulate TimeService broadcasting TickBroadcasts
        TimeService timeService = new TimeService(1, 10); // Tick time = 1 second, duration = 10 ticks
        Thread timeThread = new Thread(timeService);
        timeThread.start();

        // Wait for TimeService to complete
        timeThread.join();

        // Assert: CameraServices processed detections correctly
        int expectedDetectedObjects = 6; // 3 objects from camera 1 + 3 objects from camera 2
        assertEquals(expectedDetectedObjects, StatisticalFolder.getInstance().getNumDetectedObjects(),
                "All CameraServices should process and detect objects correctly.");

        // Ensure threads are terminated
        assertFalse(cameraThread1.isAlive(), "CameraService 1 thread should terminate.");
        assertFalse(cameraThread2.isAlive(), "CameraService 2 thread should terminate.");
        assertFalse(timeThread.isAlive(), "TimeService thread should terminate.");

        // Cleanup
        if (cameraThread1.isAlive()) cameraThread1.interrupt();
        if (cameraThread2.isAlive()) cameraThread2.interrupt();
        if (timeThread.isAlive()) timeThread.interrupt();
    }
}