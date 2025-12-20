package de.extio.game_engine.spatial2.model;

import java.util.Objects;

/**
 * Immutable implementation of CoordD2
 */
public final class MutableCoordD2 implements CoordD2 {
	
	public static MutableCoordD2 create() {
		return new MutableCoordD2(0.0, 0.0);
	}
	
	public static MutableCoordD2 create(final double x, final double y) {
		return new MutableCoordD2(x, y);
	}
	
	public static MutableCoordD2 create(final CoordD2 other) {
		return new MutableCoordD2(other.getX(), other.getY());
	}
	
	public static MutableCoordD2 create(final HasPosition2 hasPosition2) {
		return new MutableCoordD2(hasPosition2.getPosition().getX(), hasPosition2.getPosition().getY());
	}
	
	private double x;
	
	private double y;
	
	MutableCoordD2() {
		
	}
	
	MutableCoordD2(final double x, final double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public double getX() {
		return this.x;
	}
	
	@Override
	public void setX(final double x) {
		this.x = x;
	}
	
	@Override
	public double getY() {
		return this.y;
	}
	
	@Override
	public void setY(final double y) {
		this.y = y;
	}
	
	@Override
	public MutableCoordD2 setXY(final CoordD2 other) {
		this.x = other.getX();
		this.y = other.getY();
		
		return this;
	}
	
	@Override
	public MutableCoordD2 setXY(final double x, final double y) {
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	@Override
	public MutableCoordD2 add(final CoordD2 delta) {
		this.x += delta.getX();
		this.y += delta.getY();
		
		return this;
	}
	
	@Override
	public MutableCoordD2 add(final double dx, final double dy) {
		this.x += dx;
		this.y += dy;
		
		return this;
	}
	
	@Override
	public MutableCoordD2 add(final double n) {
		this.x += n;
		this.y += n;
		
		return this;
	}
	
	@Override
	public MutableCoordD2 substract(final CoordD2 delta) {
		this.x -= delta.getX();
		this.y -= delta.getY();
		
		return this;
	}
	
	@Override
	public MutableCoordD2 substract(final double dx, final double dy) {
		this.x -= dx;
		this.y -= dy;
		
		return this;
	}
	
	@Override
	public MutableCoordD2 substract(final double n) {
		this.x -= n;
		this.y -= n;
		
		return this;
	}
	
	@Override
	public MutableCoordD2 multiply(final CoordD2 delta) {
		this.x *= delta.getX();
		this.y *= delta.getY();
		
		return this;
	}
	
	@Override
	public MutableCoordD2 multiply(final double dx, final double dy) {
		this.x *= dx;
		this.y *= dy;
		
		return this;
	}
	
	@Override
	public CoordD2 multiply(final double n) {
		this.x *= n;
		this.y *= n;
		
		return this;
	}
	
	@Override
	public MutableCoordD2 divide(final CoordD2 delta) {
		if (delta.getX() != 0.0) {
			this.x /= delta.getX();
		}
		if (delta.getY() != 0.0) {
			this.y /= delta.getY();
		}
		
		return this;
	}
	
	@Override
	public MutableCoordD2 divide(final double dx, final double dy) {
		if (dx != 0.0) {
			this.x /= dx;
		}
		if (dy != 0.0) {
			this.y /= dy;
		}
		
		return this;
	}
	
	@Override
	public CoordD2 divide(final double n) {
		if (n != 0.0) {
			this.x /= n;
			this.y /= n;
		}
		
		return this;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.y);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || !(obj instanceof final CoordD2 other)) {
			return false;
		}
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.getX())) {
			return false;
		}
		if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.getY())) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "D2m[" + this.x + ", " + this.y + "]";
	}
	
}
