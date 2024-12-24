package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {

    // Fields
    private int id; // Unique identifier for the camera
    private int frequency; // Time interval at which the camera sends new events
    private STATUS CameraStatus; // Enum representing the camera's current status (Up, Down, Error)
    private List<StampedDetectedObjects> detectedObjectsList; // Time-stamped list of detected objects
}
