package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // Fields
    private final String id; // Unique identifier for the landmark
    private final String description; // Description of the landmark
    private final List<CloudPoint> coordinates; // List of coordinates in the global frame

    // Constructor
    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    // Updates the coordinates of the landmark by averaging the new CloudPoints with the existing ones
    public void updateCoordinates(List<CloudPoint> newCoordinates) {
        for (int i = 0; i < Math.min(coordinates.size(), newCoordinates.size()); i++) {
            CloudPoint currentPoint = coordinates.get(i);
            CloudPoint newPoint = newCoordinates.get(i);

            // Average only x and y, ignore z
            double newX = (currentPoint.getX() + newPoint.getX()) / 2;
            double newY = (currentPoint.getY() + newPoint.getY()) / 2;

            currentPoint.setX(newX);
            currentPoint.setY(newY);
        }

        // Append extra CloudPoints from the new list
        if (newCoordinates.size() > coordinates.size()) {
            coordinates.addAll(newCoordinates.subList(coordinates.size(), newCoordinates.size()));
        }

        // No action needed for remaining old CloudPoints if new list is shorter
    }

}
