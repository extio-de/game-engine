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
	public void add(final String producerId, final RenderingBo work) {
		this.getWorkingSetByProducer(producerId)
				.next()
				.put(work.getId(), work);
	}
	
	@Override
	public void add(final String producerId, final List<RenderingBo> work) {
		final var nextMap = this.getWorkingSetByProducer(producerId).next();
		for (final RenderingBo bo : work) {
			nextMap.put(bo.getId(), bo);
		}
	}
	
	@Override
	public Map<String, RenderingBo> getUncommittedWork(final String producerId) {
		return this.getWorkingSetByProducer(producerId).next();
	}
	
	@Override
	public RenderingBo get(final String producerId, final String id) {
		return this.getWorkingSetByProducer(producerId).next().get(id);
	}
	
	@Override
	public Map<String, RenderingBo> commit(final String producerId, final boolean clone) {
		final AtomicReference<Map<String, RenderingBo>> previousWork = new AtomicReference<>();
		final RendererWork rendererWork = this.workingSet.compute(producerId, (k, v) -> {
			if (v == null) {
				return new RendererWork(this.obtainMapFromPool(), this.obtainMapFromPool());
			}
			previousWork.set(v.live());
			if (clone) {
				final Map<String, RenderingBo> newNext = this.obtainMapFromPool();
				newNext.putAll(v.next());
				return new RendererWork(v.next(), newNext);
			}
			else {
				return new RendererWork(v.next(), this.obtainMapFromPool());
			}
		});
		
		final var previousWorkMap = previousWork.get();
		if (previousWorkMap != null) {
			this.releaseBoClasses(previousWorkMap);
		}
		
		return rendererWork.next();
	}
	
	@Override
	public void clear(final String producerId) {
		final RendererWork rendererWork = this.workingSet.remove(producerId);
		if (rendererWork != null) {
			this.releaseBoClasses(rendererWork.live());
			this.releaseBoClasses(rendererWork.next());
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
	
	private void releaseBoClasses(final Map<String, RenderingBo> work) {
		if (work == null || work.isEmpty()) {
			return;
		}

		final var releasedBoClasses = this.obtainSetFromPool();
		for (final RenderingBo renderingBO : work.values()) {
			if (releasedBoClasses.add(renderingBO.getClass())) {
				renderingBO.closeStatic();
			}
			this.rendererBoPool.release(renderingBO);
		}
		
		this.returnMapToPool(work);
		this.returnSetToPool(releasedBoClasses);
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
	
	private Set<Class<? extends RenderingBo>> obtainSetFromPool() {
		var set = this.setsPool.poll();
		if (set == null) {
			set = new HashSet<>();
		}
		return set;
	}
	
	private void returnSetToPool(final Set<Class<? extends RenderingBo>> set) {
		set.clear();
		this.setsPool.offer(set);
	}
	
	private static record RendererWork(Map<String, RenderingBo> live, Map<String, RenderingBo> next) {
		
	}
}
