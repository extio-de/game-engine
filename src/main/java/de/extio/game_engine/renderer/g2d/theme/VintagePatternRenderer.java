package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.spatial2.model.CoordI2;

public class VintagePatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		g2d.setColor(color);
		
		final var size = strength;
		
		g2d.fillRect(x, y, width, size);
		g2d.fillRect(x, y, size, height - size * 2);
		g2d.fillRect(x + width - size, y, size, height - size * 2);
		g2d.fillRect(x + size * 2, y + height - size, width - size * 4, size);
		
		g2d.fillPolygon(
			new int[] { x, x + size * 2, x + size * 2, x + size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
		g2d.fillPolygon(
			new int[] { x + width, x + width - size * 2, x + width - size * 2, x + width - size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		g2d.setColor(backgroundColor);
		g2d.fillRect(x, y + strength, width, height - strength * 3);
		g2d.fillRect(x + strength, y + height - strength * 2, width - strength * 2, strength);
		
		this.drawDecorativeBorder(g2d, x, y, width, height, strength, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var size = Math.max(3, (int) (5 * scaleFactor));
		
		final var baseColor = new float[3];
		innerBorderColor.getRGBColorComponents(baseColor);
		Color.RGBtoHSB((int) (baseColor[0] * 255), (int) (baseColor[1] * 255), (int) (baseColor[2] * 255), baseColor);
		
		final var backgroundTransparency = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 220);
		g2d.setColor(backgroundTransparency);
		g2d.fillRect(x, y + size, width, height - size * 3);
		g2d.fillRect(x + size, y + height - size * 2, width - size * 2, size);
		
		g2d.setColor(Color.getHSBColor(baseColor[0], baseColor[1], baseColor[2]));
		if (thickBorder) {
			g2d.fillRect(x, y + size * 2, size, height - size * 4);
			g2d.fillRect(x + width - size, y + size * 2, size, height - size * 4);
			g2d.fillRect(x, y + size * 2, width, size);
		}
		else {
			g2d.fillRect(x, y, size, height - size * 2);
			g2d.fillRect(x + width - size, y, size, height - size * 2);
			g2d.fillRect(x, y, width, size);
		}
		g2d.fillRect(x + size * 2, y + height - size, width - size * 4, size);
		g2d.fillPolygon(
			new int[] { x, x + size * 2, x + size * 2, x + size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
		g2d.fillPolygon(
			new int[] { x + width, x + width - size * 2, x + width - size * 2, x + width - size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
		
		if (thickBorder) {
			for (var i = 0; i < size * 2; i++) {
				g2d.setColor(Color.getHSBColor(baseColor[0], baseColor[1], baseColor[2] * ((0.5F / (size * 2) * i) + 0.5F)));
				g2d.drawLine(x, y + i, x + width, y + i);
			}
		}
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var highlight = (hovered || toggled);
		
		final var size = Math.max(2, (int) (3 * scaleFactor));
		
		Color baseColor = null;
		if (pressed) {
			baseColor = theme.getSelectionPrimary().toColor();
		}
		else if (highlight) {
			baseColor = theme.getSelectionSecondary().toColor();
		}
		else if (backgroundColor == null) {
			baseColor = theme.getBorderInner().toColor();
		}
		else {
			baseColor = backgroundColor;
		}
		
		final var hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
		
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		
		g2d.setColor(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
		g2d.fillRect(0, 0, width, size);
		g2d.fillRect(0, 0, size, height);
		g2d.fillRect(width - size, 0, size, height);
		g2d.fillRect(0, height - size, width, size);
		
		if (enabled) {
			final Color textColor;
			if (pressed) {
				textColor = theme.getSelectionPrimary().toColor();
			}
			else if (highlight) {
				textColor = theme.getTextNormal().toColor();
			}
			else {
				textColor = theme.getTextDisabled().toColor();
			}
			g2d.setColor(textColor);
			
			final var crossSize = Math.min(width, height) / 2;
			final var centerX = width / 2;
			final var centerY = height / 2;
			final var crossThickness = Math.max(1, (int) (2 * scaleFactor));
			
			g2d.fillRect(centerX - crossSize / 2, centerY - crossThickness / 2, crossSize, crossThickness);
			g2d.fillRect(centerX - crossThickness / 2, centerY - crossSize / 2, crossThickness, crossSize);
		}
	}

	private java.awt.image.BufferedImage patternCache;

	private java.awt.image.BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int cellSize = 150;
			this.patternCache = new BufferedImage(cellSize, cellSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, cellSize, cellSize);
			
			g2d.setColor(new Color(255, 255, 255, 35));
			g2d.drawLine(10, 10, 20, 30);
			g2d.drawLine(60, 50, 55, 70);
			g2d.fillOval(80, 20, 2, 2);
			
			g2d.dispose();
		}
		return this.patternCache;
	}

	@Override
	public void drawButton(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		float h, s, b;
		
		if (backgroundColor == null) {
			final var baseColor = !toggled ? theme.getBackgroundNormal() : theme.getBackgroundSelected();
			h = baseColor.getHue();
			s = baseColor.getSaturation();
			b = baseColor.getBrightness();
			
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		else {
			final var hsb = new float[3];
			Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), hsb);
			h = hsb[0];
			s = hsb[1];
			
			if (!toggled) {
				b = 0.30F;
			}
			else {
				b = 0.50F;
			}
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
		}
		
		b = Math.max(0.0f, Math.min(1.0f, b));
		Color bgColor = Color.getHSBColor(h, s, b);
		if (!enabled) {
			final var hsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
			bgColor = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.15f), Math.min(1.0f, hsb[2] * 0.55f));
		}
		
		final var bgHsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
		final float topB = pressed ? Math.max(0.0f, bgHsb[2] - 0.12f) : Math.min(1.0f, bgHsb[2] + 0.12f);
		final float bottomB = pressed ? Math.min(1.0f, bgHsb[2] + 0.04f) : Math.max(0.0f, bgHsb[2] - 0.12f);
		final var top = Color.getHSBColor(bgHsb[0], bgHsb[1], topB);
		final var bottom = Color.getHSBColor(bgHsb[0], bgHsb[1], bottomB);
		
		final var oldPaint = g2d.getPaint();
		g2d.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
		g2d.fillRect(x, y, width, height);
		g2d.setPaint(oldPaint);
		
		final var t = Math.max(1, (int) (1 * scaleFactor));
		final var shine = new Color(255, 255, 255, pressed ? 14 : 40);
		final var baseShadow = new Color(0, 0, 0, pressed ? 50 : 70);
		g2d.setColor(shine);
		g2d.fillRect(x + t, y + t, Math.max(0, width - t * 2), Math.max(0, t));
		g2d.setColor(baseShadow);
		g2d.fillRect(x + t, y + height - t * 2, Math.max(0, width - t * 2), Math.max(0, t));
		
		if (hovered || toggled) {
			final var glowBase = theme.getSelectionPrimary().toColor();
			final var glow = new Color(glowBase.getRed(), glowBase.getGreen(), glowBase.getBlue(), pressed ? 70 : 110);
			g2d.setColor(glow);
			g2d.fillRect(x + t, y + t, Math.max(0, width - t * 2), Math.max(0, t));
		}
		
		final var borderStrength = Math.max(1, (int) (2 * scaleFactor));
		final var outerBorderColor = hovered ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
		final var innerBorderColor = enabled ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, outerBorderColor);
		final var inset = Math.max(1, borderStrength);
		if (width > inset * 3 && height > inset * 3) {
			this.drawDecorativeBorder(g2d, x + inset, y + inset, width - inset * 2, height - inset * 2, Math.max(1, borderStrength / 2), innerBorderColor);
		}
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
}
