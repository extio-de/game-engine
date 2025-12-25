package de.extio.game_engine.renderer.work;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import de.extio.game_engine.renderer.model.RenderingBo;

public class RendererWorkingSetImpl implements RendererWorkingSet {
	
	private final ConcurrentMap<String, RendererWork> workingSet = new ConcurrentHashMap<>();
	
	private final BlockingQueue<Map<String, RenderingBo>> mapsPool = new ArrayBlockingQueue<>(100);
	
	private final BlockingQueue<Set<Class<? extends RenderingBo>>> setsPool = new ArrayBlockingQueue<>(100);
	
	private final RenderingBoPool rendererBoPool;
	
	public RendererWorkingSetImpl(final RenderingBoPool rendererBoPool) {
		this.rendererBoPool = rendererBoPool;
	}
	
	@Override
	public void put(final String producerId, final RenderingBo work) {
		final var previous = this.getWorkingSetByProducer(producerId)
				.next()
				.put(work.getId(), work);
		if (previous != null) {
			this.rendererBoPool.release(previous);
		}
	}
	
	@Override
	public void put(final String producerId, final List<RenderingBo> work) {
		final var nextMap = this.getWorkingSetByProducer(producerId).next();
		for (final RenderingBo bo : work) {
			final var previous = nextMap.put(bo.getId(), bo);
			if (previous != null) {
				this.rendererBoPool.release(previous);
			}
		}
	}
	
	@Override
	public void remove(final String producerId, final String id) {
		final var previous = this.getWorkingSetByProducer(producerId)
				.next()
				.remove(id);
		if (previous != null) {
			this.rendererBoPool.release(previous);
		}
	}
	
	@Override
	public Map<String, RenderingBo> getUncommittedWork(final String producerId) {
		return Map.copyOf(this.getWorkingSetByProducer(producerId).next());
	}
	
	@Override
	public RenderingBo get(final String producerId, final String id) {
		return this.getWorkingSetByProducer(producerId).next().get(id);
	}
	
	@Override
	public <T extends RenderingBo> T get(final String producerId, final String id, final Class<T> type) {
		final RenderingBo bo = this.getWorkingSetByProducer(producerId).next().get(id);
		if (bo == null) {
			return null;
		}
		return type.cast(bo);
	}
	
	@Override
	public <T extends RenderingBo> T getOrAcquire(final String producerId, final String id, final Class<T> type) {
		final var bo = this.get(producerId, id, type);
		if (bo != null) {
			return bo;
		}
		return this.rendererBoPool.acquire(id, type);
	}
	
	@Override
	public Map<String, RenderingBo> commit(final String producerId, final boolean clone) {
		final AtomicReference<Map<String, RenderingBo>> previousLiveRef = new AtomicReference<>();
		final RendererWork rendererWork = this.workingSet.compute(producerId, (k, v) -> {
			if (v == null) {
				return new RendererWork(this.obtainMapFromPool(), this.obtainMapFromPool());
			}
			previousLiveRef.set(v.live());
			if (clone) {
				final Map<String, RenderingBo> newNext = this.obtainMapFromPool();
				newNext.putAll(v.next());
				return new RendererWork(v.next(), newNext);
			}
			else {
				v.live().values().forEach(this.rendererBoPool::release);
				return new RendererWork(v.next(), this.obtainMapFromPool());
			}
		});
		
		final var previousLiveSet = previousLiveRef.get();
		if (previousLiveSet != null) {
			this.returnMapToPool(previousLiveSet);
		}
		
		return rendererWork.next();
	}
	
	@Override
	public void clear(final String producerId) {
		final RendererWork rendererWork = this.workingSet.remove(producerId);
		if (rendererWork != null) {
			rendererWork.live().values().forEach(this.rendererBoPool::release);
			rendererWork.next().values().forEach(this.rendererBoPool::release);
		}
	}
	
	@Override
	public void getLiveSet(final List<RenderingBo> combinedLiveSet, final Predicate<String> filter) {
		this.workingSet.forEach((producer, rendererWork) -> {
			if (filter == null || filter.test(producer)) {
				combinedLiveSet.addAll(rendererWork.live().values());
			}
		});
	}
	
	private RendererWork getWorkingSetByProducer(final String producerId) {
		return this.workingSet.computeIfAbsent(producerId, k -> new RendererWork(this.obtainMapFromPool(), this.obtainMapFromPool()));
	}
	
	private Map<String, RenderingBo> obtainMapFromPool() {
		var map = this.mapsPool.poll();
		if (map == null) {
			map = Collections.synchronizedMap(new HashMap<>());
		}
		return map;
	}
	
	private void returnMapToPool(final Map<String, RenderingBo> map) {
		map.clear();
		this.mapsPool.offer(map);
	}
	
	private static record RendererWork(Map<String, RenderingBo> live, Map<String, RenderingBo> next) {
		
	}
}
