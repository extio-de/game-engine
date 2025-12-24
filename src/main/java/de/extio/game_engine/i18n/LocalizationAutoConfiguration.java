package de.extio.game_engine.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.i18n.enabled", havingValue = "true", matchIfMissing = true)
public class LocalizationAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationServiceImpl.class);

	@Bean
	@ConditionalOnMissingBean
	LocalizationService localizationManager(@Value("${game-engine.i18n.load-on-start:true}") final boolean loadOnStart, @Value("${game-engine.i18n.resource:i18n.yaml}") final String resource) {
		final var localizationManager = new LocalizationServiceImpl();
		
		if (loadOnStart) {
			try (var resourceStream = LocalizationAutoConfiguration.class.getClassLoader().getResourceAsStream(resource)) {
				if (resourceStream != null) {
					localizationManager.load(resourceStream);
				}
			}
			catch (final Exception exc) {
				LOGGER.error("An exception occured while loading localizations from resource: " + resource, exc);
			}
		}

		return localizationManager;
	}
	
}
