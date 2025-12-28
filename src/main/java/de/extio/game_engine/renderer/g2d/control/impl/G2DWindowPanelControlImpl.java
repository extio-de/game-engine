package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.control.components.CustomWindowPanel;
import de.extio.game_engine.renderer.g2d.theme.G2DThemeManager;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowPanelControl;
import de.extio.game_engine.renderer.model.color.RgbaColor;

public class G2DWindowPanelControlImpl extends G2DBaseControlImpl implements WindowPanelControl {
	
	protected CustomWindowPanel control;
	
	protected boolean thickBorder;
	
	protected RgbaColor color;
	
	@Override
	public boolean isThickBorder() {
		return this.thickBorder;
	}
	
	@Override
	public void setThickBorder(final boolean thickBorder) {
		this.modified |= thickBorder != this.thickBorder;
		this.thickBorder = thickBorder;
	}
	
	@Override
	public RgbaColor getColor() {
		return this.color;
	}
	
	@Override
	public void setColor(final RgbaColor color) {
		this.modified |= (color == null) ? this.color != null : !color.equals(this.color);
		this.color = color;
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
		// Window panel is decorative, no action needed
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
		
		super.render();
	}
	
	@Override
	public void close() {
		this.control.setVisible(false);
		this.control.invalidate();
		((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().remove(this.control);
		
		this.thickBorder = false;
		this.color = null;
		
		super.close();
	}
	
	protected void createControl() {
		this.control = new CustomWindowPanel((G2DThemeManager) this.rendererData.getThemeManager());
		this.control.setName(this.id);
	}
	
	protected void initControl() {
		this.control.setLocation(this.x, this.y);
		this.control.setSize(this.width, this.height);
		this.control.setScaleFactor(this.scaleFactor);
		this.control.setThickBorder(this.thickBorder);
		this.control.setColor(this.color != null ? this.color.toAwtColor() : null);
		this.control.setVisible(this.visible);
	}
	
}
