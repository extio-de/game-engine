package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.DrawWindowRenderingBo;
import de.extio.game_engine.renderer.model.color.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.color.RgbaColor;

@Conditional(G2DRendererCondition.class)
@Component
public class G2DDrawWindow2 extends G2DAbstractRenderingBo implements DrawWindowRenderingBo {
	
	private static final Map<Long, G2DWindowCacheEntry> CACHE = new HashMap<>();
	
	private static final RgbaColor COLOR_WINDOW_BGR = new ImmutableRgbaColor(119, 119, 189);
	
	private static double LAST_SCALEFACTOR;
	
	private static AlphaComposite alphaComposite;
	
	private boolean thickBorder;
	
	private Color awtColor;
	
	public G2DDrawWindow2() {
		super(RenderingBoLayer.UI0_BGR);
		
		this.color = COLOR_WINDOW_BGR;
	}
	
	@Override
	public DrawWindowRenderingBo setThickBorder(final boolean thickBorder) {
		this.thickBorder = thickBorder;
		return this;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		this.awtColor = this.color == null ? COLOR_WINDOW_BGR.toAwtColor() : this.color.toAwtColor();
		
		if (scaleFactor != LAST_SCALEFACTOR) {
			CACHE.clear();
			LAST_SCALEFACTOR = scaleFactor;
		}
		
		final var key = Long.valueOf((long) this.width | ((long) this.height << 12) | ((this.thickBorder ? 1L : 0L) << 24) | ((long) this.awtColor.getRGB() << 32));
		var entry = CACHE.get(key);
		if (entry == null) {
			final var img = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().getGraphicsConfiguration().createCompatibleImage((int) (this.width * scaleFactor), (int) (this.height * scaleFactor), Transparency.TRANSLUCENT);
			this.paintWindow(img, scaleFactor);
			
			entry = new G2DWindowCacheEntry();
			entry.setImage(img);
			CACHE.put(key, entry);
			
		}
		entry.setUsed(true);
		
		final var currentAlphaComposite = graphics.getComposite();
		try {
			if (alphaComposite == null) {
				alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6F);
			}
			
			graphics.setComposite(alphaComposite);
			graphics.drawImage(entry.getImage(), (int) (this.x * scaleFactor), (int) (this.y * scaleFactor), null);
		}
		finally {
			graphics.setComposite(currentAlphaComposite);
		}
	}
	
	@Override
	public void apply(final RenderingBo other) {
		super.apply(other);

		if (other instanceof final G2DDrawWindow2 o) {
			this.color = o.color;
			this.thickBorder = o.thickBorder;
			this.awtColor = o.awtColor;
		}
	}

	@Override
	public void close() throws Exception {
		super.close();
		this.color = COLOR_WINDOW_BGR;
		this.thickBorder = false;
		this.awtColor = null;
	}
	
	@Override
	public void closeStatic() {
		final var it = CACHE.values().iterator();
		while (it.hasNext()) {
			final var entry = it.next();
			if (!entry.isUsed()) {
				entry.close();
				it.remove();
			}
			else {
				entry.setUsed(false);
			}
		}
	}
	
	private void paintWindow(final BufferedImage img, final double scaleFactor) {
		final var imgGraphics = img.createGraphics();
		try {
			final var swidth = (int) (this.width * scaleFactor);
			final var sheight = (int) (this.height * scaleFactor);
			final var baseColor = new float[3];
			this.awtColor.getRGBColorComponents(baseColor);
			Color.RGBtoHSB((int) (baseColor[0] * 255), (int) (baseColor[1] * 255), (int) (baseColor[2] * 255), baseColor);
			
			final var size = (int) (5 * scaleFactor);
			
			imgGraphics.setColor(Color.BLACK);
			imgGraphics.fillRect(0, size, swidth, sheight - size * 3);
			imgGraphics.fillRect(size, sheight - size * 2, swidth - size * 2, size);
			
			imgGraphics.setColor(Color.getHSBColor(baseColor[0], baseColor[1], baseColor[2]));
			if (this.thickBorder) {
				imgGraphics.fillRect(0, size * 2, size, sheight - size * 4);
				imgGraphics.fillRect(swidth - size, size * 2, swidth, sheight - size * 4);
				imgGraphics.fillRect(0, size * 2, swidth, size);
			}
			else {
				imgGraphics.fillRect(0, 0, size, sheight - size * 2);
				imgGraphics.fillRect(swidth - size, 0, swidth, sheight - size * 2);
				imgGraphics.fillRect(0, 0, swidth, size);
			}
			imgGraphics.fillRect(size * 2, sheight - size, swidth - size * 4, size);
			imgGraphics.fillPolygon(new int[] { 0, size * 2, size * 2, size }, new int[] { sheight - size * 2, sheight, sheight - size, sheight - size * 2 }, 4);
			imgGraphics.fillPolygon(new int[] { swidth, swidth - size * 2, swidth - size * 2, swidth - size }, new int[] { sheight - size * 2, sheight, sheight - size, sheight - size * 2 }, 4);
			
			if (this.thickBorder) {
				for (var i = 0; i < size * 2; i++) {
					imgGraphics.setColor(Color.getHSBColor(baseColor[0], baseColor[1], baseColor[2] * ((0.5F / (size * 2) * i) + 0.5F)));
					imgGraphics.drawLine(0, i, swidth, i);
				}
			}
		}
		finally {
			imgGraphics.dispose();
		}
	}
	
	private static class G2DWindowCacheEntry implements AutoCloseable {
		
		private BufferedImage image;
		
		private boolean used;
		
		public BufferedImage getImage() {
			return this.image;
		}
		
		public void setImage(final BufferedImage image) {
			this.image = image;
		}
		
		public boolean isUsed() {
			return this.used;
		}
		
		public void setUsed(final boolean used) {
			this.used = used;
		}
		
		@Override
		public void close() {
			if (this.image != null) {
				this.image.flush();
			}
		}
		
	}
	
}
