package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Message sent by game client engine when mouse enters viewport 
 */
public final class MouseEnterEvent extends MouseEvent {
	
	public MouseEnterEvent(final int modifiers, final CoordI2 coord) {
		super(coord, modifiers);
	}
	
}
