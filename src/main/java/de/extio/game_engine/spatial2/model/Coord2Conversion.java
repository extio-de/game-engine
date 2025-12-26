package de.extio.game_engine.spatial2.model;

import de.extio.game_engine.spatial2.SpatialUtils2;

/**
 * Helper to convert between mutable and immutable representations of tuples (coordinates and vectors)
 */
public interface Coord2Conversion {
	
	default CoordI2 toMutableCoordI2() {
		if (this instanceof final CoordI2 coordI2) {
			return MutableCoordI2.create(coordI2);
		}
		else {
			return MutableCoordI2.create((CoordD2) this);
		}
	}
	
	default CoordI2 toImmutableCoordI2() {
		if (this instanceof final CoordI2 coordI2) {
			return ImmutableCoordI2.create(coordI2);
		}
		else {
			return ImmutableCoordI2.create((CoordD2) this);
		}
	}
	
	default CoordD2 toMutableCoordD2() {
		if (this instanceof final CoordI2 coordI2) {
			return MutableCoordD2.create(coordI2);
		}
		else {
			return MutableCoordD2.create((CoordD2) this);
		}
	}
	
	default CoordD2 toImmutableCoordD2() {
		if (this instanceof final CoordI2 coordI2) {
			return ImmutableCoordD2.create(coordI2);
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
		
		if (this instanceof final ImmutableCoordI2 immutableCoordI2) {
			vNorm = ImmutableCoordD2.create(immutableCoordI2);
		}
		else if (this instanceof final MutableCoordI2 mutableCoordI2) {
			vNorm = MutableCoordD2.create(mutableCoordI2);
		}
		else {
			vNorm = (CoordD2) this;
		}
		
		final var length = SpatialUtils2.getDistance(vNorm);
		if (length > 0.0) {
			vNorm = vNorm.divide(length);
		}
		
		return vNorm;
	}
	
}
