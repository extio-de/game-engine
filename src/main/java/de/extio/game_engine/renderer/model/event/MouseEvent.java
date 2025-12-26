package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.event.Event;
import de.extio.game_engine.spatial2.model.CoordI2;

public abstract class MouseEvent implements Event {
	
	private CoordI2 scaledCoord;
	
	private CoordI2 rawCoord;
	
	private int modifiers;
	
	public MouseEvent(final CoordI2 rawCoord, final CoordI2 scaledCoord, final int modifiers) {
		this.rawCoord = rawCoord;
		this.scaledCoord = scaledCoord;
		this.modifiers = modifiers;
	}
	
	/**
	 * Coordinate of mouse event, normalized/scaled according to current viewport
	*/
	public CoordI2 getScaledCoord() {
		return this.scaledCoord;
	}
	
	public void setScaledCoord(final CoordI2 coord) {
		this.scaledCoord = coord;
	}
	
	public int getModifiers() {
		return this.modifiers;
	}
	
	public void setModifiers(final int modifiers) {
		this.modifiers = modifiers;
	}
	
	/**
	 * Raw, absolute coordinate of mouse event, without any scaling applied
	*/
	public CoordI2 getRawCoord() {
		return rawCoord;
	}
	
	public void setRawCoord(final CoordI2 rawCoord) {
		this.rawCoord = rawCoord;
	}
}
