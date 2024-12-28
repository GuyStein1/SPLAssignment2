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
    private final List<LandMark> landmarks; // Represents the map of the environment
    private final List<Pose> poses; // List of previous poses needed for calculations
    private int activeCameras; // Number of currently active camera sensors
    private int activeLiDars; // Number of currently active LiDAR sensors
    private int activeSensors; // Total number of currently active sensors (both cameras and LiDARs)
    private boolean terminated;

    private final Object lock = new Object();

    // Singleton Holder Pattern ensures lazy initialization and thread safety
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    /**
     * Provides access to the Singleton instance of FusionSlam.
     *
     * @return The Singleton instance.
     */
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Private constructor to prevent direct instantiation
    private FusionSlam() {
        landmarks = new ArrayList<>();
        poses = new ArrayList<>();
        activeCameras = 0;
        activeLiDars = 0;
        activeSensors = 0;
        terminated = false;
    }

    // Setter for active cameras
    public void setActiveCameras(int activeCameras) {
        this.activeCameras = activeCameras;
    }

    // Setter for active LiDARs
    public void setActiveLiDars(int activeLiDars) {
        this.activeLiDars = activeLiDars;
    }

    // Setter for total active sensors
    public void setActiveSensors(int activeSensors) {
        this.activeSensors = activeSensors;
    }

    // Getter for active cameras
    public int getActiveCameras() {
        return activeCameras;
    }

    // Getter for active LiDARs
    public int getActiveLiDars() {
        return activeLiDars;
    }

    // Getter for total active sensors
    public int getActiveSensors() {
        return activeSensors;
    }

    public boolean isTerminated() {
        synchronized (lock) {
            return terminated;
        }
    }

    public void setTerminated(boolean terminated) {
        synchronized (lock) {
            this.terminated = terminated;
        }
    }

    /**
     * Adds a new landmark to the global map.
     *
     * @param landMark The landmark to add.
     */
    public void addLandmark(LandMark landMark) {
        landmarks.add(landMark);
    }

    /**
     * Adds a new pose to the list of robot poses.
     *
     * @param pose The pose to add.
     */
    public void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * Retrieves the list of landmarks in the global map.
     *
     * @return A copy of the list of landmarks to avoid external modification.
     */
    public List<LandMark> getLandmarks() {
        return new ArrayList<>(landmarks);
    }

    /**
     * Retrieves the list of robot poses.
     *
     * @return A copy of the list of poses to avoid external modification.
     */
    public List<Pose> getPoses() {
        return new ArrayList<>(poses);
    }

    /**
     * Finds a pose by its timestamp.
     *
     * @param timestamp The timestamp of the desired pose.
     * @return The Pose matching the given timestamp, or null if none is found.
     */
    public Pose getPoseByTimestamp(int timestamp) {
        for (Pose pose : poses) {
            if (pose.getTime() == timestamp) {
                return pose;
            }
        }
        return null; // No matching pose found
    }

    /**
     * Transforms local coordinates to global coordinates using the robot's pose.
     *
     * @param localCoordinates The list of local CloudPoints to transform.
     * @param currentPose      The robot's pose (position and orientation).
     * @return A list of CloudPoints in the global frame.
     */
    public List<CloudPoint> transformToGlobal(List<CloudPoint> localCoordinates, Pose currentPose) {
        if (currentPose == null || localCoordinates == null) {
            throw new IllegalArgumentException("Invalid pose or coordinates");
        }

        double yaw = Math.toRadians(currentPose.getYaw()); // Convert yaw to radians
        double xRobot = currentPose.getX(); // Robot's global X-coordinate
        double yRobot = currentPose.getY(); // Robot's global Y-coordinate

        List<CloudPoint> globalCoordinates = new ArrayList<>();
        for (CloudPoint localPoint : localCoordinates) {
            double xLocal = localPoint.getX(); // Local X-coordinate
            double yLocal = localPoint.getY(); // Local Y-coordinate

            // Perform rotation and translation to transform local to global coordinates
            double xGlobal = xLocal * Math.cos(yaw) - yLocal * Math.sin(yaw) + xRobot;
            double yGlobal = xLocal * Math.sin(yaw) + yLocal * Math.cos(yaw) + yRobot;

            // Add transformed CloudPoint to the list
            globalCoordinates.add(new CloudPoint(xGlobal, yGlobal));
        }

        return globalCoordinates;
    }
}
