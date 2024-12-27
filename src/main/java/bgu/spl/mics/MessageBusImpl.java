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
		private static final MessageBusImpl INSTANCE = new MessageBusImpl();
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
		return SingletonHolder.INSTANCE;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// Create a new queue if event type doesn't have a queue in the hash map.
		eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		// Add the MicroService to the list of subscribers for the given event type.
		Queue<MicroService> subscribers = eventSubscribers.get(type);
		// Lock the queue to avoid interference with sendEvent or unregister, Synchronize on shared object explicitly
		synchronized (subscribers) {
			subscribers.add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// Create a new list if broadcast type doesn't have a list in the hash map.
		broadcastSubscribers.putIfAbsent(type, new CopyOnWriteArrayList<>());
		// Add the MicroService to the list of subscribers for the given broadcast type.
		broadcastSubscribers.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// Resolve the Future associated with the given event, allowing the sender to retrieve the result.
		@SuppressWarnings("unchecked") // Suppress unchecked cast warning
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
				synchronized (queue) {
					queue.add(b);
				}
			}
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// Retrieve the queue of MicroServices subscribed to this event type.
		// If no subscriptions exist for this event type, use an empty queue.
		Queue<MicroService> subscribers = eventSubscribers.getOrDefault(e.getClass(), new ConcurrentLinkedQueue<>());

		MicroService chosenMs; // This will hold the MicroService chosen to handle the event.

		// Synchronize on the subscribers queue to ensure thread-safe access and modification.
		synchronized (subscribers) {
			// Poll the next MicroService in the queue (round-robin approach).
			chosenMs = subscribers.poll();

			// Check if the chosen MicroService is valid (i.e., it is registered and has a queue).
			// If it's not valid, keep polling the next MicroService until a valid one is found or the queue is empty.
			while (chosenMs != null && microServiceQueues.get(chosenMs) == null) {
				chosenMs = subscribers.poll(); // Skip unregistered or invalid services.
			}

			// If a valid MicroService was found, re-add it to the end of the queue for round-robin delivery.
			if (chosenMs != null) {
				subscribers.add(chosenMs);
			}
		}

		// If a valid MicroService was chosen, proceed to send the event.
		if (chosenMs != null) {
			// Retrieve the message queue for the chosen MicroService.
			BlockingQueue<Message> queue = microServiceQueues.get(chosenMs);
			if (queue != null) {
				// Add the event to the chosen MicroService's message queue.
				queue.add(e);

				// Create a Future object to represent the result of this event.
				Future<T> future = new Future<>();

				// Map this event to its corresponding Future for result resolution later.
				eventFutures.put(e, future);

				// Return the Future object to the caller.
				return future;
			}
		}

		// If no valid MicroService was available to handle the event, return null.
		// This may happen if no MicroService is subscribed to this event type.
		return null;
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
		for (Queue<MicroService> subscribers : eventSubscribers.values()) {
			synchronized (subscribers) { // Lock only the specific queue
				subscribers.remove(m); // Synchronize on shared object explicitly
			}
		}
		for (List<MicroService> list : broadcastSubscribers.values()) {
			list.remove(m); // CopyOnWriteArrayList is inherently thread-safe for iteration and modification
		}
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

