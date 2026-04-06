package de.extio.game_engine.menu;

import de.extio.game_engine.renderer.model.event.UiControlEvent;

public interface OptionsTab {
	String tabId();

	String controlId();

	String navigationLocalizationKey();

	String fallbackCaption();

	int navigationOrder();

	default boolean visibleInNavigation() {
		return true;
	}

	void render(OptionsModuleContext context);

	boolean handle(UiControlEvent event, OptionsModuleContext context);

	default void onContentCleared(final OptionsModuleContext context) {
	}

	default void onTabOpen(final OptionsModuleContext context) {
		
	}

	default void onTabClose(final OptionsModuleContext context, final Runnable closeTab) {
		closeTab.run();
	}
}