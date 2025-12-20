package de.extio.game_engine.renderer.g2d.control.components;

import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTable extends JTable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTable.class);
	
	private static final long serialVersionUID = -9157983277960164169L;
	
	protected boolean dirty = true;
	
	private Graphics drawGraphics;
	
	public CustomTable() {
		super(new CustomTableModel());
		
		this.setIgnoreRepaint(true);
		this.setDoubleBuffered(false);
		this.putClientProperty("AATextInfoPropertyKey", null);
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
		try {
			super.paint(this.drawGraphics);
		}
		catch (final Exception ex) {
			LOGGER.warn(ex.getMessage());
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
	
	public DefaultTableModel getDefaultModel() {
		return (DefaultTableModel) this.getModel();
	}
	
	public Graphics getDrawGraphics() {
		return this.drawGraphics;
	}
	
	public void setDrawGraphics(final Graphics drawGraphics) {
		this.drawGraphics = drawGraphics;
	}
	
	private static class CustomTableModel extends DefaultTableModel {
		
		private static final long serialVersionUID = -3590639586996686965L;
		
		@Override
		public boolean isCellEditable(final int row, final int column) {
			return false;
		}
		
	}
	
}
