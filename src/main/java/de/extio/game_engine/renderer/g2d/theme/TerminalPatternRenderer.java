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
		final var tick = Math.max(1, s * 2);
		final var glyph = Math.max(1, s);
		
		g2d.setColor(color);
		final var step = glyph * 4;
		final var seg = glyph * 2;
		
		for (int gx = x; gx <= x + width - seg; gx += step) {
			g2d.fillRect(gx, y, seg, s);
			g2d.fillRect(gx + glyph, y + s, glyph, s);
			g2d.fillRect(gx, y + height - s, seg, s);
			g2d.fillRect(gx + glyph, y + height - s - s, glyph, s);
		}
		
		for (int gy = y; gy <= y + height - seg; gy += step) {
			g2d.fillRect(x, gy, s, seg);
			g2d.fillRect(x + s, gy + glyph, s, glyph);
			g2d.fillRect(x + width - s, gy, s, seg);
			g2d.fillRect(x + width - s - s, gy + glyph, s, glyph);
		}
		
		g2d.fillRect(x + s, y + s, tick, s);
		g2d.fillRect(x + width - s - tick, y + s, tick, s);
		g2d.fillRect(x + s, y + height - s - s, tick, s);
		g2d.fillRect(x + width - s - tick, y + height - s - s, tick, s);
		
		final var innerLeft = x + s + glyph * 3;
		final var innerRight = x + width - s - glyph * 3;
		final var innerTop = y + s + glyph * 2;
		final var innerBottom = y + height - s - glyph * 2;
		
		for (int gx = innerLeft; gx <= innerRight; gx += glyph * 6) {
			g2d.fillRect(gx, y + s, glyph * 2, glyph);
			g2d.fillRect(gx + glyph * 3, y + s, glyph, glyph);
			g2d.fillRect(gx, y + height - s - glyph, glyph * 2, glyph);
			g2d.fillRect(gx + glyph * 3, y + height - s - glyph, glyph, glyph);
		}
		
		for (int gy = innerTop; gy <= innerBottom; gy += glyph * 6) {
			g2d.fillRect(x + s, gy, glyph, glyph * 2);
			g2d.fillRect(x + s, gy + glyph * 3, glyph, glyph);
			g2d.fillRect(x + width - s - glyph, gy, glyph, glyph * 2);
			g2d.fillRect(x + width - s - glyph, gy + glyph * 3, glyph, glyph);
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
		final var highlight = (hovered || toggled);
		
		Color bgColor = backgroundColor;
		if (bgColor == null) {
			bgColor = theme.getBorderInner().toColor();
		}
		if (highlight) {
			bgColor = theme.getBackgroundNormal().toColor();
		}
		if (pressed) {
			bgColor = theme.getSelectionPrimary().toColor();
		}
		
		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, width, height);
		
		final var s = Math.max(1, (int) (1 * scaleFactor));
		this.drawDecorativeBorder(g2d, 0, 0, width, height, s, theme.getBorderOuter().toColor());
		
		if (!enabled) {
			return;
		}
		
		final Color textColor;
		if (pressed) {
			textColor = theme.getTextNormal().toColor();
		}
		else if (highlight) {
			textColor = theme.getSelectionSecondary().toColor();
		}
		else {
			textColor = theme.getTextDisabled().toColor();
		}
		g2d.setColor(textColor);
		
		final var fontSize = (int) (12 * scaleFactor);
		final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 12, scaleFactor);
		G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), fontSize, "X");
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
		Color bgColor;
		if (backgroundColor == null) {
			final var baseColor = !toggled ? theme.getBackgroundNormal() : theme.getBackgroundSelected();
			float b = baseColor.getBrightness();
			
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
			
			b = Math.max(0.0f, Math.min(1.0f, b));
			bgColor = Color.getHSBColor(baseColor.getHue(), baseColor.getSaturation(), b);
		}
		else {
			final var hsb = new float[3];
			Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), hsb);
			
			float b = !toggled ? 0.20F : 0.40F;
			if (pressed) {
				b += theme.getPressedBrightnessAdjustment();
			}
			else if (hovered) {
				b += theme.getHoverBrightnessAdjustment();
			}
			
			b = Math.max(0.0f, Math.min(1.0f, b));
			bgColor = Color.getHSBColor(hsb[0], hsb[1], b);
		}
		
		if (!enabled) {
			final var hsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), null);
			bgColor = Color.getHSBColor(hsb[0], Math.min(1.0f, hsb[1] * 0.15f), Math.min(1.0f, hsb[2] * 0.45f));
		}
		
		g2d.setColor(bgColor);
		g2d.fillRect(x, y, width, height);
		
		final var borderStrength = Math.max(1, (int) (1 * scaleFactor));
		final var outer = hovered || toggled ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
		final var inner = enabled ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, outer);
		
		final var inset = Math.max(1, borderStrength);
		if (width > inset * 3 && height > inset * 3) {
			this.drawDecorativeBorder(g2d, x + inset, y + inset, width - inset * 2, height - inset * 2, Math.max(1, borderStrength), inner);
		}
	}
}
