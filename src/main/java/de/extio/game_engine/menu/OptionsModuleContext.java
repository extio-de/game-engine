package de.extio.game_engine.menu;

import de.extio.game_engine.renderer.container.ScrollArea;
import de.extio.game_engine.renderer.container.Window;

public interface OptionsModuleContext {

	Window optionsWindow();

	ScrollArea contentScrollArea();

	void refreshContent();

	void rebuildWindow();

	default int contentWidth() {
		return this.optionsWindow().getNormalizedDimension().getX() - Window.MARGIN_LEFT - Window.MARGIN_RIGHT;
	}
}