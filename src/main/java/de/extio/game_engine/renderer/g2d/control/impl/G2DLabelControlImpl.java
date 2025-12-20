package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomLabel;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.model.DrawFontRenderingBoTextAlignment;
import de.extio.game_engine.renderer.model.RgbaColor;

public class G2DLabelControlImpl extends G2DBaseControlImpl implements LabelControl {
	
	protected CustomLabel control;
	
	protected RgbaColor backgroundColor;
	
	protected RgbaColor foregroundColor;
	
	protected DrawFontRenderingBoTextAlignment textAlignment;
	
	@Override
	public RgbaColor getBackgroundColor() {
		return this.backgroundColor;
	}
	
	@Override
	public void setBackgroundColor(final RgbaColor color) {
		this.modified |= (color == null) ? this.backgroundColor != null : !color.equals(this.backgroundColor);
		this.backgroundColor = color;
	}
	
	@Override
	public RgbaColor getForegroundColor() {
		return this.foregroundColor;
	}
	
	@Override
	public void setForegroundColor(final RgbaColor color) {
		this.modified |= (color != null) && !color.equals(this.foregroundColor);
		this.foregroundColor = color;
	}
	
	@Override
	public DrawFontRenderingBoTextAlignment getTextAlignment() {
		return this.textAlignment;
	}
	
	@Override
	public void setTextAlignment(final DrawFontRenderingBoTextAlignment alignment) {
		this.modified |= alignment != this.textAlignment;
		this.textAlignment = alignment;
	}
	
	@Override
	public void build() {
		super.build();
		
		this.createControl();
		this.initControl();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().add(this.control);
	}
	
	@Override
	public void performAction() {
		if (this.enabled) {
			this.rendererData.getEventConsumer().accept(new UiControlEvent(this.id, null));
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
		this.control = new CustomLabel(event -> this.performAction());
		this.control.setName(this.id);
	}
	
	protected void initControl() {
		this.control.setLocation(this.x, this.y);
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
