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
 * A floating point number tuple (can represent a point, a coordinate or a vector in 2D space).
 */
public interface CoordD2 extends Comparable<CoordD2>, Coord2Conversion {
	
	double getX();
	
	void setX(final double x);
	
	double getY();
	
	void setY(final double y);
	
	CoordD2 setXY(final CoordD2 other);
	
	CoordD2 setXY(final double x, final double y);
	
	CoordD2 add(final CoordD2 delta);
	
	CoordD2 add(final double dx, final double dy);
	
	CoordD2 add(double n);
	
	CoordD2 substract(final CoordD2 delta);
	
	CoordD2 substract(final double dx, final double dy);
	
	CoordD2 substract(double n);
	
	CoordD2 multiply(final CoordD2 delta);
	
	CoordD2 multiply(final double dx, final double dy);
	
	CoordD2 multiply(final double n);
	
	CoordD2 divide(final CoordD2 delta);
	
	CoordD2 divide(final double dx, final double dy);
	
	CoordD2 divide(final double n);
	
	@Override
	default int compareTo(final CoordD2 other) {
		return this.getY() == other.getY() ? Double.compare(this.getX(), other.getX()) : Double.compare(this.getY(), other.getY());
	}
	
}
