package de.extio.game_engine.module;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root of all modules
 */
public abstract class AbstractModule {
	
	protected final Logger LOGGER = LoggerFactory.getLogger(AbstractModule.class);
	
	protected String id = "Module_" + System.identityHashCode(this);
	
	private ModuleService moduleService;
	
	public AbstractModule() {
		
	}
	
	public String getId() {
		return this.id;
	}
	
	public void onLoad() {
		
	}
	
	public void onUnload() {
		
	}
	
	public void onActivate() {
		
	}
	
	public void onDeactivate() {
		
	}
	
	public List<ModuleExecutorCallbacks> executorCallbackSubscriptions() {
		return null;
	}
	
	public void run() {
		
	}
	
	public ModuleService getModuleService() {
		return moduleService;
	}
	
	public void setModuleService(final ModuleService moduleService) {
		this.moduleService = moduleService;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
