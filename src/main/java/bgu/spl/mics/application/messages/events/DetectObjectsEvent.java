package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.List;

/**
 * DetectObjectsEvent is sent by a CameraService to request LiDAR workers
 * to process and track detected objects.
 */
public class DetectObjectsEvent implements Event<DetectedObject> {

    private final int time; // The time at which the objects were detected.
    private final List<DetectedObject> detectedObjects; // List of detected objects.

    // Constructor
    public DetectObjectsEvent(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    // Getters
    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}