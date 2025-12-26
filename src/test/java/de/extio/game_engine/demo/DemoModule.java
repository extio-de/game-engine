package de.extio.game_engine.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.audio.AudioController;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.resource.StaticResource;

public class DemoModule extends AbstractClientModule {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private AudioController audioController;

	@Autowired
	private RenderingBoPool renderingBoPool;

	private Window mainWindow;

	@Override
	public void onLoad() {
		this.mainWindow = this.applicationContext.getBean(Window.class);
		this.mainWindow.setNormalizedPosition(RendererControl.REFERENCE_RESOLUTION.divide(9));
		this.mainWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7));
		this.mainWindow.setDraggable(true);

		var bo = this.renderingBoPool.acquire("DemoModule_MainWindow_Label_1", ControlRenderingBo.class)
				.setCaption("Welcome to Exo's Game Engine Demo!")
				.setFontSize(48)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7).substract(20).getX(), 60)
				.withPositionRelative(10, 100);
		this.mainWindow.putRenderingBo(bo);
		
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}

	// private void displayMenuEntries(final List<RenderingBo> renderingBo) {
	// 	int y = EngineFacade.instance().getRendererControl().getEffectiveViewportDimension().getY() / 2 - (this.SIZE + this.SPACING) * this.menuEntries.size() / 2;
		
	// 	for (final MenuEntry menuEntry : this.menuEntries) {
	// 		final int fy = y;
	// 		this.addRenderingBo(renderingBo, true, () -> EngineFacade.instance().getRenderingBoPool().acquire(ControlRenderingBo.class)
	// 				.setId("Menu_Label_" + menuEntry.getId())
	// 				.setCaption(menuEntry.getCaption())
	// 				.setFontSize(this.SIZE)
	// 				.setType(LabelControl.class)
	// 				.setVisible(this.isDisplayed())
	// 				.setEnabled(menuEntry.isActive())
	// 				.withDimensionAbsolute(EngineFacade.instance().getRendererControl().getEffectiveViewportDimension().getX(), this.SIZE + this.SPACING)
	// 				.withPositionAbsoluteAnchorTopLeft(0, fy));
			
	// 		y += this.SIZE + this.SPACING;
	// 	}
	// }

	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.mainWindow.getId());
	}

	@Override
	public void onActivate() {
		this.getModuleService().changeActiveState(this.mainWindow.getId(), true);
	}

	@Override
	public void onDeactivate() {
		this.getModuleService().changeActiveState(this.mainWindow.getId(), false);
	}

	@Override
	public void onShow() {
		this.getModuleService().changeDisplayState(this.mainWindow.getId(), true);
		// this.audioController.playMusic(null, List.of(new StaticResource(List.of("audio"), "race.ogg")), true);
	}

	@Override
	public void onHide() {
		this.getModuleService().changeDisplayState(this.mainWindow.getId(), false);
		this.audioController.stopMusic();
	}

}
