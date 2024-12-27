package bgu.spl.mics.application.objects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description,
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {

    // Fields
    private final String id; // Unique identifier for the tracked object
    private final int time; // The time the object was tracked
    private final String description; // Description of the object
    private final List<CloudPoint> coordinates; // List of coordinates for the object

    /**
     * Constructor for TrackedObject.
     *
     * @param id          The unique identifier for the tracked object.
     * @param time        The time the object was tracked.
     * @param description The description of the tracked object.
     * @param coordinates The list of coordinates representing the object.
     */
    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        if (time < 0) {
            throw new IllegalArgumentException("Time must be non-negative.");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty.");
        }
        if (coordinates == null || coordinates.isEmpty()) {
            throw new IllegalArgumentException("Coordinates cannot be null or empty.");
        }

        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = Collections.unmodifiableList(coordinates);
    }

    /**
     * Gets the unique identifier for the tracked object.
     *
     * @return The object's unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the time when the object was tracked.
     *
     * @return The tracking time.
     */
    public int getTime() {
        return time;
    }

    /**
     * Gets the description of the tracked object.
     *
     * @return The description of the object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the list of coordinates for the tracked object.
     *
     * @return An unmodifiable list of coordinates.
     */
    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }
}