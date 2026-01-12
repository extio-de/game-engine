package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.g2d.control.components.CustomJTextArea;
import de.extio.game_engine.renderer.g2d.control.components.CustomJTextField;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DTextfieldControlImpl2 extends G2DBaseControlImpl implements TextfieldControl {
	
	private JTextComponent textComponent;
	
	//	private CustomJScrollPane scrollPane;
	
	private final G2DSliderControlImpl scrollbar;
	
	private boolean multiLine;
	
	private boolean readonly;
	
	private String lastCaption = "";

	private long lastCaptionUpdateTime;

	private long lastCaptionUpdateTimeInternal;
	
	private String lastText = "";
	
	protected RgbaColor backgroundColor;
	
	private boolean scrollPositionModified;
	
	private double scrollPositionPerc = 0.0;
	
	@Override
	public void setCustomData(final TextfieldData data) {
		if (data != null) {
			this.setMultiLine(data.multiLine());
			this.setReadonly(data.readonly());
			this.setBackgroundColor(data.backgroundColor());
		}
	}
	
	public G2DTextfieldControlImpl2() {
		this.scrollbar = new G2DSliderControlImpl(value -> {
			this.onScroll(value.doubleValue());
		});
	}
	
	private void setBackgroundColor(final RgbaColor color) {
		this.modified |= (color == null) ? this.backgroundColor != null : !color.equals(this.backgroundColor);
		this.backgroundColor = color;
	}
	
	private void setMultiLine(final boolean multiLine) {
		if (this.textComponent == null) {
			this.multiLine = multiLine;
		}
		else {
			// throw new UnsupportedOperationException("Multiline property is read-only after initial control creation");
		}
	}
	
	private void setReadonly(final boolean readonly) {
		this.modified |= readonly != this.readonly;
		this.readonly = readonly;
	}
	
	@Override
	public BaseControl setCaption(final String caption) {
		if (this.multiLine) {
			this.modified |= caption != null && !caption.equals(this.caption);
		}
		if (caption != null) {
			this.lastCaption = caption;
		}
		this.caption = caption;
		return this;
	}

	public void setLastCaptionUpdateTime(final long lastCaptionUpdateTime) {
		if (! this.multiLine) {
			this.modified |= this.lastCaptionUpdateTime != lastCaptionUpdateTime;
		}
		this.lastCaptionUpdateTime = lastCaptionUpdateTime;
	}
	
	@Override
	public void build() {
		super.build();
		
		if (this.multiLine) {
			this.textComponent = new CustomJTextArea();
			//			this.scrollPane = new CustomJScrollPane(this.textComponent);
			
			this.textComponent.addMouseWheelListener(new MouseWheelListener() {
				
				@Override
				public void mouseWheelMoved(final MouseWheelEvent e) {
					final var displayRows = G2DTextfieldControlImpl2.this.countMatches(G2DTextfieldControlImpl2.this.lastCaption, '\n');
					if (displayRows == 0) {
						return;
					}
					
					if (e.getWheelRotation() == -1) {
						G2DTextfieldControlImpl2.this.scrollbar.setValue(Math.min(1.0, Math.max(0.0, G2DTextfieldControlImpl2.this.scrollbar.getValue() + (1.0 / (double) displayRows))));
					}
					else {
						G2DTextfieldControlImpl2.this.scrollbar.setValue(Math.min(1.0, Math.max(0.0, G2DTextfieldControlImpl2.this.scrollbar.getValue() - (1.0 / (double) displayRows))));
					}
					
					G2DTextfieldControlImpl2.this.onScroll(G2DTextfieldControlImpl2.this.scrollbar.getValue());
				}
				
			});
		}
		else {
			this.textComponent = new CustomJTextField();
			((JTextField) this.textComponent).addActionListener(event -> {
				// Simulate pressing enter and reset caption
				this.rendererData.getEventService().fire(new UiControlEvent(this.id, (this.lastText == null ? "" : this.lastText) + "\n"));
				this.caption = null;
				this.lastCaption = "";
				this.lastText = "";
				this.textComponent.setText("");
			});
		}
		this.textComponent.setName(this.id);
		this.initControl();
		
		final var mainFrame = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame();
		if (this.multiLine) {
			//			mainFrame.add(this.scrollPane);
			mainFrame.add(this.textComponent);
			this.updateAllComponentZOrder();
			
			this.scrollbar.setColor(RgbaColor.LIGHT_GRAY);
			this.scrollbar.setHorizontal(false);
			this.scrollbar.setValue(1.0);
			this.scrollbar.setValue2(1.0);
			this.scrollbar.build();
		}
		else {
			mainFrame.add(this.textComponent);
			this.updateAllComponentZOrder();
		}
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
		
		boolean componentDirty;
		if (this.multiLine) {
			componentDirty = ((CustomJTextArea) this.textComponent).isDirty();
		}
		else {
			componentDirty = ((CustomJTextField) this.textComponent).isDirty();
		}
		if (this.modified || componentDirty || this.scrollPositionModified) {
			this.rebuildBufferedImage();
			this.bufferedImageGraphics.setClip(0, 0, this.bufferedImage.getWidth(), this.bufferedImage.getHeight());
			if (this.multiLine) {
				((CustomJTextArea) this.textComponent).setDrawGraphics(this.bufferedImageGraphics);
				//				this.scrollPane.setDrawGraphics(this.bufferedImageGraphics);
			}
			else {
				((CustomJTextField) this.textComponent).setDrawGraphics(this.bufferedImageGraphics);
			}
			this.initControl();
			if (this.multiLine) {
				((CustomJTextArea) this.textComponent).setDirty(false);
			}
			else {
				((CustomJTextField) this.textComponent).setDirty(false);
			}
			this.updateAllComponentZOrder();
		}
		else if (this.positionModified) {
			this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
			this.updateScrollBarArea();
			this.positionModified = false;
		}
		
		if (this.multiLine) {
			//			this.safeInvoke(() -> this.scrollPane.paint(this.bufferedImageGraphics));
			this.safeInvoke(() -> this.textComponent.paint(this.bufferedImageGraphics));
			this.scrollbar.render();
		}
		else {
			this.safeInvoke(() -> this.textComponent.paint(this.bufferedImageGraphics));
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
		if (this.multiLine) {
			//			this.safeInvoke(() -> this.scrollPane.invalidate());
			//			((G2DRenderer) this.RendererData.getRenderer()).getMainFrame().remove(this.scrollPane);
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.textComponent);
			this.scrollbar.close();
		}
		else {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.textComponent);
		}
		
		super.close();
	}
	
	private void initControl() {
		if (this.multiLine && !this.readonly) {
			throw new UnsupportedOperationException("Multiline property can only be set for readonly textfields");
		}
		
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
		if (this.multiLine) {
			//			this.safeInvoke(() -> this.scrollPane.setLocation(this.x, this.y));
			//			this.safeInvoke(() -> this.scrollPane.setSize(this.width, this.height));
			this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
			this.positionModified = false;
			this.safeInvoke(() -> this.textComponent.setSize(this.width - 30, this.height));
			this.safeInvoke(() -> this.textComponent.setEditable(false));
			//			this.safeInvoke(() -> this.textComponent.setFocusable(false));
			this.safeInvoke(() -> ((CustomJTextArea) this.textComponent).setLineWrap(true));
			this.safeInvoke(() -> ((CustomJTextArea) this.textComponent).setWrapStyleWord(true));
			
			this.updateScrollBarArea();
			this.scrollbar.setEnabled(this.enabled);
			this.scrollbar.setVisible(this.visible);
		}
		else {
			this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
			this.safeInvoke(() -> this.textComponent.setSize(this.width, this.height));
			this.safeInvoke(() -> this.textComponent.setEditable(this.enabled));
		}
		this.safeInvoke(() -> this.textComponent.setVisible(this.visible));
		this.safeInvoke(() -> this.textComponent.setEditable(!this.readonly));
		
		try {
			if (this.multiLine) {
				if (this.scrollPositionModified || this.caption != null) {
					final var displayRows = this.countMatches(this.lastCaption, '\n');
					final var skipRows = (int) ((double) this.scrollPositionPerc * (double) displayRows);
					var offset = 0;
					for (var i = 0; i < skipRows; i++) {
						offset = this.lastCaption.indexOf('\n', offset);
						if (offset == -1) {
							break;
						}
						offset++;
					}
					String displayText;
					if (offset > -1) {
						displayText = this.lastCaption.substring(offset);
					}
					else {
						displayText = "";
					}
					
					if (!this.lastText.equals(displayText)) {
						this.safeInvoke(() -> this.textComponent.setText(displayText));
						//						this.safeInvoke(() -> this.textComponent.setCaretPosition(this.textComponent.getDocument().getLength()));
						
						this.lastText = displayText;
					}
					
					this.scrollPositionModified = false;
				}
			}
			else {
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
		}
		catch (final Exception exc) {
			this.modified = true;
		}
	}

	private void updateScrollBarArea() {
		this.scrollbar.setX(this.x + this.width - 30);
		this.scrollbar.setY(this.y);
		this.scrollbar.setWidth(30);
		this.scrollbar.setHeight(this.height);
		this.scrollbar.setScaleFactor(this.scaleFactor);
	}
	
	private void onScroll(final double value) {
		this.scrollPositionPerc = 1.0 - value;
		this.scrollPositionModified = true;
		this.scrollbar.setValue2(value);
	}
	
	private int countMatches(final String str, final char ch) {
		if (str == null || str.isEmpty()) {
			return 0;
		}
		var count = 0;
		for (var i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ch) {
				count++;
			}
		}
		return count;
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
}
