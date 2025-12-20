package de.extio.game_engine.renderer.g2d;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.RendererData.VideoOptions.VideoOptionsVideoMode;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class G2DRendererControl implements RendererControl {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(G2DRendererControl.class);
	
	private final static CoordI2 NO_SCALING_UPPER_LIMIT = ImmutableCoordI2.create(2560, 1440);
	
	private final RendererData rendererData;
	
	private final G2DRenderer renderer;
	
	private CoordI2 absoluteViewportDimension = REFERENCE_RESOLUTION;
	
	private CoordI2 effectiveViewportDimension = REFERENCE_RESOLUTION;
	
	private double scaleFactor = 1.0;
	
	private double scaleFactorModifier = 1.0;
	
	private boolean forceAutoScaling;
	
	private int frameRate = 60;
	
	private volatile long videoOptionsAppliedAt;
	
	public G2DRendererControl(final G2DRenderer renderer, final RendererData rendererData) {
		this.renderer = renderer;
		this.rendererData = rendererData;
	}
	
	@Override
	public void setTitle(final String title) {
		this.renderer.setTitle(title);
	}
	
	@Override
	public void applyVideoOptions() {
		final Runnable run = () -> {
			try {
				this.renderer.getSemaphore().acquire();
			}
			catch (final InterruptedException e) {
				return;
			}
			try {
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
				
				final G2DMainFrame mainFrame = new G2DMainFrame(this.rendererData);
				if (this.rendererData.getVideoOptions().getVideoMode() == VideoOptionsVideoMode.FULLSCREEN) {
					mainFrame.fullscreen();
				}
				else if (this.rendererData.getVideoOptions().getVideoMode() == VideoOptionsVideoMode.BORDERLESS) {
					mainFrame.borderless();
				}
				else {
					mainFrame.windowed();
				}
				mainFrame.setTitle(this.renderer.getTitle());
				if (this.rendererData.getVideoOptions().getFrameRateTarget() == 0) {
					this.rendererData.getVideoOptions().setFrameRateTarget(this.frameRate);
				}
				else {
					this.frameRate = this.rendererData.getVideoOptions().getFrameRateTarget();
				}
				this.renderer.setMainFrame(mainFrame);
				
				Toolkit.getDefaultToolkit().sync();
				
				this.updateViewPort(false, false);
			}
			finally {
				this.renderer.getSemaphore().release();
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
		return this.scaleFactorModifier;
	}
	
	@Override
	public void setScaleFactorModifier(final double scaleFactorModifier) {
		this.scaleFactorModifier = scaleFactorModifier;
		this.recalculate();
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
		// Letting the resize events do their job seemed to fix it
		
		final Runnable run = () -> {
			if (lockRenderer) {
				try {
					this.renderer.getSemaphore().acquire();
				}
				catch (final InterruptedException e) {
				}
			}
			try {
				this.updateAbsoluteViewportPortDimension();
				this.recalculate();
				this.renderer.getMainFrame().createBufferStrategy();
				this.renderer.getMainFrame().resizeListenerEnabled = true;
			}
			finally {
				if (lockRenderer) {
					this.renderer.getSemaphore().release();
				}
			}
		};
		
		if (async) {
			EventQueue.invokeLater(run);
		}
		else {
			run.run();
		}
	}
	
	private void recalculate() {
		this.calculateScaleFactor();
		this.calculateViewportEffectiveDimension();
	}
	
	private void updateAbsoluteViewportPortDimension() {
		final CoordI2 absoluteDim = this.absoluteViewportDimension;
		if (absoluteDim == null || absoluteDim.getX() != this.renderer.getMainFrame().getWidth() || absoluteDim.getY() != this.renderer.getMainFrame().getHeight()) {
			this.absoluteViewportDimension = ImmutableCoordI2.create(this.renderer.getMainFrame().getWidth(), this.renderer.getMainFrame().getHeight());
		}
	}
	
	private void calculateViewportEffectiveDimension() {
		this.effectiveViewportDimension = ImmutableCoordI2.create((int) (this.absoluteViewportDimension.getX() / this.scaleFactor), (int) (this.absoluteViewportDimension.getY() / this.scaleFactor));
		if (LOGGER.isDebugEnabled()) LOGGER.debug("Viewport absolute dimension: {}", this.absoluteViewportDimension);
		if (LOGGER.isDebugEnabled()) LOGGER.debug("Viewport effective dimension: {}", this.effectiveViewportDimension);
	}
	
	private void calculateScaleFactor() {
		final double fLow = Math.min(this.absoluteViewportDimension.getX() / (double) REFERENCE_RESOLUTION.getX(), this.absoluteViewportDimension.getY() / (double) REFERENCE_RESOLUTION.getY());
		final double fHigh = Math.min(this.absoluteViewportDimension.getX() / (double) NO_SCALING_UPPER_LIMIT.getX(), this.absoluteViewportDimension.getY() / (double) NO_SCALING_UPPER_LIMIT.getY());
		
		double factor;
		if (this.isForceAutoScaling()) {
			factor = fLow;
		}
		else {
			if (fLow < 1.0 && fHigh < 1.0) {
				factor = fLow * this.scaleFactorModifier;
			}
			else if (fLow > 1.0 && fHigh > 1.0) {
				factor = fHigh * this.scaleFactorModifier;
			}
			else {
				factor = this.scaleFactorModifier;
			}
		}
		this.scaleFactor = Math.min(SCALE_FACTOR_MAX, Math.max(SCALE_FACTOR_MIN, factor));
		
		if (LOGGER.isDebugEnabled()) LOGGER.debug("Viewport scale factor: {}", this.scaleFactor);
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
}