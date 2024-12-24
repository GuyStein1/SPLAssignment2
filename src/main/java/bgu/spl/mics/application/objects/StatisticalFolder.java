package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    /**
     * The total runtime of the system, measured in ticks.
     */
    private int systemRuntime;

    /**
     * The cumulative count of objects detected by all cameras.
     * This includes both initial detections and subsequent re-detections.
     */
    private int numDetectedObjects;

    /**
     * The cumulative count of objects tracked by all LiDAR workers.
     * This count includes both new tracks and ongoing tracking of previously detected objects.
     */
    private int numTrackedObjects;

    /**
     * The total number of unique landmarks identified and mapped within the environment.
     * This count is updated only when new landmarks are added to the map.
     */
    private int numLandmarks;

    // Getters, setters, and additional methods can be added as needed.
}