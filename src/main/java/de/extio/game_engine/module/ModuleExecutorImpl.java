package de.extio.game_engine.module;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleExecutorImpl implements ModuleExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleExecutorImpl.class);
	
	private final ModuleManager moduleManager;
	
	public ModuleExecutorImpl(final ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}
	
	@Override
	public void execute() {

		// TODO Subscriptions for modules to run only on certain conditions

		this.runTasks(module -> () -> {
			if (module instanceof final AbstractClientModule clientModule && this.moduleManager.getModulesDisplayedClientModules().contains(clientModule)) {
				this.invokeSafe(clientModule, AbstractClientModule::runUiPre);
			}
		});
		this.runTasks(module -> () -> {
			this.invokeSafe(module, AbstractModule::run);
		});
		this.runTasks(module -> () -> {
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
	
	private void runTasks(final Function<AbstractModule, Runnable> taskSupplier) {
		try (var scope = StructuredTaskScope.open(Joiner.awaitAll())) {
			for (final var module : this.moduleManager.getModulesActive()) {
				final var task = taskSupplier.apply(module);
				if (task != null) {
					scope.fork(task);
				}
			}
			scope.join();
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
}
