package de.extio.game_engine.renderer.model;

import java.awt.Color;

public final class ImmutableRgbaColor extends RgbaColor {
	
	private transient Color awtColor;
	
	ImmutableRgbaColor() {
		
	}
	
	public ImmutableRgbaColor(final RgbaColor color) {
		this.r = color.getR();
		this.g = color.getG();
		this.b = color.getB();
		this.a = color.getA();
		this.awtColor = super.toAwtColor();
	}
	
	public ImmutableRgbaColor(final int r, final int g, final int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 255;
		this.awtColor = super.toAwtColor();
	}
	
	public ImmutableRgbaColor(final int r, final int g, final int b, final int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.awtColor = super.toAwtColor();
	}
	
	@Override
	public void xor(final RgbaColor other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void or(final RgbaColor other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void and(final RgbaColor other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void screen(final RgbaColor other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setA(final int a) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setB(final int b) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setG(final int g) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setR(final int r) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Color toAwtColor() {
		if (this.awtColor != null) {
			return this.awtColor;
		}
		return super.toAwtColor();
	}
	
}
