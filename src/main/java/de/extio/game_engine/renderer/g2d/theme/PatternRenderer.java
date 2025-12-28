package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Interface for rendering common UI patterns like borders, backgrounds, shadows, etc.
 * Different implementations can provide different visual styles.
 */
public interface PatternRenderer {
	
	/**
	 * Draws a decorative border around a component.
	 *
	 * @param g2d      The graphics context
	 * @param x        X position
	 * @param y        Y position
	 * @param width    Width of the border area
	 * @param height   Height of the border area
	 * @param strength Border thickness/strength
	 * @param color    Border color
	 */
	void drawDecorativeBorder(Graphics2D g2d, int x, int y, int width, int height, int strength, Color color);
	
	/**
	 * Draws a decorative border with a filled background.
	 *
	 * @param g2d            The graphics context
	 * @param x              X position
	 * @param y              Y position
	 * @param width          Width of the border area
	 * @param height         Height of the border area
	 * @param strength       Border thickness/strength
	 * @param borderColor    Border color
	 * @param backgroundColor Background fill color
	 */
	void drawDecorativeBorderFilled(Graphics2D g2d, int x, int y, int width, int height, int strength, Color borderColor, Color backgroundColor);
	
	/**
	 * Draws a window panel with border and background.
	 *
	 * @param g2d         The graphics context
	 * @param x           X position
	 * @param y           Y position
	 * @param width       Width
	 * @param height      Height
	 * @param thickBorder Whether to use thick border style
	 * @param mainColor   Main panel color
	 * @param darkerColor Darker shade for borders
	 * @param scaleFactor UI scale factor
	 */
	void drawWindowPanel(Graphics2D g2d, int x, int y, int width, int height, boolean thickBorder, Color innerBorderColor, Color outerBorderColor, Color backgroundColor, double scaleFactor);
	
	/**
	 * Draws a window close button with appropriate styling based on state.
	 *
	 * @param g2d             The graphics context
	 * @param width           Width of the button
	 * @param height          Height of the button
	 * @param enabled         Whether the button is enabled
	 * @param state           Button state flags (hovered, pressed, toggled)
	 * @param backgroundColor Optional background color override (null to use theme defaults)
	 * @param scaleFactor     UI scale factor
	 * @param theme           Theme to use for colors
	 */
	void drawCloseButton(Graphics2D g2d, int width, int height, boolean enabled, int state, Color backgroundColor, double scaleFactor, Theme theme);

	/**
	 * Draws a background pattern for the UI on layer BACKGROUND1
	 *
	 * @param g2d      The graphics context
	 * @param offset   Offset for pattern alignment
	 * @param viewPort Viewport size
	 */
	void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort);
}
