package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    // Fields
    private final int id; // Unique identifier for the LiDAR worker
    private final int frequency; // Time interval at which the LiDAR worker sends new events
    private STATUS status; // Enum representing the LiDAR's current status (Up, Down, Error)
    private final List<TrackedObject> lastTrackedObjects; // List of last objects tracked by the LiDAR

    // Constructor
    public LiDarWorkerTracker(int id, int frequency, String filePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Initial status is set to UP
        this.lastTrackedObjects = Collections.synchronizedList(new ArrayList<>()); // Thread-safe list
    }

    /**
     * Retrieves the unique ID of the LiDAR worker tracker.
     *
     * @return The ID of the worker.
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the frequency of the LiDAR worker tracker.
     *
     * @return The frequency.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Retrieves the current status of the worker.
     *
     * @return The status (UP, DOWN, or ERROR).
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Sets the status of the worker.
     *
     * @param status The new status.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Updates the list of last tracked objects.
     *
     * @param trackedObjects The list of tracked objects.
     */
    public void updateLastTrackedObjects(List<TrackedObject> trackedObjects) {
            lastTrackedObjects.clear();
            lastTrackedObjects.addAll(trackedObjects);
    }

    /**
     * Gets the most recent tracked objects.
     *
     * @return A copy of the last tracked objects.
     */
    public List<TrackedObject> getLastTrackedObjects() {
            return new ArrayList<>(lastTrackedObjects);
    }


}
