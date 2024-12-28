package bgu.spl.mics.application.objects;

import java.util.Collections;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    // Fields
    private final int id; // Unique identifier for the camera
    private final int frequency; // Time interval at which the camera sends new events
    private STATUS status; // Enum representing the camera's current status (Up, Down, Error)
    private final List<StampedDetectedObjects> detectedObjectsList; // Time-stamped list of detected objects

    /**
     * Constructor for Camera.
     *
     * @param id                The unique identifier for the camera.
     * @param frequency         The time interval (in ticks) at which the camera sends new events.
     * @param detectedObjectsList The initial list of detected objects.
     */
    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status is UP
        this.detectedObjectsList = detectedObjectsList != null
                ? Collections.unmodifiableList(detectedObjectsList)
                : Collections.emptyList(); // Ensure immutability of preloaded data
    }

    /**
     * Gets the camera's unique identifier.
     *
     * @return The camera ID.
     */
    public int getID() {
        return id;
    }

    /**
     * Gets the camera's frequency.
     *
     * @return The frequency of the camera.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Gets the camera's status.
     *
     * @return The current status of the camera.
     */
    public STATUS getStatus() {
            return status;
    }

    /**
     * Sets the camera's status.
     *
     * @param status The new status for the camera.
     */
    public void setStatus(STATUS status) {
            this.status = status;
    }

    /**
     * Gets the StampedDetectedObjects for a specific time.
     *
     * @param currentTime The current simulation tick.
     * @return The StampedDetectedObjects for the given time, or null if no detections exist.
     */
    public StampedDetectedObjects getStampedDetectedObjectsAtTime(int currentTime) {
        for (StampedDetectedObjects stampedObjects : detectedObjectsList) {
            if (stampedObjects.getTime() == currentTime) {
                return stampedObjects;
            }
        }
        return null; // Return null if no detections are found at the given time
    }

    /**
     * Checks if there are any future detections remaining in the camera.
     *
     * @param currentTick The current simulation tick.
     * @return True if there are no more future detections, false otherwise.
     */
    public boolean hasNoMoreDetections(int currentTick) {
        for (StampedDetectedObjects detection : detectedObjectsList) {
            if (detection.getTime() > currentTick) {
                return false; // There are future detections
            }
        }
        return true; // No future detections
    }
}
