package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Message sent by game client engine on any mouse click
 */
public final class MouseClickEvent extends MouseEvent {
	
	private boolean pressed;
	
	private int button;
	
	public MouseClickEvent(final boolean pressed, final int modifiers, final int button, final CoordI2 rawCoord, final CoordI2 scaledCoord) {
		super(rawCoord, scaledCoord, modifiers);
		this.pressed = pressed;
		this.button = button;
	}
	
	public boolean isPressed() {
		return this.pressed;
	}
	
	public void setPressed(final boolean pressed) {
		this.pressed = pressed;
	}
	
	public int getButton() {
		return this.button;
	}
	
	public void setButton(final int button) {
		this.button = button;
	}
}
