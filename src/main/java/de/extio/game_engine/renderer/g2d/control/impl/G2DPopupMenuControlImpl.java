package de.extio.game_engine.renderer.g2d.control.impl;

import java.util.Objects;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomPopupMenu;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.PopupMenuControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.PopupMenuData;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.model.color.RgbaColor;

public class G2DPopupMenuControlImpl extends G2DBaseControlImpl implements PopupMenuControl {
	
	private CustomPopupMenu control;
	
	private PopupMenuData popupMenuData;
	
	private RgbaColor backgroundColor;
	
	private RgbaColor foregroundColor;
	
	private RgbaColor selectionColor;
	
	private int rowHeight;
	
	private int padding;
	
	private String lastSelectedItemId;
	
	@Override
	public void setCustomData(final PopupMenuData data) {
		this.modified |= !Objects.equals(this.popupMenuData, data);
		this.popupMenuData = data;
		if (data != null) {
			this.setBackgroundColor(data.backgroundColor());
			this.setForegroundColor(data.foregroundColor());
			this.setSelectionColor(data.selectionColor());
			this.setRowHeight(data.rowHeight());
			this.setPadding(data.padding());
		}
	}
	
	@Override
	public void build() {
		super.build();
		
		this.createControl();
		this.initControl();
		final var mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		mainFrame.add(this.control);
		this.updateAllComponentZOrder();
	}
	
	@Override
	public void performAction() {
		if (this.enabled) {
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, this.lastSelectedItemId));
		}
	}
	
	@Override
	public void render() {
		if (this.modified || this.control.isDirty()) {
			this.initControl();
			this.rebuildBufferedImage();
			this.control.paint(this.bufferedImageGraphics);
			this.control.setDirty(false);
			this.updateAllComponentZOrder();
		}
		else if (this.positionModified) {
			this.control.setLocation(this.x, this.y);
			this.positionModified = false;
		}
		this.tooltipMousePosition = this.control.getLastMousePosition();
		
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
	}
	
	protected void createControl() {
		this.control = new CustomPopupMenu(event -> {
			this.lastSelectedItemId = this.control.getLastSelectedItemId();
			if (this.enabled) {
				this.performAction();
			}
		}, this.rendererData.getThemeManager());
		this.control.setName(this.id);
	}
	
	protected void initControl() {
		this.control.setLocation(this.x, this.y);
		this.positionModified = false;
		this.control.setSize(this.width, this.height);
		this.control.setFontSize(this.fontSize);
		this.control.setScaleFactor(this.scaleFactor);
		this.control.setEnabled(this.enabled);
		this.control.setVisible(this.visible);
		this.control.setItems(this.popupMenuData != null ? this.popupMenuData.items() : null);
		this.control.setBackgroundColor(this.backgroundColor != null ? this.backgroundColor.toAwtColor() : null);
		this.control.setForegroundColor(this.foregroundColor != null ? this.foregroundColor.toAwtColor() : null);
		this.control.setSelectionColor(this.selectionColor != null ? this.selectionColor.toAwtColor() : null);
		if (this.rowHeight > 0) {
			this.control.setRowHeight((int) (this.rowHeight * this.scaleFactor));
		}
		else {
			this.control.setRowHeight(0);
		}
		if (this.padding > 0) {
			this.control.setPadding((int) (this.padding * this.scaleFactor));
		}
		else {
			this.control.setPadding(0);
		}
	}
	
	private void setBackgroundColor(final RgbaColor backgroundColor) {
		this.modified |= !Objects.equals(this.backgroundColor, backgroundColor);
		this.backgroundColor = backgroundColor;
	}
	
	private void setForegroundColor(final RgbaColor foregroundColor) {
		this.modified |= !Objects.equals(this.foregroundColor, foregroundColor);
		this.foregroundColor = foregroundColor;
	}
	
	private void setSelectionColor(final RgbaColor selectionColor) {
		this.modified |= !Objects.equals(this.selectionColor, selectionColor);
		this.selectionColor = selectionColor;
	}
	
	private void setRowHeight(final int rowHeight) {
		this.modified |= this.rowHeight != rowHeight;
		this.rowHeight = rowHeight;
	}
	
	private void setPadding(final int padding) {
		this.modified |= this.padding != padding;
		this.padding = padding;
	}
}
