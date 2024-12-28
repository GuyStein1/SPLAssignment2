package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.objects.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    private final FusionSlam fusionSlam;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // Subscribe to TrackedObjectsEvent to update landmarks
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            List<TrackedObject> trackedObjects = event.getTrackedObjects();
            int trackedObjectsTimeStamp = event.getDetectionTimeStamp();
            Pose currentPose = fusionSlam.getPoseByTimestamp(trackedObjectsTimeStamp);

            for (TrackedObject trackedObject : trackedObjects) {
                boolean exists = false;
                // Check if the landmark already exists
                for (LandMark landMark : fusionSlam.getLandmarks()) {
                    if (landMark.getId().equals(trackedObject.getId())) {
                        exists = true;
                        // Transform the trackedObject's coordinates (CloudPoints) to the global frame
                        List<CloudPoint> globalCoordinates = fusionSlam.transformToGlobal(trackedObject.getCoordinates(), currentPose);
                        // Update coordinates by averaging the last measurements with the new ones
                        landMark.updateCoordinates(globalCoordinates);
                        break;
                    }
                }
                // If landmark does not exist, create and add a new one
                if (!exists) {
                    List<CloudPoint> globalCoordinates = fusionSlam.transformToGlobal(trackedObject.getCoordinates(), currentPose);
                    LandMark newLandMark = new LandMark(
                            trackedObject.getId(),
                            trackedObject.getDescription(),
                            globalCoordinates
                    );
                    fusionSlam.getLandmarks().add(newLandMark);
                    StatisticalFolder.getInstance().incrementLandmarks(1);
                }
            }
            System.out.println("FusionSlamService processed TrackedObjectsEvent.");
        });

        // Subscribe to PoseEvent to update the robot's pose
        subscribeEvent(PoseEvent.class, event -> {
            Pose newPose = event.getPose();
            System.out.println("FusionSlamService received PoseEvent: " + newPose);
            fusionSlam.addPose(newPose);
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if ("TimeService".equals(broadcast.getSenderId())) {
                System.out.println("FusionSlamService received TerminatedBroadcast from TimeService. Terminating.");
                fusionSlam.setTerminated(true);
                terminate();
                generateOutput();
            } else {
                String sensorType = broadcast.getSenderId().split("_")[0];
                if ("Camera".equals(sensorType)) {
                    fusionSlam.setActiveCameras(fusionSlam.getActiveCameras() - 1);
                } else if ("LiDAR".equals(sensorType)) {
                    fusionSlam.setActiveLiDars(fusionSlam.getActiveLiDars() - 1);
                }
                fusionSlam.setActiveSensors(fusionSlam.getActiveSensors() - 1);
            }

            if (fusionSlam.getActiveSensors() == 0) {
                System.out.println("FusionSlamService received TerminatedBroadcast from all sensors. Terminating.");
                fusionSlam.setTerminated(true);
                terminate();
                generateOutput();
            }
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getCurrentTick();

            System.out.println("FusionSlamService received TickBroadcast: Tick " + currentTick +
                    ". FusionSlamService Status: Active Cameras = "
                    + FusionSlam.getInstance().getActiveCameras()
                    + ", Active LiDARs = "
                    + FusionSlam.getInstance().getActiveLiDars());
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println("FusionSlamService received CrashBroadcast from "
                    + broadcast.getSenderId() +  ". Terminating.");
            fusionSlam.setTerminated(true);
            terminate();
            generateOutput();
        });
    }

    /**
     * Generates the output based on the current state of FusionSlam.
     * Writes the output to a JSON file.
     */
    private void generateOutput() {
        // Collect data from FusionSlam
        List<LandMark> landmarks = fusionSlam.getLandmarks();
        List<Pose> poses = fusionSlam.getPoses();

        int systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        int numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        int numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        int numLandmarks = landmarks.size();

        // Convert landmarks into the required JSON structure
        JsonObject landMarksJson = new JsonObject();
        for (LandMark landMark : landmarks) {
            JsonObject landmarkDetails = new JsonObject();
            landmarkDetails.addProperty("id", landMark.getId());
            landmarkDetails.addProperty("description", landMark.getDescription());

            // Serialize coordinates
            JsonArray coordinatesArray = new JsonArray();
            for (CloudPoint point : landMark.getCoordinates()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.getX());
                pointJson.addProperty("y", point.getY());
                coordinatesArray.add(pointJson);
            }
            landmarkDetails.add("coordinates", coordinatesArray);

            landMarksJson.add(landMark.getId(), landmarkDetails);
        }

        // Build the full output JSON
        JsonObject output = new JsonObject();
        output.addProperty("systemRuntime", systemRuntime);
        output.addProperty("numDetectedObjects", numDetectedObjects);
        output.addProperty("numTrackedObjects", numTrackedObjects);
        output.addProperty("numLandmarks", numLandmarks);
        output.add("landMarks", landMarksJson);

        // Write to file
        try (FileWriter writer = new FileWriter("fusion_slam_output.json")) {
            Gson gson = new Gson();
            gson.toJson(output, writer);
            System.out.println("FusionSlamService: Output written to fusion_slam_output.json");
        } catch (IOException e) {
            System.err.println("FusionSlamService: Failed to write output. " + e.getMessage());
        }
    }
}
