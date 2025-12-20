package de.extio.game_engine.renderer.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.RenderingBoPool;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.RgbaColor;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawWindowRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowCloseButtonControl;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.spatial2.WorldUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordD2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.HasPositionAndDimension2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordD2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

/**
 * Helper to position, draw and manage virtual windows in the UI.
 */
public class Window implements HasPositionAndDimension2, AutoCloseable {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(Window.class);
	
	protected final static List<Window> WINDOWS = Collections.synchronizedList(new ArrayList<>());
	
	protected CoordD2 relativeScreenPosition = MutableCoordD2.create();
	
	protected final String name;

	private final RenderingBoPool renderingBoPool;
	
	private final RendererControl rendererControl;
	
	private Area2 area;
	
	private CoordI2 dragCoordPrev;
	
	private boolean dragging;
	
	private boolean draggable;
	
	private boolean drawCloseButton;
	
	private RenderingBoLayer renderingBoLayer;
	
	private CoordI2 lastEffectiveViewportDimension;
	
	private RgbaColor color;
	
	private long lastFrameDisplayed;
	
	public Window(final String name, final boolean draggable, final RenderingBoPool renderingBoPool, final RendererControl rendererControl) {
		this.name = name;
		this.draggable = draggable;
		this.renderingBoPool = renderingBoPool;
		this.rendererControl = rendererControl;
		
		WINDOWS.add(this);
		this.reset();
	}
	
