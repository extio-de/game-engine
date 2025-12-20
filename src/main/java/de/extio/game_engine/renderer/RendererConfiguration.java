package de.extio.game_engine.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.renderer.model.RenderingBo;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
public class RendererConfiguration {
	
	@SuppressWarnings("unchecked")
	@Bean
	public RenderingBoPool renderingBoPool(final List<RenderingBo> renderingBoImplementations) {
		final Map<Class<? extends RenderingBo>, Class<? extends RenderingBo>> mapping = new HashMap<>();
		for (final var impl : renderingBoImplementations) {
			for (final var interf : impl.getClass().getInterfaces()) {
				if (RenderingBo.class.isAssignableFrom(interf)) {
					mapping.put((Class<? extends RenderingBo>) interf, impl.getClass());
				}
			}
		}
		
		return new RenderingBoPoolImpl(mapping);
	}
	
	@Bean
	public RendererData rendererData(final Renderer renderer, final RendererControl rendererControl, final RenderingBoPool renderingBoPool, @Qualifier("gameEngineEventConsumer") final Consumer<Object> eventConsumer) {
		final var rendererData = new RendererData(renderer, rendererControl, renderingBoPool, eventConsumer);
		renderer.setRendererData(rendererData);
		rendererControl.setRendererData(rendererData);
		renderingBoPool.setRendererData(rendererData);
		return rendererData;
	}
	
	@Bean
	public RendererLoop rendererLoop(final RendererData rendererData) {
		final var rendererLoop = new RendererLoop(rendererData);
		rendererData.setRendererLoop(rendererLoop);
		rendererLoop.start();
		return rendererLoop;
	}
	
}
