package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.G2DRendererCondition;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.DrawImageRenderingBo;
import de.extio.game_engine.resource.StaticResource;

@Conditional(G2DRendererCondition.class)
@Component
public class G2DDrawImage extends G2DAbstractRenderingBo implements DrawImageRenderingBo {
	
	private static Map<String, CachedImage> IMAGE_CACHE = new HashMap<>();
	
	private StaticResource resource;
	
	private float transparency;
	
	private byte[] imageData;
	
	private String imageName;
	
	private int scaledX;
	
	private int scaledY;
	
	public G2DDrawImage() {
		super(RenderingBoLayer.BACKGROUND1);
		
		this.transparency = 1.0f;
	}
	
	@Override
	public DrawImageRenderingBo setResource(final StaticResource resource) {
		this.resource = resource;
		return this;
	}
	
	@Override
	public DrawImageRenderingBo setTransparency(final float transparency) {
		this.transparency = transparency;
		return this;
	}
	
	@Override
	public DrawImageRenderingBo setImageData(final byte[] data) {
		this.imageData = data;
		return this;
	}
	
	@Override
	public DrawImageRenderingBo setImageName(final String name) {
		this.imageName = name;
		return this;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		final var key = this.imageName != null && !this.imageName.isBlank() ? this.imageName : this.resource.toString();
		var cachedImage = IMAGE_CACHE.get(key);
		if (cachedImage == null) {
			cachedImage = new CachedImage();
			cachedImage.setName(key);
			
			if (this.imageData == null) {
				final var in = this.rendererData.getStaticResourceService().loadStreamByPath(this.resource);
				if (in.isPresent()) {
					try (var stream = in.get()) {
						if (stream != null) {
							cachedImage.setImage(ImageIO.read(stream));
						}
					}
					catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			else {
				try (InputStream in = new ByteArrayInputStream(this.imageData)) {
					cachedImage.setImage(ImageIO.read(in));
				}
				catch (final IOException e) {
					LOGGER.error(e.getMessage(), e);
					cachedImage.setImage(((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().getGraphicsConfiguration().createCompatibleImage(1, 1));
				}
			}
			
			IMAGE_CACHE.put(key, cachedImage);
			LOGGER.debug("Added image to cache: " + cachedImage.getName());
		}
		
		if (this.transparency < 1.0f) {
			final var currentAlphaComposite = graphics.getComposite();
			try {
				var ac = cachedImage.getAlphaComposite();
				if (ac == null) {
					ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.transparency);
					cachedImage.setAlphaComposite(ac);
				}
				
				graphics.setComposite(ac);
				this.doDraw(graphics, cachedImage, scaleFactor);
			}
			finally {
				graphics.setComposite(currentAlphaComposite);
			}
		}
		else if (this.transparency == 1.0f) {
			this.doDraw(graphics, cachedImage, scaleFactor);
		}
		
		cachedImage.setUsed(true);
	}
	
	@Override
	public void apply(final RenderingBo other) {
		super.apply(other);
		
		if (other instanceof final G2DDrawImage o) {
			this.resource = o.resource;
			this.transparency = o.transparency;
			this.imageData = o.imageData;
			this.imageName = o.imageName;
			this.scaledX = o.scaledX;
			this.scaledY = o.scaledY;
		}
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		
		this.resource = null;
		this.transparency = 1.0f;
		this.imageData = null;
		this.imageName = null;
		this.scaledX = 0;
		this.scaledY = 0;
	}
	
	@Override
	public void staticCleanupAfterFrame() {
		final var it = IMAGE_CACHE.values().iterator();
		while (it.hasNext()) {
			final var cachedImage = it.next();
			if (!cachedImage.isUsed()) {
				LOGGER.debug("Removed image from cache: " + cachedImage.getName());
				
				cachedImage.close();
				it.remove();
			}
			else {
				cachedImage.setUsed(false);
			}
		}
	}
	
	private void doDraw(final Graphics2D graphics, final CachedImage cachedImage, final double scaleFactor) {
		int x, y;
		if (this.scaledX != 0 || this.scaledY != 0) {
			x = this.scaledX;
			y = this.scaledY;
		}
		else {
			x = (int) (this.x * scaleFactor);
			y = (int) (this.y * scaleFactor);
		}
		
		if (this.width == 0 && this.height == 0) {
			graphics.drawImage(cachedImage.getImage(), x, y, null);
		}
		else {
			graphics.drawImage(cachedImage.getImage(), x, y, (int) (this.width * scaleFactor), (int) (this.height * scaleFactor), null);
		}
	}
	
	private static class CachedImage implements AutoCloseable {
		
		private String name;
		
		private BufferedImage image;
		
		private boolean used;
		
		private AlphaComposite alphaComposite;
		
		public boolean isUsed() {
			return this.used;
		}
		
		public void setUsed(final boolean used) {
			this.used = used;
		}
		
		public BufferedImage getImage() {
			return this.image;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setName(final String name) {
			this.name = name;
		}
		
		public void setImage(final BufferedImage image) {
			this.image = image;
		}
		
		public AlphaComposite getAlphaComposite() {
			return this.alphaComposite;
		}
		
		public void setAlphaComposite(final AlphaComposite alphaComposite) {
			this.alphaComposite = alphaComposite;
		}
		
		@Override
		public void close() {
			if (this.image != null) {
				this.image.flush();
			}
		}
		
		@Override
		public String toString() {
			return "CachedImage [name=" + this.name + ", used=" + this.used + "]";
		}
		
	}
	
}
