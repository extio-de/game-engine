package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Implementation of PatternRenderer that draws borders and panels in a classic "Bevel" style.
 */
@Conditional(G2DRendererCondition.class)
@Component
public class BevelPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		// Use the provided color as the base.
		// For a simple border, we can try to make a bevel out of it.
		// Let's assume 'color' is the shadow, and we need a highlight.
		
		final var hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		Color light, dark;
		if (hsb[2] < 0.5f) {
			light = color.brighter();
			dark = color;
		}
		else {
			light = color;
			dark = color.darker();
		}
		
		// Outer bevel (raised)
		drawBevel(g2d, x, y, width, height, strength + 1, true, light, dark);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		// Fill background
		g2d.setColor(backgroundColor);
		g2d.fillRect(x + strength, y + strength, width - strength * 2, height - strength * 2);
		
		// Draw border
		this.drawDecorativeBorder(g2d, x, y, width, height, strength, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var strength = Math.max((thickBorder ? 3 : 2), (int) ((thickBorder ? 4 : 2) * scaleFactor));
		
		// Main background
		g2d.setColor(backgroundColor);
		g2d.fillRect(x, y, width, height);
		
		// Raised window border
		// Use outerBorderColor as highlight, innerBorderColor as shadow
		drawBevel(g2d, x, y, width, height, strength, true, outerBorderColor, innerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var STATE_PRESSED = 2;
		final var pressed = (state & STATE_PRESSED) != 0;
		
		// Button background
		final Color btnBg = backgroundColor != null ? backgroundColor : theme.getBackgroundNormal().toColor();
		g2d.setColor(btnBg);
		g2d.fillRect(0, 0, width, height);
		
		// Button bevel
		int bevelStrength = (int) (2 * scaleFactor);
		if (bevelStrength < 1)
			bevelStrength = 1;
		
		final Color highlight = theme.getBorderOuter().toColor();
		final Color shadow = theme.getBorderInner().toColor();
		
		if (pressed) {
			drawBevel(g2d, 0, 0, width, height, bevelStrength, false, highlight, shadow);
		}
		else {
			drawBevel(g2d, 0, 0, width, height, bevelStrength, true, highlight, shadow);
		}
		
		// X icon
		if (enabled) {
			g2d.setColor(theme.getTextNormal().toColor());
			final int offset = pressed ? 1 : 0;
			
			// Draw a simple X
			final int padding = (int) (4 * scaleFactor);
			final int x1 = padding + offset;
			final int y1 = padding + offset;
			final int x2 = width - padding + offset;
			final int y2 = height - padding + offset;
			
			final Graphics2D g2 = (Graphics2D) g2d.create();
			g2.setStroke(new java.awt.BasicStroke((float) (2 * scaleFactor)));
			g2.drawLine(x1, y1, x2, y2);
			g2.drawLine(x1, y2, x2, y1);
			g2.dispose();
		}
	}
	
	private void drawBevel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final boolean raised, final Color light, final Color dark) {
		final Color topLeft = raised ? light : dark;
		final Color bottomRight = raised ? dark : light;
		
		g2d.setColor(topLeft);
		for (int i = 0; i < strength; i++) {
			g2d.drawLine(x + i, y + i, x + width - 1 - i, y + i); // Top
			g2d.drawLine(x + i, y + i, x + i, y + height - 1 - i); // Left
		}
		
		g2d.setColor(bottomRight);
		for (int i = 0; i < strength; i++) {
			g2d.drawLine(x + i, y + height - 1 - i, x + width - 1 - i, y + height - 1 - i); // Bottom
			g2d.drawLine(x + width - 1 - i, y + i, x + width - 1 - i, y + height - 1 - i); // Right
		}
	}
	
	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		g2d.setColor(new Color(255, 255, 255, 35));
		final int spacing = 20;
		final int offX = Math.floorMod(offset.getX(), spacing);
		final int offY = Math.floorMod(offset.getY(), spacing);
		
		for (int x = -spacing; x < viewPort.getX() + spacing; x += spacing) {
			for (int y = -spacing; y < viewPort.getY() + spacing; y += spacing) {
				g2d.fillRect(x + offX, y + offY, 2, 2);
			}
		}
	}
}
