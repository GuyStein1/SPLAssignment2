package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    // Fields
    private final List<LandMark> landmarks; // Represents the map of the environment
    private final List<Pose> poses; // List of previous poses needed for calculations
    private final AtomicInteger activeCameras; // Number of currently active camera sensors
    private int activeSensors; // Total number of currently active sensors (both cameras and LiDARs)
    private boolean terminated;
    private boolean crashed;
    private boolean timeTerminated;

    private final Object lock = new Object();

    // Singleton Holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Private constructor to prevent direct instantiation
    private FusionSlam() {
        landmarks = new ArrayList<>();
        poses = new ArrayList<>();
        activeCameras = new AtomicInteger(0);
        activeSensors = 0;
        terminated = false;
        crashed = false;
        timeTerminated = false;
    }

    // Setter for active cameras
    public void setActiveCameras(int activeCameras) {
        this.activeCameras.set(activeCameras);
    }

    // Setter for total active sensors
    public void setActiveSensors(int activeSensors) {
        this.activeSensors = activeSensors;
    }

    // Getter for active cameras
    public int getActiveCameras() {
        return activeCameras.get();
    }

    // Getter for total active sensors
    public int getActiveSensors() {
        return activeSensors;
    }

    // Getter and Setter to check if fusion slam terminated.
    // Synchronized because TimeService and FusionSlam access it
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

    // Getter for crashed
    public boolean isCrashed() {
        return crashed;
    }

    // Setter for crashed
    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    // Getter for time terminated
    public boolean isTimeTerminated() {
        return timeTerminated;
    }
    // Setter for time terminated
    public void setTimeTerminated (boolean timeTerminated) {
        this.timeTerminated = timeTerminated;
    }

    // Adds a new landmark to the global map
    public void addLandmark(LandMark landMark) {
        landmarks.add(landMark);
    }

    // Adds a new pose to the list of robot poses
    public void addPose(Pose pose) {
        poses.add(pose);
    }

    // Retrieves the list of landmarks in the global map
    public List<LandMark> getLandmarks() {
        return new ArrayList<>(landmarks);
    }


    // Retrieves the list of robot poses
    public List<Pose> getPoses() {
        return new ArrayList<>(poses);
    }


    // Finds a pose by its timestamp
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
     *                         Each CloudPoint represents a point in the robot's local coordinate system.
     * @param currentPose      The robot's pose (position and orientation), including location (x, y) and yaw (rotation).
     * @return A list of CloudPoints transformed to the global coordinate system.
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

    /**
     * Processes a tracked object by updating existing landmarks or adding new ones (transform tracked objects to landmarks).
     *
     * @param trackedObject The tracked object containing coordinates, a unique ID, detected time and a description.
     * @param pose          The robot's pose at the time the tracked object was detected. Used to transform
     *                      the object's local coordinates into the global coordinate system.
     */
    public void processTrackedObject(TrackedObject trackedObject, Pose pose) {
        // Check if the landmark already exists
        boolean exists = false;
        for (LandMark landMark : getLandmarks()) {
            if (landMark.getId().equals(trackedObject.getId())) {
                exists = true;
                // Transform the trackedObject's coordinates (CloudPoints) to the global frame
                List<CloudPoint> globalCoordinates = transformToGlobal(trackedObject.getCoordinates(), pose);
                // Update coordinates by averaging the last measurements with the new ones
                landMark.updateCoordinates(globalCoordinates);
                break;
            }
        }
        // If landmark does not exist, create and add a new one
        if (!exists) {
            List<CloudPoint> globalCoordinates = transformToGlobal(trackedObject.getCoordinates(), pose);
            LandMark newLandMark = new LandMark(
                    trackedObject.getId(),
                    trackedObject.getDescription(),
                    globalCoordinates
            );
            addLandmark(newLandMark);
            StatisticalFolder.getInstance().incrementLandmarks(1);
        }
    }

    /**
     * Resets the state of the FusionSlam singleton.
     * This method is intended for testing purposes only.
     */
    public void reset() {
        landmarks.clear();
        poses.clear();
        activeCameras.set(0);
        activeSensors = 0;
        terminated = false;
        crashed = false;
        timeTerminated = false;
    }
}
