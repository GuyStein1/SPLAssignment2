package bgu.spl.mics.application;

import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import com.google.gson.Gson;
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
            System.err.println("Please provide the path to the configuration file.");
            return;
        }

        String configPath = args[0];
        try (FileReader reader = new FileReader(configPath)) {
            // Parse configuration file
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);

            // Load camera configurations
            List<CameraService> cameraServices = new ArrayList<>();
            JsonObject camerasConfig = config.getAsJsonObject("Cameras");
            String cameraDataPath = camerasConfig.get("camera_datas_path").getAsString();
            List<Camera> cameras = Camera.loadCamerasFromFile(cameraDataPath, camerasConfig);

            for (Camera camera : cameras) {
                cameraServices.add(new CameraService(camera));
            }

            // Load LiDAR configurations
            List<LiDarService> lidarServices = new ArrayList<>();
            JsonObject lidarConfig = config.getAsJsonObject("LidarWorkers");
            String lidarDataPath = lidarConfig.get("lidars_data_path").getAsString();
            List<LiDarWorkerTracker> lidarWorkers = LiDarWorkerTracker.loadLiDarsFromFile(lidarDataPath, lidarConfig);

            for (LiDarWorkerTracker lidarWorker : lidarWorkers) {
                lidarServices.add(new LiDarService(lidarWorker));
            }

            // Load pose configurations
            String poseFilePath = config.get("poseJsonFile").getAsString();
            GPSIMU gpsimu = new GPSIMU(poseFilePath);
            PoseService poseService = new PoseService(gpsimu);

            // Initialize FusionSLAM and FusionSlamService
            FusionSlam fusionSlam = FusionSlam.getInstance();
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);

            // Initialize TimeService
            int tickTime = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();
            TimeService timeService = new TimeService(tickTime, duration);

            // Create and start threads
            List<Thread> threads = new ArrayList<>();
            for (CameraService cameraService : cameraServices) {
                threads.add(new Thread(cameraService));
            }
            for (LiDarService lidarService : lidarServices) {
                threads.add(new Thread(lidarService));
            }
            threads.add(new Thread(poseService));
            threads.add(new Thread(fusionSlamService));
            threads.add(new Thread(timeService));

            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                thread.join();
            }

        } catch (IOException e) {
            System.err.println("Failed to read configuration file: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
