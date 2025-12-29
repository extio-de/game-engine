package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.theme.Theme;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;

@SuppressWarnings("serial")
public class CustomButton extends CustomAbstractButton {
	
	private final G2DThemeManager themeManager;
	
	public CustomButton(final boolean toggle, final ActionListener listener, final G2DThemeManager themeManager) {
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
		
		if (patternRenderer != null) {
			patternRenderer.drawButton(g2d, 0, 0, this.getWidth(), this.getHeight(), this.isEnabled(), this.state, this.backgroundColor, this.scaleFactor, theme);
		}
		
		final var bgrStrength = (this.getWidth() < 48 || this.getHeight() < 48) ? 0 : 2;
		
		if (this.iconResource != null && (this.icon == null || !this.iconResource.equals(this.loadedIconResource))) {
			this.loadIcon();
		}
		if (this.icon != null) {
			g2d.drawImage(this.icon, 4 + bgrStrength, 4 + bgrStrength, this.getWidth() - 4 - bgrStrength, this.getHeight() - 4 - bgrStrength, 0, 0, this.icon.getWidth(), this.icon.getHeight(), null);
		}
		else if (this.caption != null) {
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
					(int) ((this.getWidth() - (textDim.getX())) / 2 / this.scaleFactor),
					(int) ((this.getHeight() - (textDim.getY())) / 2 / this.scaleFactor),
					this.fontSize,
					this.caption);
		}
	}
	
}
