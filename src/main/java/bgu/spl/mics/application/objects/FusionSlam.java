package bgu.spl.mics.application.objects;
import java.util.List;


/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Fields
    private List<LandMark> landmarks; // Represents the map of the environment
    private List<Pose> poses; // List of previous poses needed for calculations

    // Singleton instance holder
    private static class FusionSlamHolder {
        // TODO: Implement singleton instance logic.
    }
}
