package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.*;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private final LiDarWorkerTracker lidarWorker;
    private final PriorityQueue<DetectObjectsEvent> eventQueue;
    private int currentTick;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDar_" + LiDarWorkerTracker.getId());
        this.lidarWorker = LiDarWorkerTracker;
        this.eventQueue = new PriorityQueue<>(Comparator.comparingInt(DetectObjectsEvent::getTime));
        this.currentTick = 0;
        CrashOutputManager.getInstance().getLiDars().add(LiDarWorkerTracker);
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, event -> {

            System.out.println(getName() + " received DetectObjectsEvent with time " + event.getTime());

            if (event.getTime() + lidarWorker.getFrequency() <= currentTick) {

                List<TrackedObject> trackedObjects = new ArrayList<>();

                for (DetectedObject detectedObject : event.getDetectedObjects()) {
                    // Retrieve cloud points for the detected object from the LiDAR database
                    StampedCloudPoints StampedCloudPoints = LiDarDataBase.getInstance().getCloudPoints(detectedObject.getId(), event.getTime());

                    // Check for errors in the cloud points data
                    if (StampedCloudPoints != null && StampedCloudPoints.getId().equals("ERROR")) {
                        String errorDescription = getName() + " disconnected";
                        System.out.println(errorDescription + " on tick " + currentTick + ".");
                        // Update CrashOutputManager
                        CrashOutputManager.getInstance().setFaultySensor(getName());
                        CrashOutputManager.getInstance().setErrorDescription(errorDescription);
                        // Terminate
                        lidarWorker.setStatus(STATUS.ERROR);
                        sendBroadcast(new CrashedBroadcast(getName()));
                        terminate();
                        return;
                    }

                    // Create a TrackedObject with the retrieved cloud points and event data
                    TrackedObject trackedObject = new TrackedObject(
                            StampedCloudPoints.getId(),
                            StampedCloudPoints.getTime(),
                            detectedObject.getDescription(),
                            StampedCloudPoints.getCoordinates()
                    );
                    trackedObjects.add(trackedObject);
                }

                if (!trackedObjects.isEmpty()){
                    // Create and send TrackedObjectsEvent to FusionSLAM
                    sendEvent(new TrackedObjectsEvent(trackedObjects));
                    System.out.println(getName() + " sent TrackedObjectsEvent at tick " + currentTick);
                    // Update the list of last tracked objects in the LiDAR worker
                    lidarWorker.updateLastTrackedObjects(trackedObjects);
                    // Update the StatisticalFolder
                    StatisticalFolder.getInstance().incrementTrackedObjects(trackedObjects.size());
                }

                // Signal to camera the detected object event was received and processed
                complete(event, true);
            } else {
                // Log and queue the received event for future processing according to frequency
                eventQueue.add(event);
                System.out.println(getName() + " queued DetectObjectsEvent with time " + event.getTime() + " for future processing.");
            }
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            // Increment Current tick
            currentTick++;

            // Check LiDar status before processing
            if (lidarWorker.getStatus() != STATUS.UP) {
                return;
            }

            int currentTick = tick.getCurrentTick(); // Get the current tick
            int frequency = lidarWorker.getFrequency(); // Get the frequency

            // Initialize an empty list to store tracked objects
            List<TrackedObject> trackedObjects = new ArrayList<>();

            // Process events in the queue based on detection time and lidar frequency
            while (!eventQueue.isEmpty() && eventQueue.peek().getTime() + frequency <= currentTick) {

                // Poll the earliest event from the priority queue
                DetectObjectsEvent eventToProcess = eventQueue.poll();

                for (DetectedObject detectedObject : eventToProcess.getDetectedObjects()) {
                    // Retrieve cloud points for the detected object from the LiDAR database
                    StampedCloudPoints StampedCloudPoints = LiDarDataBase.getInstance().getCloudPoints(detectedObject.getId(), eventToProcess.getTime());

                    // Check for errors in the cloud points data
                    if (StampedCloudPoints != null && StampedCloudPoints.getId().equals("ERROR")) {

                        String errorDescription = getName() + " disconnected";

                        System.out.println(errorDescription + " on tick " + currentTick + ".");

                        // Update CrashOutputManager
                        CrashOutputManager.getInstance().setFaultySensor(getName());
                        CrashOutputManager.getInstance().setErrorDescription(errorDescription);

                        lidarWorker.setStatus(STATUS.ERROR);
                        sendBroadcast(new CrashedBroadcast(getName()));
                        terminate();
                        return;
                    }

                    // Create a TrackedObject with the retrieved cloud points and event data
                    TrackedObject trackedObject = new TrackedObject(
                            StampedCloudPoints.getId(),
                            StampedCloudPoints.getTime(),
                            detectedObject.getDescription(),
                            StampedCloudPoints.getCoordinates()
                    );
                    trackedObjects.add(trackedObject);
                }

                // Signal to camera the detected object event was received and processed
                complete(eventToProcess, true);
            }

            // If tracked objects were created, sent tracked objects event
            if (!trackedObjects.isEmpty()){
                // Create and send TrackedObjectsEvent to FusionSLAM
                sendEvent(new TrackedObjectsEvent(trackedObjects));
                System.out.println(getName() + " sent TrackedObjectsEvent at tick " + currentTick);
                // Update the list of last tracked objects in the LiDAR worker
                lidarWorker.updateLastTrackedObjects(trackedObjects);
                // Update the StatisticalFolder
                StatisticalFolder.getInstance().incrementTrackedObjects(trackedObjects.size());
            }

            // LiDar can terminate if all cameras are down and has no more DetectObjectsEvents to process
            if (eventQueue.isEmpty() && FusionSlam.getInstance().getActiveCameras() == 0) {
                System.out.println(getName() + " has no more events. Moving to DOWN status.");
                lidarWorker.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received TerminatedBroadcast from " + broadcast.getSenderId() + ".");

            if ("TimeService".equals(broadcast.getSenderId())) {
                System.out.println(getName() + " terminating as TimeService has ended.");
                lidarWorker.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received CrashedBroadcast from " + broadcast.getSenderId() + ". Terminating.");
            lidarWorker.setStatus(STATUS.DOWN);
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });

        // Log when initialization finished
        System.out.println(getName() + " initialized.");
    }
}
