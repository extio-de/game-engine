package de.extio.game_engine;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = BootstrapTest.TestConfig.class)
public class BootstrapTest {

	static {
        System.setProperty("java.awt.headless", "false");
    }

	@SpringBootConfiguration
    @EnableAutoConfiguration
	@ComponentScan(basePackages = "de.extio.game_engine")
    static class TestConfig {

		@Bean(name = "gameEngineEventConsumer")
		public Consumer<Object> eventConsumer() {
			return event -> {
			};
		}
    }

	@Test
	void contextLoads() {
		try {
			Thread.sleep(100000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
