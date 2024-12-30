package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.objects.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

            for (TrackedObject trackedObject : trackedObjects) {
                // Get the pose for the tracked object detection time
                Pose currentPose = fusionSlam.getPoseByTimestamp(trackedObject.getTime());
                // Check if the landmark already exists
                boolean exists = false;
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
                    fusionSlam.addLandmark(newLandMark);
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
                // Mark fusion slam as terminated (optional as only time service checks this field)
                fusionSlam.setTerminated(true);
                // Signal to fusion slam that time service terminated
                fusionSlam.setTimeTerminated(true);
                // Wait for all services to send terminated broadcast before generating output
                if (fusionSlam.getActiveSensors() == 0) {
                    if (fusionSlam.isCrashed()) {
                        CrashOutputManager.getInstance().generateCrashOutput();
                    } else {
                        generateOutput();
                    }
                    terminate();
                }
            } else {
                String sensorType = broadcast.getSenderId().split("_")[0];
                if ("Camera".equals(sensorType)) {
                    fusionSlam.setActiveCameras(fusionSlam.getActiveCameras() - 1);
                    fusionSlam.setActiveSensors(fusionSlam.getActiveSensors() - 1);
                } else if ("LiDar".equals(sensorType)) {
                    fusionSlam.setActiveSensors(fusionSlam.getActiveSensors() - 1);
                }
            }

            // Check if no sensors are active
            if (fusionSlam.getActiveSensors() == 0) {
                // If time service didn't terminate yet, signal it to terminate and produce the output after it sent terminated broadcast.
                if (!fusionSlam.isTimeTerminated()) {
                    fusionSlam.setTerminated(true);
                } else {
                    // Time service already terminated; finalize process
                    System.out.println("FusionSlamService received TerminatedBroadcast from all sensors. Terminating.");
                    if (fusionSlam.isCrashed()) {
                        CrashOutputManager.getInstance().generateCrashOutput();
                    } else {
                        generateOutput();
                    }
                    terminate();
                }
            }
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getCurrentTick();

            System.out.println("FusionSlamService received TickBroadcast: Tick " + currentTick +
                    ". FusionSlamService Status: Active Sensors = "
                    + fusionSlam.getActiveSensors());
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println("FusionSlamService received CrashBroadcast from " + broadcast.getSenderId() +  ". Terminating.");
            // Decrement sensor count because sensor that crashed doesn't send termination
            String sensorType = broadcast.getSenderId().split("_")[0];
            if ("Camera".equals(sensorType)) {
                fusionSlam.setActiveCameras(fusionSlam.getActiveCameras() - 1);
            }
            fusionSlam.setActiveSensors(fusionSlam.getActiveSensors() - 1);

            fusionSlam.setTerminated(true);
            fusionSlam.setCrashed(true);
            // Generate error output when received terminated broadcast from all services
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

        // Create JSON objects using GSON
        JsonObject output = new JsonObject();
        output.addProperty("systemRuntime", systemRuntime);
        output.addProperty("numDetectedObjects", numDetectedObjects);
        output.addProperty("numTrackedObjects", numTrackedObjects);
        output.addProperty("numLandmarks", numLandmarks);

        JsonObject landMarksJson = new JsonObject();
        for (LandMark landmark : landmarks) {
            JsonObject landmarkDetails = new JsonObject();
            landmarkDetails.addProperty("id", landmark.getId());
            landmarkDetails.addProperty("description", landmark.getDescription());

            JsonArray coordinatesArray = new JsonArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.getX());
                pointJson.addProperty("y", point.getY());
                coordinatesArray.add(pointJson);
            }
            landmarkDetails.add("coordinates", coordinatesArray);
            landMarksJson.add(landmark.getId(), landmarkDetails);
        }
        output.add("landMarks", landMarksJson);

        // Write JSON to output file
        try (FileWriter writer = new FileWriter("output_file.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(output, writer);
            System.out.println("FusionSlamService: Output written to output_file.json");
        } catch (IOException e) {
            System.err.println("FusionSlamService: Failed to write output. " + e.getMessage());
        }
    }
}
