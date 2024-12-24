package bgu.spl.mics.application.objects;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    /**
     * The unique identifier of the tracked object.
     */
    private String id;

    /**
     * The time (in some unit) when the object was tracked.
     */
    private int time;

    /**
     * A description of the tracked object.
     */
    private String description;

    /**
     * The coordinates of the tracked object, represented as an array of CloudPoint objects.
     * Each CloudPoint represents a point in the 3D environment.
     */
    private CloudPoint[] coordinates;

    // Getters, setters, and additional methods can be added as needed.
}
