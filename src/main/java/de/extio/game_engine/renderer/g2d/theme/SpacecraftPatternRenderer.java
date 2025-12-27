package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.color.RgbaColor;

/**
 * Default implementation of PatternRenderer that draws borders and panels in a decorative style.
 * This implementation provides the default theming for UI components.
 */
@Conditional(G2DRendererCondition.class)
@Component
public class SpacecraftPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var strength2 = strength * 2;
		final var strength4 = strength * 4;
		
		g2d.setColor(color);
		
		// Vertical lines
		g2d.fillRect(x, y + strength2, strength, height - strength4);
		g2d.fillRect(x + width - strength, y + strength2, strength, height - strength4);
		
		// Horizontal lines
		g2d.fillRect(x + strength2, y, width - strength4, strength);
		g2d.fillRect(x + strength2, y + height - strength, width - strength4, strength);
		
		// Corner squares
		g2d.fillRect(x + strength, y + strength, strength2, strength2);
		g2d.fillRect(x + width - strength - strength2, y + strength, strength2, strength2);
		g2d.fillRect(x + strength, y + height - strength - strength2, strength2, strength2);
		g2d.fillRect(x + width - strength - strength2, y + height - strength - strength2, strength2, strength2);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		// Fill background
		g2d.setColor(backgroundColor);
		g2d.fillRect(x + strength, y + strength, width - strength, height - strength);
		
		// Draw border on top
		this.drawDecorativeBorder(g2d, x, y, width, height, strength, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var strength = Math.max((thickBorder ? 4 : 2), (int) ((thickBorder ? 6 : 3) * scaleFactor));
		final var strength2 = strength * 2;
		final var backgroundTransparency = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 180);
		
		drawDecorativeBorderFilled(g2d, x + strength, y + strength, width - strength2, height - strength2, strength, innerBorderColor, backgroundTransparency);
		drawDecorativeBorder(g2d, x, y, width, height, strength, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var STATE_HOVERED = 4;
		final var STATE_TOGGLED = 1;
		final var STATE_PRESSED = 2;
		
		final var highlight = (state & (STATE_HOVERED | STATE_TOGGLED)) != 0;
		final var pressed = (state & STATE_PRESSED) != 0;
		
		Color bgColor = null;
		if (pressed) {
			bgColor = theme.getSelectionPrimary().toColor();
		}
		else if (highlight) {
			bgColor = theme.getBackgroundNormal().toColor();
		}
		else if (backgroundColor == null) {
			bgColor = theme.getBorderInner().toColor();
		}
		else {
			bgColor = backgroundColor;
		}
		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, width, height);
		
		if (enabled) {
			if (pressed) {
				g2d.setColor(RgbaColor.YELLOW.toAwtColor());
			}
			else if (highlight) {
				g2d.setColor(theme.getSelectionSecondary().toColor());
			}
			else {
				g2d.setColor(Color.BLACK);
			}
			final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 16, scaleFactor);
			G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), (int) (16 * scaleFactor), "X");
		}
	}
}
