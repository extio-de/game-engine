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
 * An integer tuple (can represent a point, a coordinate or a vector in 2D space).
 */
public interface CoordI2 extends HasPosition2, Comparable<CoordI2>, Coord2Conversion {
	
	int getX();
	
	void setX(final int x);
	
	int getY();
	
	void setY(final int y);
	
	CoordI2 setXY(final HasPosition2 other);
	
	CoordI2 setXY(final int x, final int y);
	
	CoordI2 add(final HasPosition2 delta);
	
	CoordI2 add(final int dx, final int dy);
	
	CoordI2 add(final int n);
	
	CoordI2 substract(final HasPosition2 delta);
	
	CoordI2 substract(final int dx, final int dy);
	
	CoordI2 substract(final int n);
	
	CoordI2 multiply(final HasPosition2 delta);
	
	CoordI2 multiply(final int dx, final int dy);
	
	CoordI2 multiply(final int n);
	
	CoordI2 divide(final HasPosition2 delta);
	
	CoordI2 divide(final int dx, final int dy);
	
	CoordI2 divide(final int n);
	
	@Override
	default CoordI2 getPosition() {
		return this;
	}
	
	@Override
	default int compareTo(final CoordI2 other) {
		return this.getY() == other.getY() ? Integer.compare(this.getX(), other.getX()) : Integer.compare(this.getY(), other.getY());
	}
	
}
