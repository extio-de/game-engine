package de.extio.game_engine.spatial2.model;

/**
 * Interface for objects occupying 2D area, having a position (top left corner) and a dimension
 */
public interface HasPositionAndDimension2 extends HasPosition2 {
	
	CoordI2 getDimension();
	
}
