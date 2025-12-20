package de.extio.game_engine.renderer.g2d.control.impl;

import de.extio.game_engine.renderer.g2d.control.components.CustomWindowCloseButton;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowCloseButtonControl;

public class G2DWindowCloseButtonControlImpl extends G2DButtonControlImpl implements WindowCloseButtonControl {
	
	private int translations;
	
	private boolean thickBorder;
	
	@Override
	protected void createControl() {
		this.control = new CustomWindowCloseButton(false, event -> this.performAction());
		this.control.setName(this.id);
	}
	
	@Override
	protected void initControl() {
		this.translate(super::initControl);
	}
	
	@Override
	public void render() {
		this.translate(super::render);
	}
	
	@Override
	protected void rebuildBufferedImage() {
		this.translate(super::rebuildBufferedImage);
	}
	
	private void translate(final Runnable run) {
		if (this.translations > 0) {
			run.run();
		}
		else {
			this.translations++;
			final var x = this.x;
			final var y = this.y;
			final var w = this.width;
			final var h = this.height;
			try {
				final var sx = (int) this.x;
				final var sy = (int) this.y;
				final var sw = (int) this.width;
				final var strength = Math.max(2, (int) (3 * this.scaleFactor));
				
				this.x = sx + sw - strength * 9;
				this.y = sy + strength * 2;
				this.width = strength * 7;
				this.height = strength * 7;
				
				run.run();
			}
			finally {
				this.x = x;
				this.y = y;
				this.width = w;
				this.height = h;
				this.translations--;
			}
		}
	}
	
	@Override
	public boolean isThickBorder() {
		return this.thickBorder;
	}
	
	@Override
	public void setThickBorder(final boolean thickBorder) {
		this.thickBorder = thickBorder;
	}
	
}
