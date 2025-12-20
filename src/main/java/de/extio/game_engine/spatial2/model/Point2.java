package de.extio.game_engine.spatial2.model;

import java.util.Objects;

/**
 * A point in 2D space
 */
public class Point2 implements HasPosition2 {
	
	private CoordI2 coord;
	
	public Point2() {
		//
	}
	
	public Point2(final CoordI2 coord) {
		this.coord = coord;
	}
	
	public void setPosition(final CoordI2 coord) {
		this.coord = coord;
	}
	
	@Override
	public CoordI2 getPosition() {
		return this.coord;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.coord);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final Point2 other)) {
			return false;
		}
		return Objects.equals(this.coord, other.coord);
	}
	
	@Override
	public String toString() {
		return "Point2 [coord=" + this.coord + "]";
	}
	
}
