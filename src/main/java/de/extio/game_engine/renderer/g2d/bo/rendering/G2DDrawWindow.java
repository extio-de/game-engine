package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Graphics2D;

import de.extio.game_engine.renderer.g2d.control.components.ComponentRenderingSupport;
import de.extio.game_engine.renderer.model.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.RgbaColor;
import de.extio.game_engine.renderer.model.bo.DrawWindowRenderingBo;

public class G2DDrawWindow extends G2DAbstractRenderingBo implements DrawWindowRenderingBo {
	
	private static final RgbaColor COLOR_WINDOW = new ImmutableRgbaColor(ComponentRenderingSupport.COLOR_COMPONENT_BORDER1.getRed(), ComponentRenderingSupport.COLOR_COMPONENT_BORDER1.getGreen(), ComponentRenderingSupport.COLOR_COMPONENT_BORDER1.getBlue());
	
	private boolean thickBorder;
	
	public G2DDrawWindow() {
		super(RenderingBoLayer.UI0_BGR);
		
		this.color = COLOR_WINDOW;
	}
	
	@Override
	public DrawWindowRenderingBo setThickBorder(final boolean thickBorder) {
		this.thickBorder = thickBorder;
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		final var sx = (int) (this.x * scaleFactor);
		final var sy = (int) (this.y * scaleFactor);
		final var sw = (int) (this.width * scaleFactor);
		final var sh = (int) (this.height * scaleFactor);
		final var strength = Math.max(2, (int) (3 * scaleFactor));
		if (this.color == null) {
			this.color = COLOR_WINDOW;
		}
		
		final var cMain = this.color.toAwtColor();
		final var rgbaDarker = new RgbaColor(cMain);
		rgbaDarker.setR(rgbaDarker.getR() / 2);
		rgbaDarker.setG(rgbaDarker.getG() / 2);
		rgbaDarker.setB(rgbaDarker.getB() / 2);
		final var cDarker = rgbaDarker.toAwtColor();
		
		if (this.thickBorder) {
			final var borderStrength = strength * 2;
			graphics.setColor(cMain);
			graphics.fillRect(sx + strength * 2, sy + strength * 2, sw - strength * 4, borderStrength);
			graphics.setColor(cDarker);
			graphics.fillRect(sx + strength * 2, sy + strength * 2 + borderStrength, sw - strength * 4, strength);
		}
		
		ComponentRenderingSupport.drawDecorativeBorder(graphics, sx, sy, sw, sh, strength, cDarker);
		ComponentRenderingSupport.drawDecorativeBorderFilled(graphics, sx + strength, sy + strength, sw - strength * 2, sh - strength * 2, strength, cMain);
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		this.color = COLOR_WINDOW;
		this.thickBorder = false;
	}
	
}
