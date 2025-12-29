package de.extio.game_engine.renderer.g2d.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.spatial2.model.CoordI2;

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

	private java.awt.image.BufferedImage patternCache;

	private java.awt.image.BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int gridSize = 50;
			this.patternCache = new BufferedImage(gridSize, gridSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, gridSize, gridSize);
			
			g2d.setColor(new Color(255, 255, 255, 35));
			g2d.setStroke(new BasicStroke(1f));
			
			g2d.drawLine(0, 0, 0, gridSize);
			g2d.drawLine(0, 0, gridSize, 0);
			
			g2d.dispose();
		}
		return this.patternCache;
	}

	@Override
	public void drawButton(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final int STATE_TOGGLED = 1;
		final int STATE_PRESSED = 2;
		final int STATE_HOVERED = 4;
		
		float h, s, b;
		
		if (backgroundColor == null) {
			final var baseColor = (state & STATE_TOGGLED) == 0 ? theme.getBackgroundNormal() : theme.getBackgroundSelected();
			h = baseColor.getHue();
			s = baseColor.getSaturation();
			b = baseColor.getBrightness();
			
			if ((state & STATE_PRESSED) != 0) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if ((state & STATE_HOVERED) != 0) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		else {
			final var hsb = new float[3];
			Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), hsb);
			h = hsb[0];
			s = hsb[1];
			
			if ((state & STATE_TOGGLED) == 0) {
				b = 0.30F;
			}
			else {
				b = 0.50F;
			}
			if ((state & STATE_PRESSED) != 0) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if ((state & STATE_HOVERED) != 0) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		
		b = Math.max(0.0f, Math.min(1.0f, b));
		final var bgColor = Color.getHSBColor(h, s, b);
		g2d.setColor(bgColor);
		g2d.fillRect(x, y, width, height);
		
		final var borderStrength = Math.max(1, (int) (2 * scaleFactor));
		final var borderColor = (state & STATE_HOVERED) != 0 ? theme.getSelectionPrimary().toColor() : 
				enabled ? theme.getBorderOuter().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, borderColor);
	}
	
	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		final var cache = this.getPatternCache();
		final int gridSize = cache.getWidth();
		
		final int offX = Math.floorMod(offset.getX(), gridSize);
		final int offY = Math.floorMod(offset.getY(), gridSize);
		
		for (int x = -gridSize; x <= viewPort.getX() + gridSize; x += gridSize) {
			for (int y = -gridSize; y <= viewPort.getY() + gridSize; y += gridSize) {
				g2d.drawImage(cache, x + offX, y + offY, null);
			}
		}
	}

}
