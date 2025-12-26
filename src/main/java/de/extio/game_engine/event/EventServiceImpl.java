package de.extio.game_engine.event;

import java.util.function.Consumer;

public class EventServiceImpl implements EventService {

	private final EventHandlerRegistry registry;
	private final EventExecutor executor;

	public EventServiceImpl(final EventHandlerRegistry registry, final EventExecutor executor) {
		this.registry = registry;
		this.executor = executor;
	}

	@Override
	public <T extends Event> void register(final Class<T> eventClass, final String consumerId, final Consumer<T> consumer) {
		this.registry.register(eventClass, consumerId, consumer);
	}

	@Override
	public void unregister(final Class<? extends Event> eventClass, final String consumerId) {
		this.registry.unregister(eventClass, consumerId);
	}

	@Override
	public void unregisterAll(final String consumerId) {
		this.registry.unregisterAll(consumerId);
	}

	@Override
	public void fire(final Event event) {
		this.executor.submit(event);
	}
}
