package de.extio.game_engine.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.storage.enabled", havingValue = "true", matchIfMissing = true)
public class StorageServiceAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	StorageService storageService() {
		return new StorageServiceImpl();
	}
}
