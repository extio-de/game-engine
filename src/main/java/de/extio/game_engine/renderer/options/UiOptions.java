package de.extio.game_engine.renderer.options;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

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
