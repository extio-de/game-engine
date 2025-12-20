package de.extio.game_engine.renderer.model;

import de.extio.game_engine.spatial2.model.HasPosition2;

/**
 * Rendering business objects implementing this interface have a dimension
 */
public interface RenderingBoHasDimension extends RenderingBo {
	
	public int getWidth();
	
	public int getHeight();
	
	RenderingBoHasDimension withDimensionAbsolute(HasPosition2 dim);
	
	RenderingBoHasDimension withDimensionAbsolute(int width, int height);
	
	/**
	 * @param width Range 0.0 (No width) to 1.0 (Viewport width)
	 * @param height Range 0.0 (No height) to 1.0 (Viewport height)
	 */
	RenderingBoHasDimension withDimensionPercentual(double width, double height);
	
	RenderingBoHasDimension withDimensionIncrementalAbsolute(HasPosition2 dim);
	
	RenderingBoHasDimension withDimensionIncrementalAbsolute(int width, int height);
	
	RenderingBoHasDimension withDimensionIncrementalPercentual(double width, double height);
}
