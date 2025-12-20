package de.extio.game_engine.renderer;

import java.util.function.Consumer;

import de.extio.game_engine.renderer.model.options.UiOptions;
import de.extio.game_engine.renderer.model.options.VideoOptions;

public class RendererData {
	
	private Renderer renderer;
	
	private RendererControl rendererControl;
	
	private RenderingBoPool renderingBoPool;
	
	private final UiOptions uiOptions = new UiOptions();
	
	private final VideoOptions videoOptions = new VideoOptions();
	
	private long frame;
	
	private Consumer<Object> eventConsumer;
	
	public RendererControl getRendererControl() {
		return this.rendererControl;
	}
	
	public void setRendererControl(final RendererControl rendererControl) {
		this.rendererControl = rendererControl;
	}
	
	public UiOptions getUiOptions() {
		return this.uiOptions;
	}
	
	public RenderingBoPool getRenderingBoPool() {
		return this.renderingBoPool;
	}
	
	public void setRenderingBoPool(final RenderingBoPool renderingBoPool) {
		this.renderingBoPool = renderingBoPool;
	}
	
	public long getFrame() {
		return this.frame;
	}
	
	public void setFrame(final long frame) {
		this.frame = frame;
	}
	
	public Renderer getRenderer() {
		return this.renderer;
	}
	
	public void setRenderer(final Renderer renderer) {
		this.renderer = renderer;
	}
	
	public VideoOptions getVideoOptions() {
		return this.videoOptions;
	}
	
	public Consumer<Object> getEventConsumer() {
		return this.eventConsumer;
	}
	
	public void setEventConsumer(final Consumer<Object> eventConsumer) {
		this.eventConsumer = eventConsumer;
	}
	
}