	public static boolean intersectsAny(final HasPositionAndDimension2 area) {
		// final long frame = EngineFacade.instance().getFrame();
		// synchronized (WINDOWS) {
		// 	for (final Window window : WINDOWS) {
		// 		if (window.lastFrameDisplayed >= frame - 1 && WorldUtils.intersects(window, area)) {
		// 			return true;
		// 		}
		// 	}
		// }
		// return false;
		synchronized (WINDOWS) {
			for (final Window window : WINDOWS) {
				if (WorldUtils2.intersects(window, area)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public void reset() {
		this.area = new Area2();
		this.area.setPosition(MutableCoordI2.create());
		this.area.setDimension(MutableCoordI2.create());
		this.releaseDragging();
		this.updateRelativeScreenPosition();
	}
	
	public void releaseDragging() {
		this.dragCoordPrev = null;
		this.dragging = false;
	}
	
	public boolean drag(final CoordI2 coord) {
		if (!this.draggable) {
			return false;
		}
		
		try {
			if (this.dragCoordPrev != null) {
				if (!this.dragging && this.intersects(coord)) {
					synchronized (WINDOWS) {
						for (final Window window : WINDOWS) {
							if (window.isDragging()) {
								return false;
							}
						}
					}
					
					this.dragging = true;
				}
				if (this.dragging) {
					this.move(MutableCoordI2.create(coord).substract(this.dragCoordPrev));
					return true;
				}
			}
		}
		finally {
			this.dragCoordPrev = coord;
		}
		
		return this.intersects(coord);
	}
	
	public void move(final CoordI2 delta) {
		this.area.getPosition().add(delta);
		this.ensureBounds();
		this.updateRelativeScreenPosition();
	}
	
	public void updateArea() {
		this.applyRelativeScreenPosition();
		this.ensureBounds();
	}
	
	public void resizeCentered(final CoordI2 dimension) {
		if (ImmutableCoordI2.zero().equals(this.area.getDimension())) {
			this.area.getDimension().setXY(dimension);
			this.updateRelativeScreenPosition();
			return;
		}
		
		final CoordI2 delta = MutableCoordI2.create(this.area.getDimension()).substract(dimension);
		if (ImmutableCoordI2.zero().equals(delta)) {
			return;
		}
		
		this.area.getDimension().setXY(dimension);
		this.move(MutableCoordI2.create(delta.getX() / 2, delta.getY() / 2));
		this.updateRelativeScreenPosition();
	}
	
	public boolean intersects(final CoordI2 coord) {
		return WorldUtils2.intersects(this.area.getPosition(), this.area.getDimension(), coord, ImmutableCoordI2.one());
	}
	
	
	public void render(final List<RenderingBo> renderingBo, final BiConsumer<List<RenderingBo>, RenderingBo> consumer) {
		final CoordI2 currentEffectiveViewportDimension = rendererControl.getEffectiveViewportDimension();
		if (!currentEffectiveViewportDimension.equals(this.lastEffectiveViewportDimension)) {
			this.lastEffectiveViewportDimension = currentEffectiveViewportDimension;
			this.updateArea();
		}
		
		RenderingBo bo = renderingBoPool.acquire(DrawWindowRenderingBo.class)
				.setThickBorder(this.draggable)
				.withDimensionAbsolute(this.area.getDimension())
				.withPositionAbsoluteAnchorTopLeft(this.area.getPosition())
				.setColor(this.color != null ? this.color : null);
		if (this.renderingBoLayer != null) {
			bo.setLayer(this.renderingBoLayer);
		}
		consumer.accept(renderingBo, bo);
		
		if (this.drawCloseButton) {
			bo = renderingBoPool.acquire(ControlRenderingBo.class)
					.setType(WindowCloseButtonControl.class)
					.setId(this.name + "_Close")
					.setCustomData(this.draggable)
					.setEnabled(true)
					.setVisible(true)
					.withDimensionAbsolute(this.area.getDimension())
					.withPositionAbsoluteAnchorTopLeft(this.area.getPosition())
					.setColor(this.color != null ? this.color : null);
			if (this.renderingBoLayer != null) {
				bo.setLayer(RenderingBoLayer.values()[Math.min(this.renderingBoLayer.ordinal() + 2, RenderingBoLayer.values().length - 1)]);
			}
			else {
				bo.setLayer(RenderingBoLayer.UI0_1);
			}
			consumer.accept(renderingBo, bo);
		}
		
		this.lastFrameDisplayed = this.rendererControl.getFrame();
	}
	
	@Override
	public void close() {
		this.releaseDragging();
		WINDOWS.remove(this);
	}
	
	protected void ensureBounds() {
		final CoordI2 effectiveDim = this.rendererControl.getEffectiveViewportDimension();
		boolean updated = false;
		
		if (this.area.getPosition().getX() < 0) {
			this.area.getPosition().setX(0);
			updated = true;
		}
		if (this.area.getPosition().getY() < 0) {
			this.area.getPosition().setY(0);
			updated = true;
		}
		if (this.area.getDimension().getX() > effectiveDim.getX()) {
			this.area.getDimension().setX(effectiveDim.getX());
			updated = true;
		}
		if (this.area.getDimension().getY() > effectiveDim.getY()) {
			this.area.getDimension().setY(effectiveDim.getY());
			updated = true;
		}
		if (this.area.getPosition().getX() + this.area.getDimension().getX() > effectiveDim.getX()) {
			this.area.getPosition().setX(effectiveDim.getX() - this.area.getDimension().getX());
			updated = true;
		}
		if (this.area.getPosition().getY() + this.area.getDimension().getY() > effectiveDim.getY()) {
			this.area.getPosition().setY(effectiveDim.getY() - this.area.getDimension().getY());
			updated = true;
		}
		
		if (updated) {
			this.updateRelativeScreenPosition();
		}
	}
	
	protected void updateRelativeScreenPosition() {
		final CoordI2 currentEffectiveViewportDimension = this.rendererControl.getEffectiveViewportDimension();
		this.relativeScreenPosition.setX((double) this.area.getPosition().getX() / (double) currentEffectiveViewportDimension.getX());
		this.relativeScreenPosition.setY((double) this.area.getPosition().getY() / (double) currentEffectiveViewportDimension.getY());
	}
	
	protected void applyRelativeScreenPosition() {
		final CoordI2 currentEffectiveViewportDimension = this.rendererControl.getEffectiveViewportDimension();
		this.area.getPosition().setX((int) (this.relativeScreenPosition.getX() * (double) currentEffectiveViewportDimension.getX()));
		this.area.getPosition().setY((int) (this.relativeScreenPosition.getY() * (double) currentEffectiveViewportDimension.getY()));
		this.ensureBounds();
	}
	
	public int getX() {
		return this.area.getPosition().getX();
	}
	
	public void setX(final int x) {
		this.area.getPosition().setX(x);
		this.ensureBounds();
		this.updateRelativeScreenPosition();
	}
	
	public int getY() {
		return this.area.getPosition().getY();
	}
	
	public void setY(final int y) {
		this.area.getPosition().setY(y);
		this.ensureBounds();
		this.updateRelativeScreenPosition();
	}
	
	public int getWidth() {
		return this.area.getDimension().getX();
	}
	
	public void setWidth(final int width) {
		this.area.getDimension().setX(width);
		this.ensureBounds();
	}
	
	public int getHeight() {
		return this.area.getDimension().getY();
	}
	
	public void setHeight(final int height) {
		this.area.getDimension().setY(height);
		this.ensureBounds();
	}
	
	@Override
	public CoordI2 getPosition() {
		return this.area.getPosition();
	}
	
	public void setPosition(final CoordI2 position) {
		this.area.getPosition().setXY(position);
		this.ensureBounds();
		this.updateRelativeScreenPosition();
	}
	
	@Override
	public CoordI2 getDimension() {
		return ImmutableCoordI2.create(this.area.getDimension());
	}
	
	public void setDimension(final CoordI2 dimension) {
		this.area.getDimension().setXY(dimension);
		this.ensureBounds();
	}
	
	public Area2 getArea() {
		return new Area2(ImmutableCoordI2.create(this.area.getPosition()), ImmutableCoordI2.create(this.area.getDimension()));
	}
	
	public boolean isDragging() {
		return this.dragging;
	}
	
	public boolean isDraggable() {
		return this.draggable;
	}
	
	public void setDraggable(final boolean draggable) {
		this.draggable = draggable;
	}
	
	public RenderingBoLayer getRenderingBoLayer() {
		return this.renderingBoLayer;
	}
	
	public void setRenderingBoLayer(final RenderingBoLayer renderingBoLayer) {
		this.renderingBoLayer = renderingBoLayer;
	}
	
	public RgbaColor getColor() {
		return this.color;
	}
	
	public void setColor(final RgbaColor color) {
		this.color = color;
	}
	
	public boolean isDrawCloseButton() {
		return this.drawCloseButton;
	}
	
	public void setDrawCloseButton(final boolean drawCloseButton) {
		this.drawCloseButton = drawCloseButton;
	}
}
