package de.extio.game_engine.module;

import java.util.List;

public interface ModuleService {
	
	void loadModule(AbstractModule module);
	
	void unloadModule(String id);
	
	List<AbstractModule> getModulesAll();
	
	List<AbstractModule> getModulesActive();
	
	List<AbstractClientModule> getModulesActiveClientModules();
	
	List<AbstractClientModule> getModulesDisplayedClientModules();
	
	void changeActiveState(String id, boolean active);
	
	void deactivateAll();
	
	void changeDisplayState(String id, boolean display);
	
	boolean isDisplayed(String id);
	
	void hideAll();
	
	void hideExcept(String... ids);
	
	void restoreVisibility();
	
	boolean isModal();
	
	List<AbstractModule> getSubscribersForCallback(ModuleExecutorCallbacks callback);

}
