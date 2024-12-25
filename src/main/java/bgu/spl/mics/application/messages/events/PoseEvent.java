package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseEvent is sent by the PoseService to provide the robot's current pose to the FusionSLAM service.
 */
public class PoseEvent implements Event<Void> {

    // Fields
    private final Pose pose;

    // Constructor
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    // Getters
    public Pose getPose() {
        return pose;
    }
}