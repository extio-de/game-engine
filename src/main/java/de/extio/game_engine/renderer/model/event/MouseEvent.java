package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.event.Event;
import de.extio.game_engine.spatial2.model.CoordI2;

public abstract class MouseEvent implements Event {
	
	private CoordI2 coord;
	
	private int modifiers;
	
	public MouseEvent(final CoordI2 coord, final int modifiers) {
		this.coord = coord;
		this.modifiers = modifiers;
	}
	
	public CoordI2 getCoord() {
		return this.coord;
	}
	
	public void setCoord(final CoordI2 coord) {
		this.coord = coord;
	}
	
	public int getModifiers() {
		return this.modifiers;
	}
	
	public void setModifiers(final int modifiers) {
		this.modifiers = modifiers;
	}
	
}
