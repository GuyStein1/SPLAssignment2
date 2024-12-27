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
    private List<StampedCloudPoints> cloudPoints; // Coordinates of objects per time

    /**
     * Private constructor for the singleton.
     *
     * @param filePath Path to the LiDAR data JSON file.
     */
    private LiDarDataBase(String filePath) {
        this.cloudPoints = loadData(filePath);
    }

    /**
     * Inner static helper class to hold the singleton instance.
     */
    private static class SingletonHelper {
        private static LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        // Set the file path before accessing the instance
        if (SingletonHelper.INSTANCE == null) {
            SingletonHelper.INSTANCE = new LiDarDataBase(filePath);
        }
        return SingletonHelper.INSTANCE;
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
     * @param filePath The path to the JSON file.
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
