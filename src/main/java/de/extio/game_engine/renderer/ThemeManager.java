package de.extio.game_engine.renderer;

import java.util.List;
import java.util.Optional;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.renderer.g2d.theme.Theme;

public interface ThemeManager {
	
	List<String> getAvailableThemeNames();

	List<String> getPatternRendererNames();
	
	Optional<Theme> loadThemeFromStorage(String themeName);
	
	void saveThemeToStorage(Theme theme);
	
	Optional<Theme> loadThemeFromStaticResource(StaticResource themeResource);

	Theme getCurrentTheme();
	
	void setCurrentTheme(Theme theme);

	void setCurrentTheme(String themeName);
	
}
