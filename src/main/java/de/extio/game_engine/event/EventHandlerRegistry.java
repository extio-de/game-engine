package de.extio.game_engine.event;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventHandlerRegistry {
	
	private final ConcurrentMap<Class<? extends Event>, List<EventConsumer>> handlers = new ConcurrentHashMap<>();
	
	public void register(final Class<? extends Event> eventClass, final String consumerId, final Consumer<Event> consumer) {
		final var eventConsumers = this.handlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());
		eventConsumers.removeIf(ec -> ec.id().equals(consumerId));
		eventConsumers.add(new EventConsumer(consumerId, consumer));
	}
	
	public void unregister(final Class<? extends Event> eventClass, final String consumerId) {
		final var consumers = this.handlers.get(eventClass);
		if (consumers != null) {
			consumers.removeIf(ec -> ec.id().equals(consumerId));
		}
	}
	
	List<EventConsumer> getHandlers(final Class<? extends Event> eventClass) {
		return this.handlers.get(eventClass);
	}
	
	record EventConsumer(String id, Consumer<Event> consumer) {
	}
}
