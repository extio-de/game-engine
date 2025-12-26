package de.extio.game_engine.renderer.work;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.RenderingBo;

/**
 * Pool for rendering business objects.
 * To render something, acquire a rendering business object from this manager, set all properties and add it to <i>List<RenderingBo> renderingBo</i> which is passed to various callback methods that are part of the rendering pipeline.
 * The renderer will then manage the bo and also takes care of releasing it back to the pool after rendering is complete.
 * It is important for performance reasons to always use pooled rendering business objects instead of creating new instances with every frame - keep in mind with HFR rendering this happens 120 times per second for hundrets of bos per cycle!
 * Another reason is encapsulation - your client code only operates on interfaces for rendering bos, the renderer implementation and therefore also the rendering bo implementations are interchangeable by design. This way we could switch to a different renderer without changing any other client code. 
 */
public interface RenderingBoPool {
	
	/**
	 * Acquires a rendering business object by type from the pool.
	 */
	<T extends RenderingBo> T acquire(String id, Class<T> clazz);

	<T extends RenderingBo> T copy(T original);

	/**
	 * Queues a rendering business object for return back to the pool. Renderer will release the BO after the current rendering cycle is complete.
	 */
	void returnToPool(RenderingBo obj);
	
	/**
	 * Releases all pending rendering business objects back to the pool. This is managed by the renderer.
	 */
	void releasePending();

	void setRendererData(RendererData rendererData);
}
