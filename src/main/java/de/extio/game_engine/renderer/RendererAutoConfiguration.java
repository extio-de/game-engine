package de.extio.game_engine.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.keyboard.KeycodeRegistry;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.storage.StorageService;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
public class RendererAutoConfiguration {
	
	@SuppressWarnings("unchecked")
	@Bean
	@ConditionalOnMissingBean
	RenderingBoPool renderingBoPool(final List<RenderingBo> renderingBoImplementations) {
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
	RendererData rendererData(final ApplicationContext applicationContext,
			final Renderer renderer,
			final RendererControl rendererControl,
			final RenderingBoPool renderingBoPool,
			final KeycodeRegistry keycodeRegistry,
			final EventService eventService,
			final StorageService storageService) {
		
		final var rendererData = new RendererData(applicationContext, renderer, rendererControl, renderingBoPool, keycodeRegistry, eventService, storageService);
		renderer.setRendererData(rendererData);
		rendererControl.setRendererData(rendererData);
		renderingBoPool.setRendererData(rendererData);
		return rendererData;
	}
	
	@Bean
	RendererLauncher rendererLauncher(final ApplicationContext applicationContext, final RendererData rendererData) {
		return new RendererLauncher(applicationContext, rendererData);
	}
	
}
