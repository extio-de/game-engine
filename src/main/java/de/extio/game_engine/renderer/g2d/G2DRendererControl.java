package de.extio.game_engine.renderer.g2d;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.event.ViewportResizeEvent;
import de.extio.game_engine.renderer.model.options.VideoOptions.VideoOptionsVideoMode;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class G2DRendererControl implements RendererControl {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(G2DRendererControl.class);
	
	// private final static CoordI2 NO_SCALING_UPPER_LIMIT = ImmutableCoordI2.create(2560, 1440);
	private final static CoordI2 NO_SCALING_UPPER_LIMIT = RendererControl.REFERENCE_RESOLUTION;
	
	private final G2DRenderer renderer;
	
	private RendererData rendererData;
	
	private CoordI2 absoluteViewportDimension = RendererControl.REFERENCE_RESOLUTION;
	
	private CoordI2 effectiveViewportDimension = RendererControl.REFERENCE_RESOLUTION;
	
	private double scaleFactor = 1.0;
	
	private volatile G2DRendererControlOptions options = new G2DRendererControlOptions(1.0);
	
	private boolean forceAutoScaling;
	
	private int frameRate = 60;
	
	private volatile long videoOptionsAppliedAt;
	
	public G2DRendererControl(final G2DRenderer renderer) {
		this.renderer = renderer;
	}
	
	@Override
	public void setTitle(final String title) {
		this.renderer.setTitle(title);
	}
	
	@Override
	public void applyVideoOptions() {
		final Runnable run = () -> {
			this.renderer.getWriteLock().lock();
			try {
				if (this.videoOptionsAppliedAt == 0)  {
					this.rendererData.getStorageService()
						.loadByPath(G2DRendererControlOptions.class, List.of("gameEngine"), "g2dRendererControlOptions")
						.ifPresent(options -> this.options = options);
				}

				if (this.renderer.getMainFrame() != null) {
					this.renderer.getMainFrame().unregisterFullScreenWindow();
					this.renderer.reset();
					this.renderer.getMainFrame().setVisible(false);
					this.renderer.getMainFrame().dispose();
					this.renderer.setMainFrame(null);
					Toolkit.getDefaultToolkit().sync();
					try {
						Thread.sleep(100l);
					}
					catch (final InterruptedException e) {
						return;
					}
				}
				
				final var mainFrame = new G2DMainFrame(this.rendererData);
				if (this.rendererData.getVideoOptions().getVideoMode() == VideoOptionsVideoMode.FULLSCREEN) {
					mainFrame.fullscreen();
				}
				else if (this.rendererData.getVideoOptions().getVideoMode() == VideoOptionsVideoMode.BORDERLESS) {
					mainFrame.borderless();
				}
				else {
					mainFrame.windowed();
				}
				mainFrame.setTitle(this.renderer.getTitle() == null ? "" : this.renderer.getTitle());
				if (this.rendererData.getVideoOptions().getFrameRateTarget() == 0) {
					this.rendererData.getVideoOptions().setFrameRateTarget(this.frameRate);
				}
				else {
					this.frameRate = this.rendererData.getVideoOptions().getFrameRateTarget();
				}
				this.renderer.setMainFrame(mainFrame);
				
				Toolkit.getDefaultToolkit().sync();
				
				this.updateViewPort(false, false);

				this.rendererData.getStorageService()
					.store(List.of("gameEngine"), "videoOptions", this.rendererData.getVideoOptions());

			}
			finally {
				this.renderer.getWriteLock().unlock();
				this.videoOptionsAppliedAt = System.currentTimeMillis();
			}
		};
		
		if (EventQueue.isDispatchThread()) {
			run.run();
		}
		else {
			try {
				EventQueue.invokeAndWait(run);
			}
			catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public long getVideoOptionsAppliedAt() {
		return this.videoOptionsAppliedAt;
	}
	
	@Override
	public CoordI2 getEffectiveViewportDimension() {
		return this.effectiveViewportDimension;
	}
	
	@Override
	public CoordI2 getAbsoluteViewportDimension() {
		return this.absoluteViewportDimension;
	}
	
	@Override
	public double getScaleFactor() {
		return this.scaleFactor;
	}
	
	@Override
	public double getScaleFactorModifier() {
		return this.options.scaleFactorModifier();
	}
	
	@Override
	public void setScaleFactorModifier(final double scaleFactorModifier) {
		if (scaleFactorModifier <= 0.1) {
			throw new IllegalArgumentException("Scale factor modifier must be greater than 0.1");
		}
		if (scaleFactorModifier > 5.0) {
			throw new IllegalArgumentException("Scale factor modifier must be less than 5.0");
		}
		if (scaleFactorModifier == this.options.scaleFactorModifier()) {
			return;
		}

		this.options = new G2DRendererControlOptions(scaleFactorModifier);
		this.rendererData.getStorageService()
			.store(List.of("gameEngine"), "g2dRendererControlOptions", this.options);
		
		this.recalculate();
		this.rendererData.getEventService().fire(new ViewportResizeEvent());
	}
	
	@Override
	public boolean isForceAutoScaling() {
		return this.forceAutoScaling;
	}
	
	@Override
	public void setForceAutoScaling(final boolean forceAutoScaling) {
		this.forceAutoScaling = forceAutoScaling;
		this.recalculate();
	}
	
	public void updateViewPort(final boolean lockRenderer, final boolean async) {
		// This method is called several times in a row by mainFrame resize event handler by intention
		// I've discovered in debugger that sometimes when only a black viewPort is displayed the volatile image backing the bufferStrategy is only a few pixels in size
		// Letting the resize events do their job seemed to fix it --> createBufferStrategy is called multiple times in a row here intentionally
		
		final Runnable run = () -> {
			if (lockRenderer) {
				this.renderer.getWriteLock().lock();
			}
			try {
				if (this.updateAbsoluteViewportPortDimension()) {
					this.recalculate();
					this.rendererData.getEventService().fire(new ViewportResizeEvent());
				}
				final var mainFrame = this.renderer.getMainFrame();
				if (mainFrame != null) {
					mainFrame.createBufferStrategy();
					mainFrame.resizeListenerEnabled = true;
				}
			}
			finally {
				if (lockRenderer) {
					this.renderer.getWriteLock().unlock();
				}
			}
		};
		
		if (async) {
			// EventQueue.invokeLater(run); // Using EventQueue here can clutter the AWT event queue with too many events when resizing on a system with high event rate
			Thread.ofVirtual()
				.name("G2DRendererControl-ViewportUpdater", 0)
				.start(run);
		}
		else {
			run.run();
		}
	}
	
	private void recalculate() {
		this.calculateScaleFactor();
		this.calculateViewportEffectiveDimension();
	}
	
	private boolean updateAbsoluteViewportPortDimension() {
		boolean changed = false;
		final var absoluteDim = this.absoluteViewportDimension;
		if (absoluteDim == null || (this.renderer.getMainFrame() != null && (absoluteDim.getX() != this.renderer.getMainFrame().getWidth() || absoluteDim.getY() != this.renderer.getMainFrame().getHeight()))) {
			this.absoluteViewportDimension = ImmutableCoordI2.create(this.renderer.getMainFrame().getWidth(), this.renderer.getMainFrame().getHeight());
			changed = true;
		}
		return changed;
	}
	
	private void calculateViewportEffectiveDimension() {
		this.effectiveViewportDimension = ImmutableCoordI2.create((int) (this.absoluteViewportDimension.getX() / this.scaleFactor), (int) (this.absoluteViewportDimension.getY() / this.scaleFactor));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Viewport absolute dimension: {}", this.absoluteViewportDimension);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Viewport effective dimension: {}", this.effectiveViewportDimension);
		}
	}
	
	private void calculateScaleFactor() {
		final var fLow = Math.min(this.absoluteViewportDimension.getX() / (double) REFERENCE_RESOLUTION.getX(), this.absoluteViewportDimension.getY() / (double) REFERENCE_RESOLUTION.getY());
		final var fHigh = Math.min(this.absoluteViewportDimension.getX() / (double) NO_SCALING_UPPER_LIMIT.getX(), this.absoluteViewportDimension.getY() / (double) NO_SCALING_UPPER_LIMIT.getY());
		
		double factor;
		if (this.isForceAutoScaling()) {
			factor = fLow;
		}
		else {
			if (fLow < 1.0 && fHigh < 1.0) {
				factor = fLow * this.options.scaleFactorModifier();
			}
			else if (fLow > 1.0 && fHigh > 1.0) {
				factor = fHigh * this.options.scaleFactorModifier();
			}
			else {
				factor = this.options.scaleFactorModifier();
			}
		}
		this.scaleFactor = Math.min(SCALE_FACTOR_MAX, Math.max(SCALE_FACTOR_MIN, factor));
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Viewport scale factor: {}", this.scaleFactor);
		}
	}
	
	@Override
	public int getFrameRate() {
		return this.frameRate;
	}
	
	@Override
	public void setFrameRate(final int frameRate) {
		this.frameRate = frameRate;
	}
	
	@Override
	public long getFrame() {
		return this.rendererData.getFrame();
	}
	
	@Override
	public void setRendererData(final RendererData rendererData) {
		this.rendererData = rendererData;
	}

	private static record G2DRendererControlOptions(double scaleFactorModifier) {
	}
}
