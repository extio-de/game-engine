package de.extio.game_engine.renderer.g2d.control.impl;

import java.util.Objects;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomAbstractButton;
import de.extio.game_engine.renderer.g2d.control.components.CustomButton;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.resource.StaticResource;

public class G2DButtonControlImpl extends G2DBaseControlImpl implements ButtonControl {
	
	protected CustomAbstractButton control;
	
	protected StaticResource iconResource;
	
	private RgbaColor backgroundColor;
	
	private boolean positionModified;
	
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
		this.control.close();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.control);
		
		super.close();
	}
	
	protected void createControl() {
		this.control = new CustomButton(false, event -> this.performAction(), ((G2DRenderer) this.rendererData.getRenderer()).getThemeManager());
		this.control.setName(this.id);
	}
	
	protected void initControl() {
		this.control.setLocation(this.x, this.y);
		this.positionModified = false;
		this.control.setSize(this.width, this.height);
		this.control.setCaption(this.caption);
		this.control.setFontSize(this.fontSize);
		this.control.setScaleFactor(this.scaleFactor);
		this.control.setEnabled(this.enabled);
		this.control.setVisible(this.visible);
		this.control.setIconResource(this.iconResource);
		this.control.setStaticResourceService(this.rendererData.getStaticResourceService());
		if (this.backgroundColor == null) {
			this.control.setBackgroundColor(null);
		}
		else {
			this.control.setBackgroundColor(this.backgroundColor.toAwtColor());
		}
	}
	
	@Override
	public BaseControl setX(final int x) {
		this.positionModified |= this.x != x;
		this.x = x;
		return this;
	}
	
	@Override
	public BaseControl setY(final int y) {
		this.positionModified |= this.y != y;
		this.y = y;
		return this;
	}
	
	@Override
	public void setIconResource(final StaticResource iconResource) {
		this.modified |= !Objects.equals(this.iconResource, iconResource);
		this.iconResource = iconResource;
	}
	
	@Override
	public StaticResource getIconResource() {
		return this.iconResource;
	}
	
	@Override
	public RgbaColor getBackgroundColor() {
		return this.backgroundColor;
	}
	
	@Override
	public void setBackgroundColor(final RgbaColor backgroundColor) {
		this.modified |= !Objects.equals(this.backgroundColor, backgroundColor);
		this.backgroundColor = backgroundColor;
	}
	
}
