package de.extio.game_engine.renderer.model.bo;

import java.util.List;

import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * This bo renders on screen effects. Required properties are dependent on the effect (must be set with #setEffect). Best is to search for examples in shipped groovy code.
 */
public interface DrawEffectRenderingBo extends RenderingBo {
	
	DrawEffectRenderingBo setEffect(DrawEffectRenderingBoEffects effect);
	
	public DrawEffectRenderingBo setCustomInt0(int customInt0);
	
	public DrawEffectRenderingBo setCustomDouble0(double customDouble0);
	
	public DrawEffectRenderingBo setCustomString0(String customString0);
	
	List<CoordI2> getRelativeCoordinates();
	
	DrawEffectRenderingBo setRelativeCoordinates(List<CoordI2> coordinates);
	
}
