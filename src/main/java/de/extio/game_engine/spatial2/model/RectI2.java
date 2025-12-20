package de.extio.game_engine.spatial2.model;

import java.util.Objects;

/**
 * A rectangle represented by top left (c0) and bottom right (c1) vertices
 */
public class RectI2 {
	
	private CoordI2 c0;
	
	private CoordI2 c1;
	
	public RectI2() {
		
	}
	
	public RectI2(final CoordI2 c0, final CoordI2 c1) {
		this.c0 = c0;
		this.c1 = c1;
	}
	
	public RectI2(final RectI2 other) {
		if (other == null) {
			return;
		}
		if (other.c0 != null) {
			if (other.c0 instanceof final ImmutableCoordI2 immutableC0) {
				this.c0 = ImmutableCoordI2.create(immutableC0);
			}
			else {
				this.c0 = MutableCoordI2.create(other.c0);
			}
		}
		if (other.c1 != null) {
			if (other.c1 instanceof final ImmutableCoordI2 immutableC1) {
				this.c1 = ImmutableCoordI2.create(immutableC1);
			}
			else {
				this.c1 = MutableCoordI2.create(other.c1);
			}
		}
	}
	
	public CoordI2 getC0() {
		return this.c0;
	}
	
	public void setC0(final CoordI2 c0) {
		this.c0 = c0;
	}
	
	public CoordI2 getC1() {
		return this.c1;
	}
	
	public void setC1(final CoordI2 c1) {
		this.c1 = c1;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.c0, this.c1);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final RectI2 other)) {
			return false;
		}
		return Objects.equals(this.c0, other.c0) && Objects.equals(this.c1, other.c1);
	}
	
	@Override
	public String toString() {
		return "RectI2 [c0=" + this.c0 + ", c1=" + this.c1 + "]";
	}
	
}
