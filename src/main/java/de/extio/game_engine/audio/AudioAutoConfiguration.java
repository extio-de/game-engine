package de.extio.game_engine.audio;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.audio.enabled", havingValue = "true", matchIfMissing = true)
public class AudioAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	AudioController audioController(final StaticResourceService resourceService, final StorageService storageService) {
		return new AudioController(resourceService, storageService);
	}
}
