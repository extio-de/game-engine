package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;

@SuppressWarnings("serial")
public class CustomSwitch extends CustomAbstractButton {
	
	private boolean drawBorder;
	
	public CustomSwitch(final boolean toggle, final ActionListener listener) {
		super(toggle, listener);
	}
	
	@Override
	public void paint(final Graphics g) {
		final var g2d = (Graphics2D) g;
		
		final var dimY = this.getHeight() - 1;
		final var dimX = (int) (dimY * 1.5);
		
		float h, s, b;
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
		
		final var bodyColor = Color.getHSBColor(h, s, b);
		Color border0Color;
		Color border1Color;
		if (dimX < 45) {
			border0Color = this.isEnabled() ? ComponentRenderingSupport.COLOR_COMPONENT_BORDER1 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER1_DIS;
			border1Color = bodyColor;
		}
		else {
			border0Color = ComponentRenderingSupport.COLOR_COMPONENT_BORDER0;
			border1Color = this.isEnabled() ? ComponentRenderingSupport.COLOR_COMPONENT_BORDER1 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER1_DIS;
		}
		
		if ((this.state & STATE_TOGGLED) == 0) {
			g2d.setColor(bodyColor);
			g2d.fillRect(8, 8, (int) (dimX / 1.5) - 12, this.getHeight() - 15);
			
			ComponentRenderingSupport.drawDecorativeBorder(g2d, 6, 4, (int) (dimX / 1.5) - 6, this.getHeight() - 9, 2, border0Color);
			ComponentRenderingSupport.drawDecorativeBorder(g2d, 8, 6, (int) (dimX / 1.5) - 10, this.getHeight() - 13, 2, border1Color);
		}
		else {
			g2d.setColor(bodyColor);
			g2d.fillRect(dimX - (int) (dimX / 1.5), 8, (int) (dimX / 1.5) - 12, this.getHeight() - 15);
			
			ComponentRenderingSupport.drawDecorativeBorder(g2d, dimX - (int) (dimX / 1.5) - 3, 4, (int) (dimX / 1.5) - 6, this.getHeight() - 9, 2, border0Color);
			ComponentRenderingSupport.drawDecorativeBorder(g2d, dimX - (int) (dimX / 1.5) - 1, 6, (int) (dimX / 1.5) - 10, this.getHeight() - 13, 2, border1Color);
		}
		
		if ((this.state & STATE_HOVERED) != 0) {
			g2d.setColor(Color.WHITE);
		}
		else if (this.isEnabled()) {
			g2d.setColor(Color.LIGHT_GRAY);
		}
		else {
			g2d.setColor(Color.GRAY);
		}
		final var textDim = G2DDrawFont.getTextDimensions(this.caption, g2d, this.fontSize, this.scaleFactor);
		G2DDrawFont.renderText(g2d,
				textDim,
				this.scaleFactor,
				(int) (dimX / this.scaleFactor) + 3,
				(int) ((this.getHeight() - (textDim.getY())) / 2 / this.scaleFactor),
				this.fontSize,
				this.caption);
		
		if (this.drawBorder) {
			ComponentRenderingSupport.drawDecorativeBorder(g2d, 0, 0, this.getWidth() - 1, this.getHeight() - 1, 2, (this.state & STATE_HOVERED) != 0 ? ComponentRenderingSupport.COLOR_COMPONENT_SELECTED_0 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER0);
			ComponentRenderingSupport.drawDecorativeBorder(g2d, 2, 2, this.getWidth() - 5, this.getHeight() - 5, 2, this.isEnabled() ? (this.state & STATE_HOVERED) != 0 ? ComponentRenderingSupport.COLOR_COMPONENT_SELECTED_1 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER1 : ComponentRenderingSupport.COLOR_COMPONENT_BORDER1_DIS);
		}
	}
	
	public boolean isDrawBorder() {
		return this.drawBorder;
	}
	
	public void setDrawBorder(final boolean drawBorder) {
		this.drawBorder = drawBorder;
	}
	
}
