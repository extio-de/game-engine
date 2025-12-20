package de.extio.game_engine.renderer.g2d.control.impl;

import java.util.List;

import de.extio.game_engine.renderer.g2d.control.G2DDrawControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public abstract class G2DAbstractToggleButtonControlImpl extends G2DButtonControlImpl implements ToggleButtonControl, SwitchControl {
	
	protected boolean drawBorder = true;
	
	@Override
	public void performAction() {
		if (this.enabled) {
			this.manageCtrlGroupToggleState();
			this.rendererData.getEventConsumer().accept(new UiControlEvent(this.id, this.control.isToggled()));
		}
	}
	
	@Override
	public boolean isToggled() {
		return this.control.isToggled();
	}
	
	@Override
	public void setToggled(final boolean toggled) {
		if (this.control != null && toggled != this.control.isToggled()) {
			this.control.setToggled(toggled);
			this.manageCtrlGroupToggleState();
		}
	}
	
	@Override
	public boolean isDrawBorder() {
		return this.drawBorder;
	}
	
	@Override
	public void setDrawBorder(final boolean draw) {
		this.modified |= draw != this.drawBorder;
		this.drawBorder = draw;
	}
	
	private void manageCtrlGroupToggleState() {
		final List<BaseControl> controls = G2DDrawControl.CONTROL_GROUPS.get(this.controlGroup);
		if (controls != null && !controls.isEmpty()) {
			boolean controlInGroupToggled = false;
			
			for (final BaseControl control : controls) {
				if (control instanceof G2DToggleButtonControlImpl) {
					final G2DToggleButtonControlImpl tbControl = (G2DToggleButtonControlImpl) control;
					if (tbControl.control == this.control) {
						continue;
					}
					controlInGroupToggled |= tbControl.control.isToggled();
					if (this.control.isToggled() && tbControl.control.isToggled()) {
						tbControl.control.setToggled(!this.control.isToggled());
					}
				}
			}
			
			if (!controlInGroupToggled) {
				this.control.setToggled(true);
			}
		}
	}
	
}
