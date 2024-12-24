package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    // Fields
    private int currentTick; // Current time in ticks
    private STATUS status; // Enum representing the GPS/IMU status (Up, Down, Error)
    private List<Pose> poseList; // List of time-stamped poses
}
