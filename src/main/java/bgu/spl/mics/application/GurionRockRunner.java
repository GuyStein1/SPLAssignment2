package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: Configuration file path is required as the first argument.");
            return;
        }

        String configPath = args[0];
        PoseService poseService = null; // Declare PoseService here to resolve scoping issue

        try (FileReader reader = new FileReader(configPath)) {
            // Parse configuration file
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);

            // Initialize Cameras
            List<CameraService> cameraServices = new ArrayList<>();
            JsonObject camerasConfig = config.getAsJsonObject("Cameras");
            String cameraDataPath = camerasConfig.get("camera_datas_path").getAsString();
            JsonArray cameraConfigs = camerasConfig.getAsJsonArray("CamerasConfigurations");

            // Parse camera data
            FileReader cameraDataReader = new FileReader(cameraDataPath);
            JsonObject cameraData = gson.fromJson(cameraDataReader, JsonObject.class);
            cameraDataReader.close();

            for (com.google.gson.JsonElement cameraConfig : cameraConfigs) {
                JsonObject cameraJson = cameraConfig.getAsJsonObject();
                int id = cameraJson.get("id").getAsInt();
                int frequency = cameraJson.get("frequency").getAsInt();
                String cameraKey = cameraJson.get("camera_key").getAsString();

                // Get the list of StampedDetectedObjects for this camera
                JsonArray stampedObjectsJson = cameraData.getAsJsonArray(cameraKey);
                List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();
                for (com.google.gson.JsonElement stampedObjectJson : stampedObjectsJson) {
                    JsonObject stampedObject = stampedObjectJson.getAsJsonObject();
                    int time = stampedObject.get("time").getAsInt();
                    JsonArray detectedObjectsJson = stampedObject.getAsJsonArray("detectedObjects");

                    List<DetectedObject> detectedObjects = new ArrayList<>();
                    for (com.google.gson.JsonElement detectedObjectJson : detectedObjectsJson) {
                        JsonObject detectedObject = detectedObjectJson.getAsJsonObject();
                        String idStr = detectedObject.get("id").getAsString();
                        String description = detectedObject.get("description").getAsString();
                        detectedObjects.add(new DetectedObject(idStr, description));
                    }
                    detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
                }

                // Create Camera and CameraService
                Camera camera = new Camera(id, frequency, detectedObjectsList);
                cameraServices.add(new CameraService(camera));
            }

            // Initialize LiDARs
            List<LiDarService> lidarServices = new ArrayList<>();
            JsonObject lidarConfig = config.getAsJsonObject("LidarWorkers");
            String lidarDataPath = lidarConfig.get("lidars_data_path").getAsString();
            JsonArray lidarConfigs = lidarConfig.getAsJsonArray("LidarConfigurations");
            for (com.google.gson.JsonElement lidarJson : lidarConfigs) {
                int id = lidarJson.getAsJsonObject().get("id").getAsInt();
                int frequency = lidarJson.getAsJsonObject().get("frequency").getAsInt();
                LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(id, frequency, lidarDataPath);
                lidarServices.add(new LiDarService(lidarWorker));
            }

            // Initialize PoseService
            String poseFilePath = config.get("poseJsonFile").getAsString();
            try (FileReader poseReader = new FileReader(poseFilePath)) {
                // Parse the pose data
                java.lang.reflect.Type poseListType = new com.google.gson.reflect.TypeToken<List<Pose>>() {}.getType();
                List<Pose> poseList = gson.fromJson(poseReader, poseListType);

                // Initialize GPSIMU
                GPSIMU gpsimu = new GPSIMU(poseList);

                // Initialize PoseService
                poseService = new PoseService(gpsimu);
            } catch (IOException e) {
                System.err.println("Error reading pose file: " + e.getMessage());
                e.printStackTrace();
                return; // Exit or handle error appropriately
            }

            // Initialize FusionSlamService
            FusionSlam fusionSlam = FusionSlam.getInstance();
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);

            // Count active cameras and sensors
            int numActiveCameras = cameraServices.size();
            int numActiveLiDars = lidarServices.size();
            int numActiveSensors = numActiveCameras + numActiveLiDars;

            // Update FusionSlam with active sensors and cameras
            fusionSlam.setActiveCameras(numActiveCameras);
            fusionSlam.setActiveSensors(numActiveSensors);

            // Print debug information (optional)
            System.out.println("Active Cameras: " + numActiveCameras);
            System.out.println("Active Sensors: " + numActiveSensors);

            // Initialize TimeService
            int tickTime = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();
            TimeService timeService = new TimeService(tickTime, duration);

            // Create threads for all services
            List<Thread> threads = new ArrayList<>();
            for (CameraService cameraService : cameraServices) {
                threads.add(new Thread(cameraService));
            }
            for (LiDarService lidarService : lidarServices) {
                threads.add(new Thread(lidarService));
            }
            if (poseService != null) {
                threads.add(new Thread(poseService)); // Add PoseService thread only if it was successfully initialized
            }
            threads.add(new Thread(fusionSlamService));
            Thread timeServiceThread = new Thread(timeService);
            threads.add(timeServiceThread);

            // Start all service threads except TimeService
            for (Thread thread : threads) {
                if (thread != timeServiceThread) {
                    thread.start();
                }
            }

            // Ensure all services are initialized before starting TimeService
            Thread.sleep(200); // Allow time for other threads to initialize

            // Start TimeService
            timeServiceThread.start();

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
