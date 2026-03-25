package de.extio.game_engine.renderer.g2d.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.G2DRendererControl;
import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;
import de.extio.game_engine.renderer.model.options.UiOptions;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

public class G2DThemeManagerTest {
	
	private static Theme theme(final String name) {
		return Theme.builder()
				.name(name)
				.patternRendererName("testPatternRenderer")
				.borderOuter(new HSBColor(0.1f, 0.2f, 0.3f))
				.borderInner(new HSBColor(0.1f, 0.2f, 0.3f))
				.borderInnerDisabled(new HSBColor(0.1f, 0.2f, 0.3f))
				.backgroundNormal(new HSBColor(0.1f, 0.2f, 0.3f))
				.backgroundSelected(new HSBColor(0.1f, 0.2f, 0.3f))
				.textNormal(new HSBColor(0.1f, 0.2f, 0.3f))
				.textDisabled(new HSBColor(0.1f, 0.2f, 0.3f))
				.selectionPrimary(new HSBColor(0.1f, 0.2f, 0.3f))
				.selectionSecondary(new HSBColor(0.1f, 0.2f, 0.3f))
				.windowBackground(new HSBColor(0.1f, 0.2f, 0.3f))
				.hoverBrightnessAdjustment(0.25f)
				.pressedBrightnessAdjustment(0.40f)
				.build();
	}
	
	@Test
	void loadsLastUsedThemeFromStorage() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final StorageService storageService = org.mockito.Mockito.mock(StorageService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		final Theme defaultTheme = theme("Default");
		final Theme persistedTheme = theme("Persisted");
		
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("lastTheme")))
				.thenReturn(Optional.of(persistedTheme));
		
		final var manager = new G2DThemeManager(
				staticResourceService,
				storageService,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		manager.setCurrentTheme((Theme) null);
		
		assertEquals("Persisted", manager.getCurrentTheme().getName());
	}
	
	@Test
	void setCurrentThemePersistsLastThemeObject() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final StorageService storageService = org.mockito.Mockito.mock(StorageService.class);
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("lastTheme")))
				.thenReturn(Optional.empty());
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		final G2DRendererControl g2dRendererControl = org.mockito.Mockito.mock(G2DRendererControl.class);
		when(g2dRendererControl.getUiOptions()).thenReturn(new UiOptions());
		final G2DRenderer g2dRenderer = org.mockito.Mockito.mock(G2DRenderer.class);
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				staticResourceService,
				storageService,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		manager.setG2dRendererControl(g2dRendererControl);
		manager.setG2dRenderer(g2dRenderer);
		
		final Theme selected = theme("Selected");
		manager.setCurrentTheme(selected);
		
		verify(storageService).store(eq(List.of("gameEngine")), eq("lastTheme"), eq(selected));
		assertTrue(manager.getAvailableThemeNames().contains("Selected"));
	}
	
	@Test
	void loadThemeFromStorageRegistersTheme() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final StorageService storageService = org.mockito.Mockito.mock(StorageService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("lastTheme")))
				.thenReturn(Optional.empty());
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				staticResourceService,
				storageService,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		
		final Theme loaded = theme("FromStorage");
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("FromStorage")))
				.thenReturn(Optional.of(loaded));
		
		final Optional<Theme> result = manager.loadThemeFromStorage("FromStorage");
		assertTrue(result.isPresent());
		assertEquals("FromStorage", result.get().getName());
		assertTrue(manager.getAvailableThemeNames().contains("FromStorage"));
	}
	
	@Test
	void loadThemeFromStaticResourceRegistersTheme() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final StorageService storageService = org.mockito.Mockito.mock(StorageService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("lastTheme")))
				.thenReturn(Optional.empty());
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				staticResourceService,
				storageService,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		
		final Theme loaded = theme("FromStaticResource");
		final StaticResource resource = new StaticResource(List.of("themes"), "fromStatic.theme");
		when(staticResourceService.loadByPath(eq(Theme.class), eq(resource)))
				.thenReturn(Optional.of(loaded));
		
		final Optional<Theme> result = manager.loadThemeFromStaticResource(resource);
		assertTrue(result.isPresent());
		assertEquals("FromStaticResource", result.get().getName());
		assertTrue(manager.getAvailableThemeNames().contains("FromStaticResource"));
	}
	
	@Test
	void saveThemeToStorageUsesThemeNameAsStorageName() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final StorageService storageService = org.mockito.Mockito.mock(StorageService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("lastTheme")))
				.thenReturn(Optional.empty());
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				staticResourceService,
				storageService,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		
		final Theme themeToSave = theme("UserTheme");
		manager.saveThemeToStorage(themeToSave);
		
		verify(storageService).store(eq(List.of("gameEngine")), eq("UserTheme"), eq(themeToSave));
	}
	
	@Test
	void saveThemeToStorageThrowsIfNoStorageService() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				staticResourceService,
				null,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		
		assertThrows(IllegalStateException.class, () -> manager.saveThemeToStorage(theme("SomeTheme")));
	}
	
	@Test
	void loadThemeFromStorageReturnsEmptyWhenStorageNotAvailable() {
		final StaticResourceService staticResourceService = org.mockito.Mockito.mock(StaticResourceService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				staticResourceService,
				null,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		
		final Optional<Theme> loaded = manager.loadThemeFromStorage("Anything");
		assertFalse(loaded.isPresent());
	}
	
	@Test
	void loadThemeFromStaticResourceReturnsEmptyWhenStaticResourceServiceNotAvailable() {
		final StorageService storageService = org.mockito.Mockito.mock(StorageService.class);
		final PatternRenderer patternRenderer = org.mockito.Mockito.mock(PatternRenderer.class);
		
		when(storageService.loadByPath(eq(Theme.class), eq(List.of("gameEngine")), eq("lastTheme")))
				.thenReturn(Optional.empty());
		
		final Theme defaultTheme = theme("Default");
		final var manager = new G2DThemeManager(
				null,
				storageService,
				List.of(patternRenderer),
				Map.of("defaultKey", defaultTheme),
				"defaultKey");
		
		final Optional<Theme> loaded = manager.loadThemeFromStaticResource(new StaticResource(List.of("themes"), "x"));
		assertFalse(loaded.isPresent());
		verify(storageService, never()).store(any(), any(), any());
	}
}
