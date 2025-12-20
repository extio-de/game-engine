/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.model;

/**
 * Immutable implementation of CoordD2
 */
public final class ImmutableCoordD2 implements CoordD2 {
	
	private final static ImmutableCoordD2 COORD_ZERO = new ImmutableCoordD2(0, 0);
	
	private final static ImmutableCoordD2 COORD_ONE = new ImmutableCoordD2(1, 1);
	
	public static ImmutableCoordD2 zero() {
		return COORD_ZERO;
	}
	
	public static ImmutableCoordD2 one() {
		return COORD_ONE;
	}
	
	public static ImmutableCoordD2 create() {
		return COORD_ZERO;
	}
	
	public static ImmutableCoordD2 create(final CoordD2 other) {
		if (other == null) {
			return null;
		}
		if (other instanceof ImmutableCoordD2) {
			return (ImmutableCoordD2) other;
		}
		return new ImmutableCoordD2(other.getX(), other.getY());
	}
	
	public static ImmutableCoordD2 create(final double x, final double y) {
		return new ImmutableCoordD2(x, y);
	}
	
	public static ImmutableCoordD2 create(final HasPosition2 hasPosition2) {
		return new ImmutableCoordD2(hasPosition2.getPosition().getX(), hasPosition2.getPosition().getY());
	}
	
	private double x;
	
	private double y;
	
	ImmutableCoordD2() {
		
	}
	
	ImmutableCoordD2(final double x, final double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public double getX() {
		return this.x;
	}
	
	@Override
	public void setX(final double x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getY() {
		return this.y;
	}
	
	@Override
	public void setY(final double y) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ImmutableCoordD2 setXY(final CoordD2 other) {
		return create(other);
	}
	
	@Override
	public ImmutableCoordD2 setXY(final double x, final double y) {
		return create(x, y);
	}
	
	@Override
	public ImmutableCoordD2 add(final CoordD2 delta) {
		return create(this.x + delta.getX(), this.y + delta.getY());
	}
	
	@Override
	public ImmutableCoordD2 add(final double dx, final double dy) {
		return create(this.x + dx, this.y + dy);
	}
	
	@Override
	public ImmutableCoordD2 add(final double n) {
		return create(this.x + n, this.y + n);
	}
	
	@Override
	public ImmutableCoordD2 substract(final CoordD2 delta) {
		return create(this.x - delta.getX(), this.y - delta.getY());
	}
	
	@Override
	public ImmutableCoordD2 substract(final double dx, final double dy) {
		return create(this.x - dx, this.y - dy);
	}
	
	@Override
	public ImmutableCoordD2 substract(final double n) {
		return create(this.x - n, this.y - n);
	}
	
	@Override
	public ImmutableCoordD2 multiply(final CoordD2 delta) {
		return create(this.x * delta.getX(), this.y * delta.getY());
	}
	
	@Override
	public ImmutableCoordD2 multiply(final double dx, final double dy) {
		return create(this.x * dx, this.y * dy);
	}
	
	@Override
	public CoordD2 multiply(final double n) {
		return create(this.x * n, this.y * n);
	}
	
	@Override
	public ImmutableCoordD2 divide(final CoordD2 delta) {
		final double dx = delta.getX() != 0.0 ? delta.getX() : 1.0;
		final double dy = delta.getY() != 0.0 ? delta.getY() : 1.0;
		return create(this.x / dx, this.y / dy);
	}
	
	@Override
	public ImmutableCoordD2 divide(final double dx, final double dy) {
		final double dx_ = dx != 0.0 ? dx : 1.0;
		final double dy_ = dy != 0.0 ? dy : 1.0;
		return create(this.x / dx_, this.y / dy_);
	}
	
	@Override
	public CoordD2 divide(final double n) {
		final double n_ = n != 0.0 ? n : 1.0;
		return create(this.x / n_, this.y / n_);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || !(obj instanceof CoordD2)) {
			return false;
		}
		final CoordD2 other = (CoordD2) obj;
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
		return "D2i[" + this.x + ", " + this.y + "]";
	}
}
