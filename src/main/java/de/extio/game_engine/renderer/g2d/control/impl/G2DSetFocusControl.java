package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Component;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SetFocusControl;

public class G2DSetFocusControl extends G2DBaseControlImpl implements SetFocusControl {
	
	protected String focusId;
	
	@Override
	public String getFocusId() {
		return this.focusId;
	}
	
	@Override
	public void setFocusId(final String id) {
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
		
		final Component[] components = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().getComponents();
		for (int i = 0; i < components.length; i++) {
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