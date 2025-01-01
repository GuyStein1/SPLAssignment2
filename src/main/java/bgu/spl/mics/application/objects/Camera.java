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


    // Constructor
    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status is UP
        this.detectedObjectsList = detectedObjectsList != null
                ? Collections.unmodifiableList(detectedObjectsList)
                : Collections.emptyList(); // Ensure immutability of preloaded data
    }

    // Gets the id of the camera
    public int getID() {
        return id;
    }


    // Gets the camera's frequency
    public int getFrequency() {
        return frequency;
    }


    // Gets the camera's status
    public STATUS getStatus() {
            return status;
    }


    // Sets the camera's status
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

    // Checks if there are any future detections remaining in the camera
    public boolean hasNoMoreDetections(int currentTick) {
        for (StampedDetectedObjects detection : detectedObjectsList) {
            if (detection.getTime() > currentTick) {
                return false; // There are future detections
            }
        }
        return true; // No future detections
    }
}
