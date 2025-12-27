package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;

@Conditional(G2DRendererCondition.class)
@Component
public class VintagePatternRenderer implements PatternRenderer {
	
	@Override
	public void drawDecorativeBorder(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color color) {
		g2d.setColor(color);
		
		final var size = strength;
		
		g2d.fillRect(x, y, width, size);
		g2d.fillRect(x, y, size, height - size * 2);
		g2d.fillRect(x + width - size, y, size, height - size * 2);
		g2d.fillRect(x + size * 2, y + height - size, width - size * 4, size);
		
		g2d.fillPolygon(
			new int[] { x, x + size * 2, x + size * 2, x + size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
		g2d.fillPolygon(
			new int[] { x + width, x + width - size * 2, x + width - size * 2, x + width - size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
	}
	
	@Override
	public void drawDecorativeBorderFilled(final Graphics2D g2d, final int x, final int y, final int width, final int height, final int strength, final Color borderColor, final Color backgroundColor) {
		g2d.setColor(backgroundColor);
		g2d.fillRect(x, y + strength, width, height - strength * 3);
		g2d.fillRect(x + strength, y + height - strength * 2, width - strength * 2, strength);
		
		this.drawDecorativeBorder(g2d, x, y, width, height, strength, borderColor);
	}
	
	@Override
	public void drawWindowPanel(final Graphics2D g2d, final int x, final int y, final int width, final int height, final boolean thickBorder, final Color innerBorderColor, final Color outerBorderColor, final Color backgroundColor, final double scaleFactor) {
		final var size = Math.max(3, (int) (5 * scaleFactor));
		
		final var baseColor = new float[3];
		innerBorderColor.getRGBColorComponents(baseColor);
		Color.RGBtoHSB((int) (baseColor[0] * 255), (int) (baseColor[1] * 255), (int) (baseColor[2] * 255), baseColor);
		
		final var backgroundTransparency = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 180);
		g2d.setColor(backgroundTransparency);
		g2d.fillRect(x, y + size, width, height - size * 3);
		g2d.fillRect(x + size, y + height - size * 2, width - size * 2, size);
		
		g2d.setColor(Color.getHSBColor(baseColor[0], baseColor[1], baseColor[2]));
		if (thickBorder) {
			g2d.fillRect(x, y + size * 2, size, height - size * 4);
			g2d.fillRect(x + width - size, y + size * 2, size, height - size * 4);
			g2d.fillRect(x, y + size * 2, width, size);
		}
		else {
			g2d.fillRect(x, y, size, height - size * 2);
			g2d.fillRect(x + width - size, y, size, height - size * 2);
			g2d.fillRect(x, y, width, size);
		}
		g2d.fillRect(x + size * 2, y + height - size, width - size * 4, size);
		g2d.fillPolygon(
			new int[] { x, x + size * 2, x + size * 2, x + size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
		g2d.fillPolygon(
			new int[] { x + width, x + width - size * 2, x + width - size * 2, x + width - size }, 
			new int[] { y + height - size * 2, y + height, y + height - size, y + height - size * 2 }, 
			4
		);
		
		if (thickBorder) {
			for (var i = 0; i < size * 2; i++) {
				g2d.setColor(Color.getHSBColor(baseColor[0], baseColor[1], baseColor[2] * ((0.5F / (size * 2) * i) + 0.5F)));
				g2d.drawLine(x, y + i, x + width, y + i);
			}
		}
	}
	
	@Override
	public void drawCloseButton(final Graphics2D g2d, final int width, final int height, final boolean enabled, final int state, final Color backgroundColor, final double scaleFactor, final Theme theme) {
		final var STATE_HOVERED = 4;
		final var STATE_TOGGLED = 1;
		final var STATE_PRESSED = 2;
		
		final var highlight = (state & (STATE_HOVERED | STATE_TOGGLED)) != 0;
		final var pressed = (state & STATE_PRESSED) != 0;
		
		final var size = Math.max(2, (int) (3 * scaleFactor));
		
		Color baseColor = null;
		if (pressed) {
			baseColor = theme.getSelectionPrimary().toColor();
		}
		else if (highlight) {
			baseColor = theme.getSelectionSecondary().toColor();
		}
		else if (backgroundColor == null) {
			baseColor = theme.getBorderInner().toColor();
		}
		else {
			baseColor = backgroundColor;
		}
		
		final var hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
		
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		
		g2d.setColor(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
		g2d.fillRect(0, 0, width, size);
		g2d.fillRect(0, 0, size, height);
		g2d.fillRect(width - size, 0, size, height);
		g2d.fillRect(0, height - size, width, size);
		
		if (enabled) {
			final Color textColor;
			if (pressed) {
				textColor = theme.getSelectionPrimary().toColor();
			}
			else if (highlight) {
				textColor = theme.getTextNormal().toColor();
			}
			else {
				textColor = theme.getTextDisabled().toColor();
			}
			g2d.setColor(textColor);
			
			final var crossSize = Math.min(width, height) / 2;
			final var centerX = width / 2;
			final var centerY = height / 2;
			final var crossThickness = Math.max(1, (int) (2 * scaleFactor));
			
			g2d.fillRect(centerX - crossSize / 2, centerY - crossThickness / 2, crossSize, crossThickness);
			g2d.fillRect(centerX - crossThickness / 2, centerY - crossSize / 2, crossThickness, crossSize);
		}
	}
}
