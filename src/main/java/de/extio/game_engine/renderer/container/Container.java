package de.extio.game_engine.renderer.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.bo.VerticalAlignment;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.MouseMoveEvent;
import de.extio.game_engine.renderer.model.event.ViewportResizeEvent;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

@Component
@Scope("prototype")
public class Container extends AbstractClientModule implements InitializingBean {
	
	protected final static List<Container> DISPLAYED_CONTAINERS = Collections.synchronizedList(new ArrayList<>());
	
	protected final ModuleService moduleService;
	
	protected final RenderingBoPool renderingBoPool;
	
	protected final RendererControl rendererControl;
	
	protected final RendererWorkingSet rendererWorkingSet;
	
	protected final EventService eventService;
	
	protected final Area2 area = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	protected final Area2 normalizedArea = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	protected boolean draggable;
	
	protected boolean dragging;
	
	protected CoordI2 dragPrevPosition;
	
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
	
	protected VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
	
	public Container(final ModuleService moduleService, final RenderingBoPool renderingBoPool, final RendererControl rendererControl, final EventService eventService, final RendererWorkingSet rendererWorkingSet) {
		this.moduleService = moduleService;
		this.renderingBoPool = renderingBoPool;
		this.rendererControl = rendererControl;
		this.eventService = eventService;
		this.rendererWorkingSet = rendererWorkingSet;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.moduleService.loadModule(this);
	}
	
	@Override
	public void onActivate() {
		this.eventService.register(ViewportResizeEvent.class, this.id, this::onViewportResize);
		this.eventService.register(MouseMoveEvent.class, this.id, this::onMouseMoveEvent);
		this.eventService.register(MouseClickEvent.class, this.id, this::onMouseClickEvent);
		this.onViewportResize(null);
	}
	
	@Override
	public void onShow() {
		DISPLAYED_CONTAINERS.add(this);
		this.draw();
	}
	
	@Override
	public void onHide() {
		DISPLAYED_CONTAINERS.remove(this);
	}
	
	protected void onViewportResize(final ViewportResizeEvent event) {
		this.setNormalizedPosition(this.normalizedArea.getPosition());
		this.setNormalizedDimension(this.normalizedArea.getDimension());
		this.adjustIfOutsideViewport();
		this.draw();
	}
	
	private boolean adjustIfOutsideViewport() {
		final var viewportDimension = this.rendererControl.getEffectiveViewportDimension();
		final var position = this.area.getPosition();
		final var dimension = this.area.getDimension();
		
		final var containerArea = this.area.getDimension().getX() * this.area.getDimension().getY();
		final var intersectionArea = this.calculateIntersectionArea(position, dimension, viewportDimension);
		final var intersectionPercentage = containerArea > 0 ? (double) intersectionArea / containerArea : 0.0;
		
		if (intersectionPercentage >= 0.1) {
			return false;
		}
		
		int adjustedX = position.getX();
		int adjustedY = position.getY();
		
		if (position.getX() >= viewportDimension.getX()) {
			adjustedX = viewportDimension.getX() - dimension.getX();
		}
		else if (position.getX() + dimension.getX() <= 0) {
			adjustedX = 0;
		}
		else if (position.getX() < 0) {
			adjustedX = 0;
		}
		else if (position.getX() + dimension.getX() > viewportDimension.getX()) {
			adjustedX = viewportDimension.getX() - dimension.getX();
		}
		
		if (position.getY() >= viewportDimension.getY()) {
			adjustedY = viewportDimension.getY() - dimension.getY();
		}
		else if (position.getY() + dimension.getY() <= 0) {
			adjustedY = 0;
		}
		else if (position.getY() < 0) {
			adjustedY = 0;
		}
		else if (position.getY() + dimension.getY() > viewportDimension.getY()) {
			adjustedY = viewportDimension.getY() - dimension.getY();
		}
		
		this.normalizedArea.getPosition().setXY(adjustedX, adjustedY);
		this.area.getPosition().setXY(adjustedX, adjustedY);
		
		this.LOGGER.debug("Adjusted container {} position to {},{} to remain visible in viewport", this.id, adjustedX, adjustedY);
		return true;
	}
	
	private int calculateIntersectionArea(final CoordI2 position, final CoordI2 dimension, final CoordI2 viewportDimension) {
		final int x1 = Math.max(0, position.getX());
		final int y1 = Math.max(0, position.getY());
		final int x2 = Math.min(viewportDimension.getX(), position.getX() + dimension.getX());
		final int y2 = Math.min(viewportDimension.getY(), position.getY() + dimension.getY());
		
		final int width = Math.max(0, x2 - x1);
		final int height = Math.max(0, y2 - y1);
		
		return width * height;
	}
	
