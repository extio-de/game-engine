package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;

@Conditional(G2DRendererCondition.class)
@Component
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
}
