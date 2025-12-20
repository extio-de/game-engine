package de.extio.game_engine.renderer;

import java.util.function.Consumer;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.G2DRendererControl;

public class RendererFactory {
	
	public static Renderer createG2DRenderer(final RenderingBoPool renderingBoPool, final Consumer<Object> eventConsumer) {
		final var rendererData = new RendererData();
		final var renderer = new G2DRenderer(rendererData);
		rendererData.setRendererControl(new G2DRendererControl(renderer, rendererData));
		rendererData.setRenderingBoPool(renderingBoPool);
		rendererData.setRenderer(renderer);
		rendererData.setEventConsumer(eventConsumer);
		
		return renderer;
	}
	
}
