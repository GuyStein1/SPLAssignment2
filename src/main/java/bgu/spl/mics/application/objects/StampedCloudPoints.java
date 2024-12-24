package bgu.spl.mics.application.objects;
import java.util.List;
/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    // Fields
    private String id; // Unique identifier for the object
    private int time; // The time the cloud points were tracked
    private List<List<Double>> cloudPoints; // List of 3D cloud points ([x, y, z])

    // TODO: Define fields and methods.
}
