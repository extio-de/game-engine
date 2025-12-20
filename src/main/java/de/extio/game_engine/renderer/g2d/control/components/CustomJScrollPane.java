package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JScrollPane;
import javax.swing.RepaintManager;

@SuppressWarnings("serial")
public class CustomJScrollPane extends JScrollPane {
	
	private Graphics drawGraphics;
	
	public CustomJScrollPane(final Component view) {
		super(view);
		
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
	}
	
	@Override
	public void update(final Graphics g) {
		if (this.drawGraphics == null) {
			return;
		}
		super.update(this.drawGraphics);
	}
	
	public Graphics getDrawGraphics() {
		return this.drawGraphics;
	}
	
	public void setDrawGraphics(final Graphics drawGraphics) {
		this.drawGraphics = drawGraphics;
	}
	
}
