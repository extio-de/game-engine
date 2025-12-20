package de.extio.game_engine.spatial2.model;

/**
 * Objects that can be indexed in SpatialIndex2D implementations
 */
public interface SpatialIndex2Capable extends HasPositionAndDimension2 {
	
	boolean isInSpatialIndex2ResultSet(long session);
	
	void includeInSpatialIndex2ResultSet(long session);
	
}
