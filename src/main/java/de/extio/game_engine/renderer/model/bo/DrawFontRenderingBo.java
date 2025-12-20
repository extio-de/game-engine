/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.renderer.model.bo;

import de.extio.game_engine.renderer.model.DrawFontRenderingBoTextAlignment;
import de.extio.game_engine.renderer.model.RenderingBoHasDimension;

/**
 * Draws text
 */
public interface DrawFontRenderingBo extends RenderingBoHasDimension {
	
	DrawFontRenderingBo setText(final String text);
	
	DrawFontRenderingBo setSize(final int size);
	
	DrawFontRenderingBo setAlignment(DrawFontRenderingBoTextAlignment alignment);
	
}
