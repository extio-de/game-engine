package de.extio.game_engine.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.storage.dialog.FileSelectionDialogModule;
import de.extio.game_engine.storage.dialog.FileSelectionMoveDialogModule;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.storage.enabled", havingValue = "true", matchIfMissing = true)
public class StorageServiceAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	StorageService storageService() {
		return new StorageServiceImpl();
	}
	
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.storage.dialog.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
	FileSelectionMoveDialogModule fileSelectionMoveDialog(final ApplicationContext applicationContext, final EventService eventService, final LocalizationService localizationService, final RenderingBoPool renderingBoPool, final StorageService storageService) {
		return new FileSelectionMoveDialogModule(applicationContext, eventService, localizationService, renderingBoPool, storageService);
	}
	
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.storage.dialog.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
	FileSelectionDialogModule fileSelectionDialog(final ApplicationContext applicationContext, final EventService eventService, final LocalizationService localizationService, final RenderingBoPool renderingBoPool, final StorageService storageService, final FileSelectionMoveDialogModule fileSelectionMoveDialog) {
		return new FileSelectionDialogModule(applicationContext, eventService, localizationService, renderingBoPool, storageService, fileSelectionMoveDialog);
	}
}
