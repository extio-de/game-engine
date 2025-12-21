package de.extio.game_engine.keyboard;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.keycode-registry.enabled", havingValue = "true", matchIfMissing = true)
public class KeycodeAutoConfiguration {

	@Bean
	KeycodeRegistry keycodeRegistry() {
		return new KeycodeRegistryImpl();
	}
}
