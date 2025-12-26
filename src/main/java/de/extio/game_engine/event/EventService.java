package de.extio.game_engine.event;

import java.util.function.Consumer;

public interface EventService {
	
	<T extends Event> void register(Class<T> eventClass, String consumerId, Consumer<T> consumer);
	
	void unregister(Class<? extends Event> eventClass, String consumerId);

	void unregisterAll(String consumerId);
	
	void fire(Event event);
	
}
