package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomButton;

public class G2DToggleButtonControlImpl extends G2DAbstractToggleButtonControlImpl {
	
	@Override
	protected void createControl() {
		this.control = new CustomButton(true, event -> this.performAction(), ((G2DRenderer) this.rendererData.getRenderer()).getThemeManager());
		this.control.setName(this.id);
	}
	
}
