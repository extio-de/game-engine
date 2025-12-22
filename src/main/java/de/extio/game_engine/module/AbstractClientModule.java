package de.extio.game_engine.module;

/**
 * Root of all UI client modules
 */
public abstract class AbstractClientModule extends AbstractModule {
	
	final static int MODULE_PRIORITY_LOW = 25;
	
	final static int MODULE_PRIORITY_NORMAL = 50;
	
	final static int MODULE_PRIORITY_HIGH = 75;
	
	public AbstractClientModule() {
		
	}
	
	public int getPriority() {
		return MODULE_PRIORITY_NORMAL;
	}
	
	public boolean isAlwaysDisplay() {
		return false;
	}
	
	public void onShow() {
		
	}
	
	public void onHide() {
		
	}
	
	public void runUiPre() {
		
	}
	
	public void runUiPost() {
		
	}
	
}
