package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.MicroService;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    // Fields
    private final int tickTime; // Time for each tick in milliseconds
    private final int duration; // Total number of ticks

    /**
     * Constructor for TimeService.
     *
     * @param TickTime The duration of each tick in milliseconds.
     * @param Duration The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime;
        this.duration = Duration;
    }

    // Added constructor for testing
    public TimeService(String[] args) {
        super("TimeService");

        // Parse and validate args
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("TimeService expects two arguments: tickTime and duration.");
        }

        try {
            this.tickTime = Integer.parseInt(args[0]); // Parse tickTime
            this.duration = Integer.parseInt(args[1]); // Parse duration
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("TimeService expects both tickTime and duration to be positive integers.");
        }

        if (this.tickTime <= 0 || this.duration <= 0) {
            throw new IllegalArgumentException("Both tickTime and duration must be greater than 0.");
        }
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        System.out.println("TimeService initialized.");
        // Run the ticking logic in a separate thread
        Thread tickThread = new Thread(() -> {
            try {
                for (int currentTick = 1; currentTick <= duration; currentTick++) {
                    // Broadcast the current tick
                    sendBroadcast(new TickBroadcast(currentTick));
                    System.out.println("TimeService broadcasted Tick: " + currentTick);
                    // Wait for the next tick
                    Thread.sleep(tickTime);
                }
                // After all ticks are complete, broadcast TerminatedBroadcast
                sendBroadcast(new TerminatedBroadcast(getName()));
                System.out.println("TimeService broadcasted TerminatedBroadcast.");
            } catch (InterruptedException e) {
                System.out.println("TimeService interrupted. Exiting...");
                Thread.currentThread().interrupt(); // Restore interrupt status
            } finally {
                terminate(); // Signal the service to terminate
            }
        });

        tickThread.start(); // Start the thread
    }
}
