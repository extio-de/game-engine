package de.extio.game_engine.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.audio.AudioControl;
import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.keyboard.KeycodeRegistry;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.container.ScrollArea;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.storage.StorageService;

public abstract class AbstractOptionsTab {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RenderingBoPool renderingBoPool;

	@Autowired
	private EventService eventService;

	@Autowired
	private LocalizationService localizationService;

	@Autowired
	private RendererControl rendererControl;

	@Autowired
	private AudioControl audioControl;

	@Autowired
	private ThemeManager themeManager;

	@Autowired
	private StorageService storageService;

	@Autowired
	private KeycodeRegistry keycodeRegistry;

	protected final ApplicationContext applicationContext() {
		return this.applicationContext;
	}

	protected final RenderingBoPool renderingBoPool() {
		return this.renderingBoPool;
	}

	protected final EventService eventService() {
		return this.eventService;
	}

	protected final LocalizationService localizationService() {
		return this.localizationService;
	}

	protected final RendererControl rendererControl() {
		return this.rendererControl;
	}

	protected final AudioControl audioControl() {
		return this.audioControl;
	}

	protected final ThemeManager themeManager() {
		return this.themeManager;
	}

	protected final StorageService storageService() {
		return this.storageService;
	}

	protected final KeycodeRegistry keycodeRegistry() {
		return this.keycodeRegistry;
	}

	protected final Window optionsWindow(final OptionsModuleContext context) {
		return context.optionsWindow();
	}

	protected final ScrollArea contentScrollArea(final OptionsModuleContext context) {
		return context.contentScrollArea();
	}

	protected final void refreshContent(final OptionsModuleContext context) {
		context.refreshContent();
	}

	protected final void rebuildWindow(final OptionsModuleContext context) {
		context.rebuildWindow();
	}

	protected final int contentWidth(final OptionsModuleContext context) {
		return context.contentWidth();
	}

	protected static String sanitizeId(final String input) {
		return input.replaceAll("[^a-zA-Z0-9]", "_");
	}
}