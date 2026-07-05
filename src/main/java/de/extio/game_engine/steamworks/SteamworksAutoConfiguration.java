package de.extio.game_engine.steamworks;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.steamworks.enabled", havingValue = "true")
@ConditionalOnClass(name = "com.codedisaster.steamworks.SteamAPI")
public class SteamworksAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean(SteamworksConnector.class)
	SteamworksConnectorImpl steamworksConnector() {
		final var connector = new SteamworksConnectorImpl();
		SteamworksConnector.setInstance(connector);
		return connector;
	}
	
	@Bean
	@ConditionalOnMissingBean
	SteamworksRunner steamworksRunner(final SteamworksConnector connector) {
		return new SteamworksRunner(connector);
	}
	
}
