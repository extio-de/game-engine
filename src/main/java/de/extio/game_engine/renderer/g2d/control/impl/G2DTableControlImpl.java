package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.control.components.ComponentRenderingSupport;
import de.extio.game_engine.renderer.g2d.control.components.CustomTable;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TableControl;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class G2DTableControlImpl extends G2DBaseControlImpl implements TableControl {
	
	protected CustomTable control;
	
	protected G2DSliderControlImpl scrollbar;
	
	protected List<Object> data;
	
	protected int rows;
	
	protected long version;
	
	protected boolean dataModified;
	
	protected boolean scrollPositionModified;
	
	protected double scrollPositionPerc = 0.0;
	
	private boolean firstColDoubleSize;
	
	public G2DTableControlImpl() {
		this.scrollbar = new G2DSliderControlImpl(value -> {
			this.onScroll(value.doubleValue());
		});
	}
	
	@Override
	public void build() {
		super.build();
		
		this.createControl();
		this.initControl();
		final var mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		mainFrame.add(this.control);
		this.updateAllComponentZOrder();
		
		this.scrollbar.setColor(RgbaColor.LIGHT_GRAY);
		this.scrollbar.setHorizontal(false);
		this.scrollbar.setValue(1.0);
		this.scrollbar.setValue2(1.0);
		this.scrollbar.build();
	}
	
	@Override
	public void performAction() {
		
	}
	
	@Override
	public void render() {
		if (this.modified || this.control.isDirty() || this.scrollPositionModified) {
			this.rebuildBufferedImage();
			this.initControl();
			this.control.setDirty(false);
			this.updateAllComponentZOrder();
		}
		this.control.paint(this.bufferedImageGraphics);
		
		this.scrollbar.render();
		
		super.render();
	}
	
	@Override
	public void close() {
		if (this.control.isFocusOwner()) {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().requestFocus();
		}
		this.control.setVisible(false);
		this.control.invalidate();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.control);
		
		super.close();
		
		this.scrollbar.close();
	}
	
	protected void createControl() {
		this.control = new CustomTable();
		this.control.setName(this.id);
		this.control.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.control.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(final MouseEvent e) {
				final var row = G2DTableControlImpl.this.control.rowAtPoint(e.getPoint());
				final var col = G2DTableControlImpl.this.control.columnAtPoint(e.getPoint());
				if (row > -1 && col > -1) {
					if (G2DTableControlImpl.this.enabled) {
						G2DTableControlImpl.this.rendererData.getEventService().fire(new UiControlEvent(G2DTableControlImpl.this.id, ImmutableCoordI2.create(row, col)));
					}
				}
			}
		});
		this.control.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				if (e.getWheelRotation() == -1) {
					final var displayRows = G2DTableControlImpl.this.control.getHeight() / G2DTableControlImpl.this.control.getRowHeight();
					G2DTableControlImpl.this.scrollbar.setValue(Math.min(1.0, Math.max(0.0, G2DTableControlImpl.this.scrollbar.getValue() + ((double) displayRows / G2DTableControlImpl.this.rows / 2.0))));
				}
				else {
					final var displayRows = G2DTableControlImpl.this.control.getHeight() / G2DTableControlImpl.this.control.getRowHeight();
					G2DTableControlImpl.this.scrollbar.setValue(Math.min(1.0, Math.max(0.0, G2DTableControlImpl.this.scrollbar.getValue() - ((double) displayRows / G2DTableControlImpl.this.rows / 2.0))));
				}
				
				G2DTableControlImpl.this.onScroll(G2DTableControlImpl.this.scrollbar.getValue());
			}
			
		});
		
		this.control.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				if (row == 0) {
					final var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					component.setFont(component.getFont().deriveFont(Font.BOLD));
					return component;
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
		
		this.dataModified = true;
	}
	
	protected void initControl() {
		//		LOGGER.debug("Table modified " + this.id);
		
		this.control.setDrawGraphics(this.bufferedImageGraphics);
		EventQueue.invokeLater(() -> {
			this.control.setLocation(this.x, this.y);
			//			this.control.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			this.control.setSize(this.width - 30, this.height);
			this.control.setEnabled(this.enabled);
			this.control.setVisible(this.visible);
			this.control.setFont(G2DDrawFont.getFont(this.scaleFactor, this.fontSize));
			this.control.setRowHeight((int) (this.control.getFont().getSize2D() * 1.25));
			
			this.control.setBackground(ComponentRenderingSupport.COLOR_COMPONENT_BGR);
			this.control.setForeground(Color.WHITE);
			
			if (this.dataModified || this.scrollPositionModified) {
				//			LOGGER.debug("Table data modified " + this.id);
				if (this.rows == 0 || this.data == null || this.data.isEmpty()) {
					this.control.getDefaultModel().setRowCount(0);
				}
				else {
					final var actualColumns = this.data.size() / this.rows;
					this.control.getDefaultModel().setColumnCount(actualColumns);
					if (this.firstColDoubleSize) {
						for (var i = 0; i < actualColumns; i++) {
							this.control.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 200 : 100);
						}
					}
					final var actualRows = (int) ((1.0 - this.scrollPositionPerc) * this.rows);
					this.control.getDefaultModel().setRowCount(actualRows);
					final var offset = (this.rows - actualRows) * actualColumns;
					for (var i = 0; i < this.data.size() - offset; i++) {
						this.control.getDefaultModel().setValueAt(this.data.get(i + offset), i / this.control.getDefaultModel().getColumnCount(), i % this.control.getDefaultModel().getColumnCount());
					}
				}
				
				this.dataModified = false;
				this.scrollPositionModified = false;
			}
		});
		
		this.scrollbar.setX(this.x + this.width - 30);
		this.scrollbar.setY(this.y);
		this.scrollbar.setWidth(30);
		this.scrollbar.setHeight(this.height);
		this.scrollbar.setScaleFactor(this.scaleFactor);
		this.scrollbar.setEnabled(this.enabled);
		this.scrollbar.setVisible(this.visible);
	}
	
	protected void onScroll(final double value) {
		this.scrollPositionPerc = 1.0 - value;
		this.scrollPositionModified = true;
		this.scrollbar.setValue2(value);
	}
	
	@Override
	public G2DBaseControlImpl setScaleFactor(final double scaleFactor) {
		this.scrollbar.setScaleFactor(scaleFactor);
		return super.setScaleFactor(scaleFactor);
	}
	
	@Override
	public BaseControl setControlGroup(final String controlGroup) {
		this.scrollbar.setControlGroup(controlGroup);
		return super.setControlGroup(controlGroup);
	}
	
	@Override
	public BaseControl setRendererData(final RendererData RendererData) {
		this.scrollbar.setRendererData(RendererData);
		return super.setRendererData(RendererData);
	}
	
	@Override
	public G2DBaseControlImpl setMainFrameGraphics(final Graphics2D graphics) {
		this.scrollbar.setMainFrameGraphics(graphics);
		return super.setMainFrameGraphics(graphics);
	}
	
	@Override
	public BaseControl setControlId(final String id) {
		this.scrollbar.setControlId(id + "_scroll");
		return super.setControlId(id);
	}
	
	@Override
	public List<Object> getData() {
		return this.data;
	}
	
	@Override
	public void setData(final List<Object> data) {
		this.data = data;
	}
	
	@Override
	public int getRows() {
		return this.rows;
	}
	
	@Override
	public void setRows(final int rows) {
		this.rows = rows;
	}
	
	@Override
	public long getVersion() {
		return this.version;
	}
	
	@Override
	public void setVersion(final long version) {
		this.modified |= version != this.version;
		this.dataModified |= version != this.version;
		this.version = version;
	}
	
	@Override
	public boolean isFirstColDoubleSize() {
		return this.firstColDoubleSize;
	}
	
	@Override
	public void setFirstColDoubleSize(final boolean firstColDoubleSize) {
		this.firstColDoubleSize = firstColDoubleSize;
	}
	
}
