package de.extio.game_engine.event;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.events.enabled", havingValue = "true", matchIfMissing = true)
public class EventsAutoConfiguration {

	@Bean
	EventHandlerRegistry eventHandlerRegistry() {
		return new EventHandlerRegistry();
	}

	@Bean
	EventExecutor eventExecutor(final EventHandlerRegistry registry) {
		return new EventExecutor(registry);
	}

	@Bean
	EventService eventService(final EventHandlerRegistry registry, final EventExecutor executor) {
		final EventService eventService = new EventService(registry, executor);
		return eventService;
	}
}
