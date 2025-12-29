package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.spatial2.model.CoordI2;

@Conditional(G2DRendererCondition.class)
@Component
public class SteampunkPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var toothHeight = Math.max(2, s);
		final var toothWidth = Math.max(3, s * 2);
		final var rivetSize = Math.max(4, s * 2);
		
		g2d.setColor(color);
		
		// Draw main border lines
		g2d.fillRect(x, y, width, s); // top
		g2d.fillRect(x, y + height - s, width, s); // bottom
		g2d.fillRect(x, y, s, height); // left
		g2d.fillRect(x + width - s, y, s, height); // right
		
		// Add gear teeth on top and bottom
		for (int xx = x + s; xx < x + width - s; xx += toothWidth * 2) {
			final int tw = Math.min(toothWidth, x + width - s - xx);
			// Top teeth pointing up
			g2d.fillPolygon(new int[]{xx, xx + tw/2, xx + tw}, new int[]{y, y - toothHeight, y}, 3);
			// Bottom teeth pointing down
			g2d.fillPolygon(new int[]{xx, xx + tw/2, xx + tw}, new int[]{y + height, y + height + toothHeight, y + height}, 3);
		}
		
		// Add gear teeth on left and right
		for (int yy = y + s; yy < y + height - s; yy += toothWidth * 2) {
			final int th = Math.min(toothWidth, y + height - s - yy);
			// Left teeth pointing left
			g2d.fillPolygon(new int[]{x, x - toothHeight, x}, new int[]{yy, yy + th/2, yy + th}, 3);
			// Right teeth pointing right
			g2d.fillPolygon(new int[]{x + width, x + width + toothHeight, x + width}, new int[]{yy, yy + th/2, yy + th}, 3);
		}
		
		// Draw rivets at corners and along borders
		final int rivetSpacing = Math.max(20, s * 10);
		// Corners
		g2d.fillOval(x + s - rivetSize / 2, y + s - rivetSize / 2, rivetSize, rivetSize);
		g2d.fillOval(x + width - s - rivetSize / 2, y + s - rivetSize / 2, rivetSize, rivetSize);
		g2d.fillOval(x + s - rivetSize / 2, y + height - s - rivetSize / 2, rivetSize, rivetSize);
		g2d.fillOval(x + width - s - rivetSize / 2, y + height - s - rivetSize / 2, rivetSize, rivetSize);
		
		// Along top and bottom
		for (int xx = x + rivetSpacing; xx < x + width - rivetSpacing; xx += rivetSpacing) {
			g2d.fillOval(xx - rivetSize / 2, y + s - rivetSize / 2, rivetSize, rivetSize);
			g2d.fillOval(xx - rivetSize / 2, y + height - s - rivetSize / 2, rivetSize, rivetSize);
		}
		
		// Along left and right
		for (int yy = y + rivetSpacing; yy < y + height - rivetSpacing; yy += rivetSpacing) {
			g2d.fillOval(x + s - rivetSize / 2, yy - rivetSize / 2, rivetSize, rivetSize);
			g2d.fillOval(x + width - s - rivetSize / 2, yy - rivetSize / 2, rivetSize, rivetSize);
		}
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
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 190);
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
		g2d.setColor(new Color(210, 180, 140, 40)); // light brown for steam
		final int cellSize = 100;
		final int offX = Math.floorMod(offset.getX(), cellSize);
		final int offY = Math.floorMod(offset.getY(), cellSize);
		
		for (int x = -cellSize; x < viewPort.getX() + cellSize; x += cellSize) {
			for (int y = -cellSize; y < viewPort.getY() + cellSize; y += cellSize) {
				final int cx = x + offX + cellSize / 2;
				final int cy = y + offY + cellSize / 2;
				final int radius = cellSize / 6;
				
				// Draw gear with more teeth
				g2d.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
				for (int i = 0; i < 12; i++) {
					final double angle = i * Math.PI / 6;
					final int tx = cx + (int) (radius * 1.3 * Math.cos(angle));
					final int ty = cy + (int) (radius * 1.3 * Math.sin(angle));
					g2d.drawLine(cx, cy, tx, ty);
				}
				
				// Draw connecting pipes
				if (x > -cellSize) {
					g2d.drawLine(cx - cellSize, cy, cx, cy); // horizontal pipe
				}
				if (y > -cellSize) {
					g2d.drawLine(cx, cy - cellSize, cx, cy); // vertical pipe
				}
				
				// Draw steam puffs (small circles)
				g2d.setColor(new Color(255, 255, 255, 20));
				for (int i = 0; i < 3; i++) {
					final int sx = cx + (int) ((Math.random() - 0.5) * radius * 2);
					final int sy = cy - radius - (int) (Math.random() * radius);
					g2d.fillOval(sx - 2, sy - 2, 4, 4);
				}
				g2d.setColor(new Color(210, 180, 140, 40));
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
		
		// Add rivets on the button
		g2d.setColor(theme.getBorderOuter().toColor());
		final int rivetSize = Math.max(3, (int) (borderStrength * 1.5));
		// Corner rivets
		g2d.fillOval(x + borderStrength - rivetSize / 2, y + borderStrength - rivetSize / 2, rivetSize, rivetSize);
		g2d.fillOval(x + width - borderStrength - rivetSize / 2, y + borderStrength - rivetSize / 2, rivetSize, rivetSize);
		g2d.fillOval(x + borderStrength - rivetSize / 2, y + height - borderStrength - rivetSize / 2, rivetSize, rivetSize);
		g2d.fillOval(x + width - borderStrength - rivetSize / 2, y + height - borderStrength - rivetSize / 2, rivetSize, rivetSize);
		
		// Center rivets if button is large enough
		if (width > 50 && height > 50) {
			g2d.fillOval(x + width / 2 - rivetSize / 2, y + borderStrength - rivetSize / 2, rivetSize, rivetSize);
			g2d.fillOval(x + width / 2 - rivetSize / 2, y + height - borderStrength - rivetSize / 2, rivetSize, rivetSize);
			g2d.fillOval(x + borderStrength - rivetSize / 2, y + height / 2 - rivetSize / 2, rivetSize, rivetSize);
			g2d.fillOval(x + width - borderStrength - rivetSize / 2, y + height / 2 - rivetSize / 2, rivetSize, rivetSize);
		}
	}
}