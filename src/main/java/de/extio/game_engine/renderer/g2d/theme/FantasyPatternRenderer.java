package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


import de.extio.game_engine.spatial2.model.CoordI2;

public class FantasyPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var s2 = s * 2;
		final var s3 = s * 3;
		
		g2d.setColor(color);
		
		g2d.fillRect(x, y, width, s);
		g2d.fillRect(x, y + height - s, width, s);
		g2d.fillRect(x, y, s, height);
		g2d.fillRect(x + width - s, y, s, height);
		
		g2d.fillRect(x + s2, y + s2, width - s3 * 2, s);
		g2d.fillRect(x + s2, y + height - s2 - s, width - s3 * 2, s);
		g2d.fillRect(x + s2, y + s2, s, height - s3 * 2);
		g2d.fillRect(x + width - s2 - s, y + s2, s, height - s3 * 2);
		
		final var tri = Math.max(6, s * 4);
		g2d.fillPolygon(new int[] { x + s, x + tri, x + s }, new int[] { y + s, y + s, y + tri }, 3);
		g2d.fillPolygon(new int[] { x + width - s, x + width - tri, x + width - s }, new int[] { y + s, y + s, y + tri }, 3);
		g2d.fillPolygon(new int[] { x + s, x + tri, x + s }, new int[] { y + height - s, y + height - s, y + height - tri }, 3);
		g2d.fillPolygon(new int[] { x + width - s, x + width - tri, x + width - s }, new int[] { y + height - s, y + height - s, y + height - tri }, 3);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		final var s = Math.max(1, strength);
		g2d.setColor(backgroundColor);
		g2d.fillRect(x + s, y + s, Math.max(0, width - s * 2), Math.max(0, height - s * 2));
		this.drawDecorativeBorder(g2d, x, y, width, height, s, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		// final var s = Math.max((thickBorder ? 3 : 2), (int) ((thickBorder ? 5 : 3) * scaleFactor));
		final var s = Math.max(2, (int) (3 * scaleFactor));
		final var s2 = s * 2;
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 200);
		this.drawDecorativeBorderFilled(g2d, x + s, y + s, width - s2, height - s2, s, innerBorderColor, bg);
		this.drawDecorativeBorder(g2d, x, y, width, height, s, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var highlight = (hovered || toggled);
		
		Color bg = backgroundColor;
		if (bg == null) {
			bg = theme.getBorderInner().toColor();
		}
		if (highlight) {
			bg = theme.getSelectionSecondary().toColor();
		}
		if (pressed) {
			bg = theme.getSelectionPrimary().toColor();
		}
		
		g2d.setColor(bg);
		g2d.fillRect(0, 0, width, height);
		
		final var s = Math.max(1, (int) (2 * scaleFactor));
		this.drawDecorativeBorder(g2d, 0, 0, width, height, s, theme.getBorderOuter().toColor());
		
		if (!enabled) {
			return;
		}
		
		final Color fg;
		if (pressed) {
			fg = theme.getSelectionSecondary().toColor();
		}
		else if (highlight) {
			fg = theme.getTextNormal().toColor();
		}
		else {
			fg = theme.getTextNormal().toColor();
		}
		g2d.setColor(fg);
		
		final var crossSize = Math.min(width, height) / 2;
		final var centerX = width / 2;
		final var centerY = height / 2;
		final var t = Math.max(1, (int) (2 * scaleFactor));
		g2d.fillRect(centerX - crossSize / 2, centerY - t / 2, crossSize, t);
		g2d.fillRect(centerX - t / 2, centerY - crossSize / 2, t, crossSize);
	}

	private java.awt.image.BufferedImage patternCache;

	private java.awt.image.BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int cellSize = 80;
			this.patternCache = new BufferedImage(cellSize, cellSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, cellSize, cellSize);
			
			g2d.setColor(new Color(255, 255, 255, 35));
			g2d.fillRect(10, 10, 2, 2);
			g2d.fillRect(50, 60, 3, 3);
			g2d.fillRect(30, 50, 1, 1);
			
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
			bgColor = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.18f), Math.min(1.0f, hsb[2] * 0.55f));
		}
		
		final var bgHsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
		final float topB = pressed ? Math.max(0.0f, bgHsb[2] - 0.12f) : Math.min(1.0f, bgHsb[2] + 0.14f);
		final float bottomB = pressed ? Math.min(1.0f, bgHsb[2] + 0.05f) : Math.max(0.0f, bgHsb[2] - 0.11f);
		final var top = Color.getHSBColor(bgHsb[0], Math.min(1.0f, bgHsb[1] * 0.95f), topB);
		final var bottom = Color.getHSBColor(bgHsb[0], bgHsb[1], bottomB);
		
		final var oldPaint = g2d.getPaint();
		g2d.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
		g2d.fillRect(x, y, width, height);
		g2d.setPaint(oldPaint);
		
		final var t = Math.max(1, (int) (1 * scaleFactor));
		final var shine = new Color(255, 255, 255, pressed ? 16 : 44);
		g2d.setColor(shine);
		g2d.fillRect(x + t, y + t, Math.max(0, width - t * 2), Math.max(0, t));
		
		final var sparkleBase = hovered || toggled ? theme.getSelectionSecondary().toColor() : theme.getBorderOuter().toColor();
		final var sparkle = new Color(sparkleBase.getRed(), sparkleBase.getGreen(), sparkleBase.getBlue(), pressed ? 45 : 70);
		g2d.setColor(sparkle);
		final int px1 = x + (width / 3);
		final int py1 = y + (height / 3);
		final int px2 = x + (width * 2 / 3);
		final int py2 = y + (height * 2 / 3);
		final int dot = Math.max(1, (int) (2 * scaleFactor));
		if (width >= 16 && height >= 12) {
			g2d.fillRect(px1, py1, dot, dot);
			g2d.fillRect(px2, py1 + dot, dot, dot);
			g2d.fillRect(px1 + dot, py2, dot, dot);
		}
		
		final var borderStrength = Math.max(1, (int) (2 * scaleFactor));
		final var outerBorderColor = hovered ? theme.getSelectionPrimary().toColor() : (toggled ? theme.getSelectionSecondary().toColor() : theme.getBorderOuter().toColor());
		final var innerBorderColor = enabled ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, outerBorderColor);
		final var inset = Math.max(1, borderStrength);
		if (width > inset * 4 && height > inset * 4) {
			this.drawDecorativeBorder(g2d, x + inset, y + inset, width - inset * 2, height - inset * 2, Math.max(1, borderStrength / 2), innerBorderColor);
		}
		
		if (hovered || toggled) {
			final var gemBase = theme.getSelectionPrimary().toColor();
			final var gem = new Color(gemBase.getRed(), gemBase.getGreen(), gemBase.getBlue(), pressed ? 70 : 110);
			g2d.setColor(gem);
			final int cx = x + width / 2;
			final int cy = y + Math.max(6, (int) (8 * scaleFactor));
			final int r = Math.max(3, (int) (4 * scaleFactor));
			g2d.fillPolygon(new int[] { cx, cx + r, cx, cx - r }, new int[] { cy - r, cy, cy + r, cy }, 4);
		}
	}
}
