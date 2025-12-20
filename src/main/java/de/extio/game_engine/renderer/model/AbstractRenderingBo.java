package de.extio.game_engine.renderer.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.spatial2.model.HasPosition2;

public abstract class AbstractRenderingBo implements RenderingBo {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractRenderingBo.class);
	
	protected RendererData rendererData;
	
	protected RgbaColor color;
	
	protected int x;
	
	protected int y;
	
	protected int localX;
	
	protected int localY;
	
	protected int width;
	
	protected int height;
	
	protected RenderingBoLayer layer;
	
	private final RenderingBoLayer defaultLayer;
	
	public AbstractRenderingBo(final RenderingBoLayer layer) {
		this.layer = layer;
		this.defaultLayer = layer;
	}
	
	@Override
	public RenderingBo withPositionRelative(final int x, final int y) {
		this.localX = x;
		this.localY = y;
		return this;
	}
	
	@Override
	public RenderingBo withPositionRelative(final HasPosition2 position) {
		if (position.getPosition() != null) {
			this.localX = position.getPosition().getX();
			this.localY = position.getPosition().getY();
		}
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorTopLeft(final int x, final int y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorTopLeft(final HasPosition2 position) {
		if (position.getPosition() != null) {
			this.withPositionAbsoluteAnchorTopLeft(position.getPosition().getX(), position.getPosition().getY());
		}
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorTopRight(final int x, final int y) {
		this.x = this.rendererData.getRendererControl().getEffectiveViewportDimension().getX() - x;
		this.y = y;
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorTopRight(final HasPosition2 position) {
		if (position.getPosition() != null) {
			this.withPositionAbsoluteAnchorTopRight(position.getPosition().getX(), position.getPosition().getY());
		}
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorBottomLeft(final int x, final int y) {
		this.x = x;
		this.y = this.rendererData.getRendererControl().getEffectiveViewportDimension().getY() - y;
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorBottomLeft(final HasPosition2 position) {
		if (position.getPosition() != null) {
			this.withPositionAbsoluteAnchorBottomLeft(position.getPosition().getX(), position.getPosition().getY());
		}
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorBottomRight(final int x, final int y) {
		this.x = this.rendererData.getRendererControl().getEffectiveViewportDimension().getX() - x;
		this.y = this.rendererData.getRendererControl().getEffectiveViewportDimension().getY() - y;
		return this;
	}
	
	@Override
	public RenderingBo withPositionAbsoluteAnchorBottomRight(final HasPosition2 position) {
		if (position.getPosition() != null) {
			this.withPositionAbsoluteAnchorBottomRight(position.getPosition().getX(), position.getPosition().getY());
		}
		return this;
	}
	
	@Override
	public RenderingBo withPositionIncrementalAbsolute(final int x, final int y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	@Override
	public RenderingBo withPositionIncrementalAbsolute(final HasPosition2 position) {
		if (position.getPosition() != null) {
			this.withPositionIncrementalAbsolute(position.getPosition().getX(), position.getPosition().getY());
		}
		return this;
	}
	
	@Override
	public RenderingBo withPositionIncrementalPercentual(final double x, final double y) {
		this.x += (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getX() * Math.min(1.0, Math.max(0.0, x)));
		this.y += (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getY() * Math.min(1.0, Math.max(0.0, y)));
		return this;
	}
	
	/**
	 * @param x Range 0.0 (left) to 1.0 (right)
	 * @param y Range 0.0 (top) to 1.0 (bottom)
	 */
	@Override
	public RenderingBo withPositionPercentual(final double x, final double y) {
		this.x = (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getX() * Math.min(1.0, Math.max(0.0, x)));
		this.y = (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getY() * Math.min(1.0, Math.max(0.0, y)));
		return this;
	}
	
	public RenderingBoHasDimension withDimensionAbsolute(final HasPosition2 dim) {
		return this.withDimensionAbsolute(dim.getPosition().getX(), dim.getPosition().getY());
	}
	
	public RenderingBoHasDimension withDimensionAbsolute(final int width, final int height) {
		if (!(this instanceof RenderingBoHasDimension)) {
			throw new IllegalArgumentException("RenderingBo is not instanceof RenderingBoHasDimension");
		}
		
		this.width = width;
		this.height = height;
		
		return (RenderingBoHasDimension) this;
	}
	
	/**
	 * @param width Range 0.0 (left) to 1.0 (right)
	 * @param height Range 0.0 (top) to 1.0 (bottom)
	 */
	public RenderingBoHasDimension withDimensionPercentual(final double width, final double height) {
		if (!(this instanceof RenderingBoHasDimension)) {
			throw new IllegalArgumentException("RenderingBo is not instanceof RenderingBoHasDimension");
		}
		
		this.width = (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getX() * Math.min(1.0, Math.max(0.0, width)));
		this.height = (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getY() * Math.min(1.0, Math.max(0.0, height)));
		
		return (RenderingBoHasDimension) this;
	}
	
	public RenderingBoHasDimension withDimensionIncrementalAbsolute(final HasPosition2 dim) {
		return this.withDimensionIncrementalAbsolute(dim.getPosition().getX(), dim.getPosition().getY());
	}
	
	public RenderingBoHasDimension withDimensionIncrementalAbsolute(final int width, final int height) {
		if (!(this instanceof RenderingBoHasDimension)) {
			throw new IllegalArgumentException("RenderingBo is not instanceof RenderingBoHasDimension");
		}
		
		this.width += width;
		this.height += height;
		
		return (RenderingBoHasDimension) this;
	}
	
	public RenderingBoHasDimension withDimensionIncrementalPercentual(final double width, final double height) {
		if (!(this instanceof RenderingBoHasDimension)) {
			throw new IllegalArgumentException("RenderingBo is not instanceof RenderingBoHasDimension");
		}
		
		this.width += (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getX() * Math.min(1.0, Math.max(0.0, width)));
		this.height += (int) (this.rendererData.getRendererControl().getEffectiveViewportDimension().getY() * Math.min(1.0, Math.max(0.0, height)));
		
		return (RenderingBoHasDimension) this;
	}
	
	@Override
	public void close() throws Exception {
		this.color = null;
		this.x = 0;
		this.y = 0;
		this.localX = 0;
		this.localY = 0;
		this.width = 0;
		this.height = 0;
		this.layer = this.defaultLayer;
	}
	
	@Override
	public void setRendererData(final RendererData RendererData) {
		this.rendererData = (RendererData) RendererData;
	}
	
	@Override
	public RenderingBo setColor(final RgbaColor color) {
		this.color = color;
		return this;
	}
	
	protected RenderingBo setX(final int x) {
		this.x = x;
		return this;
	}
	
	@Override
	public int getX() {
		return this.x;
	}
	
	protected RenderingBo setY(final int y) {
		this.y = y;
		return this;
	}
	
	@Override
	public int getY() {
		return this.y;
	}
	
	@Override
	public int getLocalX() {
		return this.localX;
	}
	
	@Override
	public int getLocalY() {
		return this.localY;
	}
	
	@Override
	public RenderingBoLayer getLayer() {
		return this.layer;
	}
	
	@Override
	public RenderingBo setLayer(final RenderingBoLayer layer) {
		if (layer != null) {
			this.layer = layer;
		}
		return this;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void setWidth(final int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void setHeight(final int height) {
		this.height = height;
	}
	
}
