package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {

    // Fields
    private String id; // Unique identifier for the tracked object
    private int time; // The time the object was tracked
    private String description; // Description of the object
    private List<CloudPoint> coordinates; // List of 3D coordinates for the object
}
