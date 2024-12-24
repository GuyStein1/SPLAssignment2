package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // Fields
    private String id; // Unique identifier for the landmark
    private String description; // Description of the landmark
    private List<CloudPoint> coordinates; // List of coordinates in the global frame
}
