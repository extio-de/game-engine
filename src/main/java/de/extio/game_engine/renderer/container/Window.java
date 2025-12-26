package de.extio.game_engine.renderer.container;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.DrawWindowRenderingBo;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;

@Component
@Scope("prototype")
public class Window extends Container {
	
	public Window( final ModuleService moduleService, final RenderingBoPool renderingBoPool, final RendererControl rendererControl, final EventService eventService, final RendererWorkingSet rendererWorkingSet) {
		super(moduleService, renderingBoPool, rendererControl, eventService, rendererWorkingSet);
	}
	
	@Override
	public void draw() {
		if (this.area.getDimension().getX() <= 0 || this.area.getDimension().getY() <= 0) {
			return;
		}
		if (this.area.getPosition().getX() + this.area.getDimension().getX() < 0 || this.area.getPosition().getY() + this.area.getDimension().getY() < 0) {
			return;
		}
		if (this.area.getPosition().getX() > this.rendererControl.getAbsoluteViewportDimension().getX() || this.area.getPosition().getY() > this.rendererControl.getAbsoluteViewportDimension().getY()) {
			return;
		}
		
		final var windowFrame = this.rendererWorkingSet.getOrAcquire(this.id, "windowFrame", DrawWindowRenderingBo.class)
				.setThickBorder(true)
				.withDimensionAbsolute(this.area.getDimension())
				.withPositionAbsoluteAnchorTopLeft(this.area.getPosition())
				.setLayer(RenderingBoLayer.UI0_BGR);
		this.rendererWorkingSet.put(this.id, windowFrame);
		
		super.draw();
	}
}
