package de.extio.game_engine.audio;

import java.io.InputStream;
import java.util.function.Consumer;

import de.extio.game_engine.resource.StaticResource;

final class AudioLoaderRequest {
	
	private StaticResource StaticResource;
	
	private Consumer<InputStream> consumer;
	
	private boolean useCache;
	
	public AudioLoaderRequest(final StaticResource StaticResource, final boolean useCache, final Consumer<InputStream> consumer) {
		this.StaticResource = StaticResource;
		this.useCache = useCache;
		this.consumer = consumer;
	}
	
	public Consumer<InputStream> getConsumer() {
		return this.consumer;
	}
	
	public void setConsumer(final Consumer<InputStream> consumer) {
		this.consumer = consumer;
	}
	
	public StaticResource getStaticResource() {
		return this.StaticResource;
	}
	
	public void setStaticResource(final StaticResource StaticResource) {
		this.StaticResource = StaticResource;
	}
	
	public boolean isUseCache() {
		return this.useCache;
	}
	
	public void setUseCache(final boolean useCache) {
		this.useCache = useCache;
	}
	
}
