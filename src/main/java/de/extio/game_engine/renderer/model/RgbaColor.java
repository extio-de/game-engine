package de.extio.game_engine.renderer.model;

import java.awt.Color;
import java.util.Objects;

public class RgbaColor {
	
	public final static RgbaColor WHITE = new ImmutableRgbaColor(255, 255, 255, 255);
	
	public final static RgbaColor LIGHT_GRAY = new ImmutableRgbaColor(192, 192, 192, 255);
	
	public final static RgbaColor GRAY = new ImmutableRgbaColor(128, 128, 128, 255);
	
	public final static RgbaColor DARK_GRAY = new ImmutableRgbaColor(64, 64, 64, 255);
	
	public final static RgbaColor DARK_GRAY2 = new ImmutableRgbaColor(32, 32, 32, 255);
	
	public final static RgbaColor BLACK = new ImmutableRgbaColor(0, 0, 0, 255);
	
	public final static RgbaColor BLACK_DARKEN = new ImmutableRgbaColor(0, 0, 0, 100);
	
	public final static RgbaColor LIGHT_RED = new ImmutableRgbaColor(255, 128, 128, 255);
	
	public final static RgbaColor RED = new ImmutableRgbaColor(255, 0, 0, 255);
	
	public final static RgbaColor DARK_RED = new ImmutableRgbaColor(128, 0, 0, 255);
	
	public final static RgbaColor DARK_RED2 = new ImmutableRgbaColor(64, 0, 0, 255);
	
	public final static RgbaColor PINK = new ImmutableRgbaColor(255, 175, 175, 255);
	
	public final static RgbaColor DARK_PINK = new ImmutableRgbaColor(128, 88, 88, 255);
	
	public final static RgbaColor DARK_PINK2 = new ImmutableRgbaColor(64, 44, 44, 255);
	
	public final static RgbaColor LIGHT_YELLOW = new ImmutableRgbaColor(235, 255, 151, 255);
	
	public final static RgbaColor YELLOW = new ImmutableRgbaColor(214, 255, 43, 255);
	
	public final static RgbaColor DARK_YELLOW = new ImmutableRgbaColor(160, 192, 33, 255);
	
	public final static RgbaColor DARK_YELLOW2 = new ImmutableRgbaColor(64, 79, 0, 255);
	
	public final static RgbaColor LIGHT_ORANGE = new ImmutableRgbaColor(255, 228, 128, 255);
	
	public final static RgbaColor ORANGE = new ImmutableRgbaColor(255, 200, 0, 255);
	
	public final static RgbaColor DARK_ORANGE = new ImmutableRgbaColor(128, 100, 0, 255);
	
	public final static RgbaColor DARK_ORANGE2 = new ImmutableRgbaColor(64, 50, 0, 255);
	
	public final static RgbaColor LIGHT_GREEN = new ImmutableRgbaColor(128, 255, 128, 255);
	
	public final static RgbaColor GREEN = new ImmutableRgbaColor(0, 255, 0, 255);
	
	public final static RgbaColor DARK_GREEN = new ImmutableRgbaColor(0, 128, 0, 255);
	
	public final static RgbaColor DARK_GREEN2 = new ImmutableRgbaColor(0, 64, 0, 255);
	
	public final static RgbaColor LIGHT_BLUE = new ImmutableRgbaColor(128, 192, 255, 255);
	
	public final static RgbaColor BLUE = new ImmutableRgbaColor(0, 0, 255, 255);
	
	public final static RgbaColor DARK_BLUE = new ImmutableRgbaColor(0, 0, 128, 255);
	
	public final static RgbaColor DARK_BLUE2 = new ImmutableRgbaColor(0, 0, 64, 255);
	
	protected int r;
	
	protected int g;
	
	protected int b;
	
	protected int a;
	
	RgbaColor() {
		
	}
	
	public RgbaColor(final int r, final int g, final int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 255;
	}
	
	public RgbaColor(final int r, final int g, final int b, final int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public RgbaColor(final Color color) {
		this.r = color.getRed();
		this.g = color.getGreen();
		this.b = color.getBlue();
		this.a = color.getAlpha();
	}
	
	public RgbaColor(final RgbaColor color) {
		this.r = color.getR();
		this.g = color.getG();
		this.b = color.getB();
		this.a = color.getA();
	}
	
	public Color toAwtColor() {
		return new Color(this.r, this.g, this.b, this.a);
	}
	
	public void xor(final RgbaColor other) {
		this.r ^= other.r;
		this.g ^= other.g;
		this.b ^= other.b;
	}
	
	public void or(final RgbaColor other) {
		this.r |= other.r;
		this.g |= other.g;
		this.b |= other.b;
	}
	
	public void and(final RgbaColor other) {
		this.r &= other.r;
		this.g &= other.g;
		this.b &= other.b;
	}
	
	public void screen(final RgbaColor other) {
		this.r = (int) ((1.0 - (1.0 - (this.r / 255.0)) * (1.0 - (other.r / 255.0))) * 255.0);
		this.g = (int) ((1.0 - (1.0 - (this.g / 255.0)) * (1.0 - (other.g / 255.0))) * 255.0);
		this.b = (int) ((1.0 - (1.0 - (this.b / 255.0)) * (1.0 - (other.b / 255.0))) * 255.0);
	}
	
	public int getR() {
		return this.r;
	}
	
	public void setR(final int r) {
		this.r = r;
	}
	
	public int getG() {
		return this.g;
	}
	
	public void setG(final int g) {
		this.g = g;
	}
	
	public int getB() {
		return this.b;
	}
	
	public void setB(final int b) {
		this.b = b;
	}
	
	public int getA() {
		return this.a;
	}
	
	public void setA(final int a) {
		this.a = a;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.r, this.g, this.b, this.a);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final RgbaColor other)) {
			return false;
		}
		return this.r == other.r && this.g == other.g && this.b == other.b && this.a == other.a;
	}
	
	@Override
	public String toString() {
		return "RgbaColor [r=" + this.r + ", g=" + this.g + ", b=" + this.b + ", a=" + this.a + "]";
	}
	
}
