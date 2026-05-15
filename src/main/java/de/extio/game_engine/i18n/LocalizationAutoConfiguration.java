package de.extio.game_engine.i18n;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.i18n.enabled", havingValue = "true", matchIfMissing = true)
public class LocalizationAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	LocalizationService localizationManager(@Value("${game-engine.i18n.load-on-start:true}") final boolean loadOnStart, @Value("${game-engine.i18n.resource:i18n/metadata.yaml}") final String resource, final StaticResourceService staticResourceService, final StorageService storageService) {
		final var localizationManager = new LocalizationServiceImpl(storageService, staticResourceService);
		
		if (loadOnStart) {
			localizationManager.load(Path.of(resource));
		}
		
		return localizationManager;
	}
	
}
