package de.extio.game_engine.renderer.work;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import de.extio.game_engine.renderer.model.RenderingBo;

public interface RendererWorkingSet {
	
	void put(String producerId, RenderingBo work);
	
	void put(String producerId, List<RenderingBo> work);

	RenderingBo get(String producerId, String id);

	<T extends RenderingBo> T get(String producerId, String id, Class<T> type);

	<T extends RenderingBo> T getOrAcquire(String producerId, String id, Class<T> type);

	void remove(String producerId, String id);
	
	Map<String, RenderingBo> getUncommittedWork(String producerId);
	
	Map<String, RenderingBo> commit(String producerId, boolean clone);
	
	void clearNext(String producerId);

	void clear(String producerId);

	void getLiveSet(List<RenderingBo> combinedLiveSet, Predicate<String> filter);
	
}