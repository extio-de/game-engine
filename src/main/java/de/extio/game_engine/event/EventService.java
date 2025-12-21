package de.extio.game_engine.event;

import java.util.function.Consumer;

public interface EventService {
	
	void register(Class<? extends Event> eventClass, String consumerId, Consumer<Event> consumer);
	
	void unregister(Class<? extends Event> eventClass, String consumerId);
	
	void fire(Event event);
	
}
