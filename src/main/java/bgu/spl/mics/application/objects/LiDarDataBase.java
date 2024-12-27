package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    // Fields
    private final List<StampedCloudPoints> cloudPoints; // Coordinates of objects per time
    //Path to the LiDAR data JSON file.
    private final String filePath;

    // Private constructor to prevent external instantiation
    private LiDarDataBase(String filePath) {
        this.filePath = filePath;
        cloudPoints = loadData();
    }

    // Static inner "Holder" class for singleton instantiation
    private static class LDBHolder {
        private static LiDarDataBase instance = null;

        // Handles actual singleton creation logic
        private static LiDarDataBase createInstance(String filePath) {
            if (instance == null) {
                instance = new LiDarDataBase(filePath); // Create and initialize the singleton
            }
            return instance; // Return the instance (only initialized once)
        }
    }

    /**
     * Returns the singleton instance of LiDarDataBase. Initializes it using the file path if
     * this is the first call.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        return LDBHolder.createInstance(filePath); // Ensures initialization
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance() {
        // Called after file path was given in the first get in the main program
        return LDBHolder.instance; // Ensures initialization
    }

    /**
     * Retrieves the most recent StampedCloudPoints for a given object ID.
     *
     * @param id The ID of the object to retrieve cloud points for.
     * @return The StampedCloudPoints.
     * @throws IllegalArgumentException if no data is found for the given ID.
     */
    public StampedCloudPoints getCloudPoints(String id) {
        StampedCloudPoints matchingCloudPoints = null;

        for (StampedCloudPoints cloud : cloudPoints) {
            if (cloud.getId().equals(id)) {
                matchingCloudPoints = cloud;
            }
        }
        return matchingCloudPoints;
    }

    /**
     * Loads the cloud points data from a JSON file.
     *
     * @return A list of StampedCloudPoints.
     */
    private List<StampedCloudPoints> loadData() {
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("Error loading LiDAR data from file: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Use this instead of List.of()
        }
    }
}
