package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomSwitch;

public class G2DSwitchControlImpl extends G2DAbstractToggleButtonControlImpl {
	
	@Override
	protected void createControl() {
		this.control = new CustomSwitch(true, event -> this.performAction(), ((G2DRenderer) this.rendererData.getRenderer()).getThemeManager());
		this.control.setName(this.id);
	}
	
	@Override
	protected void initControl() {
		super.initControl();
		((CustomSwitch) this.control).setDrawBorder(this.drawBorder);
	}
	
}
