package de.extio.game_engine.renderer.g2d.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;

/**
 * Implementation of PatternRenderer that draws a technical "Blueprint" style with grids and measurement markers.
 */
@Conditional(G2DRendererCondition.class)
@Component
public class BlueprintPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		g2d.setColor(color);
		
		// Main thin border line
		final Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.drawRect(x, y, width - 1, height - 1);
		
		// Corner accents (thicker)
		final int cornerLen = Math.min(Math.min(width, height) / 3, 20);
		final int thick = Math.max(2, strength);
		
		g2d.fillRect(x, y, cornerLen, thick); // Top-Left Horz
		g2d.fillRect(x, y, thick, cornerLen); // Top-Left Vert
		
		g2d.fillRect(x + width - cornerLen, y, cornerLen, thick); // Top-Right Horz
		g2d.fillRect(x + width - thick, y, thick, cornerLen); // Top-Right Vert
		
		g2d.fillRect(x, y + height - thick, cornerLen, thick); // Bottom-Left Horz
		g2d.fillRect(x, y + height - cornerLen, thick, cornerLen); // Bottom-Left Vert
		
		g2d.fillRect(x + width - cornerLen, y + height - thick, cornerLen, thick); // Bottom-Right Horz
		g2d.fillRect(x + width - thick, y + height - cornerLen, thick, cornerLen); // Bottom-Right Vert
		
		// Mid-point markers
		final int midX = x + width / 2;
		final int midY = y + height / 2;
		final int markerLen = 6;
		
		g2d.fillRect(midX - markerLen/2, y, markerLen, thick); // Top Mid
		g2d.fillRect(midX - markerLen/2, y + height - thick, markerLen, thick); // Bottom Mid
		g2d.fillRect(x, midY - markerLen/2, thick, markerLen); // Left Mid
		g2d.fillRect(x + width - thick, midY - markerLen/2, thick, markerLen); // Right Mid
		
		g2d.setStroke(oldStroke);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		// Fill background
		g2d.setColor(backgroundColor);
		g2d.fillRect(x, y, width, height);
		
		// Draw border
		this.drawDecorativeBorder(g2d, x, y, width, height, strength, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		// Background
		g2d.setColor(backgroundColor);
		g2d.fillRect(x, y, width, height);
		
		// Grid
		final Color gridColor = new Color(255, 255, 255, 20); // Faint white grid
		g2d.setColor(gridColor);
		final int gridSize = (int)(20 * scaleFactor);
		if (gridSize > 2) {
			for (int gx = x; gx < x + width; gx += gridSize) {
				g2d.drawLine(gx, y, gx, y + height);
			}
			for (int gy = y; gy < y + height; gy += gridSize) {
				g2d.drawLine(x, gy, x + width, gy);
			}
		}
		
		// Border
		final int strength = Math.max((thickBorder ? 3 : 2), (int) ((thickBorder ? 4 : 2) * scaleFactor));
		drawDecorativeBorder(g2d, x, y, width, height, strength, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var STATE_PRESSED = 2;
		final var pressed = (state & STATE_PRESSED) != 0;
		
		// Button background (transparent or slightly lighter blue)
		Color btnBg = backgroundColor;
		if (btnBg == null) {
			btnBg = pressed ? theme.getSelectionPrimary().toColor() : theme.getBackgroundNormal().toColor().brighter();
		}
		g2d.setColor(btnBg);
		g2d.fillRect(0, 0, width, height);
		
		// Border
		g2d.setColor(theme.getBorderOuter().toColor());
		g2d.drawRect(0, 0, width - 1, height - 1);
		
		// X icon
		if (enabled) {
			g2d.setColor(theme.getTextNormal().toColor());
			
			// Draw a technical X (crosshair style?)
			// Or just a clean X
			final int padding = (int)(4 * scaleFactor);
			final int x1 = padding;
			final int y1 = padding;
			final int x2 = width - padding;
			final int y2 = height - padding;
			
			final Graphics2D g2 = (Graphics2D) g2d.create();
			g2.setStroke(new BasicStroke((float)(2 * scaleFactor)));
			g2.drawLine(x1, y1, x2, y2);
			g2.drawLine(x1, y2, x2, y1);
			g2.dispose();
		}
	}
}
