package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;

// Import relevant broadcasts
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;

// Import relevant events
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;

// Import relevant objects
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.*;


/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    // Fields
    private final Camera camera;
    // Queue to track detections awaiting dispatch according to camera frequency
    private final Queue<StampedDetectedObjects> detections;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera_" + camera.getID());
        this.camera = camera;
        detections = new LinkedList<>();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getCurrentTick();

            // Check for new detections to queue
            StampedDetectedObjects newDetections = camera.getStampedDetectedObjectsAtTime(currentTick);
            if (newDetections != null) {
                detections.add(newDetections);
            }

            // Process pending detections to send events
            while (!detections.isEmpty() && detections.peek().getTime() + camera.getFrequency() == currentTick) {
                StampedDetectedObjects detectionToSend = detections.poll();
                sendEvent(new DetectObjectsEvent(detectionToSend.getTime(), detectionToSend.getDetectedObjects()));
                System.out.println(getName() + " sent DetectObjectsEvent with " + detectionToSend.getDetectedObjects().size() + " objects.");

                // Update StatisticalFolder
                StatisticalFolder.getInstance().incrementDetectedObjects(detectionToSend.getDetectedObjects().size());
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received CrashedBroadcast from " + broadcast.getSenderId() + ". Terminating.");
            terminate();
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received TerminatedBroadcast from " + broadcast.getSenderId() + ".");

            // Conditional termination: Only terminate if the sender is "TimeService"
            if ("TimeService".equals(broadcast.getSenderId())) {
                System.out.println(getName() + " terminating as TimeService has ended.");
                terminate();
            }
        });
    }
}