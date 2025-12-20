package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.color.RgbaColor;

@SuppressWarnings("serial")
public class CustomWindowCloseButton extends CustomAbstractButton {
	
	public CustomWindowCloseButton(final boolean toggle, final ActionListener listener) {
		super(toggle, listener);
	}
	
	@Override
	public void paint(final Graphics g) {
		final var g2d = (Graphics2D) g;
		
		final var highlight = (this.state & (STATE_HOVERED | STATE_TOGGLED)) != 0;
		final var pressed = (this.state & STATE_PRESSED) != 0;
		
		Color color = null;
		if (pressed) {
			color = ComponentRenderingSupport.COLOR_COMPONENT_SELECTED_0;
		}
		else if (highlight) {
			color = ComponentRenderingSupport.COLOR_COMPONENT_BGR;
		}
		else if (this.backgroundColor == null) {
			color = ComponentRenderingSupport.COLOR_COMPONENT_BORDER1;
		}
		else {
			color = this.backgroundColor;
		}
		g2d.setColor(color);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		if (this.isEnabled()) {
			if (pressed) {
				g2d.setColor(RgbaColor.YELLOW.toAwtColor());
			}
			else if (highlight) {
				g2d.setColor(ComponentRenderingSupport.COLOR_COMPONENT_SELECTED_1);
			}
			else {
				g2d.setColor(Color.BLACK);
			}
			final var textDim = G2DDrawFont.getTextDimensions("X", g2d, 16, this.scaleFactor);
			G2DDrawFont.renderText(g2d, textDim, 1.0, ((this.getWidth() - textDim.getX()) / 2), ((this.getHeight() - textDim.getY()) / 2), (int) (16 * this.scaleFactor), "X");
		}
	}
	
}
