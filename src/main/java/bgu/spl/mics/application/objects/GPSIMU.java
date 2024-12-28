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
    private final List<Pose> poseList; // List of time-stamped poses

    public GPSIMU(List<Pose> poseList) {
        currentTick = 0;
        this.status = STATUS.UP; // Default status
        this.poseList = poseList;
    }

    /**
     * Updates the current tick.
     *
     * @param tick The current tick.
     */
    public void updateTick(int tick) {
        this.currentTick = tick;
    }

    /**
     * Gets the current pose based on the tick.
     *
     * @return The current pose, or null if no pose is available for the tick.
     */
    public Pose getCurrentPose() {
        for (Pose pose : poseList) {
            if (pose.getTime() == currentTick) {
                return pose;
            }
        }
        return null; // No pose available for the current tick
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}

