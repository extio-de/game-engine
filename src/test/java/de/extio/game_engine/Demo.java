package de.extio.game_engine;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import de.extio.game_engine.demo.DemoModule;

@SpringBootApplication
public class Demo {
	
	public static void main(final String[] args) {
		System.setProperty("game-engine.env", "standalone");
		
		// final Logger rootLogger = Logger.getLogger("");
		// rootLogger.setLevel(Level.ALL);
		// final ConsoleHandler consoleHandler = new ConsoleHandler();
		// consoleHandler.setLevel(Level.ALL);
		// final Logger logger = Logger.getLogger("java.awt.event.Component");
		// logger.setLevel(Level.ALL);
		// logger.setUseParentHandlers(false);
		// logger.addHandler(consoleHandler);
		
		final SpringApplicationBuilder builder = new SpringApplicationBuilder(Demo.class);
		builder.headless(false);
		builder.properties("logging.level.de.extio=TRACE", "game-engine.renderer.title=Exo's Game Engine Demo");
		builder.run(args);
	}
	
	@Configuration
	@Conditional(de.extio.game_engine.Demo.StandaloneConfig.StandaloneCondition.class)
	static class StandaloneConfig {
		
		@Bean
		DemoModule demoModule() {
			return new DemoModule();
		}
		
		static class StandaloneCondition implements Condition {
			
			@Override
			public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
				final String env = context.getEnvironment().getProperty("game-engine.env");
				return "standalone".equals(env);
			}
		}
	}
}
