package de.extio.game_engine.spatial2.model;

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
		final var prime = 31;
		var result = super.hashCode();
		result = prime * result + ((this.dimension == null) ? 0 : this.dimension.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		final var other = (Area2) obj;
		if (this.dimension == null) {
			if (other.dimension != null) {
				return false;
			}
		}
		else if (!this.dimension.equals(other.dimension)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("Area2 [getPosition()=");
		builder.append(this.getPosition());
		builder.append(", dimension=");
		builder.append(this.dimension);
		builder.append("]");
		return builder.toString();
	}
	
}
