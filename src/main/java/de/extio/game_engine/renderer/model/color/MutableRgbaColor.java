package de.extio.game_engine.renderer.model.color;

import java.awt.Color;
import java.util.Objects;

public final class MutableRgbaColor implements RgbaColor {
	
	private int r;
	
	private int g;
	
	private int b;
	
	private int a;
	
	public MutableRgbaColor(final int r, final int g, final int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 255;
	}
	
	public MutableRgbaColor(final int r, final int g, final int b, final int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public MutableRgbaColor(final Color color) {
		this.r = color.getRed();
		this.g = color.getGreen();
		this.b = color.getBlue();
		this.a = color.getAlpha();
	}
	
	public MutableRgbaColor(final RgbaColor color) {
		this.r = color.getR();
		this.g = color.getG();
		this.b = color.getB();
		this.a = color.getA();
	}
	
	@Override
	public Color toAwtColor() {
		return new Color(this.r, this.g, this.b, this.a);
	}
	
	public void xor(final RgbaColor other) {
		this.r ^= other.getR();
		this.g ^= other.getG();
		this.b ^= other.getB();
	}
	
	public void or(final RgbaColor other) {
		this.r |= other.getR();
		this.g |= other.getG();
		this.b |= other.getB();
	}
	
	public void and(final RgbaColor other) {
		this.r &= other.getR();
		this.g &= other.getG();
		this.b &= other.getB();
	}
	
	public void screen(final RgbaColor other) {
		this.r = (int) ((1.0 - (1.0 - (this.r / 255.0)) * (1.0 - (other.getR() / 255.0))) * 255.0);
		this.g = (int) ((1.0 - (1.0 - (this.g / 255.0)) * (1.0 - (other.getG() / 255.0))) * 255.0);
		this.b = (int) ((1.0 - (1.0 - (this.b / 255.0)) * (1.0 - (other.getB() / 255.0))) * 255.0);
	}
	
	@Override
	public int getR() {
		return this.r;
	}
	
	public void setR(final int r) {
		this.r = r;
	}
	
	@Override
	public int getG() {
		return this.g;
	}
	
	public void setG(final int g) {
		this.g = g;
	}
	
	@Override
	public int getB() {
		return this.b;
	}
	
	public void setB(final int b) {
		this.b = b;
	}
	
	@Override
	public int getA() {
		return this.a;
	}
	
	public void setA(final int a) {
		this.a = a;
	}
	
	@Override
	public ImmutableRgbaColor toImmutable() {
		return new ImmutableRgbaColor(this);
	}
	
	@Override
	public MutableRgbaColor toMutable() {
		return new MutableRgbaColor(this);
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
		return this.r == other.getR() && this.g == other.getG() && this.b == other.getB() && this.a == other.getA();
	}
	
	@Override
	public String toString() {
		return "MutableRgbaColor [r=" + this.r + ", g=" + this.g + ", b=" + this.b + ", a=" + this.a + "]";
	}
	
}
