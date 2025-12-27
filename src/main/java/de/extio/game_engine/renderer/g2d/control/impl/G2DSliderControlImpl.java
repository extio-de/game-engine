package de.extio.game_engine.renderer.g2d.control.impl;

import java.util.function.Consumer;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomSlider;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

public class G2DSliderControlImpl extends G2DBaseControlImpl implements SliderControl {
	
	protected CustomSlider control;
	
	protected RgbaColor color;
	
	protected boolean horizontal;
	
	protected double lastValue;
	
	protected double lastValue2;
	
	protected Consumer<Double> actionConsumer;
	
	public G2DSliderControlImpl() {
		
	}
	
	public G2DSliderControlImpl(final Consumer<Double> actionConsumer) {
		this.actionConsumer = actionConsumer;
	}
	
	@Override
	public RgbaColor getColor() {
		return this.color;
	}
	
	@Override
	public void setColor(final RgbaColor color) {
		this.modified |= (color != null) && !color.equals(this.color);
		this.color = color;
	}
	
	@Override
	public double getValue() {
		return this.lastValue;
	}
	
	@Override
	public void setValue(final double value) {
		this.modified |= value != this.lastValue;
		this.lastValue = value;
	}
	
	@Override
	public double getValue2() {
		return this.lastValue2;
	}
	
	@Override
	public void setValue2(final double value2) {
		this.modified |= value2 != this.lastValue2;
		this.lastValue2 = value2;
	}
	
	@Override
	public boolean isHorizontal() {
		return this.horizontal;
	}
	
	@Override
	public void setHorizontal(final boolean horizontal) {
		this.modified |= horizontal != this.horizontal;
		this.horizontal = horizontal;
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
			if (this.actionConsumer == null) {
				this.rendererData.getEventService().fire(new UiControlEvent(this.id, this.lastValue));
			}
			else {
				this.actionConsumer.accept(this.lastValue);
			}
		}
	}
	
	@Override
	public void render() {
		this.control.releaseEvents();
		
		if (this.modified || this.control.isDirty()) {
			this.initControl();
			this.rebuildBufferedImage();
			this.control.paint(this.bufferedImageGraphics);
			this.control.setDirty(false);
			this.updateAllComponentZOrder();
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
		if (this.control.isFocusOwner()) {
			((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().requestFocus();
		}
		this.control.setVisible(false);
		this.control.invalidate();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.control);
		
		super.close();
	}
	
	protected void createControl() {
		this.control = new CustomSlider(event -> {
			this.lastValue = Double.parseDouble(event.getActionCommand());
			if (this.enabled) {
				this.performAction();
			}
		});
		this.control.setName(this.id);
	}
	
	protected void initControl() {
		this.control.setLocation(this.x, this.y);
		this.control.setSize(this.width, this.height);
		this.control.setScaleFactor(this.scaleFactor);
		this.control.setColor(this.color != null ? this.color.toAwtColor() : null);
		this.control.setHorizontal(this.horizontal);
		this.control.setValue(this.lastValue);
		this.control.setValue2(this.lastValue2);
		this.control.setEnabled(this.enabled);
		this.control.setVisible(this.visible);
	}
	
}
