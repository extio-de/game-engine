package de.extio.game_engine.renderer;

import java.util.List;

import de.extio.game_engine.renderer.g2d.theme.Theme;

public interface ThemeManager {
	
	List<String> getAvailableThemeNames();

	Theme getCurrentTheme();
	
	void setCurrentTheme(Theme theme);

	void setCurrentTheme(String themeName);
	
}
