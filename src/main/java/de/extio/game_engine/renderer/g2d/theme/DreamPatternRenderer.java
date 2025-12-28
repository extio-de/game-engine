package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;

@Conditional(G2DRendererCondition.class)
@Component
public class DreamPatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		final var s = Math.max(1, strength);
		final var arc = Math.max(6, s * 6);
		
		g2d.setColor(color);
		g2d.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
		
		final var dot = Math.max(2, s * 2);
		g2d.fillOval(x + s, y + s, dot, dot);
		g2d.fillOval(x + width - s - dot, y + s, dot, dot);
		g2d.fillOval(x + s, y + height - s - dot, dot, dot);
		g2d.fillOval(x + width - s - dot, y + height - s - dot, dot, dot);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		final var s = Math.max(1, strength);
		final var arc = Math.max(6, s * 6);
		
		g2d.setColor(backgroundColor);
		g2d.fillRoundRect(x - s, y - s, Math.max(0, width + s * 2), Math.max(0, height + s * 2), arc, arc);
		
		this.drawDecorativeBorder(g2d, x, y, width, height, s, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var s = Math.max((thickBorder ? 4 : 2), (int) ((thickBorder ? 6 : 3) * scaleFactor));
		final var s2 = s * 2;
		
		final var bg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 160);
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
			bg = theme.getWindowBackground().toColor();
		}
		if (highlight) {
			bg = theme.getSelectionSecondary().toColor();
		}
		if (pressed) {
			bg = theme.getSelectionPrimary().toColor();
		}
		
		final var bg2 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 210);
		g2d.setColor(bg2);
		g2d.fillRoundRect(0, 0, width, height, Math.max(6, (int) (10 * scaleFactor)), Math.max(6, (int) (10 * scaleFactor)));
		
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
		
		final var fontSize = (int) (15 * scaleFactor);
		final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 15, scaleFactor);
		G2DDrawFont.renderText(g2d, textDim, 1.0, ((width - textDim.getX()) / 2), ((height - textDim.getY()) / 2), fontSize, "X");
	}
}
