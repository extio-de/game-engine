package de.extio.game_engine.i18n;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.i18n.enabled", havingValue = "true", matchIfMissing = true)
public class LocalizationAutoConfiguration {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationServiceImpl.class);
	
	@Bean
	@ConditionalOnMissingBean
	LocalizationService localizationManager(@Value("${game-engine.i18n.load-on-start:true}") final boolean loadOnStart, @Value("${game-engine.i18n.resource:i18n/i18n.yaml}") final String resource, final StaticResourceService staticResourceService, final StorageService storageService) {
		final var localizationManager = new LocalizationServiceImpl(storageService);
		
		if (loadOnStart) {
			final var path = new File(resource).toPath();
			final var staticResource = new StaticResource(path.getParent() != null ? StreamSupport.stream(path.getParent().spliterator(), false).map(Path::toString).toList() : null, path.getFileName().toString());
			staticResourceService.loadStreamByPath(staticResource).ifPresent(stream -> {
				try (stream) {
					localizationManager.load(stream);
				}
				catch (final Exception e) {
					LOGGER.error("An exception occured while loading localizations from resource: " + resource, e);
				}
			});
		}
		
		return localizationManager;
	}
	
}
