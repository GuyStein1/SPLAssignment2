package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import java.util.List;

/**
 * DetectObjectsEvent is sent by a CameraService to request LiDAR workers
 * to process and track detected objects.
 */
public class DetectObjectsEvent implements Event<Boolean> {

    private final int time; // The time at which the objects were detected.
    private final int sentTime; // The time at which the event was sent.
    private final List<DetectedObject> detectedObjects; // List of detected objects.

    /**
     * Constructor for DetectObjectsEvent.
     *
     * @param time           The time at which the objects were detected.
     * @param detectedObjects The list of detected objects.
     */
    public DetectObjectsEvent(int time, int sentTime, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.sentTime = sentTime;
        this.detectedObjects = detectedObjects;
    }

    /**
     * Gets the time at which the objects were detected.
     *
     * @return The detection time.
     */
    public int getTime() {
        return time;
    }

    /**
     * Gets the time at which the event was sent.
     *
     * @return The time the event was sent.
     */
    public int getSentTime() {
        return sentTime;
    }

    /**
     * Gets the list of detected objects.
     *
     * @return The detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}