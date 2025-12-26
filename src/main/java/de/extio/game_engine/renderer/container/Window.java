package de.extio.game_engine.renderer.container;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowCloseButtonControl;
import de.extio.game_engine.renderer.model.bo.DrawWindowRenderingBo;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;

@Component
@Scope("prototype")
public class Window extends Container {
	
	private boolean closeButton;

	private String closeButtonName = "";
	
	private Runnable onCloseAction;
	
	public Window(final ModuleService moduleService, final RenderingBoPool renderingBoPool, final RendererControl rendererControl, final EventService eventService, final RendererWorkingSet rendererWorkingSet) {
		super(moduleService, renderingBoPool, rendererControl, eventService, rendererWorkingSet);
	}

	@Override
	public void onActivate() {
		if (this.closeButton) {
			this.eventService.register(UiControlEvent.class, this.getId(), this::onCloseButtonControlEvent);
		}

		super.onActivate();
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
		
		if (this.closeButton) {
			this.closeButtonName = this.id + "_CloseButton";
			final var closeButtonBo = this.renderingBoPool.acquire(this.closeButtonName, ControlRenderingBo.class)
					.setType(WindowCloseButtonControl.class)
					.setCustomData(true)
					.setEnabled(true)
					.setVisible(true)
					.withDimensionAbsolute(this.area.getDimension())
					.withPositionAbsoluteAnchorTopLeft(this.area.getPosition())
					.setLayer(RenderingBoLayer.UI0_1);
			this.rendererWorkingSet.put(this.id, closeButtonBo);
		}

		super.draw();
	}

	private void onCloseButtonControlEvent(final UiControlEvent event) {
		if (this.closeButton && this.closeButtonName.equals(event.getId())) {
			if (this.onCloseAction != null) {
				this.onCloseAction.run();
			}
			else {
				this.getModuleService().changeActiveState(this.getId(), false);
			}
		}
	}
	
	public boolean hasCloseButton() {
		return closeButton;
	}
	
	/**
	 * Sets whether the window has a close button. Must be set before activation.
	 */
	public void setCloseButton(final boolean closeButton) {
		this.closeButton = closeButton;
	}

	public Runnable getOnCloseAction() {
		return onCloseAction;
	}

	/**
	 * Sets the action to be performed when the close button is pressed.
	 * If not set, the window module will simply be deactivated on close button press.
	 * 
	 * @param onCloseAction The action to be performed on close button press.
	 */
	public void setOnCloseAction(final Runnable onCloseAction) {
		this.onCloseAction = onCloseAction;
	}
}
