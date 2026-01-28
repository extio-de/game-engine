package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.control.G2DDrawControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchData;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public abstract class G2DAbstractToggleButtonControlImpl extends G2DButtonControlImpl implements ToggleButtonControl, SwitchControl {
	
	protected boolean drawBorder = true;
	
	protected void applyToggleButtonData(final ToggleButtonData data) {
		if (data != null) {
			if (this.lastControlDataUpdateTimeInternal < this.lastControlDataUpdateTime) {
				this.setToggled(data.toggled());
				this.lastControlDataUpdateTimeInternal = this.lastControlDataUpdateTime;
			}
			this.setIconResource(data.iconResource());
		}
	}
	
	protected void applySwitchData(final SwitchData data) {
		if (data != null) {
			if (this.lastControlDataUpdateTimeInternal < this.lastControlDataUpdateTime) {
				this.setToggled(data.toggled());
				this.lastControlDataUpdateTimeInternal = this.lastControlDataUpdateTime;
			}
			this.setIconResource(data.iconResource());
			this.setDrawBorder(data.drawBorder());
		}
	}
	
	@Override
	public void performAction() {
		if (this.enabled) {
			this.manageCtrlGroupToggleState();
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, this.control.isToggled()));
		}
	}
	
	private void setToggled(final boolean toggled) {
		if (this.control != null && toggled != this.control.isToggled()) {
			this.control.setToggled(toggled);
			this.manageCtrlGroupToggleState();
		}
	}
	
	private void setDrawBorder(final boolean draw) {
		this.modified |= draw != this.drawBorder;
		this.drawBorder = draw;
	}
	
	private void manageCtrlGroupToggleState() {
		final var controls = G2DDrawControl.CONTROL_GROUPS.get(this.controlGroup);
		if (controls != null && !controls.isEmpty()) {
			var controlInGroupToggled = false;
			
			for (final BaseControl control : controls) {
				if (control instanceof final G2DToggleButtonControlImpl tbControl) {
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
