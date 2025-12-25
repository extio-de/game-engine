package de.extio.game_engine.module;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import de.extio.game_engine.renderer.work.RendererWorkingSet;

@AutoConfiguration
@ConditionalOnProperty(name = "game-engine.module.enabled", havingValue = "true", matchIfMissing = true)
public class ModuleServiceAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	ModuleService moduleService(final ApplicationContext applicationContext, final RendererWorkingSet rendererWorkingSet) {
		return new ModuleServiceImpl(applicationContext, rendererWorkingSet);
	}
	
	@Bean
	@ConditionalOnMissingBean
	ModuleExecutor moduleExecutor(final ModuleService moduleManager) {
		return new ModuleExecutorImpl(moduleManager);
	}
	
}
