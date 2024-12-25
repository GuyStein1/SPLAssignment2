package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;

import bgu.spl.mics.application.messages.events.DetectObjectsEvent;

import java.util.List;


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
    private int mbt; // Messages before termination (used for testing, -1 means unlimited)
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera_" + camera.getID());
        this.camera = camera;
        mbt = -1;
    }

    // Added constructor for testing
    public CameraService(Camera camera, String[] args) {
        super("Camera_" + camera.getID());
        this.camera = camera;

        // Parse and validate args
        if (args.length != 1) {
            throw new IllegalArgumentException("CameraService expects a single argument: mbt (number of messages before termination).");
        }
        try {
            this.mbt = Integer.parseInt(args[0]); // Parse mbt from args
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("CameraService expects mbt to be a positive integer. Received: " + args[0]);
        }

        if (this.mbt <= 0) {
            throw new IllegalArgumentException("mbt must be greater than 0. Received: " + this.mbt);
        }
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
            if (tick.getCurrentTick() % camera.getFrequency() == 0) {
                List<DetectedObject> detectedObjects = camera.getDetectedObjectsAtTime(tick.getCurrentTick());
                if (!detectedObjects.isEmpty()) {
                    sendEvent(new DetectObjectsEvent(tick.getCurrentTick(), detectedObjects));
                    System.out.println(getName() + " sent DetectObjectsEvent with " + detectedObjects.size() + " objects.");
                }
            }

            if (mbt > 0) {
                mbt--;
                if (mbt == 0) {
                    System.out.println(getName() + " processed maximum messages (mbt). Terminating.");
                    terminate();
                }
            }

        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received CrashedBroadcast. Terminating.");
            terminate();
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            System.out.println(getName() + " received TerminatedBroadcast. Terminating.");
            terminate();
        });
    }
}