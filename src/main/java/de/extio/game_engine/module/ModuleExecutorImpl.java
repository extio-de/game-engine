package de.extio.game_engine.module;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleExecutorImpl implements ModuleExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleExecutorImpl.class);
	
	private final ModuleService moduleManager;
	
	public ModuleExecutorImpl(final ModuleService moduleManager) {
		this.moduleManager = moduleManager;
	}
	
	@Override
	public void execute() {
		this.runTasks(ModuleExecutorCallbacks.UI_PRE, module -> () -> {
			if (module instanceof final AbstractClientModule clientModule && this.moduleManager.getModulesDisplayedClientModules().contains(clientModule)) {
				this.invokeSafe(clientModule, AbstractClientModule::runUiPre);
			}
		});
		this.runTasks(ModuleExecutorCallbacks.RUN, module -> () -> {
			this.invokeSafe(module, AbstractModule::run);
		});
		this.runTasks(ModuleExecutorCallbacks.UI_POST, module -> () -> {
			if (module instanceof final AbstractClientModule clientModule && this.moduleManager.getModulesDisplayedClientModules().contains(clientModule)) {
				this.invokeSafe(clientModule, AbstractClientModule::runUiPost);
			}
		});
	}
	
	private <T extends AbstractModule> void invokeSafe(final T module, final Consumer<T> consumer) {
		try {
			consumer.accept(module);
		}
		catch (final Exception exc) {
			LOGGER.error("Error in module " + module.getClass().getName(), exc);
		}
	}
	
	private void runTasks(final ModuleExecutorCallbacks callbackType, final Function<AbstractModule, Runnable> taskSupplier) {
		final var subscribers = this.moduleManager.getSubscribersForCallback(callbackType);
		if (subscribers.isEmpty()) {
			return;
		}

		try (var scope = StructuredTaskScope.open(Joiner.awaitAll())) {
			synchronized (subscribers) {
				for (final var module : subscribers) {
					final var task = taskSupplier.apply(module);
					if (task != null) {
						scope.fork(task);
					}
				}
			}
			scope.join();
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
}
