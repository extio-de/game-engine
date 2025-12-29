package de.extio.game_engine.renderer.g2d;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.g2d.theme.PatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.BevelDarkThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.BevelLightThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.BlueprintThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.ChronicleThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.ContemporaryThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.DreamThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.FantasyThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.ModernThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.NeonThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.NoirThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.SpacecraftThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.SteampunkThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.renderer.g2d.theme.UrbanThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.VintageThemeFactoryBean;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.renderer.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "game-engine.renderer.strategy", havingValue = "g2d", matchIfMissing = true)
public class G2DAutoConfiguration {
	
	@Bean
	G2DThemeManager g2dThemeManager(
			final StaticResourceService staticResourceService,
			final StorageService storageService,
			final List<PatternRenderer> patternRendererList,
			final Map<String, Theme> themes,
			@Value("${game-engine.renderer.default-theme:urbanTheme}") final String defaultThemeName) {
		return new G2DThemeManager(staticResourceService, storageService, patternRendererList, themes, defaultThemeName);
	}

	@Bean
	G2DRenderer g2dRenderer(
			@Value("${game-engine.renderer.title:}") final String title,
			final G2DThemeManager themeManager,
			final StaticResourceService staticResourceService) {
		final var renderer = new G2DRenderer();
		renderer.setTitle(title);
		renderer.setThemeManager(themeManager);
		renderer.setStaticResourceService(staticResourceService);
		themeManager.setG2dRenderer(renderer);
		return renderer;
	}
	
	@Bean
	G2DRendererControl g2dRendererControl(final G2DRenderer renderer, final G2DThemeManager themeManager) {
		var control = new G2DRendererControl(renderer);
		themeManager.setG2dRendererControl(control);
		return control;
	}

	@Bean
	FactoryBean<Theme> spacecraftTheme() {
		return new SpacecraftThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> vintageTheme() {
		return new VintageThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> modernTheme() {
		return new ModernThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> contemporaryTheme() {
		return new ContemporaryThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> chronicleTheme() {
		return new ChronicleThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> urbanTheme() {
		return new UrbanThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> dreamTheme() {
		return new DreamThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> fantasyTheme() {
		return new FantasyThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> noirTheme() {
		return new NoirThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> neonTheme() {
		return new NeonThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> bevelLightTheme() {
		return new BevelLightThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> bevelDarkTheme() {
		return new BevelDarkThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> blueprintTheme() {
		return new BlueprintThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> steampunkTheme() {
		return new SteampunkThemeFactoryBean();
	}

}
