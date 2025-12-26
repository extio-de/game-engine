package de.extio.game_engine.renderer;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

/**
 * RendererControl controls the renderer
 */
public interface RendererControl {
	
	public final static CoordI2 REFERENCE_RESOLUTION = ImmutableCoordI2.create(1920, 1080);
	
	public final static double SCALE_FACTOR_MIN = 0.5;
	
	public final static double SCALE_FACTOR_MAX = 4.0;
	
	void setTitle(String title);
	
	void applyVideoOptions();
	
	long getVideoOptionsAppliedAt();
	
	CoordI2 getAbsoluteViewportDimension();
	
	CoordI2 getEffectiveViewportDimension();
	
	double getScaleFactor();
	
	double getScaleFactorModifier();
	
	void setScaleFactorModifier(double scaleFactorModifier);
	
	boolean isForceAutoScaling();
	
	void setForceAutoScaling(boolean force);
	
	int getFrameRate();
	
	void setFrameRate(int frameRate);
	
	long getFrame();

	void setRendererData(final RendererData rendererData);
	
}
