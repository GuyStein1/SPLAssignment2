package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {

    // Fields
    private final int time; // The time the objects were detected
    private final List<DetectedObject> detectedObjects; // List of detected objects at this time step

    /**
     * Constructor for StampedDetectedObjects.
     *
     * @param time             The time step at which the objects were detected.
     * @param detectedObjects  List of detected objects at this timestamp.
     */
    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        // Use a thread-safe, unmodifiable list for detected objects
        // Defensive copy protects against external modifications to the provided list
        this.detectedObjects = detectedObjects != null
                ? Collections.synchronizedList(new ArrayList<>(detectedObjects))
                : Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Get the time stamp of detection.
     *
     * @return The time (tick) when objects were detected.
     */
    public int getTime() {
        return time; // Immutable, so directly return
    }

    /**
     * Get the list of detected objects at this timestamp.
     *
     * @return A thread-safe copy of the list of detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        synchronized (detectedObjects) {
            // Prevent concurrent modification issues by copying
            return new ArrayList<>(detectedObjects);
        }
    }

    /**
     * Get the total number of detected objects.
     *
     * @return The count of detected objects in the list.
     */
    public int getDetectedObjectCount() {
        synchronized (detectedObjects) {
            return detectedObjects.size(); // Thread-safe size access
        }
    }
}

