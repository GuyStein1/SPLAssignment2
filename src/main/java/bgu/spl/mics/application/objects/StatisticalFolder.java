package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    // Fields
    private int systemRuntime; // Total runtime of the system, measured in ticks
    private int numDetectedObjects; // Cumulative count of objects detected by cameras
    private int numTrackedObjects; // Cumulative count of objects tracked by LiDAR workers
    private int numLandmarks; // Total number of unique landmarks identified and mapped
}