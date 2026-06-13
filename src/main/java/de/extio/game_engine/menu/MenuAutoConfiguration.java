package de.extio.game_engine.menu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.renderer.work.RenderingBoPool;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.menu.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
public class MenuAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.enabled", havingValue = "true", matchIfMissing = true)
	OptionsModule optionsModule(final ApplicationContext applicationContext, final RenderingBoPool renderingBoPool, final EventService eventService,
			final LocalizationService localizationService) {
		return new OptionsModule(applicationContext, renderingBoPool, eventService, localizationService);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.audio.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.audio.enabled", havingValue = "true", matchIfMissing = true)
	OptionsAudioTab optionsAudioTab() {
		return new OptionsAudioTab();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.video.enabled", havingValue = "true", matchIfMissing = true)
	OptionsVideoTab optionsVideoTab(@Value("${game-engine.menu.options.video.scaleFactor.enabled:true}") final boolean scaleFactorEnabled) {
		return new OptionsVideoTab(scaleFactorEnabled);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.keyboard.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.keycode-registry.enabled", havingValue = "true", matchIfMissing = true)
	OptionsKeyboardTab optionsKeyboardTab() {
		return new OptionsKeyboardTab();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.themes.enabled", havingValue = "true", matchIfMissing = true)
	OptionsThemesTab optionsThemesTab() {
		return new OptionsThemesTab();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.themes.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.menu.hsb-color-picker.enabled", havingValue = "true", matchIfMissing = true)
	HsbColorPickerModule hsbColorPickerModule() {
		return new HsbColorPickerModule();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "game-engine.menu.options.themes.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.menu.theme-editor.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(name = "game-engine.menu.hsb-color-picker.enabled", havingValue = "true", matchIfMissing = true)
	ThemeEditorModule themeEditorModule() {
		return new ThemeEditorModule();
	}
}