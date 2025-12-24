package de.extio.game_engine.resource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.resource.enabled", havingValue = "true", matchIfMissing = true)
public class StaticResourceAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	StaticResourceService staticResourceService() {
		return new StaticResourceServiceImpl();
	}
}
