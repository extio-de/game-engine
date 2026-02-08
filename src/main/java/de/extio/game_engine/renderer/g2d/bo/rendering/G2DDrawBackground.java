package de.extio.game_engine.renderer.g2d.bo.rendering;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import de.extio.game_engine.renderer.g2d.G2DRendererControl;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.options.UiOptions;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class G2DDrawBackground extends G2DAbstractRenderingBo {
	
	private static VolatileImage BACKGROUND_IMAGE;
	
	private static BufferedImage BACKGROUND_BUFFEREDIMAGE;
	
	private static StaticResource BACKGROUND_KEY;
	
	private final static int[] BUILTIN_SCROLL_OFFSET_X = new int[2];
	
	private final CoordI2 bgrOffset = MutableCoordI2.create();
	
	private final CoordI2 sourceOffset = MutableCoordI2.create();
	
	private final CoordI2 destPosition = MutableCoordI2.create();
	
	private final CoordI2 destPosition2 = MutableCoordI2.create();
	
	private final CoordI2 overflow = MutableCoordI2.create();
	
	public G2DDrawBackground() {
		super(RenderingBoLayer.BACKGROUND0);
	}
	
	@Override
	public void apply(final RenderingBo other) {
		super.apply(other);
		
		if (other instanceof final G2DDrawBackground o) {
			this.bgrOffset.setXY(o.bgrOffset);
			this.sourceOffset.setXY(o.sourceOffset);
			this.destPosition.setXY(o.destPosition);
			this.destPosition2.setXY(o.destPosition2);
			this.overflow.setXY(o.overflow);
		}
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		
		this.bgrOffset.setXY(0, 0);
		this.sourceOffset.setXY(0, 0);
		this.destPosition.setXY(0, 0);
		this.destPosition2.setXY(0, 0);
		this.overflow.setXY(0, 0);
	}
	
	@Override
	public boolean isScreenshotRelevant() {
		return false;
	}
	
	@Override
	public void render(final Graphics2D graphics, final double scaleFactor, final boolean force) {
		final var windowDim = ((G2DRendererControl) this.rendererData.getRendererControl()).getAbsoluteViewportDimension();
		
		this.scroll(0, this.rendererData.getUiOptions().isBackgroundScrolling0(), this.rendererData.getUiOptions().isBackgroundScrollingReverse0(), windowDim);
		this.tileBackground(graphics, 0, this.rendererData.getUiOptions().getBackgroundResource0(),
				this.bgrOffset.setXY(this.rendererData.getUiOptions().getBackgroundOffset0()).add(BUILTIN_SCROLL_OFFSET_X[0], 0), windowDim);
		
		this.scroll(1, this.rendererData.getUiOptions().isBackgroundScrolling1(), this.rendererData.getUiOptions().isBackgroundScrollingReverse1(), windowDim);
		((G2DThemeManager) this.rendererData.getThemeManager()).getCurrentPatternRenderer().drawBackgroundPattern(graphics, this.bgrOffset.setXY(this.rendererData.getUiOptions().getBackgroundOffset1()).add(BUILTIN_SCROLL_OFFSET_X[1], 0), windowDim);
	}
	
	private void tileBackground(final Graphics2D graphics, final int index, final StaticResource backgroundResource, final CoordI2 offset, final CoordI2 viewPort) {
		if (!Objects.equals(backgroundResource, G2DDrawBackground.BACKGROUND_KEY)) {
			this.loadImage(graphics, index, backgroundResource);
		}
		if (G2DDrawBackground.BACKGROUND_IMAGE == null) {
			return;
		}
		else if (BACKGROUND_IMAGE.validate(graphics.getDeviceConfiguration()) != VolatileImage.IMAGE_OK) {
			this.createVolatileBackgroundImage(graphics, index);
		}
		
		final var uiOptions = this.rendererData.getUiOptions();
		if (uiOptions.getBackgroundMode0() == UiOptions.BackgroundMode.SCALING) {
			graphics.drawImage(G2DDrawBackground.BACKGROUND_IMAGE,
					0, 0,
					viewPort.getX(), viewPort.getY(),
					0, 0,
					G2DDrawBackground.BACKGROUND_IMAGE.getWidth(), G2DDrawBackground.BACKGROUND_IMAGE.getHeight(),
					null);
			return;
		}
		
		this.sourceOffset.setXY(Math.floorMod(offset.getX(), G2DDrawBackground.BACKGROUND_IMAGE.getWidth()), Math.floorMod(offset.getY(), G2DDrawBackground.BACKGROUND_IMAGE.getHeight()));
		this.destPosition.setXY(0, 0);
		do {
			this.destPosition2
					.setXY(this.destPosition)
					.add(G2DDrawBackground.BACKGROUND_IMAGE.getWidth(), G2DDrawBackground.BACKGROUND_IMAGE.getHeight())
					.substract(this.sourceOffset);
			
			this.overflow
					.setXY(viewPort)
					.substract(this.destPosition2);
			
			graphics.drawImage(G2DDrawBackground.BACKGROUND_IMAGE,
					this.destPosition.getX(), //dx1
					this.destPosition.getY(),
					this.destPosition2.getX() + Math.min(0, this.overflow.getX()), //dx2
					this.destPosition2.getY() + Math.min(0, this.overflow.getY()),
					this.sourceOffset.getX(), //sx1
					this.sourceOffset.getY(),
					G2DDrawBackground.BACKGROUND_IMAGE.getWidth() + Math.min(0, this.overflow.getX()), //sx2
					G2DDrawBackground.BACKGROUND_IMAGE.getHeight() + Math.min(0, this.overflow.getY()),
					null);
			
			this.destPosition.add(G2DDrawBackground.BACKGROUND_IMAGE.getWidth() - this.sourceOffset.getX(), 0);
			if (this.destPosition.getX() < viewPort.getX()) {
				this.sourceOffset.setX(0);
			}
			else {
				this.destPosition.add(0, G2DDrawBackground.BACKGROUND_IMAGE.getHeight() - this.sourceOffset.getY());
				this.destPosition.setX(0);
				this.sourceOffset.setXY(Math.floorMod(offset.getX(), G2DDrawBackground.BACKGROUND_IMAGE.getWidth()), 0);
			}
		} while (this.destPosition.getY() < viewPort.getY());
	}
	
	private void scroll(final int index, final boolean scrolling, final boolean reverse, final CoordI2 viewPort) {
		if (!scrolling) {
			BUILTIN_SCROLL_OFFSET_X[index] = 0;
		}
		else if (this.rendererData.getFrame() % Math.max(1, this.rendererData.getRendererControl().getFrameRate() / 30) == 0) {
			if (G2DDrawBackground.BACKGROUND_IMAGE != null) {
				this.bgrOffset.setXY(G2DDrawBackground.BACKGROUND_IMAGE.getWidth(), G2DDrawBackground.BACKGROUND_IMAGE.getHeight());
			}
			else {
				this.bgrOffset.setXY(viewPort);
			}
			
			if (reverse) {
				if (--BUILTIN_SCROLL_OFFSET_X[index] < -this.bgrOffset.getX()) {
					BUILTIN_SCROLL_OFFSET_X[index] += this.bgrOffset.getX();
				}
			}
			else {
				if (++BUILTIN_SCROLL_OFFSET_X[index] > this.bgrOffset.getX()) {
					BUILTIN_SCROLL_OFFSET_X[index] -= this.bgrOffset.getX();
				}
			}
		}
	}
	
	@SuppressWarnings("resource")
	private void loadImage(final Graphics2D graphics, final int index, final StaticResource backgroundResource) {
		if (G2DDrawBackground.BACKGROUND_IMAGE != null) {
			G2DDrawBackground.BACKGROUND_IMAGE.flush();
			G2DDrawBackground.BACKGROUND_BUFFEREDIMAGE.flush();
		}
		G2DDrawBackground.BACKGROUND_IMAGE = null;
		G2DDrawBackground.BACKGROUND_BUFFEREDIMAGE = null;
		G2DDrawBackground.BACKGROUND_KEY = null;
		G2DDrawBackground.BUILTIN_SCROLL_OFFSET_X[index] = 0;
		
		if (backgroundResource != null) {
			final var in = this.rendererData.getStaticResourceService().loadStreamByPath(backgroundResource);
			if (in.isPresent()) {
				try (var stream = in.get()) {
					if (stream != null) {
						G2DDrawBackground.BACKGROUND_KEY = backgroundResource;
						
						G2DDrawBackground.BACKGROUND_BUFFEREDIMAGE = ImageIO.read(stream);
						
						this.createVolatileBackgroundImage(graphics, index);
					}
				}
				catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private void createVolatileBackgroundImage(final Graphics2D graphics, final int index) {
		if (G2DDrawBackground.BACKGROUND_IMAGE != null) {
			G2DDrawBackground.BACKGROUND_IMAGE.flush();
		}
		G2DDrawBackground.BACKGROUND_IMAGE = null;
		
		final var gc = graphics.getDeviceConfiguration();
		G2DDrawBackground.BACKGROUND_IMAGE = gc.createCompatibleVolatileImage(G2DDrawBackground.BACKGROUND_BUFFEREDIMAGE.getWidth(), G2DDrawBackground.BACKGROUND_BUFFEREDIMAGE.getHeight());
		G2DDrawBackground.BACKGROUND_IMAGE.validate(gc);
		Graphics g = null;
		try {
			g = G2DDrawBackground.BACKGROUND_IMAGE.createGraphics();
			g.drawImage(G2DDrawBackground.BACKGROUND_BUFFEREDIMAGE, 0, 0, null);
		}
		finally {
			if (g != null) {
				g.dispose();
			}
		}
	}
	
}
