package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;

import de.extio.game_engine.renderer.model.RenderingBoLayer;

public class G2DDrawFpsHistory extends G2DAbstractRenderingBo {
	
	private Collection<Integer> history;
	
	private final int[] thresholds = new int[4];
	
	private int thresholdsFrameRate;
	
	public G2DDrawFpsHistory() {
		super(RenderingBoLayer.TOP);
	}
	
	public G2DDrawFpsHistory setHistory(final Collection<Integer> history) {
		this.history = history;
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (this.thresholdsFrameRate != this.rendererData.getRendererControl().getFrameRate()) {
			this.thresholdsFrameRate = this.rendererData.getRendererControl().getFrameRate();
			this.thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate() * 0.2);
			this.thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate() * 0.75);
			this.thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate() * 0.9);
			this.thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate());
		}
		
		final var x = (int) (this.x * scaleFactor);
		final var y = (int) (this.y * scaleFactor);
		var i = 0;
		for (final Integer val : this.history) {
			if (val == null) {
				continue;
			}
			final var val_ = Math.max(1, Math.min((int) (100 * scaleFactor), (int) (val.intValue() * scaleFactor)));
			
			if (val_ < this.thresholds[0]) {
				graphics.setColor(Color.DARK_GRAY);
			}
			else if (val_ < this.thresholds[1]) {
				graphics.setColor(Color.GRAY);
			}
			else if (val_ < this.thresholds[2]) {
				graphics.setColor(Color.YELLOW);
			}
			else if (val_ < this.thresholds[3]) {
				graphics.setColor(Color.ORANGE);
			}
			else {
				graphics.setColor(Color.RED);
			}
			
			graphics.drawLine(x + i, y, x + i++, y + val_);
		}
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		this.history = null;
	}
}
