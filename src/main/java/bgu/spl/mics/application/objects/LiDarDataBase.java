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

    // Private constructor to prevent external instantiation
    private LiDarDataBase(String filePath) {
        cloudPoints = loadData(filePath);
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
     * Retrieves the most recent StampedCloudPoints for a given object ID and time.
     *
     * @param id   The ID of the object to retrieve cloud points for.
     * @param time The timestamp to match.
     * @return The StampedCloudPoints if a match is found; otherwise, null.
     */
    public StampedCloudPoints getCloudPoints(String id, int time) {
        for (StampedCloudPoints cloud : cloudPoints) {
            // Check if the ID and time match
            if (cloud.getId().equals(id) && cloud.getTime() == time) {
                return cloud;
            }
            // If the ID is "ERROR" and time matches, return this as an error
            if (cloud.getId().equals("ERROR") && cloud.getTime() == time) {
                return cloud;
            }
        }
        return null; // No matching cloud points found
    }

    /**
     * Loads the cloud points data from a JSON file.
     *
     * @return A list of StampedCloudPoints.
     */
    private List<StampedCloudPoints> loadData(String filePath) {
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
