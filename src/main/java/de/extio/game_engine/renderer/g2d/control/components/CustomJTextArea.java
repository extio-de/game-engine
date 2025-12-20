package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Graphics;

import javax.swing.JTextArea;
import javax.swing.RepaintManager;

import de.extio.game_engine.renderer.g2d.control.G2DControlHasExclusiveKeyEvent;

@SuppressWarnings("serial")
public class CustomJTextArea extends JTextArea implements G2DControlHasExclusiveKeyEvent {
	
	//	private final static Color CARET_COLOR = new ImmutableRgbaColor(RgbaColor.YELLOW_SPACECRAFT.getR(), RgbaColor.YELLOW_SPACECRAFT.getG(), RgbaColor.YELLOW_SPACECRAFT.getB(), 190).toAwtColor();
	//	
	//	private final static Color CARET_COLOR2 = new ImmutableRgbaColor(RgbaColor.YELLOW_SPACECRAFT.getR() / 2, RgbaColor.YELLOW_SPACECRAFT.getG() / 2, RgbaColor.YELLOW_SPACECRAFT.getB() / 2, 100).toAwtColor();
	
	protected boolean dirty = true;
	
	private Graphics drawGraphics;
	
	public CustomJTextArea() {
		this.setIgnoreRepaint(true);
		this.putClientProperty("AATextInfoPropertyKey", null);
		this.setDoubleBuffered(false);
		RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
	}
	
	@Override
	public void paintAll(final Graphics g) {
		if (this.drawGraphics == null) {
			return;
		}
		super.paintAll(this.drawGraphics);
	}
	
	@Override
	public void paint(final Graphics g) {
		if (this.drawGraphics == null) {
			return;
		}
		super.paint(this.drawGraphics);
		
		//		try {
		//			Rectangle2D rect = this.getUI().modelToView2D(this, this.getCaret().getDot(), Bias.Forward);
		//			this.drawGraphics.setColor((System.currentTimeMillis() % 1000 < 500) ? CARET_COLOR : CARET_COLOR2);
		//			this.drawGraphics.fillRect((int)rect.getX(), (int)rect.getY(), Math.max(2, (int)rect.getWidth()), Math.max(6, (int)rect.getHeight()));
		//		}
		//		catch (BadLocationException e) {
		//		}
	}
	
	@Override
	public void update(final Graphics g) {
		this.dirty = true;
		
		if (this.drawGraphics == null) {
			return;
		}
		super.update(this.drawGraphics);
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
	
	public Graphics getDrawGraphics() {
		return this.drawGraphics;
	}
	
	public void setDrawGraphics(final Graphics drawGraphics) {
		this.drawGraphics = drawGraphics;
	}
}
