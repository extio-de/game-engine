package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;

@SuppressWarnings("serial")
public class CustomWindowCloseButton extends CustomAbstractButton {
	
	private final G2DThemeManager themeManager;
	
	public CustomWindowCloseButton(final boolean toggle, final ActionListener listener, final G2DThemeManager themeManager) {
		super(toggle, listener);
		this.themeManager = themeManager;
	}
	
	@Override
	public void paint(final Graphics g) {
		if (this.themeManager == null) {
			return;
		}
		
		final var g2d = (Graphics2D) g;
		final var theme = this.themeManager.getCurrentTheme();
		final var patternRenderer = this.themeManager.getPatternRenderer(theme.getPatternRendererName());
		
		if (patternRenderer != null) {
			patternRenderer.drawCloseButton(g2d, this.getWidth(), this.getHeight(), this.isEnabled(), this.state.isPressed(), this.state.isHovered(), this.state.isToggled(), this.backgroundColor, this.scaleFactor, theme);
		}
	}
	
}
