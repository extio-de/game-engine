package de.extio.game_engine.renderer.g2d;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.Renderer;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DAbstractRenderingBo;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawBackground;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFpsHistory;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControl;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControlTooltip;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.TakeScreenshotEvent;
import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.util.RingBuffer;

public class G2DRenderer implements Renderer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(G2DRenderer.class);
	
	private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);

	private static final short ZINDEX_STEPS = RenderingBoLayer.UI_TOP - RenderingBoLayer.UI_BGR + 1;
	
	private final RingBuffer<Integer> fpsHistory = new RingBuffer<>(90);
	
	private final StringBuilder fpsStringBuilder = new StringBuilder(15);
	
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	
	private final List<RenderingBo> renderingBOs = new ArrayList<>();
	
	private final Map<Class<? extends RenderingBo>, RenderingBo> usedRenderingBoTypes = new HashMap<>();
	
	private volatile G2DMainFrame mainFrame;
	
	private String rendererModuleId;
	
	private RendererData rendererData;
	
	private long fpsMeasurement;
	
	private int fpsCur;
	
	private int fps;
	
	private long frameStart;
	
	private long frameDur;
	
	private String title;
	
	private volatile boolean takeScreenshot;
	
	private StaticResource previousDefaultFont = null;
	
	public G2DRenderer() {
		LOGGER.info("ctor");
		
		System.setProperty("java.awt.headless", "false");
		System.setProperty("sun.java2d.uiScale", "1");
		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("awt.nativeDoubleBuffering", "true");
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
	}
	
	@Override
	public void show() {
		try {
			LOGGER.info("show()");
			
			final var rendererModule = new G2DRendererModule();
			this.rendererModuleId = rendererModule.getId();
			this.rendererData.getModuleService().loadModule(rendererModule);
			this.rendererData.getModuleService().changeActiveState(rendererModule.getId(), true);
			this.rendererData.getModuleService().changeDisplayState(rendererModule.getId(), true);
			
			EventQueue.invokeAndWait(() -> {
				this.rendererData.getRendererControl().applyVideoOptions();
			});
			EventQueue.invokeAndWait(() -> {
				// this invokeAndWait() call is also important to wait until applyVideoOptions() has been completed, which also puts events to AWT event queue
				LOGGER.debug("Viewport initialized");
				this.rendererData.getRendererWorkingSet().put(this.rendererModuleId, this.rendererData.getRenderingBoPool().acquire("G2DRenderer_background", G2DDrawBackground.class));
				this.rendererData.getRendererWorkingSet().put(this.rendererModuleId, this.rendererData.getRenderingBoPool().acquire("G2DRenderer_tooltip", G2DDrawControlTooltip.class));
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
	public void shutdown() {
		LOGGER.info("shutdown()");
		
		this.rwLock.writeLock().lock();
		try {
			if (this.mainFrame != null) {
				this.mainFrame.unregisterFullScreenWindow();
				this.reset();
				this.mainFrame.setVisible(false);
				this.mainFrame.dispose();
				this.mainFrame = null;
			}
		}
		finally {
			this.rwLock.writeLock().unlock();
		}
	}
	
	@Override
	public void run() throws InterruptedException {
		try {
			this.rwLock.readLock().lockInterruptibly();
			try {
				if (this.mainFrame == null) {
					return;
				}
				
				final var viewPortDimension = this.rendererData.getRendererControl().getAbsoluteViewportDimension();
				if (viewPortDimension.getX() < 10 || viewPortDimension.getY() < 10) {
					// Some race condition happens rarely (mainly Windows 11) that window size is not initialized after entering full screen. Put a plaster on it...
					throw new G2DRendererWindowNotInitializedException();
				}
				
				if (this.previousDefaultFont == null && this.rendererData.getUiOptions().getFontResource() != null && !this.rendererData.getUiOptions().getFontResource().equals(this.previousDefaultFont)) {
					this.previousDefaultFont = this.rendererData.getUiOptions().getFontResource();
					G2DDrawFont.updateDefaultFont(this.rendererData.getStaticResourceService(), this.rendererData.getUiOptions().getFontResource());
				}
				
				BufferedImage screenshotImg = null;
				Graphics2D screenshotGraphics = null;
				final var takeScreenshot_ = this.takeScreenshot;
				if (takeScreenshot_) {
					this.takeScreenshot = false;
					screenshotImg = this.mainFrame.getGraphicsConfiguration().createCompatibleImage(viewPortDimension.getX(), viewPortDimension.getY());
					screenshotGraphics = screenshotImg.createGraphics();
					screenshotGraphics.setRenderingHints(G2DRenderingHintFactory.createDefault());
				}
				
				this.drawStatistics();
				this.rendererData.getRendererWorkingSet().commit(this.rendererModuleId, true);
				this.rendererData.getRendererWorkingSet().getLiveSet(this.renderingBOs, this.rendererData.getModuleService()::isDisplayed);
				this.renderingBOs.sort((bo0, bo1) -> Integer.compare(this.getEffectiveLayer(bo0), this.getEffectiveLayer(bo1)));
				
				Graphics2D screenGraphics = null;
				try {
					final var bufferStrategy = this.mainFrame.getBufferStrategy();
					if (bufferStrategy == null) {
						LOGGER.warn("bufferStrategy == null");
						return;
					}
					
					screenGraphics = (Graphics2D) bufferStrategy.getDrawGraphics();
					screenGraphics.setRenderingHints(G2DRenderingHintFactory.createDefault());
					screenGraphics.setColor(Color.BLACK);
					screenGraphics.fillRect(0, 0, viewPortDimension.getX(), viewPortDimension.getY());
					
					usedRenderingBoTypes.clear();
					for (final RenderingBo renderingBO : this.renderingBOs) {
						if (renderingBO instanceof final G2DAbstractRenderingBo g2dAbstractRenderingBo) {
							usedRenderingBoTypes.putIfAbsent(g2dAbstractRenderingBo.getClass(), g2dAbstractRenderingBo);
							g2dAbstractRenderingBo.render(screenGraphics, this.rendererData.getRendererControl().getScaleFactor(), false);
							if (takeScreenshot_ && g2dAbstractRenderingBo.isScreenshotRelevant()) {
								g2dAbstractRenderingBo.render(screenshotGraphics, this.rendererData.getRendererControl().getScaleFactor(), true);
							}
						}
					}
					
					for (final RenderingBo bo : usedRenderingBoTypes.values()) {
						bo.staticCleanupAfterFrame();
					}
					
					this.rendererData.getRenderingBoPool().releasePending();
					
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
				}
				
				if (takeScreenshot_) {
					this.publishScreenshot(screenshotImg, screenshotGraphics);
				}
			}
			finally {
				this.renderingBOs.clear();
				this.rwLock.readLock().unlock();
			}
		}
		catch (final G2DRendererWindowNotInitializedException e) {
			LOGGER.error("G2DRendererWindowNotInitializedException");
			Thread.sleep(250l);
			this.rendererData.getRendererControl().applyVideoOptions();
		}
	}
	
	private void drawStatistics() {
		if (!this.rendererData.getUiOptions().isDrawFps()) {
			return;
		}
		
		// FPS
		
		fpsStringBuilder.setLength(0);
		fpsStringBuilder.append(this.fps);
		fpsStringBuilder.append("fps ");
		fpsStringBuilder.append(this.frameDur);
		fpsStringBuilder.append("ms");
		
		final var drawFont = this.rendererData.getRenderingBoPool().acquire("g2DRenderer_fpsText", DrawFontRenderingBo.class)
				.setText(fpsStringBuilder.toString())
				.setSize(14)
				.setColor(RgbaColor.WHITE)
				.setLayer(RenderingBoLayer.TOP)
				.withPositionAbsoluteAnchorTopRight(100, 35);
		this.rendererData.getRendererWorkingSet().put(this.rendererModuleId, drawFont);
		
		this.fpsHistory.add(Integer.valueOf((int) this.frameDur));
		final var drawFpsHistory = this.rendererData.getRenderingBoPool().acquire("g2DRenderer_fpsHistory", G2DDrawFpsHistory.class)
				.setHistory(this.fpsHistory)
				.setLayer(RenderingBoLayer.TOP)
				.withPositionAbsoluteAnchorTopRight(100, 50);
		this.rendererData.getRendererWorkingSet().put(this.rendererModuleId, drawFpsHistory);
	}
	
	private void frameCap() throws InterruptedException {
		final var fpsCurrentTime = System.currentTimeMillis();
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
		final var target = this.frameStart + 1000000000L / this.rendererData.getRendererControl().getFrameRate();
		this.frameDur = (System.nanoTime() - this.frameStart) / 1000000L;
		final var delta = target - System.nanoTime();
		spinWait(delta);
		this.frameStart = System.nanoTime();
	}
	
	private static void spinWait(final long nanoDuration) throws InterruptedException {
		final var end = System.nanoTime() + nanoDuration;
		var timeLeft = nanoDuration;
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
		try (var baos = new ByteArrayOutputStream()) {
			ImageIO.write(screenshotImg, "png", baos);
			baos.flush();
			
			LOGGER.debug("Took screenshot");
			
			this.rendererData.getEventService().fire(new TakeScreenshotEvent(null, baos.toByteArray()));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			screenshotGraphics.dispose();
			screenshotImg.flush();
		}
	}
	
	private int getEffectiveLayer(final RenderingBo bo) {
		final var layer = bo.getLayer();
		if (layer >= RenderingBoLayer.UI_BGR && layer <= RenderingBoLayer.UI_TOP) {
			return layer + (bo.getZIndex() * ZINDEX_STEPS);
		}
		return layer;
	}
	
	public G2DMainFrame getMainFrame() {
		return this.mainFrame;
	}
	
	public void setMainFrame(final G2DMainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	public Lock getReadLock() {
		return this.rwLock.readLock();
	}
	
	public Lock getWriteLock() {
		return this.rwLock.writeLock();
	}
	
	@Override
	public void setTitle(final String title) {
		this.title = title;
		if (this.mainFrame != null && title != null) {
			this.mainFrame.setTitle(title);
		}
	}
	
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public void setRendererData(final RendererData rendererData) {
		this.rendererData = rendererData;
	}
	
	public static class G2DRendererModule extends AbstractClientModule {
	}
}
