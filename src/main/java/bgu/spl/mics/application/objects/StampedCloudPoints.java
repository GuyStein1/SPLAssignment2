package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {

    // Fields
    private final String id; // Unique identifier for the object
    private final int time; // The time the cloud points were tracked
    private final List<List<Double>> cloudPoints; // List of cloud points

    // Constructor
    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    // Getters
    public String getId() {
        return id;
    }


    public int getTime() {
        return time;
    }

    // Converts the raw cloud points to a list of CloudPoint objects
    public List<CloudPoint> getCoordinates() {
        List<CloudPoint> coordinates = new ArrayList<>();
        for (List<Double> point : cloudPoints) {
                coordinates.add(new CloudPoint(point.get(0), point.get(1)));
        }
        return coordinates;
    }

    // Retrieves the total number of cloud points stored
    public int getTotalPoints() {
        return cloudPoints.size();
    }

}
