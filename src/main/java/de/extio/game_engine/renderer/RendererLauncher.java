package de.extio.game_engine.renderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class RendererLauncher implements InitializingBean, DisposableBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RendererLauncher.class);
	
	private final ApplicationContext applicationContext;
	
	private final RendererData rendererData;
	
	private RendererLoop rendererLoop;
	
	private Thread rendererLoopThread;
	
	private Thread watchDogThread;
	
	public RendererLauncher(final ApplicationContext applicationContext, final RendererData rendererData) {
		this.applicationContext = applicationContext;
		this.rendererData = rendererData;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.launch();
	}
	
	@Override
	public void destroy() throws Exception {
		this.shutdown();
	}
	
	private void launch() {
		if (this.rendererLoopThread != null && this.rendererLoopThread.isAlive()) {
			return;
		}
		
		LOGGER.info("Launching RendererLoop");
		this.rendererLoop = new RendererLoop(this.rendererData);
		rendererData.setRendererLoop(rendererLoop);
		this.rendererLoopThread = Thread.ofPlatform()
				.name("RendererLoop")
				.daemon(false)
				.start(this.rendererLoop);
		
		LOGGER.info("Launching RendererWatchDog");
		this.watchDogThread = Thread.ofPlatform()
				.name("RendererWatchDog")
				.daemon(true)
				.start(() -> {
					try {
						this.rendererLoopThread.join();
						LOGGER.error("RendererLoop thread has exited unexpectedly, shutting down application");
						SpringApplication.exit(this.applicationContext, () -> 1);
					}
					catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				});
	}
	
	private void shutdown() {
		if (this.watchDogThread != null && this.watchDogThread.isAlive()) {
			LOGGER.info("Shutting down RendererWatchDog");
			this.watchDogThread.interrupt();
			try {
				this.watchDogThread.join(5000);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		this.watchDogThread = null;
		
		if (this.rendererLoopThread != null && this.rendererLoopThread.isAlive() && this.rendererLoop != null) {
			LOGGER.info("Shutting down RendererLoop");
			this.rendererLoop.shutdown();
			try {
				this.rendererLoopThread.join(5000);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		this.rendererLoopThread = null;
		this.rendererLoop = null;
		this.rendererData.setRendererLoop(null);
		
		this.rendererData.getRenderer().shutdown();
	}
	
}
