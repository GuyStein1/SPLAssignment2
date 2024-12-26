package bgu.spl.mics;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.objects.*;

import bgu.spl.mics.application.services.CameraService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CameraServiceTest {

    private CameraService cameraService;
    private Camera camera;

    @BeforeEach
    void setup() {
        // Prepare test data
        DetectedObject obj1 = new DetectedObject("Wall_1", "Wall");
        DetectedObject errorObj = new DetectedObject("ERROR", "Simulated error");
        StampedDetectedObjects detection1 = new StampedDetectedObjects(1, Arrays.asList(obj1));
        StampedDetectedObjects detectionError = new StampedDetectedObjects(2, Arrays.asList(errorObj));
        camera = new Camera(1, 1, Arrays.asList(detection1, detectionError));
        cameraService = new CameraService(camera);
    }

    @Test
    void testErrorDetection() throws InterruptedException {
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        messageBus.register(cameraService);

        // Run CameraService in its own thread
        Thread serviceThread = new Thread(cameraService::run);
        serviceThread.start();

        // Simulate TickBroadcast for the error tick
        messageBus.sendBroadcast(new TickBroadcast(2));

        // Wait for the CameraService to process the tick
        Thread.sleep(100);

        // Verify that a CrashedBroadcast was sent
        boolean crashedBroadcastSent = false;
        while (messageBus.awaitMessage(cameraService) instanceof CrashedBroadcast) {
            crashedBroadcastSent = true;
        }

        assertTrue(crashedBroadcastSent);

        serviceThread.join();
    }

    @Test
    void testDetectObjectsEvent() throws InterruptedException {
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        messageBus.register(cameraService);

        // Run CameraService in its own thread
        Thread serviceThread = new Thread(cameraService::run);
        serviceThread.start();

        // Simulate TickBroadcast for the detection tick
        messageBus.sendBroadcast(new TickBroadcast(1));

        // Wait for the CameraService to process the tick
        Thread.sleep(100);

        // Verify that a DetectObjectsEvent was sent
        boolean detectEventSent = false;
        while (messageBus.awaitMessage(cameraService) instanceof DetectObjectsEvent) {
            detectEventSent = true;
        }

        assertTrue(detectEventSent);

        serviceThread.join();
    }
}
