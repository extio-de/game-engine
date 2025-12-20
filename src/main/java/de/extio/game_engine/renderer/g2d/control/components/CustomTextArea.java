package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Graphics;
import java.awt.TextArea;

import de.extio.game_engine.renderer.g2d.control.G2DControlHasExclusiveKeyEvent;

@SuppressWarnings("serial")
public class CustomTextArea extends TextArea implements G2DControlHasExclusiveKeyEvent {
	
	protected boolean dirty = true;
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public CustomTextArea(final String text, final int rows, final int columns, final int scrollbars) {
		super(text, rows, columns, scrollbars);
	}
	
	@Override
	public void update(final Graphics g) {
		this.dirty = true;
	}
}
