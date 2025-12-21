package de.extio.game_engine.event;

import java.util.function.Consumer;

public class EventService {

	private final EventHandlerRegistry registry;
	private final EventExecutor executor;

	public EventService(final EventHandlerRegistry registry, final EventExecutor executor) {
		this.registry = registry;
		this.executor = executor;
	}

	public void register(final Class<? extends Event> eventClass, final String consumerId, final Consumer<Event> consumer) {
		this.registry.register(eventClass, consumerId, consumer);
	}

	public void unregister(final Class<? extends Event> eventClass, final String consumerId) {
		this.registry.unregister(eventClass, consumerId);
	}

	public void fire(final Event event) {
		this.executor.submit(event);
	}
}
