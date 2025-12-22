package de.extio.game_engine.renderer;

public interface Renderer {
	
	void show();
	
	void run() throws InterruptedException;
	
	void reset();

	void shutdown();
	
	void takeScreenshot();
	
	void setTitle(final String title);
	
	void setRendererData(final RendererData rendererData);

}
