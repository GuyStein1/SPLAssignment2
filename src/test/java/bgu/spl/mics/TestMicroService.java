package bgu.spl.mics;

import java.util.concurrent.CountDownLatch;

/**
 * A test implementation of the abstract MicroService class.
 * Includes nested static classes for TestEvent and TestBroadcast to avoid creating extra files.
 */
public class TestMicroService extends MicroService {
    private final CountDownLatch latch;

    public TestMicroService(String name, CountDownLatch latch) {
        super(name);
        this.latch = latch;
    }

    @Override
    protected void initialize() {
        // Subscribe to TestEvent
        subscribeEvent(TestEvent.class, event -> {
            complete(event, getName() + " handled " + event.getData());
        });

        // Subscribe to TestBroadcast
        subscribeBroadcast(TestBroadcast.class, broadcast -> {
            System.out.println(getName() + " received broadcast: " + broadcast.getData());
        });

        // Signal that initialization is complete
        latch.countDown();
    }

    /**
     * A test implementation of the Event interface.
     */
    public static class TestEvent implements Event<String> {
        private final String data;

        public TestEvent(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    /**
     * A test implementation of the Broadcast interface.
     */
    public static class TestBroadcast implements Broadcast {
        private final String data;

        public TestBroadcast(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }
}