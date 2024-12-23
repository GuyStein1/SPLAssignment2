package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	// Maps each MicroService to its own message queue
	private final Map<MicroService, BlockingQueue<Message>> microServiceQueues;

	// Maps event types to a list of MicroServices subscribed to them
	private final Map<Class<? extends Event<?>>, Queue<MicroService>> eventSubscribers;

	// Maps broadcast types to a list of MicroServices subscribed to them
	private final Map<Class<? extends Broadcast>, List<MicroService>> broadcastSubscribers;

	// Maps events to their corresponding Future objects
	private final Map<Event<?>, Future<?>> eventFutures;

	// Singleton instance of MessageBus
	private static class SingletonHolder {
		private static final MessageBusImpl instance = new MessageBusImpl();
	}

	// Private constructor to enforce singleton
	// Use ConcurrentHashMap which supports concurrency with threads
	private MessageBusImpl() {
		microServiceQueues = new ConcurrentHashMap<>();
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		eventFutures = new ConcurrentHashMap<>();
	}

	// Public method to get the singleton instance
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// Create a new queue if event type doesn't have a queue in the hash map.
		eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		// Add the MicroService to the list of subscribers for the given event type.
		eventSubscribers.get(type).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// Create a new list if broadcast type doesn't have a list in the hash map.
		// Using CopyOnWriteArrayList ensures thread-safe iteration without additional synchronization
		broadcastSubscribers.putIfAbsent(type, new CopyOnWriteArrayList<>());
		// Add the MicroService to the list of subscribers for the given broadcast type.
		broadcastSubscribers.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// Resolve the Future associated with the given event, allowing the sender to retrieve the result.
		Future<T> future = (Future<T>) eventFutures.get(e);
		if (future != null) {
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// Get the list of subscribers to broadcast type from broadcastSubscribers hash map.
		List<MicroService> subscribers = broadcastSubscribers.getOrDefault(b.getClass(), Collections.emptyList());
		// Add the broadcast message to each subscriber's queue
		for (MicroService subscriber : subscribers) {
			BlockingQueue<Message> queue = microServiceQueues.get(subscriber);
			if (queue != null) {
				queue.add(b);
			}
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// Events are routed to one MicroService in a round-robin manner among those subscribed to the event type.
		Queue<MicroService> subscribers = eventSubscribers.getOrDefault(e.getClass(), new ConcurrentLinkedQueue<>());
		MicroService chosenMs;
		// Synchronize to ensure thread-safe round-robin delivery
		synchronized (subscribers) {
			chosenMs = subscribers.poll(); // Get the next subscriber in the queue.
			if (chosenMs != null) {
				subscribers.add(chosenMs); // Re-add the subscriber to the end of the queue for round-robin.
			}
		}
		if (chosenMs != null) {
			BlockingQueue<Message> queue = microServiceQueues.get(chosenMs);
			if (queue != null) {
				queue.add(e); // Add the event to the subscriber's message queue.
				Future<T> future = new Future<>();
				eventFutures.put(e, future); // Map the event to its corresponding Future.
				return future;
			}
		}
		return null; // Return null if there are no subscribers for the event.
	}

	@Override
	public void register(MicroService m) {
		// Create a queue for the MicroService
		microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		microServiceQueues.remove(m);
		// Remove from all event subscription lists
		eventSubscribers.values().forEach(queue -> queue.remove(m));
		// Remove from all broadcast subscription lists
		// CopyOnWriteArrayList allows safe iteration during modification, so no additional synchronization needed
		broadcastSubscribers.values().forEach(list -> list.remove(m));
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// Get the microservice's queue.
		BlockingQueue<Message> queue = microServiceQueues.get(m);
		if (queue == null) {
			throw new IllegalStateException("MicroService is not registered.");
		}
		// Retrieves the next message from the MicroService's queue, blocking if no messages are available.
		return queue.take();
	}
}

