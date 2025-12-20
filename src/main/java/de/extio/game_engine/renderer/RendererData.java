package de.extio.game_engine.renderer;

import java.util.function.Consumer;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class RendererData {
	
	private Renderer renderer;
	
	private RendererControl rendererControl;
	
	private RenderingBoPool renderingBoPool;
	
	private final UiOptions uiOptions = new UiOptions();
	
	private final VideoOptions videoOptions = new VideoOptions();
	
	private long frame;
	
	private Consumer<Object> eventConsumer;
	
	public final class UiOptions {
		
		private String backgroundResourceName0 = null;
		
		private boolean backgroundScrolling0;
		
		private boolean backgroundScrollingReverse0;
		
		private boolean backgroundScrolling1;
		
		private boolean backgroundScrollingReverse1;
		
		private boolean drawFps;
		
		private CoordI2 backgroundOffset0 = MutableCoordI2.create();
		
		private CoordI2 backgroundOffset1 = MutableCoordI2.create();
		
		public UiOptions() {
			if ("true".equals(System.getProperty("debug"))) {
				this.drawFps = true;
			}
		}
		
		public boolean isDrawFps() {
			return this.drawFps;
		}
		
		public void setDrawFps(final boolean drawFps) {
			this.drawFps = drawFps;
		}
		
		public String getBackgroundResourceName0() {
			return this.backgroundResourceName0;
		}
		
		public void setBackgroundResourceName0(final String background0) {
			this.backgroundResourceName0 = background0;
		}
		
		public boolean isBackgroundScrolling0() {
			return this.backgroundScrolling0;
		}
		
		public void setBackgroundScrolling0(final boolean backgroundScrolling0) {
			this.backgroundScrolling0 = backgroundScrolling0;
		}
		
		public boolean isBackgroundScrollingReverse0() {
			return this.backgroundScrollingReverse0;
		}
		
		public void setBackgroundScrollingReverse0(final boolean backgroundScrollingReverse0) {
			this.backgroundScrollingReverse0 = backgroundScrollingReverse0;
		}
		
		public boolean isBackgroundScrolling1() {
			return this.backgroundScrolling1;
		}
		
		public void setBackgroundScrolling1(final boolean backgroundScrolling1) {
			this.backgroundScrolling1 = backgroundScrolling1;
		}
		
		public boolean isBackgroundScrollingReverse1() {
			return this.backgroundScrollingReverse1;
		}
		
		public void setBackgroundScrollingReverse1(final boolean backgroundScrollingReverse1) {
			this.backgroundScrollingReverse1 = backgroundScrollingReverse1;
		}
		
		public CoordI2 getBackgroundOffset0() {
			return this.backgroundOffset0;
		}
		
		public void setBackgroundOffset0(final CoordI2 backgroundOffset0) {
			this.backgroundOffset0 = backgroundOffset0;
		}
		
		public CoordI2 getBackgroundOffset1() {
			return this.backgroundOffset1;
		}
		
		public void setBackgroundOffset1(final CoordI2 backgroundOffset1) {
			this.backgroundOffset1 = backgroundOffset1;
		}
		
	}
	
	public final class VideoOptions {
		
		private VideoOptionsVideoMode videoMode = VideoOptionsVideoMode.BORDERLESS;
		
		private int fullScreenNumber = -1;
		
		private double scaleFactorModifier = 1.0;
		
		private int frameRateTarget = 60;
		
		public double getScaleFactorModifier() {
			return this.scaleFactorModifier;
		}
		
		public void setScaleFactorModifier(final double scaleFactorModifier) {
			this.scaleFactorModifier = scaleFactorModifier;
		}
		
		public VideoOptionsVideoMode getVideoMode() {
			return this.videoMode;
		}
		
		public void setVideoMode(final VideoOptionsVideoMode videoMode) {
			this.videoMode = videoMode;
		}
		
		public int getFullScreenNumber() {
			return this.fullScreenNumber;
		}
		
		public void setFullScreenNumber(final int fullScreenNumber) {
			this.fullScreenNumber = fullScreenNumber;
		}
		
		public int getFrameRateTarget() {
			return this.frameRateTarget;
		}
		
		public void setFrameRateTarget(final int frameRateTarget) {
			this.frameRateTarget = frameRateTarget;
		}
		
		public static enum VideoOptionsVideoMode {
			
			WINDOW,
			BORDERLESS,
			FULLSCREEN
		
		}
		
	}	
	
	public RendererControl getRendererControl() {
		return this.rendererControl;
	}
	
	public void setRendererControl(final RendererControl rendererControl) {
		this.rendererControl = rendererControl;
	}
	
	public UiOptions getUiOptions() {
		return this.uiOptions;
	}
	
	public RenderingBoPool getRenderingBoPool() {
		return this.renderingBoPool;
	}
	
	public void setRenderingBoPool(final RenderingBoPool renderingBoPool) {
		this.renderingBoPool = renderingBoPool;
	}
	
	public long getFrame() {
		return this.frame;
	}
	
	public void setFrame(final long frame) {
		this.frame = frame;
	}
	
	public Renderer getRenderer() {
		return this.renderer;
	}
	
	public void setRenderer(final Renderer renderer) {
		this.renderer = renderer;
	}

	public VideoOptions getVideoOptions() {
		return videoOptions;
	}

	public Consumer<Object> getEventConsumer() {
		return eventConsumer;
	}

	public void setEventConsumer(Consumer<Object> eventConsumer) {
		this.eventConsumer = eventConsumer;
	}

}
