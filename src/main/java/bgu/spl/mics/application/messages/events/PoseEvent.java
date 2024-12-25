package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseEvent is sent by the PoseService to provide the robot's current pose to the FusionSLAM service.
 */
public class PoseEvent implements Event<Void> {

    private final Pose pose; // The robot's pose.

    /**
     * Constructor for PoseEvent.
     *
     * @param pose The robot's pose.
     */
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    /**
     * Gets the robot's pose.
     *
     * @return The pose.
     */
    public Pose getPose() {
        return pose;
    }
}