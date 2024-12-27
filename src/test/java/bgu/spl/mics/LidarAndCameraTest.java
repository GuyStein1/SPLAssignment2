package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.messages.broadcasts.*;
import bgu.spl.mics.application.messages.events.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LidarAndCameraTest {

    private LiDarDataBase lidarDatabase;

    @BeforeEach
    public void setup() {
        StatisticalFolder.getInstance().reset();

        // Initialize LiDARDataBase with the provided test data file
        String lidarDataFilePath = "C:\\Users\\guyst\\OneDrive\\Desktop\\GitHubRepos\\SPLAssignment2\\example input\\lidar_data.json"; // Update path as necessary
        lidarDatabase = LiDarDataBase.getInstance(lidarDataFilePath);
    }

    @Test
    public void testSimulationWithMultipleCamerasAndLiDARs() throws InterruptedException {
        // Setup Cameras
        StampedDetectedObjects camera1Detection1 = new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("Wall_1", "Wall")
        ));
        StampedDetectedObjects camera1Detection2 = new StampedDetectedObjects(4, Arrays.asList(
                new DetectedObject("Wall_3", "Wall"),
                new DetectedObject("Chair_Base_1", "Chair Base")
        ));
        Camera camera1 = new Camera(1, 3, Arrays.asList(camera1Detection1, camera1Detection2)); // Frequency = 3
        CameraService cameraService1 = new CameraService(camera1);

        StampedDetectedObjects camera2Detection1 = new StampedDetectedObjects(6, Arrays.asList(
                new DetectedObject("Wall_4", "Wall")
        ));
        StampedDetectedObjects camera2Detection2 = new StampedDetectedObjects(8, Arrays.asList(
                new DetectedObject("Wall_5", "Wall")
        ));
        Camera camera2 = new Camera(2, 2, Arrays.asList(camera2Detection1, camera2Detection2)); // Frequency = 2
        CameraService cameraService2 = new CameraService(camera2);

        // Setup LiDARs
        LiDarWorkerTracker lidar1 = new LiDarWorkerTracker(1, 2); // Frequency = 2
        LiDarService lidarService1 = new LiDarService(lidar1);

        LiDarWorkerTracker lidar2 = new LiDarWorkerTracker(2, 3); // Frequency = 3
        LiDarService lidarService2 = new LiDarService(lidar2);

        // TimeService
        TimeService timeService = new TimeService(1, 10); // Tick time = 1s, duration = 10 ticks

        // Run all services in separate threads
        Thread cameraThread1 = new Thread(cameraService1);
        Thread cameraThread2 = new Thread(cameraService2);
        Thread lidarThread1 = new Thread(lidarService1);
        Thread lidarThread2 = new Thread(lidarService2);
        Thread timeThread = new Thread(timeService);

        cameraThread1.start();
        cameraThread2.start();
        lidarThread1.start();
        lidarThread2.start();

        // Make sure threads finish initializing before time service
        Thread.sleep(100);

        timeThread.start();

        // Wait for all services to complete
        timeThread.join();

        // Assert: Verify the correct processing of detections and tracked objects
        assertEquals(5, StatisticalFolder.getInstance().getNumDetectedObjects(),
                "The number of detected objects should match the total detections from cameras.");
        assertTrue(StatisticalFolder.getInstance().getNumTrackedObjects() > 0,
                "LiDAR services should track objects and update the statistics.");

        // Ensure threads are terminated
        assertFalse(cameraThread1.isAlive(), "CameraService 1 thread should terminate.");
        assertFalse(cameraThread2.isAlive(), "CameraService 2 thread should terminate.");
        assertFalse(lidarThread1.isAlive(), "LiDAR Service 1 thread should terminate.");
        assertFalse(lidarThread2.isAlive(), "LiDAR Service 2 thread should terminate.");
        assertFalse(timeThread.isAlive(), "TimeService thread should terminate.");

        // Cleanup
        if (cameraThread1.isAlive()) cameraThread1.interrupt();
        if (cameraThread2.isAlive()) cameraThread2.interrupt();
        if (lidarThread1.isAlive()) lidarThread1.interrupt();
        if (lidarThread2.isAlive()) lidarThread2.interrupt();
        if (timeThread.isAlive()) timeThread.interrupt();
    }

    @Test
    public void testSimulationWithThreeCamerasAndLiDARs() throws InterruptedException {
        // Setup Cameras
        StampedDetectedObjects camera1Detection1 = new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("Wall_1", "Wall")
        ));
        StampedDetectedObjects camera1Detection2 = new StampedDetectedObjects(4, Arrays.asList(
                new DetectedObject("Wall_3", "Wall")
        ));
        Camera camera1 = new Camera(1, 2, Arrays.asList(camera1Detection1, camera1Detection2)); // Frequency = 2
        CameraService cameraService1 = new CameraService(camera1);

        StampedDetectedObjects camera2Detection1 = new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("Chair_Base_1", "Chair Base")
        ));
        StampedDetectedObjects camera2Detection2 = new StampedDetectedObjects(5, Arrays.asList(
                new DetectedObject("Circular_Base_1", "Circular Base")
        ));
        Camera camera2 = new Camera(2, 3, Arrays.asList(camera2Detection1, camera2Detection2)); // Frequency = 3
        CameraService cameraService2 = new CameraService(camera2);

        StampedDetectedObjects camera3Detection1 = new StampedDetectedObjects(3, Arrays.asList(
                new DetectedObject("Wall_4", "Wall")
        ));
        StampedDetectedObjects camera3Detection2 = new StampedDetectedObjects(6, Arrays.asList(
                new DetectedObject("Door", "Door")
        ));
        Camera camera3 = new Camera(3, 3, Arrays.asList(camera3Detection1, camera3Detection2)); // Frequency = 3
        CameraService cameraService3 = new CameraService(camera3);

        // Setup LiDARs
        LiDarWorkerTracker lidar1 = new LiDarWorkerTracker(1, 2); // Frequency = 2
        LiDarService lidarService1 = new LiDarService(lidar1);

        LiDarWorkerTracker lidar2 = new LiDarWorkerTracker(2, 3); // Frequency = 3
        LiDarService lidarService2 = new LiDarService(lidar2);

        LiDarWorkerTracker lidar3 = new LiDarWorkerTracker(3, 4); // Frequency = 4
        LiDarService lidarService3 = new LiDarService(lidar3);

        // TimeService
        TimeService timeService = new TimeService(1, 10); // Tick time = 1s, duration = 10 ticks

        // Run all services in separate threads
        Thread cameraThread1 = new Thread(cameraService1);
        Thread cameraThread2 = new Thread(cameraService2);
        Thread cameraThread3 = new Thread(cameraService3);
        Thread lidarThread1 = new Thread(lidarService1);
        Thread lidarThread2 = new Thread(lidarService2);
        Thread lidarThread3 = new Thread(lidarService3);
        Thread timeThread = new Thread(timeService);

        cameraThread1.start();
        cameraThread2.start();
        cameraThread3.start();
        lidarThread1.start();
        lidarThread2.start();
        lidarThread3.start();

        // Ensure threads finish initializing before time service
        Thread.sleep(100);

        timeThread.start();

        // Wait for all services to complete
        timeThread.join();

        // Assert: Verify the correct processing of detections and tracked objects
        assertEquals(6, StatisticalFolder.getInstance().getNumDetectedObjects(),
                "The number of detected objects should match the total detections from cameras.");
        assertTrue(StatisticalFolder.getInstance().getNumTrackedObjects() > 0,
                "LiDAR services should track objects and update the statistics.");

        // Ensure threads are terminated
        assertFalse(cameraThread1.isAlive(), "CameraService 1 thread should terminate.");
        assertFalse(cameraThread2.isAlive(), "CameraService 2 thread should terminate.");
        assertFalse(cameraThread3.isAlive(), "CameraService 3 thread should terminate.");
        assertFalse(lidarThread1.isAlive(), "LiDAR Service 1 thread should terminate.");
        assertFalse(lidarThread2.isAlive(), "LiDAR Service 2 thread should terminate.");
        assertFalse(lidarThread3.isAlive(), "LiDAR Service 3 thread should terminate.");
        assertFalse(timeThread.isAlive(), "TimeService thread should terminate.");

        // Cleanup
        if (cameraThread1.isAlive()) cameraThread1.interrupt();
        if (cameraThread2.isAlive()) cameraThread2.interrupt();
        if (cameraThread3.isAlive()) cameraThread3.interrupt();
        if (lidarThread1.isAlive()) lidarThread1.interrupt();
        if (lidarThread2.isAlive()) lidarThread2.interrupt();
        if (lidarThread3.isAlive()) lidarThread3.interrupt();
        if (timeThread.isAlive()) timeThread.interrupt();
    }
}
