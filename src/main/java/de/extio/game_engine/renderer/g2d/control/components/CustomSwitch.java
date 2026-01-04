package de.extio.game_engine.renderer.g2d.control.components;

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
		final var patternRenderer = this.themeManager.getPatternRenderer(theme.getPatternRendererName());
		
		final var dimY = this.getHeight() - 1;
		final var dimX = (int) (dimY * 1.5);
		
		if ((this.state & STATE_TOGGLED) == 0) {
			if (patternRenderer != null) {
				patternRenderer.drawButton(g2d, 6, 4, (int) (dimX / 1.5) - 6, this.getHeight() - 9, this.isEnabled(), this.state, null, this.scaleFactor, theme);
			}
		}
		else {
			if (patternRenderer != null) {
				patternRenderer.drawButton(g2d, dimX - (int) (dimX / 1.5) - 3, 4, (int) (dimX / 1.5) - 6, this.getHeight() - 9, this.isEnabled(), this.state, null, this.scaleFactor, theme);
			}
		}
		
		if ((this.state & STATE_HOVERED) != 0) {
			g2d.setColor(theme.getTextNormal().adjustBrightness(theme.getHoverBrightnessAdjustment()).toColor());
		}
		else if ((this.state & STATE_TOGGLED) != 0) {
			g2d.setColor(theme.getTextNormal().adjustBrightness(theme.getHoverBrightnessAdjustment()).toColor());
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
			if (patternRenderer != null) {
				patternRenderer.drawDecorativeBorder(g2d, 0, 0, this.getWidth() - 1, this.getHeight() - 1, 2, borderColor1);
				
				// final var borderColor2 = this.isEnabled() ? 
				// 		(this.state & STATE_HOVERED) != 0 ? theme.getSelectionSecondary().toColor() : theme.getBorderInner().toColor() : 
				// 		theme.getBorderInnerDisabled().toColor();
				// patternRenderer.drawDecorativeBorder(g2d, 2, 2, this.getWidth() - 5, this.getHeight() - 5, 2, borderColor2);
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
