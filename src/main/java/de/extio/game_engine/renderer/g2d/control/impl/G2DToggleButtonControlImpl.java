package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomButton;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;

public class G2DToggleButtonControlImpl extends G2DAbstractToggleButtonControlImpl {
	
	@Override
	protected void createControl() {
		this.control = new CustomButton(true, event -> this.performAction(), (G2DThemeManager) this.rendererData.getThemeManager());
		this.control.setName(this.id);
	}
	
}
