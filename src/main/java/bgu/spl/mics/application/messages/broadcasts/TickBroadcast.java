package bgu.spl.mics.application.messages.broadcasts;

import bgu.spl.mics.Broadcast;

/**
 * TickBroadcast is sent by the TimeService to notify all subscribed microservices
 * of the current simulation tick.
 */
public class TickBroadcast implements Broadcast {

    // Fields
    private final int currentTick; // The current simulation tick.

    /**
     * Constructor for TickBroadcast.
     *
     * @param currentTick The current simulation tick.
     */
    public TickBroadcast(int currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Gets the current simulation tick.
     *
     * @return The current tick.
     */
    public int getCurrentTick() {
        return currentTick;
    }
}