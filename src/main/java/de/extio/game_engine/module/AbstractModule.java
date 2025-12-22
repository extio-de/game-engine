package de.extio.game_engine.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root of all modules
 */
public abstract class AbstractModule {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractModule.class);
	
	public AbstractModule() {
		
	}

	public void onLoad() {
		
	}
	
	public void onUnload() {
		
	}
	
	public void onActivate() {
		
	}
	
	public void onDeactivate() {
		
	}
	
	
	public void run() {
		
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
