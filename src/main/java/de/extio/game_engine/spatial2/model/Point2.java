package de.extio.game_engine.spatial2.model;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.coord == null) ? 0 : this.coord.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		final Point2 other = (Point2) obj;
		if (this.coord == null) {
			if (other.coord != null) {
				return false;
			}
		}
		else if (!this.coord.equals(other.coord)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Point2 [coord=");
		builder.append(this.coord);
		builder.append("]");
		return builder.toString();
	}
	
}
