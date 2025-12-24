package de.extio.game_engine.audio;

import java.util.Collection;

import de.extio.game_engine.resource.StaticResource;

interface AudioStrategy {
	
	void start();
	
	void stop();
	
	void run();
	
	void queueSfx(Collection<StaticResource> audioFiles);
	
	void clearSfx();
	
	void queueMusic(Collection<StaticResource> audioFiles);
	
	void clearMusic();
	
}
