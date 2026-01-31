package de.extio.game_engine.renderer.g2d;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawBackground;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawEffect;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFpsHistory;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawImage;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawTest;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControl;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControlTooltip;
import de.extio.game_engine.renderer.g2d.theme.BevelPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.BlueprintPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.ChroniclePatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.ContemporaryPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.DreamPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.FantasyPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.g2d.theme.MetroPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.ModernPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.PatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.SpacecraftPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.SteampunkPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.UrbanPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.VintagePatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.TerminalPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.AsciiPatternRenderer;
import de.extio.game_engine.renderer.g2d.theme.BevelDarkThemeFactoryBean;
import de.extio.game_engine.renderer.g2d.theme.AsciiThemeFactoryBean;
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
import de.extio.game_engine.renderer.g2d.theme.TerminalThemeFactoryBean;
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
			@Value("${game-engine.renderer.default-theme:modernTheme}") final String defaultThemeName) {
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

	@Bean
	FactoryBean<Theme> asciiTheme() {
		return new AsciiThemeFactoryBean();
	}

	@Bean
	FactoryBean<Theme> terminalTheme() {
		return new TerminalThemeFactoryBean();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawBackground g2dDrawBackground() {
		return new G2DDrawBackground();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawEffect g2dDrawEffect() {
		return new G2DDrawEffect();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawFont g2dDrawFont() {
		return new G2DDrawFont();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawFpsHistory g2dDrawFpsHistory() {
		return new G2DDrawFpsHistory();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawImage g2dDrawImage() {
		return new G2DDrawImage();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawTest g2dDrawTest() {
		return new G2DDrawTest();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawControl g2dDrawControl() {
		return new G2DDrawControl();
	}

	@Bean
	@ConditionalOnMissingBean
	G2DDrawControlTooltip g2dDrawControlTooltip() {
		return new G2DDrawControlTooltip();
	}

	@Bean
	@ConditionalOnMissingBean
	BevelPatternRenderer bevelPatternRenderer() {
		return new BevelPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	BlueprintPatternRenderer blueprintPatternRenderer() {
		return new BlueprintPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	ChroniclePatternRenderer chroniclePatternRenderer() {
		return new ChroniclePatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	ContemporaryPatternRenderer contemporaryPatternRenderer() {
		return new ContemporaryPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	DreamPatternRenderer dreamPatternRenderer() {
		return new DreamPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	FantasyPatternRenderer fantasyPatternRenderer() {
		return new FantasyPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	MetroPatternRenderer metroPatternRenderer() {
		return new MetroPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	ModernPatternRenderer modernPatternRenderer() {
		return new ModernPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	SpacecraftPatternRenderer spacecraftPatternRenderer() {
		return new SpacecraftPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	SteampunkPatternRenderer steampunkPatternRenderer() {
		return new SteampunkPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	UrbanPatternRenderer urbanPatternRenderer() {
		return new UrbanPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	VintagePatternRenderer vintagePatternRenderer() {
		return new VintagePatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	AsciiPatternRenderer asciiPatternRenderer() {
		return new AsciiPatternRenderer();
	}

	@Bean
	@ConditionalOnMissingBean
	TerminalPatternRenderer terminalPatternRenderer() {
		return new TerminalPatternRenderer();
	}

}
