package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;

@Conditional(G2DRendererCondition.class)
@Component
public class G2DDrawFpsHistory extends G2DAbstractRenderingBo {
	
	private Collection<Integer> history;
	
	private final static int[] thresholds = new int[4];
	
	private static int thresholdsFrameRate;
	
	public G2DDrawFpsHistory() {
		super(RenderingBoLayer.TOP);
	}
	
	public G2DDrawFpsHistory setHistory(final Collection<Integer> history) {
		this.history = history;
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (thresholdsFrameRate != this.rendererData.getRendererControl().getFrameRate()) {
			thresholdsFrameRate = this.rendererData.getRendererControl().getFrameRate();
			thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate() * 0.2);
			thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate() * 0.75);
			thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate() * 0.9);
			thresholds[0] = (int) (1000.0 / this.rendererData.getRendererControl().getFrameRate());
		}
		
		final var x = (int) (this.x * scaleFactor);
		final var y = (int) (this.y * scaleFactor);
		var i = 0;
		for (final Integer val : this.history) {
			if (val == null) {
				continue;
			}
			final var val_ = Math.max(1, Math.min((int) (100 * scaleFactor), (int) (val.intValue() * scaleFactor)));
			
			if (val_ < thresholds[0]) {
				graphics.setColor(Color.DARK_GRAY);
			}
			else if (val_ < thresholds[1]) {
				graphics.setColor(Color.GRAY);
			}
			else if (val_ < thresholds[2]) {
				graphics.setColor(Color.YELLOW);
			}
			else if (val_ < thresholds[3]) {
				graphics.setColor(Color.ORANGE);
			}
			else {
				graphics.setColor(Color.RED);
			}
			
			graphics.drawLine(x + i, y, x + i++, y + val_);
		}
	}
	
	@Override
	public void apply(final RenderingBo other) {
		super.apply(other);

		if (other instanceof final G2DDrawFpsHistory o) {
			this.history = o.history;
		}
	}

	@Override
	public void close() throws Exception {
		super.close();
		this.history = null;
	}
}
