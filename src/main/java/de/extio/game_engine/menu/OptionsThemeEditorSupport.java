package de.extio.game_engine.menu;

import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.Theme;

public interface OptionsThemeEditorSupport {

	void openNew(Theme baseTheme, String proposedName, Window parentWindow);

	void openEdit(Theme theme, Window parentWindow);
}