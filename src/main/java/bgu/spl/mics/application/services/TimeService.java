package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;
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

                    // Update system runtime in StatisticalFolder
                    StatisticalFolder.getInstance().incrementSystemRuntime();

                    // Wait for the next tick
                    Thread.sleep(tickTime);
                }
                // After all ticks are complete, broadcast TerminatedBroadcast
                sendBroadcast(new TerminatedBroadcast(getName()));
                System.out.println("TimeService broadcasted TerminatedBroadcast.");
            } catch (InterruptedException e) {
                System.out.println("TimeService interrupted. Terminating...");
                Thread.currentThread().interrupt(); // Restore interrupt status
            } finally {
                terminate(); // Signal the service to terminate
            }
        });

        tickThread.start(); // Start the thread
    }
}
