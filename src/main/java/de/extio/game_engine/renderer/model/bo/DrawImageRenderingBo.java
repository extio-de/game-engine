package de.extio.game_engine.renderer.model.bo;

import de.extio.game_engine.renderer.model.RenderingBoHasDimension;
import de.extio.game_engine.resource.StaticResource;

/**
 * Draws an image. You can either set the resource name (and optional mod name) or the raw image data as byte[] in png format.
 */
public interface DrawImageRenderingBo extends RenderingBoHasDimension {
	
	DrawImageRenderingBo setResource(StaticResource resourceName);
	
	/**
	 * Also ImageName must be set if byte[] image data is used.
	 */
	DrawImageRenderingBo setImageData(byte[] data);

	/**
	 * Must be set if byte[] image data is used.
	 */
	DrawImageRenderingBo setImageName(String name);
	
	/**
	 * @param transparency Values between 0.0 and 1.0
	 */
	DrawImageRenderingBo setTransparency(float transparency);
	
}
