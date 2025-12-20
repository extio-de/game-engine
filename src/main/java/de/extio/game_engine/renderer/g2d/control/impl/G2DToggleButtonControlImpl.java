package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.control.components.CustomButton;

public class G2DToggleButtonControlImpl extends G2DAbstractToggleButtonControlImpl {
	
	@Override
	protected void createControl() {
		this.control = new CustomButton(true, event -> this.performAction());
		this.control.setName(this.id);
	}
	
}
