package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

/**
 * DetectObjectsEvent is sent by a CameraService to request LiDAR workers
 * to process and track detected objects.
 */
public class DetectObjectsEvent implements Event<Boolean> {

    private final StampedDetectedObjects stampedDetectedObjects;

    /**
     * Constructor for DetectObjectsEvent.
     */
    public DetectObjectsEvent(StampedDetectedObjects stampedDetectedObjects) {
        this.stampedDetectedObjects = stampedDetectedObjects;
    }

    /**
     * Gets the time at which the objects were detected.
     *
     * @return The detection time.
     */
    public int getTime() {
        return stampedDetectedObjects.getTime();
    }

    /**
     * Gets the list of detected objects.
     *
     * @return The detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        return stampedDetectedObjects.getDetectedObjects();
    }
}