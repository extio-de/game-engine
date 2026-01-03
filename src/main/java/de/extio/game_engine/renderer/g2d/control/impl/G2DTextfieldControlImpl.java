package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Color;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.TextField;
import java.util.Objects;

import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.control.components.CustomTextArea;
import de.extio.game_engine.renderer.g2d.control.components.CustomTextField;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DTextfieldControlImpl extends G2DBaseControlImpl implements TextfieldControl {
	
	private TextComponent textComponent;
	
	private boolean multiLine;
	
	private String lastText;
	
	@Override
	public void setCustomData(final TextfieldData data) {
		if (data != null) {
			this.setMultiLine(data.multiLine());
			this.setReadonly(data.readonly());
			this.setBackgroundColor(data.backgroundColor());
		}
	}
	
	private boolean isMultiLine() {
		return this.multiLine;
	}
	
	private void setMultiLine(final boolean multiLine) {
		if (this.textComponent != null) {
			throw new UnsupportedOperationException("Multiline property is read-only after initial control creation");
		}
		this.multiLine = multiLine;
	}
	
	@Override
	public BaseControl setCaption(final String caption) {
		this.modified |= caption != null && (!Objects.equals(this.caption, caption) || !Objects.equals(this.textComponent.getText(), caption));
		this.caption = caption;
		return this;
	}
	
	@Override
	public void build() {
		super.build();
		
		if (this.multiLine) {
			this.textComponent = new CustomTextArea("", 1, 1, TextArea.SCROLLBARS_VERTICAL_ONLY);
		}
		else {
			this.textComponent = new CustomTextField();
		}
		this.textComponent.setName(this.id);
		this.initTextField();
		
		if (!this.multiLine) {
			((TextField) this.textComponent).addActionListener(event -> {
				// Simulate pressing enter
				this.rendererData.getEventService().fire(new UiControlEvent(this.id, (this.lastText != null ? this.lastText : "") + "\n"));
			});
		}
		
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
		if (!Objects.equals(this.lastText, curComponentText) && !((this.lastText == null || this.lastText.isEmpty()) && (curComponentText == null || curComponentText.isEmpty()))) {
			this.lastText = curComponentText;
			this.rendererData.getEventService().fire(new UiControlEvent(this.id, this.lastText));
		}
		
		boolean componentDirty;
		if (this.multiLine) {
			componentDirty = ((CustomTextArea) this.textComponent).isDirty();
		}
		else {
			componentDirty = ((CustomTextField) this.textComponent).isDirty();
		}
		if (this.modified || componentDirty) {
			//LOGGER.debug("TextComponent modified " + this.id);
			this.initTextField();
			this.rebuildBufferedImage();
			this.safeInvoke(() -> this.textComponent.paint(this.bufferedImageGraphics));
			if (this.multiLine) {
				((CustomTextArea) this.textComponent).setDirty(false);
			}
			else {
				((CustomTextField) this.textComponent).setDirty(false);
			}
			this.updateAllComponentZOrder();
		}
		else if (this.positionModified) {
			this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
			this.positionModified = false;
		}
		
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
	
	private void initTextField() {
		final ThemeManager themeManager = (G2DThemeManager) this.rendererData.getThemeManager();
		final Color bgColor = themeManager.getCurrentTheme().getBackgroundNormal().toColor();
		final Color fgColor = themeManager.getCurrentTheme().getTextNormal().toColor();
		this.safeInvoke(() -> this.textComponent.setBackground(bgColor));
		this.safeInvoke(() -> this.textComponent.setForeground(fgColor));
		this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
		this.positionModified = false;
		this.safeInvoke(() -> this.textComponent.setSize(this.width, this.height));
		this.safeInvoke(() -> this.textComponent.setFont(G2DDrawFont.getFont(this.scaleFactor, this.fontSize)));
		this.safeInvoke(() -> this.textComponent.setVisible(this.visible));
		this.safeInvoke(() -> this.textComponent.setEditable(this.enabled));
		
		try {
			if (this.caption != null && !this.textComponent.getText().equals(this.caption)) {
				final var pos = this.textComponent.getCaretPosition();
				this.safeInvoke(() -> this.textComponent.setText(this.caption));
				this.safeInvoke(() -> this.textComponent.setCaretPosition(pos));
				this.lastText = this.caption;
			}
		}
		catch (final Exception exc) {
			this.modified = true;
		}
		
		if (this.multiLine) {
			this.safeInvoke(() -> this.textComponent.setEditable(false));
			this.safeInvoke(() -> this.textComponent.setFocusable(false));
			final var scrollPos = (this.caption != null ? this.caption.lastIndexOf('\n') : -1) + 1;
			this.safeInvoke(() -> this.textComponent.setCaretPosition(scrollPos));
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
	
	private RgbaColor getBackgroundColor() {
		return null;
	}
	
	private void setBackgroundColor(final RgbaColor color) {
		
	}
	
	private boolean isReadonly() {
		return false;
	}
	
	private void setReadonly(final boolean readonly) {
		
	}
	
}
