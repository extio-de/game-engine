package de.extio.game_engine.renderer.g2d.theme;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.resource.StaticResourceService;

public class G2DThemeManager implements ThemeManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(G2DThemeManager.class);
	
	private G2DRenderer g2dRenderer;

	private final StaticResourceService staticResourceService;
	
	private final Map<String, PatternRenderer> patternRenderers;
	private final Map<String, Theme> themes;
	private final Theme defaultTheme;
	
	private Theme currentTheme;
	
	public G2DThemeManager(
			final StaticResourceService staticResourceService,
			final List<PatternRenderer> patternRendererList,
			final Map<String, Theme> themes,
			final Theme defaultTheme) {
		this.staticResourceService = staticResourceService;
		this.patternRenderers = patternRendererList.stream()
				.collect(Collectors.toMap(
						renderer -> renderer.getClass().getSimpleName().substring(0, 1).toLowerCase() + renderer.getClass().getSimpleName().substring(1),
						Function.identity()));
		this.themes = Objects.requireNonNull(themes, "themes cannot be null");
		this.defaultTheme = Objects.requireNonNull(defaultTheme, "defaultTheme cannot be null");
		
		LOGGER.info("G2DThemeManager initialized with {} pattern renderers: {}", this.patternRenderers.size(), this.patternRenderers.keySet());
		LOGGER.info("G2DThemeManager initialized with {} themes: {}", this.themes.size(), this.themes.keySet());
		this.currentTheme = this.defaultTheme;
	}
	
	@Override
	public List<String> getAvailableThemeNames() {
		return this.themes.values().stream()
				.map(Theme::getName)
				.distinct()
				.sorted()
				.toList();
	}

	@Override
	public void setCurrentTheme(final String themeName) {
		if (themeName == null || themeName.isBlank()) {
			throw new IllegalArgumentException("themeName cannot be null/blank");
		}

		final var direct = this.themes.get(themeName);
		if (direct != null) {
			this.setCurrentTheme(direct);
			return;
		}

		final var byDisplayName = this.themes.values().stream()
				.filter(theme -> theme != null && theme.getName() != null && theme.getName().equalsIgnoreCase(themeName))
				.findFirst()
				.orElse(null);
		if (byDisplayName != null) {
			this.setCurrentTheme(byDisplayName);
			return;
		}

		this.setCurrentTheme(this.loadTheme(themeName));
	}

	@Override
	public Theme getCurrentTheme() {
		return this.currentTheme;
	}
	
	public PatternRenderer getPatternRenderer(final String rendererName) {
		return this.patternRenderers.get(rendererName);
	}
	
	@Override
	public void setCurrentTheme(final Theme theme) {
		if (theme == null) {
			throw new IllegalArgumentException("Theme cannot be null");
		}
		
		this.currentTheme = theme;
		LOGGER.info("Theme switched to: {}", theme.getName());

		this.g2dRenderer.reset();
	}
	
	public Theme loadTheme(final String themeName) {
		Objects.requireNonNull(this.staticResourceService, "staticResourceService");
		LOGGER.warn("Theme loading from StaticResourceService not yet implemented. Requested theme: {}. Using default theme.", themeName);
		return this.defaultTheme;
	}

	public void setG2dRenderer(final G2DRenderer g2dRenderer) {
		this.g2dRenderer = g2dRenderer;
	}
}
