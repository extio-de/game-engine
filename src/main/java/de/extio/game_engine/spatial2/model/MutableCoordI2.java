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
public final class MutableCoordI2 implements CoordI2 {
	
	public static MutableCoordI2 create() {
		return new MutableCoordI2(0, 0);
	}
	
	public static MutableCoordI2 create(final int x, final int y) {
		return new MutableCoordI2(x, y);
	}
	
	public static MutableCoordI2 create(final HasPosition2 other) {
		return new MutableCoordI2(other.getPosition().getX(), other.getPosition().getY());
	}
	
	public static MutableCoordI2 create(final CoordD2 other) {
		return new MutableCoordI2((int) other.getX(), (int) other.getY());
	}
	
	private int x;
	
	private int y;
	
	MutableCoordI2() {
		
	}
	
	MutableCoordI2(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int getX() {
		return this.x;
	}
	
	@Override
	public void setX(final int x) {
		this.x = x;
	}
	
	@Override
	public int getY() {
		return this.y;
	}
	
	@Override
	public void setY(final int y) {
		this.y = y;
	}
	
	@Override
	public MutableCoordI2 setXY(final HasPosition2 other) {
		this.x = other.getPosition().getX();
		this.y = other.getPosition().getY();
		
		return this;
	}
	
	@Override
	public MutableCoordI2 setXY(final int x, final int y) {
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 add(final HasPosition2 delta) {
		this.x += delta.getPosition().getX();
		this.y += delta.getPosition().getY();
		
		return this;
	}
	
	@Override
	public MutableCoordI2 add(final int dx, final int dy) {
		this.x += dx;
		this.y += dy;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 add(final int n) {
		this.x += n;
		this.y += n;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 substract(final HasPosition2 delta) {
		this.x -= delta.getPosition().getX();
		this.y -= delta.getPosition().getY();
		
		return this;
	}
	
	@Override
	public MutableCoordI2 substract(final int dx, final int dy) {
		this.x -= dx;
		this.y -= dy;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 substract(final int n) {
		this.x -= n;
		this.y -= n;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 multiply(final HasPosition2 delta) {
		this.x *= delta.getPosition().getX();
		this.y *= delta.getPosition().getY();
		
		return this;
	}
	
	@Override
	public MutableCoordI2 multiply(final int dx, final int dy) {
		this.x *= dx;
		this.y *= dy;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 multiply(final int n) {
		this.x *= n;
		this.y *= n;
		
		return this;
	}
	
	@Override
	public MutableCoordI2 divide(final HasPosition2 delta) {
		if (delta.getPosition().getX() != 0) {
			this.x /= delta.getPosition().getX();
		}
		if (delta.getPosition().getY() != 0) {
			this.y /= delta.getPosition().getY();
		}
		
		return this;
	}
	
	@Override
	public MutableCoordI2 divide(final int dx, final int dy) {
		if (dx != 0) {
			this.x /= dx;
		}
		if (dy != 0) {
			this.y /= dy;
		}
		
		return this;
	}
	
	@Override
	public MutableCoordI2 divide(final int n) {
		if (n != 0) {
			this.x /= n;
			this.y /= n;
		}
		
		return this;
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
		return "I2m[" + this.x + ", " + this.y + "]";
	}
	
}
