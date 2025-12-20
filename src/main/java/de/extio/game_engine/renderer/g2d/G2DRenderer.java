package de.extio.game_engine.renderer.g2d;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.Renderer;
import de.extio.game_engine.renderer.RenderingBoPoolImpl;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControl;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControlTooltip;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DAbstractRenderingBo;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawBackground;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawEffect;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFpsHistory;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawImage;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawTest;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawWindow;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawEffectRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawImageRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawTestRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawWindowRenderingBo;
import de.extio.game_engine.renderer.model.event.TakeScreenshotEvent;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.RgbaColor;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.util.RingBuffer;

public class G2DRenderer implements Renderer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(G2DRenderer.class);
	
	private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(3);
	
	private volatile G2DMainFrame mainFrame;
	
	private final RendererData rendererData;
	
	private final RingBuffer<Integer> fpsHistory = new RingBuffer<>(90);
	
	private long fpsMeasurement;
	
	private int fpsCur;
	
	private int fps;
	
	private long frameStart;
	
	private long frameDur;
	
	private final Semaphore semaphore = new Semaphore(1);
	
	private String title;
	
	private volatile boolean takeScreenshot;
	
	public G2DRenderer(final RendererData rendererData) {
		LOGGER.info("ctor");
		
		System.setProperty("sun.java2d.uiScale", "1");
		System.setProperty("awt.nativeDoubleBuffering", "true");
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		
		this.rendererData = rendererData;
		
		// TODO: Discover with spring dm
		RenderingBoPoolImpl.INTERFACE_TO_BO_G2D.put(DrawFontRenderingBo.class, G2DDrawFont.class);
		RenderingBoPoolImpl.INTERFACE_TO_BO_G2D.put(DrawImageRenderingBo.class, G2DDrawImage.class);
		RenderingBoPoolImpl.INTERFACE_TO_BO_G2D.put(ControlRenderingBo.class, G2DDrawControl.class);
		RenderingBoPoolImpl.INTERFACE_TO_BO_G2D.put(DrawWindowRenderingBo.class, G2DDrawWindow.class);
		RenderingBoPoolImpl.INTERFACE_TO_BO_G2D.put(DrawTestRenderingBo.class, G2DDrawTest.class);
		RenderingBoPoolImpl.INTERFACE_TO_BO_G2D.put(DrawEffectRenderingBo.class, G2DDrawEffect.class);
	}
	
	@Override
	public void show() {
		try {
			EventQueue.invokeAndWait(() -> {
				this.rendererData.getRendererControl().applyVideoOptions();
			});
			EventQueue.invokeAndWait(() -> {
				// this invokeAndWait() call is also important to wait until applyVideoOptions() has been completed, which also puts events to AWT event queue
				LOGGER.debug("Viewport initialized");
			});
		}
		catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void takeScreenshot() {
		this.takeScreenshot = true;
	}
	
	@Override
	public void reset() {
		G2DDrawControl.reset();
	}
	
	@Override
	public void run(final List<RenderingBo> renderingBOs) throws InterruptedException {
		try {
			this.semaphore.acquire();
			try {
				if (this.mainFrame == null) {
					return;
				}
				
				final CoordI2 viewPortDimension = this.rendererData.getRendererControl().getAbsoluteViewportDimension();
				
				if (viewPortDimension.getX() < 10 || viewPortDimension.getY() < 10) {
					// Some race condition happens rarely (mainly Windows 11) that window size is not initialized after entering full screen. Put a plaster on it...
					throw new G2DRendererWindowNotInitializedException();
				}
				
				BufferedImage screenshotImg = null;
				Graphics2D screenshotGraphics = null;
				final boolean takeScreenshot_ = this.takeScreenshot;
				if (takeScreenshot_) {
					this.takeScreenshot = false;
					screenshotImg = this.mainFrame.getGraphicsConfiguration().createCompatibleImage(viewPortDimension.getX(), viewPortDimension.getY());
					screenshotGraphics = screenshotImg.createGraphics();
					screenshotGraphics.setRenderingHints(G2DRenderingHintFactory.createDefault());
				}
				
				this.addStaticRenderingBOs(renderingBOs);
				renderingBOs.sort((bo0, bo1) -> bo0.getLayer().compareTo(bo1.getLayer()));
				
				Graphics2D screenGraphics = null;
				try {
					final BufferStrategy bufferStrategy = this.mainFrame.getBufferStrategy();
					if (bufferStrategy == null) {
						LOGGER.warn("bufferStrategy == null");
						return;
					}
					
					screenGraphics = (Graphics2D) bufferStrategy.getDrawGraphics();
					screenGraphics.setRenderingHints(G2DRenderingHintFactory.createDefault());
					screenGraphics.setColor(Color.BLACK);
					screenGraphics.fillRect(0, 0, viewPortDimension.getX(), viewPortDimension.getY());
					
					for (final RenderingBo renderingBO : renderingBOs) {
						if (renderingBO instanceof G2DAbstractRenderingBo) {
							((G2DAbstractRenderingBo) renderingBO).render(screenGraphics, this.rendererData.getRendererControl().getScaleFactor(), false);
							if (takeScreenshot_ && ((G2DAbstractRenderingBo) renderingBO).isScreenshotRelevant()) {
								((G2DAbstractRenderingBo) renderingBO).render(screenshotGraphics, this.rendererData.getRendererControl().getScaleFactor(), true);
							}
						}
					}
					
					//
					// Rendering cycle END
					//
					
					this.frameCap();
					
					//
					// Rendering cycle START
					//
					
					bufferStrategy.show();
					Toolkit.getDefaultToolkit().sync();
				}
				finally {
					if (screenGraphics != null) {
						screenGraphics.dispose();
					}
					
					for (final RenderingBo renderingBO : renderingBOs) {
						this.rendererData.getRenderingBoPool().release(renderingBO);
					}
					// TODO discover with marker interface
					G2DDrawControl.closeStatic();
					G2DDrawImage.closeStatic();
					G2DDrawControlTooltip.closeStatic();
				}
				
				if (takeScreenshot_) {
					this.publishScreenshot(screenshotImg, screenshotGraphics);
				}
			}
			finally {
				this.semaphore.release();
			}
		}
		catch (final G2DRendererWindowNotInitializedException e) {
			LOGGER.error("G2DRendererWindowNotInitializedException");
			Thread.sleep(250l);
			this.rendererData.getRendererControl().applyVideoOptions();
		}
	}
	
	private void addStaticRenderingBOs(final List<RenderingBo> renderingBOs) {
		renderingBOs.add(this.rendererData.getRenderingBoPool().acquire(G2DDrawBackground.class));
		renderingBOs.add(this.rendererData.getRenderingBoPool().acquire(G2DDrawControlTooltip.class));
		this.drawStatistics(renderingBOs);
	}
	
	private void drawStatistics(final List<RenderingBo> renderingBOs) {
		if (!this.rendererData.getUiOptions().isDrawFps()) {
			return;
		}
		
		// FPS
		
		final StringBuilder sb = new StringBuilder(15);
		sb.append(this.fps);
		sb.append("fps ");
		sb.append(this.frameDur);
		sb.append("ms");
		
		final RenderingBo drawFont = this.rendererData.getRenderingBoPool().acquire(DrawFontRenderingBo.class)
				.setText(sb.toString())
				.setSize(14)
				.setColor(RgbaColor.WHITE)
				.setLayer(RenderingBoLayer.TOP)
				.withPositionAbsoluteAnchorTopRight(100, 35);
		renderingBOs.add(drawFont);
		
		this.fpsHistory.add(Integer.valueOf((int) this.frameDur));
		final RenderingBo drawFpsHistory = this.rendererData.getRenderingBoPool().acquire(G2DDrawFpsHistory.class)
				.setHistory(this.fpsHistory)
				.setLayer(RenderingBoLayer.TOP)
				.withPositionAbsoluteAnchorTopRight(100, 50);
		renderingBOs.add(drawFpsHistory);
	}
	
	private void frameCap() throws InterruptedException {
		final long fpsCurrentTime = System.currentTimeMillis();
		if (fpsCurrentTime - this.fpsMeasurement >= 1000) {
			this.fpsMeasurement = fpsCurrentTime;
			this.fps = this.fpsCur;
			this.fpsCur = 1;
		}
		else {
			this.fpsCur++;
		}
		
		if (this.frameStart == 0) {
			this.frameStart = System.nanoTime();
		}
		final long target = this.frameStart + 1000000000L / this.rendererData.getRendererControl().getFrameRate();
		this.frameDur = (System.nanoTime() - this.frameStart) / 1000000L;
		final long delta = target - System.nanoTime();
		spinWait(delta);
		this.frameStart = System.nanoTime();
	}
	
	private static void spinWait(final long nanoDuration) throws InterruptedException {
		final long end = System.nanoTime() + nanoDuration;
		long timeLeft = nanoDuration;
		do {
			if (timeLeft > SLEEP_PRECISION) {
				Thread.sleep(1);
			}
			else {
				Thread.yield();
			}
			
			timeLeft = end - System.nanoTime();
		} while (timeLeft > 0);
		
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}
	
	private void publishScreenshot(final BufferedImage screenshotImg, final Graphics2D screenshotGraphics) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(screenshotImg, "png", baos);
			baos.flush();
			
			LOGGER.debug("Took screenshot");
			
			this.rendererData.getEventConsumer().accept(new TakeScreenshotEvent(null, baos.toByteArray()));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			screenshotGraphics.dispose();
			screenshotImg.flush();
		}
	}
	
	public G2DMainFrame getMainFrame() {
		return this.mainFrame;
	}
	
	public void setMainFrame(final G2DMainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	public Semaphore getSemaphore() {
		return this.semaphore;
	}
	
	@Override
	public void setTitle(final String title) {
		this.title = title;
		if (this.mainFrame != null) {
			this.mainFrame.setTitle(title);
		}
	}
	
	public String getTitle() {
		return this.title;
	}
}
