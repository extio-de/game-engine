package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TooltipControl;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class G2DTooltipControl extends G2DBaseControlImpl implements TooltipControl {
	
	@Override
	public void performAction() {
		
	}
	
	@Override
	public void render() {
		this.tooltipMousePosition = ImmutableCoordI2.zero();
		super.render();
	}
}
