package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Message sent by game client engine when mouse enters viewport 
 */
public final class MouseExitEvent extends MouseEvent {
	
	public MouseExitEvent(final int modifiers, final CoordI2 rawCoord, final CoordI2 scaledCoord) {
		super(rawCoord, scaledCoord, modifiers);
	}
	
}
