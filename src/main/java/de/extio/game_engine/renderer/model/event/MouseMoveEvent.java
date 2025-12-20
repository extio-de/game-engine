package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Message sent by game client engine when mouse has been moved 
 */
public final class MouseMoveEvent extends MouseEvent {
	
	private boolean drag;
	
	private int button;
	
	public MouseMoveEvent(final boolean drag, final int modifiers, final CoordI2 coord, final int button) {
		super(coord, modifiers);
		this.drag = drag;
		this.button = button;
	}
	
	public boolean isDrag() {
		return this.drag;
	}
	
	public void setDrag(final boolean drag) {
		this.drag = drag;
	}
	
	public int getButton() {
		return this.button;
	}
	
	public void setButton(final int button) {
		this.button = button;
	}
	
}
