package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomSingleLineTextField;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DSingleLineTextfieldControlImpl extends G2DBaseControlImpl implements TextfieldControl {

	private CustomSingleLineTextField textField;

	private boolean readonly;

	private long lastCaptionUpdateTime;

	private long lastCaptionUpdateTimeInternal;

	private String lastText = "";

	private RgbaColor backgroundColor;

	@Override
	public void setCustomData(final TextfieldData data) {
		if (data != null) {
			this.setReadonly(data.readonly());
			this.setBackgroundColor(data.backgroundColor());
		}
	}

	public G2DSingleLineTextfieldControlImpl() {
	}

	private void setBackgroundColor(final RgbaColor color) {
		this.modified |= (color == null) ? this.backgroundColor != null : !color.equals(this.backgroundColor);
		this.backgroundColor = color;
	}

	private void setReadonly(final boolean readonly) {
		this.modified |= readonly != this.readonly;
		this.readonly = readonly;
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

		this.textField = new CustomSingleLineTextField(
				this.rendererData.getThemeManager(),
				text -> {
					this.lastText = text;
					this.lastCaptionUpdateTimeInternal = System.currentTimeMillis();
					try {
						Thread.sleep(2);
					}
					catch (final InterruptedException e) {
					}
					this.rendererData.getEventService().fire(new UiControlEvent(this.id, text));
				},
				() -> {
					final String submitText = (this.lastText == null ? "" : this.lastText) + "\n";
					this.textField.setText("");
					this.lastText = "";
					this.caption = null;
					this.lastCaptionUpdateTimeInternal = System.currentTimeMillis();
					this.rendererData.getEventService().fire(new UiControlEvent(this.id, submitText));
				});

		this.textField.setName(this.id);
		this.textField.setFocusable(true);
		this.initControl();

		final var mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		mainFrame.add(this.textField);
		this.updateAllComponentZOrder();
	}

	@Override
	public void performAction() {
	}

	@Override
	public void render() {
		if (this.lastCaptionUpdateTime > this.lastCaptionUpdateTimeInternal && this.caption != null && !this.caption.equals(this.lastText)) {
			this.textField.setText(this.caption);
			this.lastText = this.caption;
			this.lastCaptionUpdateTimeInternal = this.lastCaptionUpdateTime;
		}

		final boolean componentDirty = this.textField.isDirty();
		if (this.modified || componentDirty) {
			this.rebuildBufferedImage();
			this.bufferedImageGraphics.setClip(0, 0, this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
			this.textField.setSize(this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
			if (this.modified || componentDirty) {
				this.initControl();
			}
			this.textField.paint(this.bufferedImageGraphics);
			this.textField.setDirty(false);
			this.updateAllComponentZOrder();
		}
		else if (this.positionModified) {
			this.textField.setLocation(this.x, this.y);
			this.positionModified = false;
			this.updateAllComponentZOrder();
		}

		this.tooltipMousePosition = this.textField.getLastMousePosition();
		this.textField.paint(this.bufferedImageGraphics);

		super.render();
	}

	@Override
	public void close() {
		if (this.textField.isFocusOwner()) {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().requestFocus();
		}
		this.textField.setVisible(false);
		this.textField.invalidate();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.textField);

		super.close();
	}

	private void initControl() {
		final var theme = this.rendererData.getThemeManager().getCurrentTheme();
		final var bgColor = this.backgroundColor != null ? this.backgroundColor.toAwtColor() : theme.getBackgroundNormal().toColor();
		final var fgColor = theme.getTextNormal().toColor();

		this.textField.setBackgroundColor(bgColor);
		this.textField.setForegroundColor(fgColor);
		this.textField.setFontSize(this.fontSize);
		this.textField.setScaleFactor(this.scaleFactor);
		this.textField.setLocation(this.x, this.y);
		this.textField.setSize(this.width, this.height);
		this.positionModified = false;
		this.textField.setVisible(this.visible);
		this.textField.setEnabled(this.enabled);
		this.textField.setReadonly(this.readonly);
	}
}
