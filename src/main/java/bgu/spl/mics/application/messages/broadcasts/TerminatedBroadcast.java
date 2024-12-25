package bgu.spl.mics.application.messages.broadcasts;

import bgu.spl.mics.Broadcast;

/**
 * TerminatedBroadcast is sent by a service to notify the system that it has terminated gracefully.
 * It is received by all subscribers who have registered for this type of broadcast.
 */
public class TerminatedBroadcast implements Broadcast {

    // Fields
    private final String senderId; // The ID of the service that is terminating.

    /**
     * Constructor for TerminatedBroadcast.
     *
     * @param senderId The ID of the service that is terminating.
     */
    public TerminatedBroadcast(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the ID of the service that is terminating.
     *
     * @return The sender ID.
     */
    public String getSenderId() {
        return senderId;
    }
}