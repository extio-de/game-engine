package de.extio.game_engine.renderer.g2d.control;

import java.awt.Color;
import java.awt.Graphics2D;

import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.bo.rendering.G2DAbstractRenderingBo;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.color.MutableRgbaColor;
import de.extio.game_engine.spatial2.model.CoordI2;

@Component
public class G2DDrawControlTooltip extends G2DAbstractRenderingBo {
	
	public static record TooltipRecord(CoordI2 position, String text) {
	}
	
	public static TooltipRecord TOOLTIP = null;
	
	private static Color BACKGROUND = (new MutableRgbaColor(0, 0, 0, 150)).toAwtColor();
	
	public G2DDrawControlTooltip() {
		super(RenderingBoLayer.TOP);
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		if (TOOLTIP == null) {
			return;
		}
		
		final var textDim = G2DDrawFont.getTextDimensions(TOOLTIP.text(), graphics, 14, scaleFactor);
		
		graphics.setColor(BACKGROUND);
		graphics.fillRect(TOOLTIP.position().getX(), TOOLTIP.position().getY(), textDim.getX() + 20, textDim.getY() + 20);
		
		graphics.setColor(Color.WHITE);
		G2DDrawFont.renderText(graphics, scaleFactor, (int) ((TOOLTIP.position().getX() + 10) / scaleFactor), (int) ((TOOLTIP.position().getY() + 10) / scaleFactor), 14, TOOLTIP.text());
	}
	
	public static void closeStatic() {
		TOOLTIP = null;
	}
}
