package de.extio.game_engine.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import de.extio.game_engine.renderer.work.RendererWorkingSet;

public class ModuleServiceImpl implements ModuleService, ApplicationListener<ContextRefreshedEvent> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleServiceImpl.class);
	
	private final ApplicationContext applicationContext;

	private final List<AbstractModule> modulesAll = Collections.synchronizedList(new ArrayList<>());
	
	private final Map<String, AbstractModule> modulesByIdMap = new ConcurrentHashMap<>();
	
	private final RendererWorkingSet rendererWorkingSet;
	
	private List<AbstractModule> modulesAllView = List.of();
	
	private List<AbstractModule> modulesActiveView = List.of();
	
	private List<AbstractClientModule> modulesActiveClientModulesView = List.of();
	
	private List<AbstractClientModule> modulesDisplayedClientModulesView = List.of();
	
	private final List<AbstractModule> modulesActive = Collections.synchronizedList(new ArrayList<>());
	
	private final List<AbstractClientModule> modulesDisplayed = Collections.synchronizedList(new ArrayList<>());
	
	private final Stack<List<AbstractClientModule>> lastVisibleStates = new Stack<>();
	
	private final Map<ModuleExecutorCallbacks, List<AbstractModule>> executorCallbackMap = Collections.synchronizedMap(new EnumMap<>(ModuleExecutorCallbacks.class));
	
	public ModuleServiceImpl(ApplicationContext applicationContext, final RendererWorkingSet rendererWorkingSet) {
		this.applicationContext = applicationContext;
		this.rendererWorkingSet = rendererWorkingSet;
	}

	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		final var modulesInitial = this.applicationContext.getBeansOfType(AbstractModule.class, false, false);
		if (modulesInitial == null || modulesInitial.isEmpty()) {
			return;
		}
		modulesInitial.values().forEach(this::load);
	}
	
	@Override
	public List<AbstractModule> getModulesAll() {
		return this.modulesAllView;
	}
	
	@Override
	public List<AbstractModule> getModulesActive() {
		return this.modulesActiveView;
	}
	
	@Override
	public List<AbstractClientModule> getModulesActiveClientModules() {
		return this.modulesActiveClientModulesView;
	}
	
	@Override
	public List<AbstractClientModule> getModulesDisplayedClientModules() {
		return modulesDisplayedClientModulesView;
	}
	
	@Override
	public synchronized void loadModule(final AbstractModule module) {
		if (this.modulesByIdMap.containsKey(module.getId())) {
			LOGGER.debug("AbstractModule already loaded: " + module.getId());
			return;
		}
		
		this.load(module);
	}
	
	private void load(final AbstractModule module) {
		module.setModuleService(this);
		this.modulesAll.add(module);
		this.modulesByIdMap.put(module.getId(), module);
		this.sortModules(this.modulesAll);
		synchronized (this.modulesAll) {
			this.modulesAllView = List.copyOf(this.modulesAll);
		}
		LOGGER.debug("Loaded module {} {}", module.getClass().getName(), module.getId());
		this.invokeSafe(module, m -> m.onLoad());
	}
	
	@Override
	public synchronized void unloadModule(final String id) {
		try {
			final AbstractModule module = this.modulesByIdMap.get(id);
			
			if (module == null) {
				LOGGER.debug("AbstractModule not loaded: " + id);
				return;
			}
			
			this.changeActiveState(id, false);
			this.modulesAll.remove(module);
			this.modulesByIdMap.remove(id);
			this.sortModules(this.modulesAll);
			synchronized (this.modulesAll) {
				this.modulesAllView = List.copyOf(this.modulesAll);
			}
			
			this.invokeSafe(module, m -> m.onUnload());
			if (module instanceof final AbstractClientModule clientModule) {
				this.rendererWorkingSet.clear(clientModule.getId());
			}
			
			LOGGER.info("Unloaded module {}", id);
		}
		catch (final Exception e) {
			LOGGER.error("Error unloading module " + id, e);
		}
	}
	
	@Override
	public synchronized void changeActiveState(final String id, final boolean active) {
		this.modulesActiveView.stream()
				.filter(module -> module.getId().equals(id))
				.findAny()
				.ifPresentOrElse(module -> {
					if (!active) {
						if (module instanceof final AbstractClientModule clientModule) {
							this.changeDisplayState(clientModule.getId(), false);
						}
						this.modulesActive.remove(module);
						this.sortModules(this.modulesActive);
						synchronized (this.modulesActive) {
							this.modulesActiveView = List.copyOf(this.modulesActive);
							this.modulesActiveClientModulesView = this.modulesActive.stream()
									.filter(m -> m instanceof AbstractClientModule)
									.map(m -> (AbstractClientModule) m)
									.toList();
						}
						if (module instanceof final AbstractClientModule clientModule) {
							this.rendererWorkingSet.clear(clientModule.getId());
						}
						synchronized (this.executorCallbackMap) {
							final var subscriptions = module.executorCallbackSubscriptions();
							if (subscriptions != null) {
								for (final var subscription : subscriptions) {
									executorCallbackMap.computeIfAbsent(subscription, k -> Collections.synchronizedList(new ArrayList<>())).remove(module);
								}
							}
						}
						this.invokeSafe(module, AbstractModule::onDeactivate);
						
						LOGGER.info("Deactivated {}", id);
					}
				}, () -> {
					if (active) {
						final AbstractModule module = this.modulesByIdMap.get(id);
						if (module != null) {
							if (module instanceof final AbstractClientModule clientModule && clientModule.isAlwaysDisplay()) {
								this.changeDisplayState(clientModule.getId(), true);
							}
							this.modulesActive.add(module);
							this.sortModules(this.modulesActive);
							synchronized (this.modulesActive) {
								this.modulesActiveView = List.copyOf(this.modulesActive);
								this.modulesActiveClientModulesView = this.modulesActive.stream()
										.filter(m -> m instanceof AbstractClientModule)
										.map(m -> (AbstractClientModule) m)
										.toList();
							}
							synchronized (this.executorCallbackMap) {
								final var subscriptions = module.executorCallbackSubscriptions();
								if (subscriptions != null) {
									for (final var subscription : subscriptions) {
										executorCallbackMap.computeIfAbsent(subscription, k -> Collections.synchronizedList(new ArrayList<>())).add(module);
									}
								}
							}
							this.invokeSafe(module, AbstractModule::onActivate);
							
							LOGGER.info("Activated {}", id);
						}
					}
				});
	}
	
	@Override
	public void deactivateAll() {
		for (final AbstractModule module : this.modulesActiveView) {
			this.changeActiveState(module.getId(), false);
		}
		this.lastVisibleStates.clear();
	}
	
	@Override
	public synchronized void changeDisplayState(final String id, final boolean display) {
		final AbstractModule module = this.modulesByIdMap.get(id);
		if (module instanceof final AbstractClientModule clientModule) {
			synchronized (this.modulesDisplayed) {
				final boolean curDisplayed = this.modulesDisplayed.stream().anyMatch(mod -> mod.getId().equals(id));
				
				final boolean doDisplay = display || clientModule.isAlwaysDisplay();
				if (doDisplay && !curDisplayed) {
					this.modulesDisplayed.add(clientModule);
					clientModule.setDisplayed(true);
					LOGGER.info("Displayed {}", id);
					this.invokeSafe(clientModule, m -> ((AbstractClientModule) m).onShow());
				}
				else if (!doDisplay && curDisplayed) {
					clientModule.setDisplayed(false);
					this.modulesDisplayed.removeIf(mod -> mod.getId().equals(id));
					LOGGER.info("Hidden {}", id);
					this.invokeSafe(clientModule, m -> ((AbstractClientModule) m).onHide());
				}
				this.modulesDisplayedClientModulesView = List.copyOf(this.modulesDisplayed);
			}
		}
	}
	
	@Override
	public boolean isDisplayed(final String id) {
		final var module = this.modulesByIdMap.get(id);
		if (module != null && module instanceof final AbstractClientModule clientModule) {
			return clientModule.isDisplayed();
		}
		return false;
	}
	
	@Override
	public void hideAll() {
		this.lastVisibleStates.clear();
		
		final var modules = this.modulesDisplayedClientModulesView;
		for (final var module : modules) {
			if ((module instanceof final AbstractClientModule clientModule) && !clientModule.isAlwaysDisplay()) {
				this.changeDisplayState(clientModule.getId(), false);
			}
		}
	}
	
	@Override
	@SafeVarargs
	public final void hideExcept(final String... ids) {
		final List<AbstractClientModule> state = new ArrayList<>();
		final var keep = new HashSet<>(Arrays.asList(ids));
		
		final var modules = this.modulesDisplayedClientModulesView;
		for (final var module : modules) {
			if (module instanceof final AbstractClientModule clientModule &&
					!clientModule.isAlwaysDisplay() &&
					!keep.contains(clientModule.getId())) {
				this.changeDisplayState(clientModule.getId(), false);
				state.add(clientModule);
			}
		}
		
		if (!state.isEmpty()) {
			this.lastVisibleStates.push(state);
		}
	}
	
	@Override
	public void restoreVisibility() {
		if (this.lastVisibleStates.isEmpty()) {
			return;
		}
		
		synchronized (this.lastVisibleStates) {
			for (final AbstractClientModule module : this.lastVisibleStates.pop()) {
				this.changeDisplayState(module.getId(), true);
			}
		}
	}
	
	@Override
	public boolean isModal() {
		return !this.lastVisibleStates.isEmpty();
	}
	
	@Override
	public List<AbstractModule> getSubscribersForCallback(final ModuleExecutorCallbacks callback) {
		return this.executorCallbackMap.getOrDefault(callback, List.of());
	}
	
	private void invokeSafe(final AbstractModule module, final Consumer<AbstractModule> consumer) {
		try {
			consumer.accept(module);
		}
		catch (final Exception exc) {
			LOGGER.error("Error in module " + module.getClass().getName(), exc);
		}
	}
	
	private void sortModules(final List<AbstractModule> modules) {
		modules.sort((o1, o2) -> {
			if (o1 instanceof final AbstractClientModule a1 && o2 instanceof final AbstractClientModule a2) {
				return Integer.compare(a2.getPriority(), a1.getPriority());
			}
			return Boolean.compare(o1 instanceof AbstractClientModule, o2 instanceof AbstractClientModule);
		});
	}
	
}
