package de.extio.game_engine.renderer.model.options;

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
