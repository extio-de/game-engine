package de.extio.game_engine;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

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
	
}
