package de.extio.game_engine.renderer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.springframework.context.ApplicationContext;

import de.extio.game_engine.keyboard.KeycodeRegistry;
import de.extio.game_engine.renderer.model.options.UiOptions;
import de.extio.game_engine.renderer.model.options.VideoOptions;

public class RendererData {

	private final ApplicationContext applicationContext;
	
	private final Renderer renderer;
	
	private final RendererControl rendererControl;
	
	private final RenderingBoPool renderingBoPool;
	
	private final Consumer<Object> eventConsumer;
	
	private final UiOptions uiOptions = new UiOptions();
	
	private final VideoOptions videoOptions = new VideoOptions();
	
	private final AtomicLong frame = new AtomicLong();

	private final KeycodeRegistry keycodeRegistry;
	
	private RendererLoop rendererLoop;
	
	public RendererData(final ApplicationContext applicationContext, final Renderer renderer, final RendererControl rendererControl, final RenderingBoPool renderingBoPool, final Consumer<Object> eventConsumer, final KeycodeRegistry keycodeRegistry) {
		this.applicationContext = applicationContext;
		this.renderer = renderer;
		this.rendererControl = rendererControl;
		this.renderingBoPool = renderingBoPool;
		this.eventConsumer = eventConsumer;
		this.keycodeRegistry = keycodeRegistry;
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
	
	public Consumer<Object> getEventConsumer() {
		return this.eventConsumer;
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
	
}
