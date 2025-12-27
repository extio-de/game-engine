package de.extio.game_engine.renderer.model;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.spatial2.model.HasPosition2;

/**
 * RenderingBo is the basic contract for all business objects (bo) implementing building blocks for rendering.
 * For rendering stuff, usually you are acquiring an instance of an implementation of this interface via RenderingBoPool, set all properties and add it to <i>List<RenderingBo> renderingBo</i> which is passed to various callback methods that are part of the rendering pipeline. 
 * It is recommended to view my technical presentation on youtube about the game to learn more about how rendering works. 
 */
public interface RenderingBo extends AutoCloseable {

	void setId(String id);

	String getId();

	/**
	 * Sets a color you want to use for rendering. It depends on the rendering bo how this information is treated. 
	 */
	RenderingBo setColor(RgbaColor color);
	
	int getX();
	
	int getY();
	
	int getLocalX();
	
	int getLocalY();
	
	/**
	 * Sets the layer to render this bo on. Business objects are rendered layered on top of each other in the natural order of RenderingBoLayer enum fields. Objects on the same layer are rendered in the order of being added to <i>List<RenderingBo> renderingBo</i>.
	 */
	RenderingBo setLayer(short layer);
	
	short getLayer();
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the top left. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionAbsoluteAnchorTopLeft(int x, int y);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the top left. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport. 
	 */
	RenderingBo withPositionAbsoluteAnchorTopLeft(HasPosition2 position);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the top right. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport. 
	 */
	RenderingBo withPositionAbsoluteAnchorTopRight(int x, int y);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the top right. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionAbsoluteAnchorTopRight(HasPosition2 position);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the bottom left. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionAbsoluteAnchorBottomLeft(int x, int y);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the bottom left. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionAbsoluteAnchorBottomLeft(HasPosition2 position);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the bottom right. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionAbsoluteAnchorBottomRight(int x, int y);
	
	/**
	 * Positions this bo with an absolute position on the viewport. x0 y0 coordinates are at the bottom right. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionAbsoluteAnchorBottomRight(HasPosition2 position);
	
	/**
	 * Positions this bo with a percentual position on the viewport.
	 * @param x Range 0.0 (left) to 1.0 (right)
	 * @param y Range 0.0 (top) to 1.0 (bottom)
	 */
	RenderingBo withPositionPercentual(double x, double y);
	
	/**
	 * Positions this bo with an incremental position relative to the position the bo is already at. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionIncrementalAbsolute(int x, int y);
	
	/**
	 * Positions this bo with an incremental position relative to the position the bo is already at. Call RendererControl#getEffectiveViewportDimension to get the absolute dimension of the viewport.
	 */
	RenderingBo withPositionIncrementalAbsolute(HasPosition2 position);
	
	/**
	 * Positions this bo with a percenutal position relative to the position the bo is already at.
	 */
	RenderingBo withPositionIncrementalPercentual(double x, double y);
	
	/**
	 * Positions this bo relative to it's parent. This can be for example the game world for positioning tiles or a composite entity. Unit is usually not pixels but the position in the tile raster.
	 */
	RenderingBo withPositionRelative(int x, int y);
	
	/**
	 * Positions this bo relative to it's parent. This can be for example game world for positioning tiles or a composite entity. Unit is usually not pixels but the position in the tile raster.
	 */
	RenderingBo withPositionRelative(HasPosition2 position);

	void apply(RenderingBo other);

	void staticCleanupAfterFrame();

	void setRendererData(RendererData RendererData);

}
