package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomMultiLineTextArea;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.MultiLineTextAreaControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.MultiLineTextAreaData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DMultiLineTextAreaControlImpl extends G2DBaseControlImpl implements MultiLineTextAreaControl {
	
	private CustomMultiLineTextArea textArea;
	
	private boolean readonly;
	
	private long lastCaptionUpdateTime;
	
	private long lastCaptionUpdateTimeInternal;
	
	private String lastText = "";
	
	private RgbaColor backgroundColor;
	
	private boolean scrollPositionModified;
	
	private boolean requestFocus;
	
	@Override
	public void setCustomData(final MultiLineTextAreaData data) {
		if (data != null) {
			this.setReadonly(data.readonly());
			this.setBackgroundColor(data.backgroundColor());
			this.setRequestFocus(data.requestFocus());
		}
	}
	
	public G2DMultiLineTextAreaControlImpl() {
	}
	
	private void setBackgroundColor(final RgbaColor color) {
		this.modified |= (color == null) ? this.backgroundColor != null : !color.equals(this.backgroundColor);
		this.backgroundColor = color;
	}
	
	private void setReadonly(final boolean readonly) {
		this.modified |= readonly != this.readonly;
		this.readonly = readonly;
	}
	
	private void setRequestFocus(final boolean requestFocus) {
		this.modified |= requestFocus != this.requestFocus;
		this.requestFocus = requestFocus;
	}
	
	@Override
	public BaseControl setCaption(final String caption) {
		this.caption = caption;
		return this;
	}
	
	public void setLastCaptionUpdateTime(final long lastCaptionUpdateTime) {
		this.modified |= this.lastCaptionUpdateTime != lastCaptionUpdateTime;
		this.lastCaptionUpdateTime = lastCaptionUpdateTime;
	}
	
	@Override
	public void build() {
		super.build();
		
		this.textArea = new CustomMultiLineTextArea(this.rendererData.getThemeManager(), text -> {
			this.lastText = text;
			this.lastCaptionUpdateTimeInternal = System.currentTimeMillis();
			try {
				Thread.sleep(2); // Ensure that the next caption update time (e.g. reacting on UiControlEvent) is always after the last caption update time internal to trigger the caption update in the render method
			}
			catch (final InterruptedException e) {
			}
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, text));
		});
		
		this.textArea.setName(this.id);
		this.textArea.setFocusable(true);
		this.initControl();
		
		final var mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		mainFrame.add(this.textArea);
		this.updateAllComponentZOrder();
		
		if (this.requestFocus) {
			this.textArea.requestFocus();
		}
	}
	
	@Override
	public void performAction() {
	}
	
	@Override
	public void render() {
		if (this.lastCaptionUpdateTime > this.lastCaptionUpdateTimeInternal && this.caption != null && !this.caption.equals(this.lastText)) {
			this.textArea.setText(this.caption);
			this.lastText = this.caption;
			this.lastCaptionUpdateTimeInternal = this.lastCaptionUpdateTime;
		}
		
		final boolean componentDirty = this.textArea.isDirty();
		if (this.modified || componentDirty || this.scrollPositionModified) {
			this.rebuildBufferedImage();
			this.bufferedImageGraphics.setClip(0, 0, this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
			this.textArea.setSize(this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
			if (this.modified || componentDirty) {
				this.initControl();
			}
			this.textArea.paint(this.bufferedImageGraphics);
			this.textArea.setDirty(false);
			this.scrollPositionModified = false;
			this.updateAllComponentZOrder();
		}
		else if (this.positionModified) {
			this.textArea.setLocation(this.x, this.y);
			this.positionModified = false;
			this.updateAllComponentZOrder();
		}
		
		this.textArea.paint(this.bufferedImageGraphics);
		
		super.render();
	}
	
	@Override
	public void close() {
		if (this.textArea.isFocusOwner()) {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().requestFocus();
		}
		this.textArea.setVisible(false);
		this.textArea.invalidate();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.textArea);
		
		super.close();
	}
	
	private void initControl() {
		final var theme = this.rendererData.getThemeManager().getCurrentTheme();
		final var bgColor = this.backgroundColor != null ? this.backgroundColor.toAwtColor() : theme.getBackgroundNormal().toColor();
		final var fgColor = theme.getTextNormal().toColor();
		
		this.textArea.setBackgroundColor(bgColor);
		this.textArea.setForegroundColor(fgColor);
		this.textArea.setFontSize(this.fontSize);
		this.textArea.setScaleFactor(this.scaleFactor);
		this.textArea.setLocation(this.x, this.y);
		this.textArea.setSize(this.width, this.height);
		this.positionModified = false;
		this.textArea.setVisible(this.visible);
		this.textArea.setEnabled(this.enabled);
		this.textArea.setReadonly(this.readonly);
	}
}
