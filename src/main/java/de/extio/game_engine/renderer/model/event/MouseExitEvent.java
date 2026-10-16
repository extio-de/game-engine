package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Message sent by game client engine when mouse enters viewport 
 */
public final class MouseExitEvent extends MouseEvent {
	
	private final String controlId;
	
	public MouseExitEvent(final int modifiers, final CoordI2 rawCoord, final CoordI2 scaledCoord) {
		this(null, modifiers, rawCoord, scaledCoord);
	}
	
	public MouseExitEvent(final String controlId, final int modifiers, final CoordI2 rawCoord, final CoordI2 scaledCoord) {
		super(rawCoord, scaledCoord, modifiers);
		this.controlId = controlId;
	}
	
	/**
	 * Control ID the mouse exited, null if the mouse exited the viewport
	 */
	public String getControlId() {
		return this.controlId;
	}
	
}
