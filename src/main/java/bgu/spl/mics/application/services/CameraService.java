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
import bgu.spl.mics.application.objects.STATUS;

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
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            // Check camera status before processing
            if (camera.getStatus() != STATUS.UP) {
                return;
            }

            int currentTick = tick.getCurrentTick();

            // Check for new detections to queue
            StampedDetectedObjects newDetections = camera.getStampedDetectedObjectsAtTime(currentTick);
            if (newDetections != null) {
                // Check for errors in the new detections
                boolean hasError = newDetections.getDetectedObjects().stream()
                        .anyMatch(detectedObject -> "ERROR".equals(detectedObject.getId()));

                if (hasError) {
                    System.out.println(getName() + " detected an error in tick " + currentTick + ". Sending CrashedBroadcast.");
                    camera.setStatus(STATUS.ERROR); // Update camera status to ERROR
                    sendBroadcast(new CrashedBroadcast(getName()));
                    terminate();
                    return; // Stop further processing
                }

                // Add valid detections to the queue
                detections.add(newDetections);
                System.out.println(getName() + " queued " + newDetections.getDetectedObjects().size() +
                        " objects at tick " + currentTick);
            }

            // Process pending detections to send events according to frequency
            while (!detections.isEmpty() && detections.peek().getTime() + camera.getFrequency() == currentTick) {
                StampedDetectedObjects detectionToSend = detections.poll();
                sendEvent(new DetectObjectsEvent(detectionToSend.getTime(), detectionToSend.getDetectedObjects()));
                System.out.println(getName() + " sent DetectObjectsEvent with " + detectionToSend.getDetectedObjects().size() +
                        " objects at tick " + currentTick);

                // Update StatisticalFolder
                StatisticalFolder.getInstance().incrementDetectedObjects(detectionToSend.getDetectedObjects().size());
            }

            // Check if the camera has no more detections
            if (detections.isEmpty() && camera.hasNoMoreDetections(currentTick)) {
                System.out.println(getName() + " has no more detections. Moving to DOWN status.");
                camera.setStatus(STATUS.DOWN); // Move to DOWN status
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }

        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received CrashedBroadcast from " + broadcast.getSenderId() + ". Terminating.");
            camera.setStatus(STATUS.DOWN); // Update camera status to DOWN
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received TerminatedBroadcast from " + broadcast.getSenderId() + ".");

            // Conditional termination: Only terminate if the sender is "TimeService"
            if ("TimeService".equals(broadcast.getSenderId())) {
                System.out.println(getName() + " terminating as TimeService has ended.");
                camera.setStatus(STATUS.DOWN); // Update camera status to DOWN
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        // Log when initialization finished
        System.out.println(getName() + " initialized.");
    }
}