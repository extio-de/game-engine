package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SetFocusControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SetFocusData;

public class G2DSetFocusControl extends G2DBaseControlImpl implements SetFocusControl {
	
	protected String focusId;
	
	@Override
	public void setCustomData(final SetFocusData data) {
		if (data != null) {
			this.setFocusId(data.focusId());
		}
	}
	
	private void setFocusId(final String id) {
		this.focusId = id;
	}
	
	@Override
	public void build() {
		
	}
	
	@Override
	public void performAction() {
		
	}
	
	@Override
	public void render() {
		if (isNullOrEmpty(this.focusId)) {
			return;
		}
		
		final var components = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().getComponents();
		for (var i = 0; i < components.length; i++) {
			if (this.focusId.equals(components[i].getName())) {
				components[i].requestFocus();
				break;
			}
		}
	}
	
	@Override
	public void close() {
		super.close();
		
		this.setFocusId(null);
	}
	
	private static boolean isNullOrEmpty(final String value) {
		return value == null || value.isEmpty();
	}
	
}
