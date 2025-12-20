package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Graphics;
import java.awt.TextField;

import de.extio.game_engine.renderer.g2d.control.G2DControlHasExclusiveKeyEvent;

@SuppressWarnings("serial")
public class CustomTextField extends TextField implements G2DControlHasExclusiveKeyEvent {
	
	protected boolean dirty = true;
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public CustomTextField() {
		
	}
	
	@Override
	public void update(final Graphics g) {
		this.dirty = true;
	}
	
}
