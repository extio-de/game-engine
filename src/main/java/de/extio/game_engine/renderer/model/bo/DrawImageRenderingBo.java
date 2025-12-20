/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
