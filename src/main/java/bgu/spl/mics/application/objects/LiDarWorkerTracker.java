package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    // Fields
    private int id; // Unique identifier for the LiDAR worker
    private int frequency; // Time interval at which the LiDAR worker sends new events
    private STATUS status; // Enum representing the LiDAR's current status (Up, Down, Error)
    private List<TrackedObject> lastTrackedObjects; // List of last objects tracked by the LiDAR

    // TODO: Define fields and methods.
}
