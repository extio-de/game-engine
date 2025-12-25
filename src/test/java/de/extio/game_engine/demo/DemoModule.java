package de.extio.game_engine.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.audio.AudioController;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.resource.StaticResource;

public class DemoModule extends AbstractClientModule {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private AudioController audioController;

	private Window mainWindow;

	@Override
	public void onLoad() {
		this.mainWindow = this.applicationContext.getBean(Window.class);
		this.mainWindow.setPosition(RendererControl.REFERENCE_RESOLUTION.divide(9));
		this.mainWindow.setDimension(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7));
		this.mainWindow.setDraggable(true);
		
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}

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
		this.audioController.playMusic(null, List.of(new StaticResource(List.of("audio"), "race.ogg")), true);
	}

	@Override
	public void onHide() {
		this.getModuleService().changeDisplayState(this.mainWindow.getId(), false);
		this.audioController.stopMusic();
	}

}
