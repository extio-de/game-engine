package de.extio.game_engine.renderer.g2d;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "game-engine.renderer.strategy", havingValue = "g2d", matchIfMissing = true)
public class G2DAutoConfiguration {

	@Bean
	public G2DRenderer g2dRenderer(@Value("${game-engine.renderer.title:}") final String title) {
		final var renderer = new G2DRenderer();
		renderer.setTitle(title);
		return renderer;
	}
	
	@Bean
	public G2DRendererControl g2dRendererControl(final G2DRenderer renderer) {
		return new G2DRendererControl(renderer);
	}

}
