package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        // Use CopyOnWriteArrayList for thread-safe read-mostly access
        this.detectedObjects = detectedObjects != null
                ? new CopyOnWriteArrayList<>(detectedObjects)
                : new CopyOnWriteArrayList<>();
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
     * @return An immutable copy of the list for safe read access.
     */
    public List<DetectedObject> getDetectedObjects() {
        return Collections.unmodifiableList(new ArrayList<>(detectedObjects)); // Safe for readers
    }
}