	protected void onMouseMoveEvent(final MouseMoveEvent event) {
		if (this.draggable && event.isDrag()) {
			Container foregroundContainer = null;
			synchronized (DISPLAYED_CONTAINERS) {
				for (final var container : DISPLAYED_CONTAINERS) {
					if (container.dragging) {
						foregroundContainer = container;
						break;
					}
					if (SpatialUtils2.intersects(container.area.getPosition(), container.area.getDimension(), event.getCoord(), ImmutableCoordI2.one())) {
						// TODO z-index handling
						foregroundContainer = container;
					}
				}
			}
			if (foregroundContainer == this) {
				if (!this.dragging) {
					this.dragging = true;
					this.dragPrevPosition = null;
					this.LOGGER.debug("Started dragging container {}", this.id);
				}
				if (this.dragPrevPosition != null) {
					final var delta = event.getCoord().substract(this.dragPrevPosition);
					if (!ImmutableCoordI2.zero().equals(delta)) {
						this.setNormalizedPosition(this.getNormalizedPosition().add(delta));
					}
				}
				this.dragPrevPosition = event.getCoord();
			}
		}
	}
	
	protected void onMouseClickEvent(final MouseClickEvent event) {
		if (!event.isPressed() && this.dragging) {
			this.dragging = false;
			this.dragPrevPosition = null;
			if (this.adjustIfOutsideViewport()) {
				this.draw();
			}
			this.LOGGER.debug("Stopped dragging container {}", this.id);
		}
	}
	
	/**
	 * Override in subclasses, but make sure to call super.draw() at the end if overridden
	 */
	public void draw() {
		final var uncommitted = this.rendererWorkingSet.getUncommittedWork(this.id);
		for (final RenderingBo bo : uncommitted.values()) {
			bo.withPositionAbsoluteAnchorTopLeft(this.area.getPosition()
					.toMutableCoordD2()
					.add(bo.getLocalX(), bo.getLocalY())
					.toImmutableCoordI2());
			this.rendererWorkingSet.put(this.id, bo);
		}
		
		this.rendererWorkingSet.commit(this.id, true);
	}
	
	public void putRenderingBo(final RenderingBo renderingBo) {
		this.rendererWorkingSet.put(this.id, renderingBo);
	}
	
	public void removeRenderingBo(final String renderingBoId) {
		this.rendererWorkingSet.remove(this.id, renderingBoId);
	}
	
	public void clearRenderingBos() {
		this.rendererWorkingSet.clearNext(this.id);
	}
	
	public void setNormalizedPosition(final CoordI2 normalizedPosition) {
		this.normalizedArea.getPosition().setXY(normalizedPosition);
		switch (this.horizontalAlignment) {
			case LEFT -> this.area.getPosition().setX(normalizedPosition.getX());
			case CENTER -> this.area.getPosition().setX(normalizedPosition.getX() + (this.rendererControl.getEffectiveViewportDimension().getX() - RendererControl.REFERENCE_RESOLUTION.getX()) / 2);
			case RIGHT -> this.area.getPosition().setX(normalizedPosition.getX() + this.rendererControl.getEffectiveViewportDimension().getX() - RendererControl.REFERENCE_RESOLUTION.getX());
		}
		switch (this.verticalAlignment) {
			case TOP -> this.area.getPosition().setY(normalizedPosition.getY());
			case CENTER -> this.area.getPosition().setY(normalizedPosition.getY() + (this.rendererControl.getEffectiveViewportDimension().getY() - RendererControl.REFERENCE_RESOLUTION.getY()) / 2);
			case BOTTOM -> this.area.getPosition().setY(normalizedPosition.getY() + this.rendererControl.getEffectiveViewportDimension().getY() - RendererControl.REFERENCE_RESOLUTION.getY());
		}
		this.draw();
	}
	
	public CoordI2 getNormalizedPosition() {
		return this.normalizedArea.getPosition().toImmutableCoordI2();
	}
	
	public void setNormalizedDimension(final CoordI2 normalizedDimension) {
		this.normalizedArea.getDimension().setXY(normalizedDimension);
		this.area.getDimension().setXY(normalizedDimension);
		this.draw();
	}
	
	public CoordI2 getNormalizedDimension() {
		return this.normalizedArea.getDimension().toImmutableCoordI2();
	}
	
	public boolean isDraggable() {
		return draggable;
	}
	
	public void setDraggable(final boolean draggable) {
		this.draggable = draggable;
	}
	
	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}
	
	public void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}
	
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}
	
	public void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}
}
