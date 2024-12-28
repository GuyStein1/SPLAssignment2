package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {

    // Fields
    private double x; // X-coordinate
    private double y; // Y-coordinate

    /**
     * Constructor for CloudPoint.
     *
     * @param x The X-coordinate of the point.
     * @param y The Y-coordinate of the point.
     */
    public CloudPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the X-coordinate of the point.
     *
     * @return The X-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the Y-coordinate of the point.
     *
     * @return The Y-coordinate.
     */
    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
