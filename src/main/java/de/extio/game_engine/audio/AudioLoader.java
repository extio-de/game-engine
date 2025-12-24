package de.extio.game_engine.audio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.resource.StaticResourceService;

final class AudioLoader implements Runnable {
	
	private final static int CACHE_SIZE_LIMIT = 128;
	
	private final static int CACHE_ELEMENT_BYTES_LIMIT = 262144;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AudioLoader.class);
	
	private final BlockingQueue<AudioLoaderRequest> queue = new LinkedBlockingQueue<>(50);
	
	private final Map<String, CacheEntry> lruCache = new LRUCache<>();

	private final StaticResourceService resourceService;
	
	public AudioLoader(final StaticResourceService resourceService) {
		this.resourceService = resourceService;
	}
	
	@Override
	public void run() {
		LOGGER.debug("start");
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				final AudioLoaderRequest request = this.queue.take();
				
				if (request.isUseCache()) {
					final String cacheKey = request.getStaticResource().toString();
					
					final CacheEntry cacheEntry = this.lruCache.get(cacheKey);
					if (cacheEntry != null) {
						LOGGER.debug("Loading audio file from cache: {}", cacheKey);
						
						cacheEntry.lastAccessTime().set(System.currentTimeMillis());
						request.getConsumer().accept(new ByteArrayInputStream(cacheEntry.data()));
					}
					else {
						LOGGER.debug("Loading audio file from disk: {}", cacheKey);
						
						this.resourceService.loadStreamByPath(request.getStaticResource()).ifPresent(resourceStream -> {
							try (InputStream stream = resourceStream) {
								final byte[] bytes = stream.readAllBytes();
								LOGGER.debug("Loaded {} bytes", bytes.length);
								
								if (bytes.length <= CACHE_ELEMENT_BYTES_LIMIT) {
									this.lruCache.put(cacheKey, new CacheEntry(bytes, new AtomicLong(System.currentTimeMillis())));
									LOGGER.debug("Added to cache: {}", cacheKey);
								}
								
								request.getConsumer().accept(new ByteArrayInputStream(bytes));
							}
							catch (final IOException e) {
								LOGGER.error(e.getMessage(), e);
							}
						});
					}
				}
				else {
					LOGGER.debug("Loading audio file from disk (no cache): {}", request.getStaticResource());
					
					// Stream will be closed by audio player async, don't close it here
					this.resourceService.loadStreamByPath(request.getStaticResource()).ifPresent(request.getConsumer()::accept);
				}
			}
			catch (final Exception e) {
				if (e instanceof InterruptedException) {
					LOGGER.info(e.getClass().getName());
					break;
				}
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		LOGGER.debug("end");
	}
	
	public BlockingQueue<AudioLoaderRequest> getQueue() {
		return this.queue;
	}
	
	private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
		
		public LRUCache() {
			super((int) (CACHE_SIZE_LIMIT * 1.5), 0.75f, true);
		}
		
		@Override
		protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
			return this.size() >= CACHE_SIZE_LIMIT;
		}
	}
	
	private static record CacheEntry(byte[] data, AtomicLong lastAccessTime) {}
}
