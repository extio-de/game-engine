/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.renderer;

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
	<T extends RenderingBo> T acquire(Class<T> clazz);
	
	/**
	 * Releases a rendering business object back to the pool. This is usually managed by the renderer.
	 */
	void release(RenderingBo obj);
	
}
