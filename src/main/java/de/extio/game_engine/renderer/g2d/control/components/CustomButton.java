package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.spatial2.model.CoordI2;

@SuppressWarnings("serial")
public class CustomButton extends CustomAbstractButton {
	
	public CustomButton(final boolean toggle, final ActionListener listener) {
		super(toggle, listener);
	}
	
	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		
		float h, s, b;
		
		final int borderStrength = (this.getWidth() < 48 || this.getHeight() < 48) ? 1 : 2;
		final int bgrStrength = (this.getWidth() < 48 || this.getHeight() < 48) ? 0 : 2;
		
		if (this.backgroundColor == null) {
			if ((this.state & STATE_TOGGLED) == 0) {
				h = ComponentRenderingSupport.HSB_COMPONENT_BGR.h();
				s = ComponentRenderingSupport.HSB_COMPONENT_BGR.s();
				b = ComponentRenderingSupport.HSB_COMPONENT_BGR.b();
			}
			else {
				h = ComponentRenderingSupport.HSB_COMPONENT_SELECTED_0.b();
				s = ComponentRenderingSupport.HSB_COMPONENT_SELECTED_0.s();
				b = ComponentRenderingSupport.HSB_COMPONENT_SELECTED_0.b();
			}
			if ((this.state & STATE_PRESSED) != 0) {
				b += 0.40F;
			}
			else if ((this.state & STATE_HOVERED) != 0) {
				b += 0.25F;
			}
		}
		else {
			final float[] hsb = new float[3];
			Color.RGBtoHSB(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), hsb);
			h = hsb[0];
			s = hsb[1];
			
			if ((this.state & STATE_TOGGLED) == 0) {
				b = 0.30F;
			}
			else {
				b = 0.50F;
			}
			if ((this.state & STATE_PRESSED) != 0) {
				b += 0.40F;
			}
			else if ((this.state & STATE_HOVERED) != 0) {
				b += 0.25F;
			}
		}
		g2d.setColor(Color.getHSBColor(h, s, b));
		g2d.fillRect(2 + bgrStrength, 2 + bgrStrength, this.getWidth() - 5 - bgrStrength * 2, this.getHeight() - 5 - bgrStrength * 2);
		
		if (this.iconResourceName != null && (this.icon == null || !this.iconResourceName.equals(this.loadedIconResourceName))) {
			this.loadIcon();
		}
		if (this.icon != null) {
			g2d.drawImage(this.icon, 4 + bgrStrength, 4 + bgrStrength, this.getWidth() - 4 - bgrStrength, this.getHeight() - 4 - bgrStrength, 0, 0, this.icon.getWidth(), this.icon.getHeight(), null);
		}
		else if (this.caption != null) {
			if ((this.state & (STATE_HOVERED | STATE_TOGGLED)) != 0) {
				g2d.setColor(Color.WHITE);
			}
			else if (this.isEnabled()) {
				g2d.setColor(Color.LIGHT_GRAY);
			}
			else {
				g2d.setColor(Color.GRAY);
			}
			final CoordI2 textDim = G2DDrawFont.getTextDimensions(this.caption, g2d, this.fontSize, this.scaleFactor);
			G2DDrawFont.renderText(g2d,
					textDim,
					this.scaleFactor,
					(int) ((this.getWidth() - (textDim.getX())) / 2 / this.scaleFactor),
					(int) ((this.getHeight() - (textDim.getY())) / 2 / this.scaleFactor),
					this.fontSize,
					this.caption);
		}
		
		Color color = (this.state & STATE_HOVERED) != 0 ? ComponentRenderingSupport.COLOR_COMPONENT_SELECTED_0 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER0;
		ComponentRenderingSupport.drawDecorativeBorder(g2d, 0, 0, this.getWidth() - 1, this.getHeight() - 1, borderStrength, color);
		color = this.isEnabled() ? (this.state & STATE_HOVERED) != 0 ? ComponentRenderingSupport.COLOR_COMPONENT_SELECTED_1 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER1 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER1_DIS;
		ComponentRenderingSupport.drawDecorativeBorder(g2d, borderStrength, borderStrength, this.getWidth() - 1 - 2 * borderStrength, this.getHeight() - 1 - 2 * borderStrength, borderStrength, color);
	}
	
}
