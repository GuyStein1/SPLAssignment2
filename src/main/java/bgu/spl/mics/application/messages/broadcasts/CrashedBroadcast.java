package bgu.spl.mics.application.messages.broadcasts;

import bgu.spl.mics.Broadcast;

/**
 * CrashedBroadcast is sent by a service to notify the system of its critical failure.
 * It is received by all subscribers who have registered for this type of broadcast.
 */
public class CrashedBroadcast implements Broadcast {

    // Fields
    private final String senderId; // The ID of the service that crashed.

    // Constructor
    public CrashedBroadcast(String senderId) {
        this.senderId = senderId;
    }

    // Getter
    public String getSenderId() {
        return senderId;
    }
}