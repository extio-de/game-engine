package de.extio.game_engine.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.extio.game_engine.audio.AudioController;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.resource.StaticResource;

public class DemoModule extends AbstractClientModule {
	
	@Autowired
	private AudioController audioController;

	@Override
	public void onLoad() {
		this.getModuleService().changeActiveState(this.getClass(), true);
		this.getModuleService().changeDisplayState(this.getClass(), true);
	}
	
	@Override
	public void onShow() {
		this.audioController.playMusic(null, List.of(new StaticResource(List.of("audio"), "race.ogg")), true);
	}

	@Override
	public void onHide() {
		this.audioController.stopMusic();
	}

}
