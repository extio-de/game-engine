package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;

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
		final var s = Math.max((thickBorder ? 5 : 3), (int) ((thickBorder ? 7 : 4) * scaleFactor));
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
			fg = theme.getTextNormal().toColor();
		}
		else if (highlight) {
			fg = Color.BLACK;
		}
		else {
			fg = theme.getTextDisabled().toColor();
		}
		g2d.setColor(fg);
		
		final var crossSize = Math.min(width, height) / 2;
		final var centerX = width / 2;
		final var centerY = height / 2;
		final var t = Math.max(1, (int) (2 * scaleFactor));
		g2d.fillRect(centerX - crossSize / 2, centerY - t / 2, crossSize, t);
		g2d.fillRect(centerX - t / 2, centerY - crossSize / 2, t, crossSize);
	}
}
