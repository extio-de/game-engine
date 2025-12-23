package de.extio.game_engine.renderer.work;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.model.RenderingBo;

public interface RendererWorkingSet {
	
	void add(Class<? extends AbstractClientModule> producer, RenderingBo work);
	
	void add(Class<? extends AbstractClientModule> producer, List<RenderingBo> work);

	RenderingBo get(Class<? extends AbstractClientModule> producer, String id);
	
	Map<String, RenderingBo> getUncommittedWork(Class<? extends AbstractClientModule> producer);
	
	Map<String, RenderingBo> commit(Class<? extends AbstractClientModule> producer, boolean clone);
	
	void clear(Class<? extends AbstractClientModule> producer);

	void getLiveSet(List<RenderingBo> combinedLiveSet, Predicate<Class<? extends AbstractClientModule>> filter);
	
}
