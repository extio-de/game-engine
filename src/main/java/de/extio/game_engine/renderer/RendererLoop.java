package de.extio.game_engine.renderer;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RendererLoop implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RendererLoop.class);
	
	private volatile boolean shutdown;
	
	private final RendererData rendererData;
	
	public RendererLoop(final RendererData rendererData) {
		this.rendererData = rendererData;
	}
	
	@Override
	public void run() {
		this.shutdown = false;
		
		try {
			this.rendererData.getRenderer().show();
			
			LOGGER.info("Renderer loop started");
			int exceptionCount = 0;
			while (!this.shutdown) {
				try {
					this.rendererData.nextFrame();
					this.rendererData.getRenderer().run(new ArrayList<>()); // TODO: Pass actual renderables
					exceptionCount = 0;
				}
				catch (final InterruptedException e) {
					LOGGER.info("Renderer loop interrupted, shutting down");
					Thread.currentThread().interrupt();
					break;
				}
				catch (final Exception e) {
					LOGGER.error("Renderer loop caught exception", e);
					if (++exceptionCount >= 10) {
						LOGGER.error("Renderer loop exceeded maximum exception count, shutting down");
						break;
					}
				}
			}
			
			LOGGER.info("Renderer loop exited");
		}
		catch (final Throwable t) {
			LOGGER.error("Renderer loop crashed", t);
		}
	}
	
	public void shutdown() {
		LOGGER.info("RendererLoop shutdown requested");
		this.shutdown = true;
	}
	
}
