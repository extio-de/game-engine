package de.extio.game_engine.renderer;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.ApplicationContext;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.keyboard.KeycodeRegistry;
import de.extio.game_engine.module.ModuleExecutor;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.model.options.UiOptions;
import de.extio.game_engine.renderer.model.options.VideoOptions;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

public class RendererData {

	private final ApplicationContext applicationContext;
	
	private final Renderer renderer;
	
	private final RendererControl rendererControl;
	
	private final RenderingBoPool renderingBoPool;
	
	private final UiOptions uiOptions = new UiOptions();
	
	private final VideoOptions videoOptions = new VideoOptions();
	
	private final AtomicLong frame = new AtomicLong();

	private final KeycodeRegistry keycodeRegistry;

	private final EventService eventService;

	private final StorageService storageService;

	private final RendererWorkingSet rendererWorkingSet;

	private final ModuleService moduleService;

	private final ModuleExecutor moduleExecutor;

	private final StaticResourceService staticResourceService;

	private final ThemeManager themeManager;
	
	private RendererLoop rendererLoop;
	
	public RendererData(final ApplicationContext applicationContext, final Renderer renderer, final RendererControl rendererControl, final RenderingBoPool renderingBoPool, final KeycodeRegistry keycodeRegistry, final EventService eventService, final StorageService storageService, final RendererWorkingSet rendererWorkingSet, final ModuleService moduleService, final ModuleExecutor moduleExecutor, final StaticResourceService staticResourceService, final ThemeManager themeManager) {
		this.applicationContext = applicationContext;
		this.renderer = renderer;
		this.rendererControl = rendererControl;
		this.renderingBoPool = renderingBoPool;
		this.keycodeRegistry = keycodeRegistry;
		this.eventService = eventService;
		this.storageService = storageService;
		this.rendererWorkingSet = rendererWorkingSet;
		this.moduleService = moduleService;
		this.moduleExecutor = moduleExecutor;
		this.staticResourceService = staticResourceService;
		this.themeManager = themeManager;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public RendererControl getRendererControl() {
		return this.rendererControl;
	}
	
	public UiOptions getUiOptions() {
		return this.uiOptions;
	}
	
	public RenderingBoPool getRenderingBoPool() {
		return this.renderingBoPool;
	}
	
	public long getFrame() {
		return this.frame.get();
	}

	public long nextFrame() {
		return this.frame.incrementAndGet();
	}
	
	public Renderer getRenderer() {
		return this.renderer;
	}
	
	public VideoOptions getVideoOptions() {
		return this.videoOptions;
	}
	
	public void setRendererLoop(final RendererLoop rendererLoop) {
		this.rendererLoop = rendererLoop;
	}

	public RendererLoop getRendererLoop() {
		return this.rendererLoop;
	}

	public KeycodeRegistry getKeycodeRegistry() {
		return this.keycodeRegistry;
	}
	
	public EventService getEventService() {
		return this.eventService;
	}

	public StorageService getStorageService() {
		return storageService;
	}

	public RendererWorkingSet getRendererWorkingSet() {
		return rendererWorkingSet;
	}

	public ModuleService getModuleService() {
		return moduleService;
	}

	public ModuleExecutor getModuleExecutor() {
		return moduleExecutor;
	}

	public StaticResourceService getStaticResourceService() {
		return staticResourceService;
	}
	
	public ThemeManager getThemeManager() {
		return themeManager;
	}

}
