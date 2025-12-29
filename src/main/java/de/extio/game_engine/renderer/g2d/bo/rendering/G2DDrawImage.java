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
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

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
		super(RenderingBoLayer.UI0);
		
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
		int x, y, w, h;
		final int visX, visY, visW, visH;
		if (this.scaledX != 0 || this.scaledY != 0) {
			x = this.scaledX;
			y = this.scaledY;
		}
		else {
			x = (int) (this.x * scaleFactor);
			y = (int) (this.y * scaleFactor);
		}
		w = (int) ((this.width > 0 ? this.width : cachedImage.getImage().getWidth()) * scaleFactor);
		h = (int) ((this.height > 0 ? this.height : cachedImage.getImage().getHeight()) * scaleFactor);
		visX = (int) (this.visibleAreaX * scaleFactor);
		visY = (int) (this.visibleAreaY * scaleFactor);
		visW = (int) (this.visibleAreaWidth * scaleFactor);
		visH = (int) (this.visibleAreaHeight * scaleFactor);
	
		Area2 intersection = null;
		if (visX != 0 || visY != 0 || visW != 0 || visH != 0) {
			final var controlArea = new Area2(ImmutableCoordI2.create(x, y), ImmutableCoordI2.create(w, h));
			final var visibleArea = new Area2(ImmutableCoordI2.create(visX, visY), ImmutableCoordI2.create(visW, visH));
			intersection = SpatialUtils2.intersectAreas(controlArea, visibleArea);
			if (intersection == null) {
				return;
			}
		}

		if (intersection != null) {
			try {
				final double dstPerSrcX = (double) w / (double) cachedImage.getImage().getWidth();
				final double dstPerSrcY = (double) h / (double) cachedImage.getImage().getHeight();
				graphics.drawImage(
					cachedImage.getImage(),
					intersection.getPosition().getX(),
					intersection.getPosition().getY(),
					intersection.getPosition().getX() + intersection.getDimension().getX(),
					intersection.getPosition().getY() + intersection.getDimension().getY(),
					Math.max(0, Math.min(cachedImage.getImage().getWidth(), (int) Math.round((intersection.getPosition().getX() - x) / dstPerSrcX))),
					Math.max(0, Math.min(cachedImage.getImage().getHeight(), (int) Math.round((intersection.getPosition().getY() - y) / dstPerSrcY))),
					Math.max(0, Math.min(cachedImage.getImage().getWidth(), (int) Math.round((intersection.getPosition().getX() - x + intersection.getDimension().getX()) / dstPerSrcX))),
					Math.max(0, Math.min(cachedImage.getImage().getHeight(), (int) Math.round((intersection.getPosition().getY() - y + intersection.getDimension().getY()) / dstPerSrcY))),
					null);
			}
			catch (final Exception e) {
				LOGGER.error("Error drawing image '" + cachedImage.getName() + "': " + e.getMessage(), e);
			}
		}
		else if (this.width	== 0 && this.height == 0) {
			graphics.drawImage(cachedImage.getImage(), x, y, null);
		}
		else {
			graphics.drawImage(cachedImage.getImage(), x, y, w, h, null);
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
