package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomLabel;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DLabelControlImpl extends G2DBaseControlImpl implements LabelControl {
	
	protected CustomLabel control;
	
	protected RgbaColor backgroundColor;
	
	protected RgbaColor foregroundColor;
	
	protected HorizontalAlignment textAlignment;
	
	@Override
	public void setCustomData(final LabelData data) {
		if (data != null) {
			this.setBackgroundColor(data.backgroundColor());
			this.setForegroundColor(data.foregroundColor());
			this.setTextAlignment(data.textAlignment());
		}
	}
	
	private void setBackgroundColor(final RgbaColor color) {
		this.modified |= (color == null) ? this.backgroundColor != null : !color.equals(this.backgroundColor);
		this.backgroundColor = color;
	}
	
	private void setForegroundColor(final RgbaColor color) {
		this.modified |= (color != null) && !color.equals(this.foregroundColor);
		this.foregroundColor = color;
	}
	
	private HorizontalAlignment getTextAlignment() {
		return this.textAlignment;
	}
	
	private void setTextAlignment(final HorizontalAlignment alignment) {
		this.modified |= alignment != this.textAlignment;
		this.textAlignment = alignment;
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
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, null));
		}
	}
	
	@Override
	public void render() {
		if (this.modified || this.control.isDirty()) {
			//LOGGER.debug("Label modified " + this.id);
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
		if (this.tooltip != null) {
			this.tooltipMousePosition = this.control.getLastMousePosition();
		}
		else {
			this.tooltipMousePosition = null;
		}
		
		super.render();
	}
	
	@Override
	public void close() {
		this.control.setVisible(false);
		this.control.invalidate();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.control);
		
		super.close();
	}
	
	protected void createControl() {
		this.control = new CustomLabel(event -> this.performAction(), this.rendererData.getThemeManager());
		this.control.setName(this.id);
	}
	
	protected void initControl() {
		this.control.setLocation(this.x, this.y);
		this.positionModified = false;
		this.control.setSize(this.width, this.height);
		this.control.setCaption(this.caption);
		this.control.setFontSize(this.fontSize);
		this.control.setScaleFactor(this.scaleFactor);
		this.control.setBackgroundColor(this.backgroundColor != null ? this.backgroundColor.toAwtColor() : null);
		this.control.setForegroundColor(this.foregroundColor != null ? this.foregroundColor.toAwtColor() : null);
		this.control.setEnabled(this.enabled);
		this.control.setVisible(this.visible);
		this.control.setTextAlignment(this.getTextAlignment());
	}
	
}
