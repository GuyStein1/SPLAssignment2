package bgu.spl.mics;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class CameraTest {

    private Camera camera;

    @BeforeEach
    void setUp() {
        // Create a list of stamped detected objects
        List<StampedDetectedObjects> detectedObjectsList = Arrays.asList(
                new StampedDetectedObjects(1, Arrays.asList(
                        new DetectedObject("obj1", "description1"),
                        new DetectedObject("obj2", "description2")
                )),
                new StampedDetectedObjects(2, Arrays.asList(
                        new DetectedObject("obj3", "description3"),
                        new DetectedObject("ERROR", "Test Error")
                )),
                new StampedDetectedObjects(3, Arrays.asList(
                        new DetectedObject("obj5", "description5")
                ))
        );

        // Initialize the Camera instance with the detected objects list
        camera = new Camera(1, 1, detectedObjectsList);
    }

    @Test
    void testGetStampedDetectedObjectsAtTime_ValidTime() {
        // Test case for time = 2
        int testTime = 2;
        StampedDetectedObjects result = camera.getStampedDetectedObjectsAtTime(testTime);

        // Assertions
        assertNotNull(result, "StampedDetectedObjects should not be null for valid time.");
        assertEquals(2, result.getTime(), "The returned time should match the requested time.");
        assertEquals(2, result.getDetectedObjects().size(), "The number of detected objects should match.");
        assertEquals("obj3", result.getDetectedObjects().get(0).getId(), "The first detected object ID should match.");
        assertEquals("description3", result.getDetectedObjects().get(0).getDescription(), "The first detected object description should match.");
        assertEquals("ERROR", result.getDetectedObjects().get(1).getId(), "The second detected object ID should match.");
        assertEquals("Test Error", result.getDetectedObjects().get(1).getDescription(), "The second detected object description should match.");
    }

    @Test
    void testGetStampedDetectedObjectsAtTime_InvalidTime() {
        // Test case for a time that does not exist in the list
        int testTime = 5;
        StampedDetectedObjects result = camera.getStampedDetectedObjectsAtTime(testTime);

        // Assertions
        assertNull(result, "StampedDetectedObjects should be null for a time with no detections.");
    }

    @Test
    void testGetStampedDetectedObjectsAtTime_FirstAndLastTime() {
        // Test case for the first timestamp in the list
        int firstTime = 1;
        StampedDetectedObjects firstResult = camera.getStampedDetectedObjectsAtTime(firstTime);

        // Assertions for the first timestamp
        assertNotNull(firstResult, "StampedDetectedObjects should not be null for the first time.");
        assertEquals(1, firstResult.getTime(), "The time should match the first timestamp.");
        assertEquals(2, firstResult.getDetectedObjects().size(), "The number of detected objects should match for the first timestamp.");

        // Test case for the last timestamp in the list
        int lastTime = 3;
        StampedDetectedObjects lastResult = camera.getStampedDetectedObjectsAtTime(lastTime);

        // Assertions for the last timestamp
        assertNotNull(lastResult, "StampedDetectedObjects should not be null for the last time.");
        assertEquals(3, lastResult.getTime(), "The time should match the last timestamp.");
        assertEquals(1, lastResult.getDetectedObjects().size(), "The number of detected objects should match for the last timestamp.");
        assertEquals("obj5", lastResult.getDetectedObjects().get(0).getId(), "The detected object ID for the last timestamp should match.");
    }
}