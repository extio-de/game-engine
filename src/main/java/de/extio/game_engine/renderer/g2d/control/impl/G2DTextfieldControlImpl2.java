package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.ComponentRenderingSupport;
import de.extio.game_engine.renderer.g2d.control.components.CustomJTextArea;
import de.extio.game_engine.renderer.g2d.control.components.CustomJTextField;
import de.extio.game_engine.renderer.g2d.bo.rendering.G2DDrawFont;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.model.RgbaColor;

public class G2DTextfieldControlImpl2 extends G2DBaseControlImpl implements TextfieldControl {
	
	private JTextComponent textComponent;
	
	//	private CustomJScrollPane scrollPane;
	
	private final G2DSliderControlImpl scrollbar;
	
	private boolean multiLine;
	
	private boolean readonly;
	
	private String lastCaption = "";
	
	private String lastText = "";
	
	protected RgbaColor backgroundColor;
	
	private boolean scrollPositionModified;
	
	private double scrollPositionPerc = 0.0;
	
	public G2DTextfieldControlImpl2() {
		this.scrollbar = new G2DSliderControlImpl(value -> {
			this.onScroll(value.doubleValue());
		});
	}
	
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
	public boolean isMultiLine() {
		return this.multiLine;
	}
	
	@Override
	public void setMultiLine(final boolean multiLine) {
		if (this.textComponent != null) {
			throw new UnsupportedOperationException("Multiline property is read-only after initial control creation");
		}
		this.multiLine = multiLine;
	}
	
	@Override
	public boolean isReadonly() {
		return this.readonly;
	}
	
	@Override
	public void setReadonly(final boolean readonly) {
		this.modified |= readonly != this.readonly;
		this.readonly = readonly;
	}
	
	@Override
	public BaseControl setCaption(final String caption) {
		if (this.multiLine) {
			this.modified |= caption != null && !caption.equals(this.caption);
		}
		else {
			this.modified |= caption != null && (!caption.equals(this.caption) || !caption.equals(this.textComponent.getText()));
		}
		if (caption != null) {
			this.lastCaption = caption;
		}
		this.caption = caption;
		return this;
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
					final int displayRows = countMatches(G2DTextfieldControlImpl2.this.lastCaption, '\n');
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
				// Simulate pressing enter
				this.rendererData.getEventConsumer().accept(new UiControlEvent(this.id, (this.lastText == null ? "" : this.lastText) + "\n"));
			});
		}
		this.textComponent.setName(this.id);
		this.initControl();
		
		if (this.multiLine) {
			//			((G2DRenderer) this.RendererData.getRenderer()).getMainFrame().add(this.scrollPane);
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().add(this.textComponent);
			
			this.scrollbar.setColor(RgbaColor.LIGHT_GRAY);
			this.scrollbar.setHorizontal(false);
			this.scrollbar.setValue(1.0);
			this.scrollbar.setValue2(1.0);
			this.scrollbar.build();
		}
		else {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().add(this.textComponent);
		}
	}
	
	@Override
	public void performAction() {
		
	}
	
	@Override
	public void render() {
		final String curComponentText = this.textComponent.getText();
		if (!curComponentText.equals(this.lastText) && !(this.lastText == null || this.lastText.isEmpty() || curComponentText == null || curComponentText.isEmpty())) {
			this.lastText = curComponentText;
			this.rendererData.getEventConsumer().accept(new UiControlEvent(this.id, this.lastText));
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
		this.safeInvoke(() -> this.textComponent.setBackground(this.backgroundColor != null ? this.backgroundColor.toAwtColor() : ComponentRenderingSupport.COLOR_COMPONENT_BGR));
		this.safeInvoke(() -> this.textComponent.setForeground(Color.WHITE));
		this.safeInvoke(() -> this.textComponent.setFont(G2DDrawFont.getFont(this.scaleFactor, this.fontSize)));
		if (this.multiLine) {
			//			this.safeInvoke(() -> this.scrollPane.setLocation(this.x, this.y));
			//			this.safeInvoke(() -> this.scrollPane.setSize(this.width, this.height));
			this.safeInvoke(() -> this.textComponent.setLocation(this.x, this.y));
			this.safeInvoke(() -> this.textComponent.setSize(this.width - 30, this.height));
			this.safeInvoke(() -> this.textComponent.setEditable(false));
			//			this.safeInvoke(() -> this.textComponent.setFocusable(false));
			this.safeInvoke(() -> ((CustomJTextArea) this.textComponent).setLineWrap(true));
			this.safeInvoke(() -> ((CustomJTextArea) this.textComponent).setWrapStyleWord(true));
			
			this.scrollbar.setX(this.x + this.width - 30);
			this.scrollbar.setY(this.y);
			this.scrollbar.setWidth(30);
			this.scrollbar.setHeight(this.height);
			this.scrollbar.setScaleFactor(this.scaleFactor);
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
					final int displayRows = countMatches(this.lastCaption, '\n');
					final int skipRows = (int) ((double) this.scrollPositionPerc * (double) displayRows);
					int offset = 0;
					for (int i = 0; i < skipRows; i++) {
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
				if (this.caption != null && !this.textComponent.getText().equals(this.caption)) {
					final int pos = this.textComponent.getCaretPosition();
					this.safeInvoke(() -> this.textComponent.setText(this.caption));
					if (pos < this.textComponent.getText().length()) {
						this.safeInvoke(() -> this.textComponent.setCaretPosition(pos));
					}
					
					this.lastText = this.caption;
				}
			}
		}
		catch (final Exception exc) {
			this.modified = true;
		}
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
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
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
	public BaseControl setId(final String id) {
		this.scrollbar.setId(id + "_scroll");
		return super.setId(id);
	}
	
}
