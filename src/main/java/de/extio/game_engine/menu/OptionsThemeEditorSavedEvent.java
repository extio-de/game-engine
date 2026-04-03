package de.extio.game_engine.menu;

import de.extio.game_engine.event.Event;
import de.extio.game_engine.renderer.model.Theme;

public record OptionsThemeEditorSavedEvent(boolean saved, Theme theme) implements Event {
}