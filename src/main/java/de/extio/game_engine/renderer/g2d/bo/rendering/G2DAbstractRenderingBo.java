package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Graphics2D;

import de.extio.game_engine.renderer.model.AbstractRenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;

public abstract class G2DAbstractRenderingBo extends AbstractRenderingBo {
	
	public G2DAbstractRenderingBo(final RenderingBoLayer layer) {
		super(layer);
	}
	
	public boolean isScreenshotRelevant() {
		return true;
	}
	
	public abstract void render(Graphics2D graphics, double scaleFactor, boolean force);
	
	public void closeStatic() {
		
	}
}
