package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages crash-related output information.
 * This singleton stores necessary data to generate the crash output JSON file.
 */
public class CrashOutputManager {

    // Fields
    private final AtomicReference<String> faultySensor = new AtomicReference<>(null);
    private final AtomicReference<String> errorDescription = new AtomicReference<>(null);
    private final Map<String, StampedDetectedObjects> lastFramesOfCameras = new ConcurrentHashMap<>();
    private final List<LiDarWorkerTracker> liDars = Collections.synchronizedList(new ArrayList<>());

    // Private constructor to prevent instantiation
    private CrashOutputManager() {
    }

    // Static inner class for Singleton instance
    private static class CrashOutputManagerHolder {
        private static final CrashOutputManager INSTANCE = new CrashOutputManager();
    }

    public static CrashOutputManager getInstance() {
        return CrashOutputManagerHolder.INSTANCE;
    }

    // Getters and setters
    public String getFaultySensor() {
        return faultySensor.get();
    }

    // Make sure the faulty sensor is the first one that had an error.
    public void setFaultySensor(String faultySensor) {
        this.faultySensor.compareAndSet(null, faultySensor);
    }

    // Make sure the error description matches the first sensor that had an error.
    public void setErrorDescription(String errorDescription) {
        this.errorDescription.compareAndSet(null, errorDescription);
    }

    public String getErrorDescription() {
        return errorDescription.get();
    }

    public Map<String, StampedDetectedObjects> getLastFramesOfCameras() {
        return lastFramesOfCameras;
    }

    public List<LiDarWorkerTracker> getLiDars() {
        return liDars;
    }

    // Method to generate crash output JSON
    public void generateCrashOutput() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject output = new JsonObject();
        output.addProperty("error", errorDescription.get());
        output.addProperty("faultySensor", faultySensor.get());
        output.add("lastCamerasFrame", gson.toJsonTree(lastFramesOfCameras));

        // Collect last tracked objects from all LiDARs
        JsonObject lidarJson = new JsonObject();
        for (LiDarWorkerTracker lidar : liDars) {
            List<TrackedObject> lastTracked = lidar.getLastTrackedObjects();
            if (!lastTracked.isEmpty()) {
                lidarJson.add("LiDarTrackerWorker" + lidar.getId(), gson.toJsonTree(lastTracked));
            }
        }
        output.add("lastLidarFrames", lidarJson);

        output.add("poses", gson.toJsonTree(FusionSlam.getInstance().getPoses()));

        JsonObject statisticsWithLandmarks = gson.toJsonTree(StatisticalFolder.getInstance()).getAsJsonObject();

        // Create a JsonObject for landmarks
        JsonObject landmarksJson = new JsonObject();
        for (LandMark landmark : FusionSlam.getInstance().getLandmarks()) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("id", landmark.getId());
            landmarkJson.addProperty("description", landmark.getDescription());
            landmarkJson.add("coordinates", gson.toJsonTree(landmark.getCoordinates()));

            // Use the landmark's ID as the key in the landmarks object
            landmarksJson.add(landmark.getId(), landmarkJson);
        }
        statisticsWithLandmarks.add("landmarks", landmarksJson);

        // Add the combined object to the output
        output.add("statistics", statisticsWithLandmarks);

        // Write to file
        try (FileWriter writer = new FileWriter(FusionSlam.getInstance().getOutputPath() + "OutputError.json")) {
            gson.toJson(output, writer);
            System.out.println("CrashOutputManager: Crash output written to OutputError.json");
        } catch (IOException e) {
            System.err.println("CrashOutputManager: Failed to write crash output. " + e.getMessage());
        }
    }
}
