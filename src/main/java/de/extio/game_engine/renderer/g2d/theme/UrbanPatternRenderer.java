package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import de.extio.game_engine.spatial2.model.CoordI2;

public class UrbanPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var seg = Math.max(s * 3, 6);
		final var gap = Math.max(s, 2);
		
		g2d.setColor(color);
		
		for (int xx = x; xx < x + width; xx += seg + gap) {
			final var w = Math.min(seg, x + width - xx);
			g2d.fillRect(xx, y, w, s);
			g2d.fillRect(xx, y + height - s, w, s);
		}
		
		for (int yy = y; yy < y + height; yy += seg + gap) {
			final var h = Math.min(seg, y + height - yy);
			g2d.fillRect(x, yy, s, h);
			g2d.fillRect(x + width - s, yy, s, h);
		}
		
		final var pin = Math.max(2, s * 2);
		g2d.fillRect(x + s, y + s, pin, pin);
		g2d.fillRect(x + width - s - pin, y + s, pin, pin);
		g2d.fillRect(x + s, y + height - s - pin, pin, pin);
		g2d.fillRect(x + width - s - pin, y + height - s - pin, pin, pin);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		final var s = Math.max(1, strength);
		g2d.setColor(backgroundColor);
		g2d.fillRect(x - s, y - s, Math.max(0, width + s * 2), Math.max(0, height + s * 2));
		this.drawDecorativeBorder(g2d, x, y, width, height, s, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var s = Math.max((thickBorder ? 4 : 2), (int) ((thickBorder ? 6 : 3) * scaleFactor));
		final var s2 = s * 2;
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 200);
		this.drawDecorativeBorderFilled(g2d, x + s, y + s, width - s2, height - s2, s, innerBorderColor, bg);
		this.drawDecorativeBorder(g2d, x, y, width, height, s, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var highlight = (hovered || toggled);
		
		final var base = (backgroundColor != null) ? backgroundColor : theme.getBorderInner().toColor();
		final var hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
		
		final Color bg;
		if (pressed) {
			bg = theme.getSelectionPrimary().toColor();
		}
		else if (highlight) {
			bg = theme.getSelectionSecondary().toColor();
		}
		else {
			bg = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.6f), Math.min(1.0f, hsb[2] * 0.35f));
		}
		
		g2d.setColor(bg);
		g2d.fillRect(0, 0, width, height);
		
		final var s = Math.max(1, (int) (2 * scaleFactor));
		this.drawDecorativeBorder(g2d, 0, 0, width, height, s, theme.getBorderOuter().toColor());
		
		if (!enabled) {
			return;
		}
		
		final var crossSize = Math.min(width, height) / 2;
		final var centerX = width / 2;
		final var centerY = height / 2;
		final var t = Math.max(1, (int) (2 * scaleFactor));
		
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
		
		g2d.fillRect(centerX - crossSize / 2, centerY - t / 2, crossSize, t);
		g2d.fillRect(centerX - t / 2, centerY - crossSize / 2, t, crossSize);
	}

	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		g2d.setColor(new Color(255, 255, 255, 35));
		final int cellSize = 60;
		final int offX = Math.floorMod(offset.getX(), cellSize);
		final int offY = Math.floorMod(offset.getY(), cellSize);
		
		for (int x = -cellSize; x < viewPort.getX() + cellSize; x += cellSize) {
			for (int y = -cellSize; y < viewPort.getY() + cellSize; y += cellSize) {
				// Vertical streaks
				g2d.fillRect(x + offX + 10, y + offY + 5, 2, 15);
				g2d.fillRect(x + offX + 40, y + offY + 35, 2, 10);
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
			bgColor = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.20f), Math.min(1.0f, hsb[2] * 0.55f));
		}
		
		final var bgHsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
		final float topB = pressed ? Math.max(0.0f, bgHsb[2] - 0.10f) : Math.min(1.0f, bgHsb[2] + 0.10f);
		final float bottomB = pressed ? Math.min(1.0f, bgHsb[2] + 0.04f) : Math.max(0.0f, bgHsb[2] - 0.12f);
		final var top = Color.getHSBColor(bgHsb[0], bgHsb[1], topB);
		final var bottom = Color.getHSBColor(bgHsb[0], bgHsb[1], bottomB);
		
		final var oldPaint = g2d.getPaint();
		g2d.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
		g2d.fillRect(x, y, width, height);
		g2d.setPaint(oldPaint);
		
		final var t = Math.max(1, (int) (1 * scaleFactor));
		final var hatchBase = theme.getBorderOuter().toColor();
		final var hatch = new Color(hatchBase.getRed(), hatchBase.getGreen(), hatchBase.getBlue(), pressed ? 28 : 38);
		g2d.setColor(hatch);
		final int step = Math.max(10, (int) (14 * scaleFactor));
		for (int i = x - height; i < x + width + height; i += step) {
			g2d.drawLine(i, y, i + height, y + height);
		}
		
		if (hovered || toggled) {
			final var accentBase = theme.getSelectionPrimary().toColor();
			final var accent = new Color(accentBase.getRed(), accentBase.getGreen(), accentBase.getBlue(), pressed ? 110 : 150);
			g2d.setColor(accent);
			final int stripe = Math.max(2, (int) (4 * scaleFactor));
			g2d.fillRect(x + t, y + t, stripe, Math.max(0, height - t * 2));
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
}
