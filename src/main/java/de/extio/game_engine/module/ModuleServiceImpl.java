package de.extio.game_engine.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.extio.game_engine.renderer.work.RendererWorkingSet;

public class ModuleServiceImpl implements InitializingBean, ModuleService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleServiceImpl.class);
	
	private final List<AbstractModule> modulesInitial;
	
	private final List<AbstractModule> modulesAll = Collections.synchronizedList(new ArrayList<>());
	
	private final RendererWorkingSet rendererWorkingSet;
	
	private List<AbstractModule> modulesAllView = List.of();
	
	private List<AbstractModule> modulesActiveView = List.of();
	
	private List<AbstractClientModule> modulesActiveClientModulesView = List.of();
	
	private List<AbstractClientModule> modulesDisplayedClientModulesView = List.of();
	
	private final List<AbstractModule> modulesActive = Collections.synchronizedList(new ArrayList<>());
	
	private final List<AbstractClientModule> modulesDisplayed = Collections.synchronizedList(new ArrayList<>());
	
	private final Stack<List<AbstractClientModule>> lastVisibleStates = new Stack<>();
	
	private final Map<ModuleExecutorCallbacks, List<AbstractModule>> executorCallbackMap = Collections.synchronizedMap(new EnumMap<>(ModuleExecutorCallbacks.class));
	
	public ModuleServiceImpl(final List<AbstractModule> initialModules, final RendererWorkingSet rendererWorkingSet) {
		this.modulesInitial = initialModules;
		this.rendererWorkingSet = rendererWorkingSet;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.modulesInitial == null || this.modulesInitial.isEmpty()) {
			return;
		}
		this.modulesInitial.forEach(this::load);
		this.modulesInitial.clear();
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
	public synchronized void loadModule(final Class<? extends AbstractModule> clazz) {
		synchronized (this.modulesAll) {
			if (this.modulesAll.stream().anyMatch(module -> clazz.isInstance(module))) {
				LOGGER.debug("AbstractModule already loaded: " + clazz.getName());
				return;
			}
		}
		
		try {
			final AbstractModule module = AbstractModule.class.cast(clazz.getDeclaredConstructor().newInstance());
			this.load(module);
		}
		catch (final Exception e) {
			LOGGER.error("Error loading module " + clazz.getName(), e);
		}
	}
	
	private void load(final AbstractModule module) {
		module.setModuleService(this);
		this.modulesAll.add(module);
		this.sortModules(this.modulesAll);
		synchronized (this.modulesAll) {
			this.modulesAllView = List.copyOf(this.modulesAll);
		}
		LOGGER.info("Loaded module " + module.getClass().getName());
		this.invokeSafe(module, m -> m.onLoad());
	}
	
	@Override
	public synchronized void unloadModule(final Class<? extends AbstractModule> clazz) {
		try {
			AbstractModule module;
			synchronized (this.modulesAll) {
				module = this.modulesAll.stream().filter(mod -> clazz.isInstance(mod)).findAny().orElse(null);
			}
			
			if (module == null) {
				LOGGER.debug("AbstractModule not loaded: " + clazz.getName());
				return;
			}
			
			this.changeActiveState(clazz, false);
			this.modulesAll.remove(module);
			this.sortModules(this.modulesAll);
			synchronized (this.modulesAll) {
				this.modulesAllView = List.copyOf(this.modulesAll);
			}
			
			this.invokeSafe(module, m -> m.onUnload());
			if (module instanceof final AbstractClientModule clientModule) {
				this.rendererWorkingSet.clear(clientModule.getClass());
			}
			
			LOGGER.info("Unloaded module " + clazz.getName());
		}
		catch (final Exception e) {
			LOGGER.error("Error unloading module " + clazz.getName(), e);
		}
	}
	
	@Override
	public void changeActiveState(final String className, final boolean active) {
		try {
			final var clazz = Class.forName(className);
			this.changeActiveState(clazz.asSubclass(AbstractModule.class), active);
		}
		catch (final ClassNotFoundException e) {
			LOGGER.error("Class not found: " + className, e);
		}
	}
	
	@Override
	public synchronized void changeActiveState(final Class<? extends AbstractModule> clazz, final boolean active) {
		this.modulesActiveView.stream()
				.filter(module -> clazz.isAssignableFrom(module.getClass()))
				.findAny()
				.ifPresentOrElse(module -> {
					if (!active) {
						if (module instanceof final AbstractClientModule clientModule) {
							this.changeDisplayState(clientModule.getClass(), false);
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
							this.rendererWorkingSet.clear(clientModule.getClass());
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
						
						LOGGER.info("Deactivated " + clazz.getName());
					}
				}, () -> {
					if (active) {
						synchronized (this.modulesAll) {
							this.modulesAll.stream()
									.filter(module -> clazz.isAssignableFrom(module.getClass()))
									.findFirst()
									.ifPresent(module -> {
										if (module instanceof final AbstractClientModule clientModule && clientModule.isAlwaysDisplay()) {
											this.changeDisplayState(clientModule.getClass(), true);
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
										
										LOGGER.info("Activated " + clazz.getName());
									});
						}
					}
				});
	}
	
	@Override
	public void deactivateAll() {
		for (final AbstractModule module : this.modulesActiveView) {
			this.changeActiveState(module.getClass(), false);
		}
		this.lastVisibleStates.clear();
	}
	
	@Override
	public void changeDisplayState(final String className, final boolean active) {
		try {
			final var clazz = Class.forName(className);
			this.changeDisplayState(clazz.asSubclass(AbstractClientModule.class), active);
		}
		catch (final ClassNotFoundException e) {
			LOGGER.error("Class not found: " + className, e);
		}
	}
	
	@Override
	public synchronized void changeDisplayState(final Class<? extends AbstractClientModule> clazz, final boolean display) {
		synchronized (this.modulesAll) {
			synchronized (this.modulesDisplayed) {
				for (final var module : this.modulesAll) {
					if (module instanceof final AbstractClientModule clientModule && clazz.isAssignableFrom(module.getClass())) {
						final boolean curDisplayed = this.modulesDisplayed.stream().anyMatch(mod -> clazz.isAssignableFrom(mod.getClass()));
						
						final boolean doDisplay = display || clientModule.isAlwaysDisplay();
						if (doDisplay && !curDisplayed) {
							this.modulesDisplayed.add(clientModule);
							clientModule.setDisplayed(true);
							LOGGER.info("Displayed {}", clazz.getName());
							this.invokeSafe(clientModule, m -> ((AbstractClientModule) m).onShow());
						}
						else if (!doDisplay && curDisplayed) {
							clientModule.setDisplayed(false);
							this.modulesDisplayed.removeIf(mod -> clazz.isAssignableFrom(mod.getClass()));
							LOGGER.info("Hidden {}", clazz.getName());
							this.invokeSafe(clientModule, m -> ((AbstractClientModule) m).onHide());
						}
						this.modulesDisplayedClientModulesView = List.copyOf(this.modulesDisplayed);
						
						break;
					}
				}
			}
		}
	}
	
	@Override
	public boolean isDisplayed(final String className) {
		try {
			final var clazz = Class.forName(className);
			return this.isDisplayed(clazz.asSubclass(AbstractClientModule.class));
		}
		catch (final ClassNotFoundException e) {
			LOGGER.error("Class not found: " + className, e);
			return false;
		}
	}
	
	@Override
	public boolean isDisplayed(final Class<? extends AbstractClientModule> clazz) {
		final var modules = this.modulesDisplayedClientModulesView;
		for (int i = 0; i < modules.size(); i++) {
			if (clazz.isAssignableFrom(modules.get(i).getClass())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void hideAll() {
		this.lastVisibleStates.clear();
		
		final var modules = this.modulesDisplayedClientModulesView;
		for (final var module : modules) {
			if ((module instanceof final AbstractClientModule clientModule) && !clientModule.isAlwaysDisplay()) {
				this.changeDisplayState(clientModule.getClass(), false);
			}
		}
	}
	
	@Override
	@SafeVarargs
	public final void hideExcept(final String... classNames) {
		final List<AbstractClientModule> state = new ArrayList<>();
		final var keep = new HashSet<>(Arrays.asList(classNames));
		
		final var modules = this.modulesDisplayedClientModulesView;
		for (final var module : modules) {
			if (module instanceof final AbstractClientModule clientModule &&
					!clientModule.isAlwaysDisplay() &&
					!keep.contains(clientModule.getClass().getName())) {
				this.changeDisplayState(clientModule.getClass(), false);
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
				this.changeDisplayState(module.getClass(), true);
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
