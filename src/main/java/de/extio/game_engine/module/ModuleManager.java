package de.extio.game_engine.module;

import java.util.List;

public interface ModuleManager {
	
	void loadModule(Class<? extends AbstractModule> clazz);
	
	void unloadModule(Class<? extends AbstractModule> clazz);
	
	List<AbstractModule> getModulesAll();
	
	List<AbstractModule> getModulesActive();
	
	List<AbstractClientModule> getModulesActiveClientModules();
	
	List<AbstractClientModule> getModulesDisplayedClientModules();
	
	void changeActiveState(String className, boolean active);
	
	void changeActiveState(Class<? extends AbstractModule> clazz, boolean active);
	
	void deactivateAll();
	
	void changeDisplayState(String className, boolean active);
	
	void changeDisplayState(Class<? extends AbstractClientModule> clazz, boolean display);
	
	boolean isDisplayed(String className);
	
	boolean isDisplayed(Class<? extends AbstractClientModule> clazz);
	
	void hideAll();
	
	void hideExcept(String... classNames);
	
	void restoreVisibility();
	
	boolean isModal();
	
	List<AbstractModule> getSubscribersForCallback(ModuleExecutorCallbacks callback);

}
