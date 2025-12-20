package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JTextField;
import javax.swing.RepaintManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;

import de.extio.game_engine.renderer.g2d.control.G2DControlHasExclusiveKeyEvent;
import de.extio.game_engine.renderer.model.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.RgbaColor;

@SuppressWarnings("serial")
public class CustomJTextField extends JTextField implements G2DControlHasExclusiveKeyEvent {
	
	//	private final static Logger LOGGER = LogManager.getLogger();
	
	private final static Color CARET_COLOR = new ImmutableRgbaColor(RgbaColor.YELLOW.getR(), RgbaColor.YELLOW.getG(), RgbaColor.YELLOW.getB(), 190).toAwtColor();
	
	private final static Color CARET_COLOR2 = new ImmutableRgbaColor(RgbaColor.YELLOW.getR() / 2, RgbaColor.YELLOW.getG() / 2, RgbaColor.YELLOW.getB() / 2, 100).toAwtColor();
	
	protected boolean dirty = true;
	
	private Graphics drawGraphics;
	
	public CustomJTextField() {
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
		
		if (this.isFocusOwner()) {
			try {
				final var rect = this.getUI().modelToView2D(this, this.getCaret().getDot(), Bias.Forward);
				if (rect != null) {
					this.drawGraphics.setColor((System.currentTimeMillis() % 1000 < 500) ? CARET_COLOR : CARET_COLOR2);
					this.drawGraphics.fillRect((int) rect.getX(), (int) rect.getY(), Math.max(2, (int) rect.getWidth()), Math.max(6, (int) rect.getHeight()));
				}
			}
			catch (final BadLocationException e) {
			}
		}
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
