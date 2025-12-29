package de.extio.game_engine.renderer.work;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import de.extio.game_engine.renderer.model.RenderingBo;

public interface RendererWorkingSet {
	
	void put(String producerId, RenderingBo work);
	
	void put(String producerId, List<RenderingBo> work);

	/**
	 * Returns the RenderingBo with the given id from the uncommitted work of the given producer.
	 * If found, a DEEP copy is returned. If not found, null is returned
	 * If a RenderingBo is modified after this call, the changes will NOT be reflected in the active working set. You must call this.{@link #put(String, RenderingBo)} to update the working set.
	 */
	RenderingBo get(String producerId, String id);

	/**
	 * Returns the RenderingBo with the given id from the uncommitted work of the given producer.
	 * If found, a DEEP copy is returned. If not found, null is returned
	 * If a RenderingBo is modified after this call, the changes will NOT be reflected in the active working set. You must call this.{@link #put(String, RenderingBo)} to update the working set.
	 */
	<T extends RenderingBo> T get(String producerId, String id, Class<T> type);

	/**
	 * Returns the RenderingBo with the given id from the uncommitted work of the given producer or creates a new one if not found.
	 * If found, a DEEP copy is returned. If not found, a new instance is acquired from the pool and returned.
	 * If a RenderingBo is modified after this call, the changes will NOT be reflected in the active working set. You must call this.{@link #put(String, RenderingBo)} to update the working set.
	 */
	<T extends RenderingBo> T getOrAcquire(String producerId, String id, Class<T> type);

	void remove(String producerId, String id);
	
	/**
	 * Returns an unmodifiable DEEP copy of the uncommitted work for the given producer.
	 * If a RenderingBo is modified after this call, the changes will NOT be reflected in the active working set. You must call this.{@link #put(String, RenderingBo)} to update the working set.
	 */
	Map<String, RenderingBo> getUncommittedWork(String producerId);
	
	Map<String, RenderingBo> commit(String producerId, boolean clone);
	
	void clearNext(String producerId);

	void clear(String producerId);

	void getLiveSet(List<RenderingBo> combinedLiveSet, Predicate<String> filter);
	
}