package de.extio.game_engine.renderer.container;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.RendererControl;
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
		this.setPosition(this.normalizedArea.getPosition());
		this.setDimension(this.normalizedArea.getDimension());
		this.draw();
	}

	protected void draw() {
		// Override in subclasses, but make sure to call super.draw() at the end if overridden
		this.rendererWorkingSet.commit(this.id, true);
	}
	
	public void setPosition(final CoordI2 normalizedPosition) {
		this.normalizedArea.getPosition().setXY(normalizedPosition);
		this.area.getPosition().setXY(
				(int) (normalizedPosition.getX() * ((double) this.rendererControl.getEffectiveViewportDimension().getX() / RendererControl.REFERENCE_RESOLUTION.getX())),
				(int) (normalizedPosition.getY() * ((double) this.rendererControl.getEffectiveViewportDimension().getY() / RendererControl.REFERENCE_RESOLUTION.getY())));
		this.draw();
		this.LOGGER.debug("Effective viewport dimension: {}", this.rendererControl.getEffectiveViewportDimension());
	}
	
	public CoordI2 getPosition() {
		return this.normalizedArea.getPosition().toImmutableCoordI2();
	}
	
	public void setDimension(final CoordI2 normalizedDimension) {
		this.normalizedArea.getDimension().setXY(normalizedDimension);
		this.area.getDimension().setXY(
				(int) (normalizedDimension.getX() * ((double) this.rendererControl.getEffectiveViewportDimension().getX() / RendererControl.REFERENCE_RESOLUTION.getX())),
				(int) (normalizedDimension.getY() * ((double) this.rendererControl.getEffectiveViewportDimension().getY() / RendererControl.REFERENCE_RESOLUTION.getY())));
		this.draw();
	}
	
	public CoordI2 getDimension() {
		return this.normalizedArea.getDimension().toImmutableCoordI2();
	}
	
	public boolean isDraggable() {
		return draggable;
	}
	
	public void setDraggable(final boolean draggable) {
		this.draggable = draggable;
	}
}
