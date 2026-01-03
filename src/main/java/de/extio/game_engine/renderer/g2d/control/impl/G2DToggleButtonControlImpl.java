package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomButton;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchData;

public class G2DToggleButtonControlImpl extends G2DAbstractToggleButtonControlImpl {
	
	@Override
	public void setCustomData(final ToggleButtonData data) {
		this.applyToggleButtonData(data);
	}
	
	@Override
	public void setCustomData(final SwitchData data) {
		// Not used in ToggleButton
	}
	
	@Override
	protected void createControl() {
		this.control = new CustomButton(true, event -> this.performAction(), (G2DThemeManager) this.rendererData.getThemeManager());
		this.control.setName(this.id);
	}
	
}
