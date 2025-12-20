/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.model;

import de.extio.game_engine.spatial2.WorldUtils2;

/**
 * Helper to convert between mutable and immutable representations of tuples (coordinates and vectors)
 */
public interface Coord2Conversion {
	
	default CoordI2 toMutableCoordI2() {
		if (this instanceof CoordI2) {
			return MutableCoordI2.create((CoordI2) this);
		}
		else {
			return MutableCoordI2.create((CoordD2) this);
		}
	}
	
	default CoordI2 toImmutableCoordI2() {
		if (this instanceof CoordI2) {
			return ImmutableCoordI2.create((CoordI2) this);
		}
		else {
			return ImmutableCoordI2.create((CoordD2) this);
		}
	}
	
	default CoordD2 toMutableCoordD2() {
		if (this instanceof CoordI2) {
			return MutableCoordD2.create((CoordI2) this);
		}
		else {
			return MutableCoordD2.create((CoordD2) this);
		}
	}
	
	default CoordD2 toImmutableCoordD2() {
		if (this instanceof CoordI2) {
			return ImmutableCoordD2.create((CoordI2) this);
		}
		else {
			return ImmutableCoordD2.create((CoordD2) this);
		}
	}
	
	/**
	 * Converts a vector to a unit normal vector
	 */
	default CoordD2 toVNorm() {
		CoordD2 vNorm;
		
		if (this instanceof ImmutableCoordI2) {
			vNorm = ImmutableCoordD2.create((ImmutableCoordI2) this);
		}
		else if (this instanceof MutableCoordI2) {
			vNorm = MutableCoordD2.create((MutableCoordI2) this);
		}
		else {
			vNorm = (CoordD2) this;
		}
		
		final double length = WorldUtils2.getDistance(vNorm);
		if (length > 0.0) {
			vNorm = vNorm.divide(length);
		}
		
		return vNorm;
	}
	
}
