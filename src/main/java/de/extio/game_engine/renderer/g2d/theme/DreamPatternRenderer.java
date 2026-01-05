package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.spatial2.model.CoordI2;

public class DreamPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var arc = Math.max(6, s * 6);
		
		g2d.setColor(color);
		g2d.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
		
		final var dot = Math.max(2, s * 2);
		g2d.fillOval(x + s, y + s, dot, dot);
		g2d.fillOval(x + width - s - dot, y + s, dot, dot);
		g2d.fillOval(x + s, y + height - s - dot, dot, dot);
		g2d.fillOval(x + width - s - dot, y + height - s - dot, dot, dot);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		final var s = Math.max(1, strength);
		final var arc = Math.max(6, s * 6);
		
		g2d.setColor(backgroundColor);
		g2d.fillRoundRect(x - s, y - s, Math.max(0, width + s * 2), Math.max(0, height + s * 2), arc, arc);
		
		this.drawDecorativeBorder(g2d, x, y, width, height, s, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var s = Math.max((thickBorder ? 4 : 2), (int) ((thickBorder ? 6 : 3) * scaleFactor));
		final var s2 = s * 2;
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 160);
		this.drawDecorativeBorderFilled(g2d, x + s, y + s, width - s2, height - s2, s, innerBorderColor, bg);
		this.drawDecorativeBorder(g2d, x, y, width, height, s, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var STATE_HOVERED = 4;
		final var STATE_TOGGLED = 1;
		final var STATE_PRESSED = 2;
		
		final var highlight = (state & (STATE_HOVERED | STATE_TOGGLED)) != 0;
		final var pressed = (state & STATE_PRESSED) != 0;
		
		Color bg = backgroundColor;
		if (bg == null) {
			bg = theme.getWindowBackground().toColor();
		}
		if (highlight) {
			bg = theme.getSelectionSecondary().toColor();
		}
		if (pressed) {
			bg = theme.getSelectionPrimary().toColor();
		}
		
		final var bg2 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 210);
		g2d.setColor(bg2);
		g2d.fillRoundRect(0, 0, width, height, Math.max(6, (int) (10 * scaleFactor)), Math.max(6, (int) (10 * scaleFactor)));
		
		final var s = Math.max(1, (int) (2 * scaleFactor));
		this.drawDecorativeBorder(g2d, 0, 0, width, height, s, theme.getBorderOuter().toColor());
		
		if (!enabled) {
			return;
		}
		
		final Color fg;
		if (pressed) {
			fg = theme.getTextNormal().toColor();
		}
		else if (highlight) {
			fg = Color.BLACK;
		}
		else {
			fg = theme.getTextDisabled().toColor();
		}
		g2d.setColor(fg);
		
		final var fontSize = (int) (15 * scaleFactor);
		final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 15, scaleFactor);
		G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), fontSize, "X");
	}
	
	private java.awt.image.BufferedImage patternCache;
	
	private java.awt.image.BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int cellSize = 100;
			this.patternCache = new BufferedImage(cellSize, cellSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			
			// Clear background (transparent)
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, cellSize, cellSize);
			
			// Draw pattern
			g2d.setColor(new Color(255, 255, 255, 35));
			g2d.fillOval(20, 20, 40, 40);
			g2d.fillOval(70, 80, 15, 15);
			
			g2d.dispose();
		}
		return this.patternCache;
	}
	
	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		final var cache = this.getPatternCache();
		final int cellSize = cache.getWidth();
		
		final int offX = Math.floorMod(offset.getX(), cellSize);
		final int offY = Math.floorMod(offset.getY(), cellSize);
		
		for (int x = -cellSize; x < viewPort.getX() + cellSize; x += cellSize) {
			for (int y = -cellSize; y < viewPort.getY() + cellSize; y += cellSize) {
				g2d.drawImage(cache, x + offX, y + offY, null);
			}
		}
	}
	
	@Override
	public void drawButton(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final int STATE_TOGGLED = 1;
		final int STATE_PRESSED = 2;
		final int STATE_HOVERED = 4;
		
		final boolean toggled = (state & STATE_TOGGLED) != 0;
		final boolean pressed = (state & STATE_PRESSED) != 0;
		final boolean hovered = (state & STATE_HOVERED) != 0;
		
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
		final var bgColor2 = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 210);
		
		final var arc = Math.max(6, (int) (10 * scaleFactor));
		g2d.setColor(bgColor2);
		g2d.fillRoundRect(x, y, width, height, arc, arc);
		
		// Add vertical gradient overlay for depth
		if (height > 20) {
			for (int i = 0; i < height / 3; i++) {
				g2d.setColor(new Color(255, 255, 255, (int) (20 - (i * 20.0 / (height / 3)))));
				g2d.fillRect(x + arc / 2, y + i, width - arc, 1);
			}
		}
		
		if (toggled && width > 35 && height > 35) {
			g2d.setColor(new Color(255, 255, 255, 80));
			g2d.fillOval(x + width / 4, y + height / 4, 5, 5);
			g2d.fillOval(x + width * 2 / 3, y + height / 3, 3, 3);
			g2d.fillOval(x + width / 3, y + height * 2 / 3, 4, 4);
		}
		
		int shimmerAlpha = hovered ? 30 : 18;
		if (pressed) {
			shimmerAlpha = 12;
		}
		if (!enabled) {
			shimmerAlpha = 8;
		}
		if (shimmerAlpha > 0 && width > 8 && height > 8) {
			g2d.setColor(new Color(255, 255, 255, shimmerAlpha));
			final int shimmerHeight = hovered ? height / 2 : Math.max(3, height / 3);
			g2d.fillRoundRect(x + 2, y + 2, width - 4, shimmerHeight, arc, arc);
		}
		final var borderStrength = Math.max(1, (int) (2 * scaleFactor));
		final var borderColor = hovered ? theme.getSelectionPrimary().toColor() : enabled ? theme.getBorderOuter().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, borderColor);
	}
}
