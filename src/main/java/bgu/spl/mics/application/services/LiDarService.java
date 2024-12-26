package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.DetectObjectsEvent;
import bgu.spl.mics.application.messages.events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    private final Queue<DetectObjectsEvent> eventQueue;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDar_" + lidarWorker.getId());
        this.lidarWorker = lidarWorker;
        this.eventQueue = new LinkedList<>();
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
            System.out.println(getName() + " received DetectObjectsEvent for objects detected at time " + event.getTime());

            // Enqueue the event for delayed processing
            eventQueue.add(event);
            System.out.println(getName() + " queued DetectObjectsEvent for future processing.");
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getCurrentTick(); // Get the current tick
            int frequency = lidarWorker.getFrequency(); // Get the frequency

            System.out.println(getName() + " processing Tick: " + currentTick);

            // Process events in the queue if their time aligns with the frequency
            while (!eventQueue.isEmpty() && eventQueue.peek().getTime() + frequency == currentTick) {
                DetectObjectsEvent eventToProcess = eventQueue.poll();

                List<TrackedObject> trackedObjects = new ArrayList<>();

                for (DetectedObject detectedObject : eventToProcess.getDetectedObjects()) {
                    // Retrieve cloud points for the detected object
                    StampedCloudPoints cloudPoints = LiDarDataBase.getCloudPoints(detectedObject.getId());

                    // Create a TrackedObject using the event data and cloud points
                    TrackedObject trackedObject = new TrackedObject(
                            detectedObject.getId(),
                            eventToProcess.getTime(),
                            detectedObject.getDescription(),
                            cloudPoints.getCoordinates()
                    );
                    trackedObjects.add(trackedObject);
                }

                // Create and send TrackedObjectsEvent to FusionSLAM
                sendEvent(new TrackedObjectsEvent(trackedObjects));
                System.out.println(getName() + " sent TrackedObjectsEvent for time " + eventToProcess.getTime());

                // Update the StatisticalFolder
                StatisticalFolder.getInstance().incrementTrackedObjects(trackedObjects.size());
            }
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received TerminatedBroadcast from " + broadcast.getSenderId() + ".");

            if ("TimeService".equals(broadcast.getSenderId())) {
                System.out.println(getName() + " terminating as TimeService has ended.");
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received CrashedBroadcast from " + broadcast.getSenderId() + ". Terminating.");
            terminate();
        });

        System.out.println(getName() + " initialized.");
    }
}
