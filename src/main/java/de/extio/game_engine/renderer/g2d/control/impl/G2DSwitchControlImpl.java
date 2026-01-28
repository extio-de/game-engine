package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.control.components.CustomSwitch;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;

public class G2DSwitchControlImpl extends G2DAbstractToggleButtonControlImpl {
	
	@Override
	public void setCustomData(final SwitchData data) {
		this.applySwitchData(data);
	}
	
	@Override
	public void setCustomData(final ToggleButtonData data) {
		// Not used in Switch
	}
	
	@Override
	protected void createControl() {
		this.control = new CustomSwitch(true, event -> this.performAction(), (G2DThemeManager) this.rendererData.getThemeManager());
		this.control.setName(this.id);
		this.lastControlDataUpdateTimeInternal = 0;
	}
	
	@Override
	protected void initControl() {
		super.initControl();
		((CustomSwitch) this.control).setDrawBorder(this.drawBorder);
	}
	
}
