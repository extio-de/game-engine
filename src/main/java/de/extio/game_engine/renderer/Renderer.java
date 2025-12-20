package de.extio.game_engine.renderer;

import java.util.List;

import de.extio.game_engine.renderer.model.RenderingBo;

public interface Renderer {
	
	void show();
	
	void run(List<RenderingBo> renderingBOs) throws InterruptedException;
	
	void reset();
	
	void takeScreenshot();
	
	void setTitle(final String title);
	
	void setRendererData(final RendererData rendererData);

}
