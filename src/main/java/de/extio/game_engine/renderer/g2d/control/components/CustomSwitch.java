package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;

@SuppressWarnings("serial")
public class CustomSwitch extends CustomAbstractButton {
	
	private boolean drawBorder;
	private final G2DThemeManager themeManager;
	
	public CustomSwitch(final boolean toggle, final ActionListener listener, final G2DThemeManager themeManager) {
		super(toggle, listener);
		this.themeManager = themeManager;
	}
	
	@Override
	public void paint(final Graphics g) {
		if (this.themeManager == null) {
			return;
		}
		
		final var g2d = (Graphics2D) g;
		final Theme theme = this.themeManager.getCurrentTheme();
		
		final var dimY = this.getHeight() - 1;
		final var dimX = (int) (dimY * 1.5);
		
		float h, s, b;
		final var baseColor = (this.state & STATE_TOGGLED) == 0 ? theme.getBackgroundNormal() : theme.getBackgroundSelected();
		h = baseColor.getHue();
		s = baseColor.getSaturation();
		b = baseColor.getBrightness();
		
		if ((this.state & STATE_PRESSED) != 0) {
			b += theme.getPressedBrightnessAdjustment();
		}
		else if ((this.state & STATE_HOVERED) != 0) {
			b += theme.getHoverBrightnessAdjustment();
		}
		
		b = Math.max(0.0f, Math.min(1.0f, b));
		final var bodyColor = Color.getHSBColor(h, s, b);
		Color border0Color;
		Color border1Color;
		
		if (dimX < 45) {
			border0Color = this.isEnabled() ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
			border1Color = bodyColor;
		}
		else {
			border0Color = theme.getBorderOuter().toColor();
			border1Color = this.isEnabled() ? theme.getBorderInner().toColor() : theme.getBorderInnerDisabled().toColor();
		}
		
		if ((this.state & STATE_TOGGLED) == 0) {
			g2d.setColor(bodyColor);
			g2d.fillRect(8, 8, (int) (dimX / 1.5) - 12, this.getHeight() - 15);
			
			final var patternRenderer = this.themeManager.getPatternRenderer(theme.getPatternRendererName());
			if (patternRenderer != null) {
				patternRenderer.drawDecorativeBorder(g2d, 6, 4, (int) (dimX / 1.5) - 6, this.getHeight() - 9, 2, border0Color);
				patternRenderer.drawDecorativeBorder(g2d, 8, 6, (int) (dimX / 1.5) - 10, this.getHeight() - 13, 2, border1Color);
			}
		}
		else {
			g2d.setColor(bodyColor);
			g2d.fillRect(dimX - (int) (dimX / 1.5), 8, (int) (dimX / 1.5) - 12, this.getHeight() - 15);
			
			final var patternRenderer = this.themeManager.getPatternRenderer(theme.getPatternRendererName());
			if (patternRenderer != null) {
				patternRenderer.drawDecorativeBorder(g2d, dimX - (int) (dimX / 1.5) - 3, 4, (int) (dimX / 1.5) - 6, this.getHeight() - 9, 2, border0Color);
				patternRenderer.drawDecorativeBorder(g2d, dimX - (int) (dimX / 1.5) - 1, 6, (int) (dimX / 1.5) - 10, this.getHeight() - 13, 2, border1Color);
			}
		}
		
		// Text color based on state
		if ((this.state & STATE_HOVERED) != 0) {
			g2d.setColor(theme.getTextNormal().adjustBrightness(theme.getHoverBrightnessAdjustment()).toColor());
		}
		else if ((this.state & STATE_TOGGLED) != 0) {
			g2d.setColor(theme.getSelectionPrimary().toColor());
		}
		else if (this.isEnabled()) {
			g2d.setColor(theme.getTextNormal().toColor());
		}
		else {
			g2d.setColor(theme.getTextDisabled().toColor());
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
			final var borderColor1 = (this.state & STATE_HOVERED) != 0 ? theme.getSelectionPrimary().toColor() : theme.getBorderOuter().toColor();
			final var patternRenderer = this.themeManager.getPatternRenderer(theme.getPatternRendererName());
			if (patternRenderer != null) {
				patternRenderer.drawDecorativeBorder(g2d, 0, 0, this.getWidth() - 1, this.getHeight() - 1, 2, borderColor1);
				
				final var borderColor2 = this.isEnabled() ? 
						(this.state & STATE_HOVERED) != 0 ? theme.getSelectionSecondary().toColor() : theme.getBorderInner().toColor() : 
						theme.getBorderInnerDisabled().toColor();
				patternRenderer.drawDecorativeBorder(g2d, 2, 2, this.getWidth() - 5, this.getHeight() - 5, 2, borderColor2);
			}
		}
	}
	
	public boolean isDrawBorder() {
		return this.drawBorder;
	}
	
	public void setDrawBorder(final boolean drawBorder) {
		this.drawBorder = drawBorder;
	}
	
}
