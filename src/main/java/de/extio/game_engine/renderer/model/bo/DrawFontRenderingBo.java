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
