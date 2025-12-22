package de.extio.game_engine.renderer.work;

import java.util.List;
import java.util.Map;

import de.extio.game_engine.renderer.model.RenderingBo;

public interface RendererWorkingSet {
	
	void add(String producer, RenderingBo work);
	
	void add(String producer, List<RenderingBo> work);

	RenderingBo get(String producer, String id);
	
	Map<String, RenderingBo> getUncommittedWork(String producer);
	
	Map<String, RenderingBo> commit(String producer, boolean clone);
	
	void getLiveSet(List<RenderingBo> combinedLiveSet);
	
}
