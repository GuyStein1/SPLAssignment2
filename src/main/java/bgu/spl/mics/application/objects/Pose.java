package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    // Fields
    private final float x; // X-coordinate of the robot
    private final float y; // Y-coordinate of the robot
    private final float yaw; // Orientation angle relative to the charging station
    private final int time; // Time when the robot reaches this pose

    // Constructor
    public Pose(int time, float x, float y, float yaw) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.yaw = yaw;
    }

    // Getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getYaw() {
        return yaw;
    }

    public int getTime() {
        return time;
    }

    // To String for debug purposes
    @Override
    public String toString() {
        return "Pose{time=" + time + ", x=" + x + ", y=" + y + ", yaw=" + yaw + "}";
    }
}
