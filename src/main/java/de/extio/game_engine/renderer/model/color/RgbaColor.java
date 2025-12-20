package de.extio.game_engine.renderer.model.color;

import java.awt.Color;

public interface RgbaColor {
	
	RgbaColor WHITE = new ImmutableRgbaColor(255, 255, 255, 255);
	
	RgbaColor LIGHT_GRAY = new ImmutableRgbaColor(192, 192, 192, 255);
	
	RgbaColor GRAY = new ImmutableRgbaColor(128, 128, 128, 255);
	
	RgbaColor DARK_GRAY = new ImmutableRgbaColor(64, 64, 64, 255);
	
	RgbaColor DARK_GRAY2 = new ImmutableRgbaColor(32, 32, 32, 255);
	
	RgbaColor BLACK = new ImmutableRgbaColor(0, 0, 0, 255);
	
	RgbaColor BLACK_DARKEN = new ImmutableRgbaColor(0, 0, 0, 100);
	
	RgbaColor LIGHT_RED = new ImmutableRgbaColor(255, 128, 128, 255);
	
	RgbaColor RED = new ImmutableRgbaColor(255, 0, 0, 255);
	
	RgbaColor DARK_RED = new ImmutableRgbaColor(128, 0, 0, 255);
	
	RgbaColor DARK_RED2 = new ImmutableRgbaColor(64, 0, 0, 255);
	
	RgbaColor PINK = new ImmutableRgbaColor(255, 175, 175, 255);
	
	RgbaColor DARK_PINK = new ImmutableRgbaColor(128, 88, 88, 255);
	
	RgbaColor DARK_PINK2 = new ImmutableRgbaColor(64, 44, 44, 255);
	
	RgbaColor LIGHT_YELLOW = new ImmutableRgbaColor(235, 255, 151, 255);
	
	RgbaColor YELLOW = new ImmutableRgbaColor(214, 255, 43, 255);
	
	RgbaColor DARK_YELLOW = new ImmutableRgbaColor(160, 192, 33, 255);
	
	RgbaColor DARK_YELLOW2 = new ImmutableRgbaColor(64, 79, 0, 255);
	
	RgbaColor LIGHT_ORANGE = new ImmutableRgbaColor(255, 228, 128, 255);
	
	RgbaColor ORANGE = new ImmutableRgbaColor(255, 200, 0, 255);
	
	RgbaColor DARK_ORANGE = new ImmutableRgbaColor(128, 100, 0, 255);
	
	RgbaColor DARK_ORANGE2 = new ImmutableRgbaColor(64, 50, 0, 255);
	
	RgbaColor LIGHT_GREEN = new ImmutableRgbaColor(128, 255, 128, 255);
	
	RgbaColor GREEN = new ImmutableRgbaColor(0, 255, 0, 255);
	
	RgbaColor DARK_GREEN = new ImmutableRgbaColor(0, 128, 0, 255);
	
	RgbaColor DARK_GREEN2 = new ImmutableRgbaColor(0, 64, 0, 255);
	
	RgbaColor LIGHT_BLUE = new ImmutableRgbaColor(128, 192, 255, 255);
	
	RgbaColor BLUE = new ImmutableRgbaColor(0, 0, 255, 255);
	
	RgbaColor DARK_BLUE = new ImmutableRgbaColor(0, 0, 128, 255);
	
	RgbaColor DARK_BLUE2 = new ImmutableRgbaColor(0, 0, 64, 255);
	
	int getR();
	
	int getG();
	
	int getB();
	
	int getA();
	
	Color toAwtColor();
	
	ImmutableRgbaColor toImmutable();
	
	MutableRgbaColor toMutable();
	
}
