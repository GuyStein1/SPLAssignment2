package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    // Fields
    private int time; // The time the objects were detected
    private List<DetectedObject> detectedObjects; // List of detected objects at this time step
    // TODO: Define fields and methods.
}
