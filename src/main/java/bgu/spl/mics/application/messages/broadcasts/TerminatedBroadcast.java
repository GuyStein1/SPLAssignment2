package bgu.spl.mics.application.messages.broadcasts;

import bgu.spl.mics.Broadcast;

/**
 * TerminatedBroadcast is sent by a service to notify the system that it has terminated gracefully.
 * It is received by all subscribers who have registered for this type of broadcast.
 */
public class TerminatedBroadcast implements Broadcast {

    // Fields
    private final String senderId; // The ID of the service that is terminating.

    // Constructor
    public TerminatedBroadcast(String senderId) {
        this.senderId = senderId;
    }

    // Getter
    public String getSenderId() {
        return senderId;
    }
}