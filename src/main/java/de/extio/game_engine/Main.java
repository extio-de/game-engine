package de.extio.game_engine;

import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.type.AnnotatedTypeMetadata;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class Main {
	
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

		final SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
		builder.headless(false);
		builder.properties("logging.level.de.extio=TRACE");
		builder.run(args);
	}
	
	static class StandaloneEnvCondition implements Condition {
		
		public StandaloneEnvCondition() {
		}

		@Override
		public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
			final String env = System.getProperty("game-engine.env", "prod");
			return "standalone".equalsIgnoreCase(env);
		}
	}
}
