package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.spatial2.model.CoordI2;

@Conditional(G2DRendererCondition.class)
@Component
public class ModernPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var s2 = s * 2;
		
		g2d.setColor(color);
		
		g2d.fillRect(x, y, width, s);
		g2d.fillRect(x, y + height - s, width, s);
		g2d.fillRect(x, y, s, height);
		g2d.fillRect(x + width - s, y, s, height);
		
		final var notch = Math.max(1, s2);
		g2d.fillRect(x + s, y + s, notch, s);
		g2d.fillRect(x + width - s - notch, y + s, notch, s);
		g2d.fillRect(x + s, y + height - s - s, notch, s);
		g2d.fillRect(x + width - s - notch, y + height - s - s, notch, s);
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
		final var s = Math.max((thickBorder ? 3 : 2), (int) ((thickBorder ? 5 : 3) * scaleFactor));
		final var s2 = s * 2;
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 210);
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
		
		final var s = Math.max(1, (int) (2 * scaleFactor));
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
			textColor = Color.BLACK;
		}
		g2d.setColor(textColor);
		
		final var fontSize = (int) (14 * scaleFactor);
		final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 14, scaleFactor);
		G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), fontSize, "X");
	}

	private java.awt.image.BufferedImage patternCache;

	private java.awt.image.BufferedImage getPatternCache() {
		if (this.patternCache == null) {
			final int spacing = 60;
			this.patternCache = new BufferedImage(spacing, spacing, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = this.patternCache.createGraphics();
			
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0, 0, spacing, spacing);
			
			g2d.setColor(new Color(255, 255, 255, 35));
			final int len = 10;
			final int cx = spacing / 2;
			final int cy = spacing / 2;
			g2d.drawLine(cx - len, cy, cx + len, cy);
			g2d.drawLine(cx, cy - len, cx, cy + len);
			
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
