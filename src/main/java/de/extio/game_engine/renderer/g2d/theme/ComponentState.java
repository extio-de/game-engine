package de.extio.game_engine.renderer.g2d.theme;

/**
 * Represents the visual state of a UI component.
 * Used by the theming system to determine which colors to apply.
 */
public enum ComponentState {
	/**
	 * Normal, default state - enabled, not interacted with
	 */
	NORMAL,
	
	/**
	 * Mouse cursor is hovering over the component
	 */
	HOVERED,
	
	/**
	 * Component is currently being pressed/clicked
	 */
	PRESSED,
	
	/**
	 * Component is in toggled/selected state (for toggle buttons, switches)
	 */
	TOGGLED,
	
	/**
	 * Component is disabled and cannot be interacted with
	 */
	DISABLED,
	
	/**
	 * Component has keyboard focus (for text fields)
	 */
	FOCUSED
}
