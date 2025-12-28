package de.extio.game_engine.renderer.g2d.theme;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.G2DRendererControl;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

public class G2DThemeManager implements ThemeManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(G2DThemeManager.class);
	
	private static final List<String> THEME_STORAGE_PATH = List.of("gameEngine", "themes");
	private static final String THEME_STORAGE_NAME = "lastTheme";
	
	private G2DRenderer g2dRenderer;
	private G2DRendererControl g2dRendererControl;

	private final StaticResourceService staticResourceService;
	private final StorageService storageService;
	
	private final Map<String, PatternRenderer> patternRenderers;
	private final Map<String, Theme> themes;
	private final Theme defaultTheme;
	
	private Theme currentTheme;
	
	public G2DThemeManager(
			final StaticResourceService staticResourceService,
			final StorageService storageService,
			final List<PatternRenderer> patternRendererList,
			final Map<String, Theme> themes,
			final String defaultThemeName) {
		this.staticResourceService = staticResourceService;
		this.storageService = storageService;
		this.patternRenderers = patternRendererList.stream()
				.collect(Collectors.toMap(
						renderer -> renderer.getClass().getSimpleName().substring(0, 1).toLowerCase() + renderer.getClass().getSimpleName().substring(1),
						Function.identity()));
		this.themes = new ConcurrentHashMap<>(Objects.requireNonNull(themes, "themes cannot be null"));
		this.defaultTheme = Objects.requireNonNull(this.themes.get(defaultThemeName), "defaultTheme cannot be null");
		
		LOGGER.info("G2DThemeManager initialized with {} pattern renderers: {}", this.patternRenderers.size(), this.patternRenderers.keySet());
		LOGGER.info("G2DThemeManager initialized with {} themes: {}", this.themes.size(), this.themes.keySet());
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
	public Optional<Theme> loadThemeFromStorage(final String themeName) {
		if (themeName == null || themeName.isBlank()) {
			throw new IllegalArgumentException("themeName cannot be null/blank");
		}
		if (this.storageService == null) {
			return Optional.empty();
		}
		final Optional<Theme> loaded = this.storageService.loadByPath(Theme.class, THEME_STORAGE_PATH, themeName);
		loaded.ifPresent(this::registerTheme);
		return loaded;
	}
	
	@Override
	public void saveThemeToStorage(final Theme theme) {
		if (theme == null) {
			throw new IllegalArgumentException("theme cannot be null");
		}
		if (theme.getName() == null || theme.getName().isBlank()) {
			throw new IllegalArgumentException("theme.name cannot be null/blank");
		}
		if (this.storageService == null) {
			throw new IllegalStateException("storageService not available");
		}
		this.storageService.store(THEME_STORAGE_PATH, theme.getName(), theme);
	}
	
	@Override
	public Optional<Theme> loadThemeFromStaticResource(final StaticResource themeResource) {
		Objects.requireNonNull(themeResource, "themeResource");
		if (this.staticResourceService == null) {
			return Optional.empty();
		}
		final Optional<Theme> loaded = this.staticResourceService.loadByPath(Theme.class, themeResource);
		loaded.ifPresent(this::registerTheme);
		return loaded;
	}

	@Override
	public void setCurrentTheme(final String themeName) {
		if (themeName == null || themeName.isBlank()) {
			this.applyLastUsedOrDefault();
			return;
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
	public void setCurrentTheme(final Theme theme) {
		if (theme == null) {
			this.applyLastUsedOrDefault();
			return;
		}
		
		this.applyTheme(theme);
		this.registerTheme(theme);
		LOGGER.info("Theme switched to: {}", theme.getName());
		this.persistLastUsedTheme(theme);
	}
	
	@Override
	public Theme getCurrentTheme() {
		return this.currentTheme;
	}
	
	private Theme loadTheme(final String themeName) {
		try {
			final Optional<Theme> fromStorage = this.loadThemeFromStorage(themeName);
			if (fromStorage.isPresent()) {
				LOGGER.info("Loaded theme '{}' from storage", themeName);
				return fromStorage.get();
			}
		}
		catch (final Exception e) {
			LOGGER.warn("Could not load theme '{}' from storage. Using default.", themeName, e);
		}
		
		LOGGER.warn("Theme '{}' not found in storage. Using default theme.", themeName);
		return this.defaultTheme;
	}

	private void applyLastUsedOrDefault() {
		try {
			final Optional<Theme> persistedTheme = this.storageService.loadByPath(Theme.class, THEME_STORAGE_PATH, THEME_STORAGE_NAME);
			if (persistedTheme.isPresent()) {
				this.applyTheme(persistedTheme.get());
				this.registerTheme(this.currentTheme);
				LOGGER.info("Loaded last used theme: {}", this.currentTheme.getName());
			}

			this.applyTheme(this.defaultTheme);
			LOGGER.info("Loaded default theme: {}", this.currentTheme.getName());
		}
		catch (final Exception e) {
			LOGGER.warn("Could not load last used theme. Using default.", e);
		}
	}

	private void applyTheme(Theme theme) {
		this.currentTheme = theme;
		this.g2dRendererControl.getUiOptions().setFontResource(new StaticResource(List.of("renderer"), this.currentTheme.getFont()));
		this.g2dRenderer.reset();
	}

	private void registerTheme(final Theme theme) {
		if (theme == null || theme.getName() == null || theme.getName().isBlank()) {
			return;
		}
		this.themes.put(theme.getName(), theme);
	}
	
	private void persistLastUsedTheme(final Theme theme) {
		if (this.storageService == null) {
			return;
		}
		try {
			this.storageService.store(THEME_STORAGE_PATH, THEME_STORAGE_NAME, theme);
		}
		catch (final Exception e) {
			LOGGER.warn("Could not persist last used theme", e);
		}
	}

	public void setG2dRenderer(final G2DRenderer g2dRenderer) {
		this.g2dRenderer = g2dRenderer;
	}

	public void setG2dRendererControl(G2DRendererControl g2dRendererControl) {
		this.g2dRendererControl = g2dRendererControl;
	}

	public PatternRenderer getPatternRenderer(final String rendererName) {
		return this.patternRenderers.get(rendererName);
	}
}
