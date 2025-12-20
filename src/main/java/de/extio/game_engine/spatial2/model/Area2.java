package de.extio.game_engine.spatial2.model;

import java.util.Objects;

/**
 * A 2D area, having a position (top left corner) and a dimension
 */
public class Area2 extends Point2 implements HasPositionAndDimension2, SpatialIndex2Capable {
	
	private CoordI2 dimension;
	
	private transient long bsi2Session;
	
	public Area2() {
		super();
	}
	
	public Area2(final CoordI2 coord, final CoordI2 dimension) {
		super(coord);
		this.setDimension(dimension);
	}
	
	public Area2(final HasPositionAndDimension2 other) {
		super(ImmutableCoordI2.create(other.getPosition()));
		this.setDimension(ImmutableCoordI2.create(other.getDimension()));
	}
	
	@Override
	public CoordI2 getDimension() {
		return this.dimension;
	}
	
	public void setDimension(final CoordI2 dimension) {
		this.dimension = dimension;
	}
	
	@Override
	public boolean isInSpatialIndex2ResultSet(final long session) {
		return this.bsi2Session == session;
	}
	
	@Override
	public void includeInSpatialIndex2ResultSet(final long session) {
		this.bsi2Session = session;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.dimension);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || !(obj instanceof final Area2 other)) {
			return false;
		}
		return Objects.equals(this.dimension, other.dimension);
	}
	
	@Override
	public String toString() {
		return "Area2 [getPosition()=" + this.getPosition() + ", dimension=" + this.dimension + "]";
	}
	
}
