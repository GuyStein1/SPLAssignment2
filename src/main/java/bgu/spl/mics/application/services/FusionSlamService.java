package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.objects.*;

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
                // output logic!!!!!!!!!!
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
                // output logic!!!!!!
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
            // output logic!!!!
        });
    }
}
