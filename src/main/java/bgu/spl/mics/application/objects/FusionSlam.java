package bgu.spl.mics.application.objects;
import java.util.ArrayList;
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

    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    public synchronized void addLandmark(LandMark landMark) {
        landmarks.add(landMark);
    }

    public synchronized void addPose(Pose pose) {
        poses.add(pose);
    }

    public synchronized List<LandMark> getLandmarks() {
        return new ArrayList<>(landmarks);
    }

    public synchronized List<Pose> getPoses() {
        return new ArrayList<>(poses);
    }
}
