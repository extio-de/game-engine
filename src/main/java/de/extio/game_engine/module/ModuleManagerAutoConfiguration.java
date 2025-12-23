package de.extio.game_engine.module;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.renderer.work.RendererWorkingSet;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.module.enabled", havingValue = "true", matchIfMissing = true)
public class ModuleManagerAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	ModuleManager moduleManager(final List<AbstractModule> initialModules, final RendererWorkingSet rendererWorkingSet) {
		return new ModuleManagerImpl(initialModules, rendererWorkingSet);
	}
	
	@Bean
	@ConditionalOnMissingBean
	ModuleExecutor moduleExecutor(final ModuleManager moduleManager) {
		return new ModuleExecutorImpl(moduleManager);
	}
	
}
