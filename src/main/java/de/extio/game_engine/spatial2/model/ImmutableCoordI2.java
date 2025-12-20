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
 * Immutable implementation of CoordI2
 */
public final class ImmutableCoordI2 implements CoordI2 {
	
	private final static int COORD_CACHE_DIM = 100;
	
	private final static int COORD_CACHE_BOUND = 50;
	
	private final static int COORD_CACHE_SIZE = COORD_CACHE_DIM * COORD_CACHE_DIM;
	
	private final static ImmutableCoordI2[] COORD_CACHE = new ImmutableCoordI2[COORD_CACHE_SIZE];
	
	private volatile static ImmutableCoordI2 LAST_CACHED = new ImmutableCoordI2(0, 0);
	
	private final static ImmutableCoordI2 COORD_ZERO = new ImmutableCoordI2(0, 0);
	
	private final static ImmutableCoordI2 COORD_ONE = new ImmutableCoordI2(1, 1);
	
	static {
		int i = 0;
		for (int x = -COORD_CACHE_BOUND; x < COORD_CACHE_BOUND; x++) {
			for (int y = -COORD_CACHE_BOUND; y < COORD_CACHE_BOUND; y++) {
				COORD_CACHE[i++] = new ImmutableCoordI2(x, y);
			}
		}
	}
	
	public static ImmutableCoordI2 zero() {
		return COORD_ZERO;
	}
	
	public static ImmutableCoordI2 one() {
		return COORD_ONE;
	}
	
	public static ImmutableCoordI2 create() {
		return COORD_ZERO;
	}
	
	public static ImmutableCoordI2 create(final HasPosition2 other) {
		if (other == null) {
			return null;
		}
		if (other instanceof ImmutableCoordI2) {
			return (ImmutableCoordI2) other;
		}
		return ImmutableCoordI2.create(other.getPosition().getX(), other.getPosition().getY());
	}
	
	public static ImmutableCoordI2 create(final int x, final int y) {
		if (x >= -COORD_CACHE_BOUND && x < COORD_CACHE_BOUND && y >= -COORD_CACHE_BOUND && y < COORD_CACHE_BOUND) {
			final int idx = (x + COORD_CACHE_BOUND) * COORD_CACHE_DIM + (y + COORD_CACHE_BOUND);
			if (idx >= 0 && idx < COORD_CACHE_SIZE) {
				return COORD_CACHE[idx];
			}
		}
		
		ImmutableCoordI2 cached = LAST_CACHED;
		if (cached.getX() != x || cached.getY() != y) {
			cached = new ImmutableCoordI2(x, y);
			LAST_CACHED = cached;
		}
		return cached;
	}
	
	public static ImmutableCoordI2 create(final CoordD2 other) {
		return ImmutableCoordI2.create((int) other.getX(), (int) other.getY());
	}
	
	private int x;
	
	private int y;
	
	ImmutableCoordI2() {
		
	}
	
	ImmutableCoordI2(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int getX() {
		return this.x;
	}
	
	@Override
	public void setX(final int x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getY() {
		return this.y;
	}
	
	@Override
	public void setY(final int y) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ImmutableCoordI2 setXY(final HasPosition2 other) {
		return create(other);
	}
	
	@Override
	public ImmutableCoordI2 setXY(final int x, final int y) {
		return create(x, y);
	}
	
	@Override
	public ImmutableCoordI2 add(final HasPosition2 delta) {
		return create(this.x + delta.getPosition().getX(), this.y + delta.getPosition().getY());
	}
	
	@Override
	public ImmutableCoordI2 add(final int dx, final int dy) {
		return create(this.x + dx, this.y + dy);
	}
	
	@Override
	public ImmutableCoordI2 add(final int n) {
		return create(this.x + n, this.y + n);
	}
	
	@Override
	public ImmutableCoordI2 substract(final HasPosition2 delta) {
		return create(this.x - delta.getPosition().getX(), this.y - delta.getPosition().getY());
	}
	
	@Override
	public ImmutableCoordI2 substract(final int dx, final int dy) {
		return create(this.x - dx, this.y - dy);
	}
	
	@Override
	public ImmutableCoordI2 substract(final int n) {
		return create(this.x - n, this.y - n);
	}
	
	@Override
	public ImmutableCoordI2 multiply(final HasPosition2 delta) {
		return create(this.x * delta.getPosition().getX(), this.y * delta.getPosition().getY());
	}
	
	@Override
	public ImmutableCoordI2 multiply(final int dx, final int dy) {
		return create(this.x * dx, this.y * dy);
	}
	
	@Override
	public ImmutableCoordI2 multiply(final int n) {
		return create(this.x * n, this.y * n);
	}
	
	@Override
	public ImmutableCoordI2 divide(final HasPosition2 delta) {
		final int dx = delta.getPosition().getX() != 0 ? delta.getPosition().getX() : 1;
		final int dy = delta.getPosition().getY() != 0 ? delta.getPosition().getY() : 1;
		return create(this.x / dx, this.y / dy);
	}
	
	@Override
	public ImmutableCoordI2 divide(final int dx, final int dy) {
		final int dx_ = dx != 0 ? dx : 1;
		final int dy_ = dy != 0 ? dy : 1;
		return create(this.x / dx_, this.y / dy_);
	}
	
	@Override
	public ImmutableCoordI2 divide(final int n) {
		final int n_ = n != 0 ? n : 1;
		return create(this.x / n_, this.y / n_);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || !(obj instanceof CoordI2)) {
			return false;
		}
		final CoordI2 other = (CoordI2) obj;
		if (this.x != other.getX()) {
			return false;
		}
		if (this.y != other.getY()) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "I2i[" + this.x + ", " + this.y + "]";
	}
}
