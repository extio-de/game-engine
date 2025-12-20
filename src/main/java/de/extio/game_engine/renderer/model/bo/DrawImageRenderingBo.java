package de.extio.game_engine.renderer.model.bo;

import de.extio.game_engine.renderer.model.RenderingBoHasDimension;

/**
 * Draws an image. You can either set the resource name (and optional mod name) or the raw image data as byte[] in png format.
 */
public interface DrawImageRenderingBo extends RenderingBoHasDimension {
	
	DrawImageRenderingBo setResourceName(String resourceName);
	
	DrawImageRenderingBo setImageData(byte[] data);
	
	/**
	 * @param transparency Values between 0.0 and 1.0
	 */
	DrawImageRenderingBo setTransparency(float transparency);
	
}
