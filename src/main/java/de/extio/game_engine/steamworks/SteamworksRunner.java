package de.extio.game_engine.steamworks;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.SmartLifecycle;

public final class SteamworksRunner implements SmartLifecycle {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int SLEEP_MS = 33;
	
	private final SteamworksConnector connector;
	
	private final AtomicBoolean running = new AtomicBoolean();
	
	private Thread thread;
	
	public SteamworksRunner(final SteamworksConnector connector) {
		this.connector = connector;
	}
	
	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			LOGGER.debug(() -> "SteamworksRunner starting");
			
			this.thread = new Thread(this::run, "SteamworksRunner");
			this.thread.setDaemon(true);
			this.thread.start();
		}
	}
	
	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false)) {
			LOGGER.debug(() -> "SteamworksRunner stopping");
			final Thread t = this.thread;
			if (t != null) {
				t.interrupt();
				try {
					t.join(3000);
				}
				catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	@Override
	public boolean isRunning() {
		return this.running.get();
	}
	
	@Override
	public boolean isAutoStartup() {
		return true;
	}
	
	@Override
	public void stop(final Runnable callback) {
		this.stop();
		callback.run();
	}
	
	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}
	
	private void run() {
		try {
			while (this.running.get() && this.connector.isActive()) {
				this.connector.runCallbacks();
				Thread.sleep(SLEEP_MS);
			}
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		catch (final Throwable t) {
			LOGGER.warn("Error in SteamworksRunner", t);
		}
		finally {
			this.connector.shutdown();
			LOGGER.debug(() -> "SteamworksRunner stopped");
		}
	}
	
}
