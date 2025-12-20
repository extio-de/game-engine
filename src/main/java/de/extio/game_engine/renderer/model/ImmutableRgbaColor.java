package de.extio.game_engine.renderer.model;

import java.awt.Color;
import java.util.Objects;

public final class ImmutableRgbaColor implements RgbaColor {
	
	private final int r;
	
	private final int g;
	
	private final int b;
	
	private final int a;
	
	private transient Color awtColor;
	
	public ImmutableRgbaColor(final Color color) {
		this.r = color.getRed();
		this.g = color.getGreen();
		this.b = color.getBlue();
		this.a = color.getAlpha();
		this.awtColor = color;
	}

	public ImmutableRgbaColor(final RgbaColor color) {
		this.r = color.getR();
		this.g = color.getG();
		this.b = color.getB();
		this.a = color.getA();
	}
	
	public ImmutableRgbaColor(final int r, final int g, final int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 255;
	}
	
	public ImmutableRgbaColor(final int r, final int g, final int b, final int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	@Override
	public int getR() {
		return this.r;
	}
	
	@Override
	public int getG() {
		return this.g;
	}
	
	@Override
	public int getB() {
		return this.b;
	}
	
	@Override
	public int getA() {
		return this.a;
	}
	
	@Override
	public Color toAwtColor() {
		if (this.awtColor != null) {
			return this.awtColor;
		}
		this.awtColor = new Color(this.r, this.g, this.b, this.a);
		return this.awtColor;
	}
	
	@Override
	public ImmutableRgbaColor toImmutable() {
		return this;
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
		return "ImmutableRgbaColor [r=" + this.r + ", g=" + this.g + ", b=" + this.b + ", a=" + this.a + "]";
	}
	
}
