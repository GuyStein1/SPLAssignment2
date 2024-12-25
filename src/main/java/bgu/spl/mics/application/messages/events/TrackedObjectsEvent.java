package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;

/**
 * TrackedObjectsEvent is sent by a LiDAR worker to the FusionSLAM service.
 * It contains a list of tracked objects with their positional data.
 */
public class TrackedObjectsEvent implements Event<Void> {

    // Fields
    private final List<TrackedObject> trackedObjects; // List of tracked objects.

    // Constructor
    public TrackedObjectsEvent(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    // Getter
    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
}