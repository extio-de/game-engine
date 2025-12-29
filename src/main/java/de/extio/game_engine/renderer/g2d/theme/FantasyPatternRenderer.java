package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.spatial2.model.CoordI2;

@Conditional(G2DRendererCondition.class)
@Component
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
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 175);
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
				enabled ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		this.drawDecorativeBorder(g2d, x, y, width, height, borderStrength, borderColor);
	}
}
