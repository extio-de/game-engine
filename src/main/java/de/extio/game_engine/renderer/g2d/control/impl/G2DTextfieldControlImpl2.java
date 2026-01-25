package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JTextField;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.control.components.CustomJTextField;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DTextfieldControlImpl2 extends G2DBaseControlImpl implements TextfieldControl {
	
	private CustomJTextField textComponent;
	
	private boolean readonly;
	
	private long lastCaptionUpdateTime;

	private long lastCaptionUpdateTimeInternal;
	
	private String lastText = "";
	
	protected RgbaColor backgroundColor;
	
	@Override
	public void setCustomData(final TextfieldData data) {
		if (data != null) {
			this.setReadonly(data.readonly());
			this.setBackgroundColor(data.backgroundColor());
		}
	}
	
	public G2DTextfieldControlImpl2() {
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
		
		this.textComponent = new CustomJTextField();
		this.textComponent.setFocusable(true);
		this.textComponent.addActionListener(event -> {
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, (this.lastText == null ? "" : this.lastText) + "\n"));
			this.caption = null;
			this.lastText = "";
			this.textComponent.setText("");
		});
		this.textComponent.setName(this.id);
		this.initControl();
		
		final var mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		mainFrame.add(this.textComponent);
		this.updateAllComponentZOrder();
	}
	
	@Override
	public void performAction() {
		
	}
	
	@Override
	public void render() {
		final var curComponentText = this.textComponent.getText();
		if (!curComponentText.equals(this.lastText)) {
			this.lastText = curComponentText;
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, this.lastText));
		}
		
		final boolean componentDirty = this.textComponent.isDirty();
		if (this.modified || componentDirty) {
			this.rebuildBufferedImage();
			this.bufferedImageGraphics.setClip(0, 0, this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
			this.textComponent.setDrawGraphics(this.bufferedImageGraphics);
			this.initControl();
			this.textComponent.setDirty(false);
			this.updateAllComponentZOrder();
		}
		else if (this.positionModified) {
			this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
			this.positionModified = false;
		}
		
		this.safeInvoke(() -> this.textComponent.paint(this.bufferedImageGraphics));
		
		super.render();
	}
	
	@Override
	public void close() {
		if (this.textComponent.isFocusOwner()) {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().requestFocus();
		}
		this.safeInvoke(() -> this.textComponent.setVisible(false));
		this.safeInvoke(() -> this.textComponent.invalidate());
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.textComponent);
		
		super.close();
	}
	
	private void initControl() {
		final ThemeManager themeManager = (G2DThemeManager) this.rendererData.getThemeManager();
		final Color bgColor;
		if (this.backgroundColor != null) {
			bgColor = this.backgroundColor.toAwtColor();
		} else {
			bgColor = themeManager.getCurrentTheme().getBackgroundNormal().toColor();
		}
		final var fgColor = themeManager.getCurrentTheme().getTextNormal().toColor();
		this.safeInvoke(() -> this.textComponent.setBackground(bgColor));
		this.safeInvoke(() -> this.textComponent.setForeground(fgColor));
		this.safeInvoke(() -> this.textComponent.setFont(G2DDrawFont.getFont(this.scaleFactor, this.fontSize)));
		this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
		this.safeInvoke(() -> this.textComponent.setSize(this.width, this.height));
		this.safeInvoke(() -> this.textComponent.setEditable(this.enabled));
		this.safeInvoke(() -> this.textComponent.setVisible(this.visible));
		this.safeInvoke(() -> this.textComponent.setEditable(!this.readonly));
		
		try {
			if (this.caption != null && !this.textComponent.getText().equals(this.caption) && (this.lastCaptionUpdateTimeInternal == 0L || this.lastCaptionUpdateTimeInternal < this.lastCaptionUpdateTime)) {
				final var pos = this.textComponent.getCaretPosition();
				this.safeInvoke(() -> this.textComponent.setText(this.caption));
				if (pos < this.textComponent.getText().length()) {
					this.safeInvoke(() -> this.textComponent.setCaretPosition(pos));
				}
				this.lastText = this.caption;
				this.lastCaptionUpdateTimeInternal = this.lastCaptionUpdateTime;
			}
		}
		catch (final Exception exc) {
			this.modified = true;
		}
	}

	private void safeInvoke(final Runnable runnable) {
		try {
			runnable.run();
		}
		catch (final Exception exc) {
			LOGGER.warn(exc.getMessage(), exc);
		}
	}
	
	@Override
	public G2DBaseControlImpl setScaleFactor(final double scaleFactor) {
		return super.setScaleFactor(scaleFactor);
	}
	
	@Override
	public BaseControl setControlGroup(final String controlGroup) {
		return super.setControlGroup(controlGroup);
	}
	
	@Override
	public BaseControl setRendererData(final RendererData RendererData) {
		return super.setRendererData(RendererData);
	}
	
	@Override
	public G2DBaseControlImpl setMainFrameGraphics(final Graphics2D graphics) {
		return super.setMainFrameGraphics(graphics);
	}
	
	@Override
	public BaseControl setControlId(final String id) {
		return super.setControlId(id);
	}
}
