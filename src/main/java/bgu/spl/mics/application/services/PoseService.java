package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.events.PoseEvent;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast to send PoseEvent at every tick
        subscribeBroadcast(TickBroadcast.class, tick -> {

            // Check GPSIMU status before processing
            if (gpsimu.getStatus() != STATUS.UP) {
                return;
            }

            gpsimu.updateTick(tick.getCurrentTick());

            // Fetch the current pose
            Pose currentPose = gpsimu.getCurrentPose();
            if (currentPose != null) {
                // Create and send a PoseEvent
                PoseEvent poseEvent = new PoseEvent(currentPose);
                sendEvent(poseEvent);
                System.out.println("PoseService sent PoseEvent: " + currentPose +
                                   ", at tick " + tick.getCurrentTick() + ".");
            } else {
                System.out.println("PoseService: No more poses available. Terminating.");
                gpsimu.setStatus(STATUS.DOWN);
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast to handle system crashes
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println("PoseService received CrashedBroadcast. Terminating.");
            gpsimu.setStatus(STATUS.DOWN);
            terminate();
        });

        // Log when initialization finished
        System.out.println("PoseService initialized.");
    }
}
