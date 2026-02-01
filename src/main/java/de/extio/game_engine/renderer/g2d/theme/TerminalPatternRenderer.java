package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.spatial2.model.CoordI2;

public class TerminalPatternRenderer implements PatternRenderer {
	
	private BufferedImage patternCache;
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var glyph = Math.max(1, s);
		
		g2d.setColor(color);
		final var step = glyph * 4;
		final var seg = glyph * 2;
		
		for (int gx = x; gx <= x + width - seg; gx += step) {
			g2d.drawString("══", gx, y + glyph);
			g2d.drawString("═", gx + glyph, y + s + glyph);
			g2d.drawString("══", gx, y + height);
			g2d.drawString("═", gx + glyph, y + height - s);
		}
		
		for (int gy = y; gy <= y + height - seg; gy += step) {
			g2d.drawString("│", x, gy + glyph);
			g2d.drawString("│", x, gy + glyph + glyph);
			g2d.drawString("│", x + s, gy + glyph + glyph);
			g2d.drawString("│", x + width - glyph, gy + glyph);
			g2d.drawString("│", x + width - glyph, gy + glyph + glyph);
			g2d.drawString("│", x + width - s - glyph, gy + glyph + glyph);
		}
		
		final var innerLeft = x + s + glyph * 3;
		final var innerRight = x + width - s - glyph * 3;
		final var innerTop = y + s + glyph * 2;
		final var innerBottom = y + height - s - glyph * 2;
		
		for (int gx = innerLeft; gx <= innerRight; gx += glyph * 6) {
			g2d.drawString("══", gx, y + s + glyph);
			g2d.drawString("═", gx + glyph * 3, y + s + glyph);
			g2d.drawString("══", gx, y + height - s);
			g2d.drawString("═", gx + glyph * 3, y + height - s);
		}
		
		for (int gy = innerTop; gy <= innerBottom; gy += glyph * 6) {
			g2d.drawString("│", x + s, gy + glyph);
			g2d.drawString("│", x + s, gy + glyph + glyph);
			g2d.drawString("│", x + s, gy + glyph * 3 + glyph);
			g2d.drawString("│", x + width - s - glyph, gy + glyph);
			g2d.drawString("│", x + width - s - glyph, gy + glyph + glyph);
			g2d.drawString("│", x + width - s - glyph, gy + glyph * 3 + glyph);
		}
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
		final var s = Math.max((thickBorder ? 2 : 1), (int) ((thickBorder ? 3 : 2) * scaleFactor));
		final var s2 = s * 2;
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 230);
		this.drawDecorativeBorderFilled(g2d, x + s, y + s, width - s2, height - s2, s, innerBorderColor, bg);
		this.drawDecorativeBorder(g2d, x, y, width, height, s, outerBorderColor);
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final boolean pressed, final boolean hovered, final boolean toggled, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		
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
		final Color bottomRight = raised ? light : dark;
		final Color topLeft = raised ? dark : light;
		
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
	
	private BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int spacing = 56;
			this.patternCache = new BufferedImage(spacing, spacing, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, spacing, spacing);
			
			g2d.setColor(new Color(255, 225, 170, 14));
			for (int y = 0; y < spacing; y += 7) {
				g2d.fillRect(0, y, spacing, 1);
			}
			
			final var soft = new Color(255, 210, 140, 26);
			final var strong = new Color(255, 205, 120, 36);
			
			final int cx = spacing / 2;
			final int cy = spacing / 2;
			
			g2d.setColor(soft);
			g2d.fillRect(cx - 10, cy - 10, 2, 2);
			g2d.fillRect(cx + 8, cy - 10, 2, 2);
			g2d.fillRect(cx - 10, cy + 8, 2, 2);
			g2d.fillRect(cx + 8, cy + 8, 2, 2);
			
			g2d.setColor(strong);
			g2d.fillRect(cx - 3, cy - 6, 1, 12);
			g2d.fillRect(cx - 6, cy - 3, 12, 1);
			
			g2d.setColor(soft);
			g2d.fillRect(cx + 14, cy + 14, 3, 1);
			g2d.fillRect(cx + 16, cy + 12, 1, 3);
			
			g2d.fillRect(cx - 18, cy + 6, 6, 1);
			g2d.fillRect(cx - 18, cy + 6, 1, 4);
			g2d.fillRect(cx - 13, cy + 6, 1, 4);
			g2d.fillRect(cx - 16, cy + 9, 3, 1);
			
			g2d.fillRect(cx + 2, cy - 16, 6, 1);
			g2d.fillRect(cx + 7, cy - 16, 1, 4);
			g2d.dispose();
		}
		return this.patternCache;
	}
	
	@Override
	public void drawBackgroundPattern(final Graphics2D g2d, final CoordI2 offset, final CoordI2 viewPort) {
		final var cache = this.getPatternCache();
		final int spacing = cache.getWidth();
		
		final int offX = Math.floorMod(offset.getX(), spacing);
		final int offY = Math.floorMod(offset.getY(), spacing);
		
		for (int x = -spacing; x < viewPort.getX() + spacing; x += spacing) {
			for (int y = -spacing; y < viewPort.getY() + spacing; y += spacing) {
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
		final var bgColor = Color.getHSBColor(h, s, b);
		g2d.setColor(bgColor);
		g2d.fillRect(x, y, width, height);
		
		int bevelStrength = (int) (2 * scaleFactor);
		if (bevelStrength < 1)
			bevelStrength = 1;
		
		final Color highlight = hovered ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
		final Color shadow = enabled ? 
				hovered ? theme.getSelectionSecondary().toColor() : theme.getBorderInner().toColor() : 
				theme.getBorderInnerDisabled().toColor();
		
		drawBevel(g2d, x, y, width, height, bevelStrength, !pressed, highlight, shadow);
	}
}
