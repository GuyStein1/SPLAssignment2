package bgu.spl.mics.application.messages.broadcasts;

import bgu.spl.mics.Broadcast;

/**
 * CrashedBroadcast is sent by a service to notify the system of its critical failure.
 * It is received by all subscribers who have registered for this type of broadcast.
 */
public class CrashedBroadcast implements Broadcast {

    // Fields
    private final String senderId; // The ID of the service that crashed.

    /**
     * Constructor for CrashedBroadcast.
     *
     * @param senderId The ID of the service that crashed.
     */
    public CrashedBroadcast(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the ID of the service that crashed.
     *
     * @return The sender ID.
     */
    public String getSenderId() {
        return senderId;
    }
}