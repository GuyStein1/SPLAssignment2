package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
    // Fields
    private final String id; // Unique identifier for the detected object
    private final String description; // Description of the object

    /**
     * Constructor for DetectedObject.
     * Initializes a detected object with a unique ID and a description.
     *
     * @param id          The unique identifier for the detected object.
     * @param description The description of the object.
     */
    public DetectedObject(String id, String description) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty.");
        }
        this.id = id;
        this.description = description;
    }

    /**
     * Gets the unique ID of the detected object.
     *
     * @return The object's unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the description of the detected object.
     *
     * @return The object's description.
     */
    public String getDescription() {
        return description;
    }

}
