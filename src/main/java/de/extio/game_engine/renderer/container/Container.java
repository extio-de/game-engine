package de.extio.game_engine.renderer.container;

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
import de.extio.game_engine.renderer.model.event.ViewportResizeEvent;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

@Component
@Scope("prototype")
public class Container extends AbstractClientModule implements InitializingBean {
	
	protected final ModuleService moduleService;
	
	protected final RenderingBoPool renderingBoPool;
	
	protected final RendererControl rendererControl;
	
	protected final RendererWorkingSet rendererWorkingSet;
	
	protected final EventService eventService;
	
	protected final Area2 area = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	protected final Area2 normalizedArea = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	protected boolean draggable;
	
	protected boolean dragging;
	
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
		this.onViewportResize(null);
	}
	
	@Override
	public void onShow() {
		this.draw();
	}
	
	@Override
	public void onDeactivate() {
		this.eventService.unregister(ViewportResizeEvent.class, this.id);
	}
	
	protected void onViewportResize(final ViewportResizeEvent event) {
		this.setNormalizedPosition(this.normalizedArea.getPosition());
		this.setNormalizedDimension(this.normalizedArea.getDimension());
		this.draw();
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
		this.LOGGER.debug("Effective viewport dimension: {}", this.rendererControl.getEffectiveViewportDimension());
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
