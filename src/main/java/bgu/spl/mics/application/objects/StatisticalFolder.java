package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    // Fields
    private final AtomicInteger systemRuntime; // Total runtime of the system, measured in ticks
    private final AtomicInteger numDetectedObjects; // Cumulative count of objects detected by cameras
    private final AtomicInteger numTrackedObjects; // Cumulative count of objects tracked by LiDAR workers
    private final AtomicInteger numLandmarks; // Total number of unique landmarks identified and mapped

    // Private constructor for singleton
    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }

    /**
     * Nested static helper class to hold the singleton instance.
     * The instance is created only when the getInstance() method is called for the first time.
     */
    private static class SingletonHelper {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    /**
     * Returns the singleton instance of StatisticalFolder.
     *
     * @return The shared instance of StatisticalFolder.
     */
    public static StatisticalFolder getInstance() {
        return SingletonHelper.INSTANCE;
    }

    // Increment methods (thread-safe using AtomicInteger)
    public void incrementSystemRuntime() {
        systemRuntime.incrementAndGet();
    }

    public void incrementDetectedObjects(int count) {
        numDetectedObjects.addAndGet(count);
    }

    public void incrementTrackedObjects(int count) {
        numTrackedObjects.addAndGet(count);
    }

    public void incrementLandmarks(int count) {
        numLandmarks.addAndGet(count);
    }

    // Getter methods (thread-safe using AtomicInteger)
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    // Add a reset method for testing purposes
    public void reset() {
        systemRuntime.set(0);
        numDetectedObjects.set(0);
        numTrackedObjects.set(0);
        numLandmarks.set(0);
    }
}