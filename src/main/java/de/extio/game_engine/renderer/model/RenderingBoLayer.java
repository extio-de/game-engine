package de.extio.game_engine.renderer.model;

public class RenderingBoLayer {
	
	public static final short BACKGROUND0 = 100;
	public static final short BACKGROUND1 = 110;
	public static final short BACKGROUND2 = 120;

	public static final short FOREGROUND0 = 1000;
	public static final short FOREGROUND1 = 1100;
	public static final short FOREGROUND2 = 1200;
	public static final short FOREGROUND3 = 1300;
	public static final short FOREGROUND4 = 1400;
	public static final short FOREGROUND5 = 1500;
	public static final short FOREGROUND6 = 1600;

	public static final short UI_BGR = 2000;
	public static final short UI0 = 2100;
	public static final short UI1 = 2200;
	public static final short UI2 = 2300;
	public static final short UI_TOP = 2999;
	// Note: UI repeats layers per z-index up to TOP

	public static final short TOP = 30000;

	private RenderingBoLayer() {
	}

}
