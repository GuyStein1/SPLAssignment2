package bgu.spl.mics.application.objects;

import java.util.ArrayList;
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
        this.detectedObjectsList = detectedObjectsList != null ? detectedObjectsList : new ArrayList<>();
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
     * Adds a new stamped detected object to the list.
     *
     * @param stampedDetectedObjects The new stamped detected object to add.
     */
    public void addDetectedObjects(StampedDetectedObjects stampedDetectedObjects) {
        detectedObjectsList.add(stampedDetectedObjects);
    }
}
