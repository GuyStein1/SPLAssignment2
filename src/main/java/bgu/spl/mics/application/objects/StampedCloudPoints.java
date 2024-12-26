package bgu.spl.mics.application.objects;
import java.util.List;
/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    // Fields
    private final String id; // Unique identifier for the object
    private final int time; // The time the cloud points were tracked
    private final List<List<Double>> cloudPoints; // List of 3D cloud points ([x, y, z])

    /**
     * Constructor for StampedCloudPoints.
     *
     * @param id          The unique identifier for the object.
     * @param time        The time the cloud points were tracked.
     * @param cloudPoints The list of 3D cloud points (each being a list of size 3: [x, y, z]).
     */
    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        if (time < 0) {
            throw new IllegalArgumentException("Time must be non-negative");
        }

        if (cloudPoints == null || cloudPoints.isEmpty()) {
            throw new IllegalArgumentException("CloudPoints cannot be null or empty");
        }

        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    /**
     * Gets the unique identifier for the object.
     *
     * @return The unique identifier (ID).
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the time at which the cloud points were tracked.
     *
     * @return The timestamp.
     */
    public int getTime() {
        return time;
    }

    /**
     * Gets the list of cloud points.
     *
     * @return The list of 3D cloud points.
     */
    public List<List<Double>> getCloudPoints() {
        return cloudPoints;
    }

    /**
     * Calculates the total number of cloud points stored.
     *
     * @return The total number of cloud points.
     */
    public int getTotalPoints() {
        return cloudPoints.size();
    }

}
