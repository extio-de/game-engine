package de.extio.game_engine.renderer.g2d.control.impl;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.g2d.G2DRenderer;
import de.extio.game_engine.renderer.g2d.G2DRenderingHintFactory;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControlTooltip;
import de.extio.game_engine.renderer.g2d.control.G2DDrawControlTooltip.TooltipRecord;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.BaseControl;
import de.extio.game_engine.spatial2.WorldUtils2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public abstract class G2DBaseControlImpl implements BaseControl {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(G2DBaseControlImpl.class);
	
	protected int x;
	
	protected int y;
	
	protected int width;
	
	protected int height;
	
	protected int fontSize;
	
	protected Graphics2D mainFrameGraphics;
	
	protected RendererData rendererData;
	
	protected String id;
	
	protected boolean inUse;
	
	protected String caption;
	
	protected double scaleFactor;
	
	protected boolean modified;
	
	protected String controlGroup;
	
	protected boolean visible;
	
	protected boolean enabled;
	
	protected BufferedImage bufferedImage;
	
	protected Graphics2D bufferedImageGraphics;
	
	protected String tooltip;
	
	protected CoordI2 tooltipMousePosition;
	
	@Override
	public BaseControl setInUse(final boolean inUse) {
		this.inUse = inUse;
		return this;
	}
	
	@Override
	public boolean getInUse() {
		return this.inUse;
	}
	
	@Override
	public BaseControl setControlId(final String id) {
		this.id = id;
		return this;
	}
	
	@Override
	public BaseControl setCaption(final String caption) {
		this.modified |= caption != null && !Objects.equals(this.caption, caption);
		this.caption = caption;
		return this;
	}
	
	@Override
	public BaseControl setRendererData(final RendererData RendererData) {
		this.rendererData = (RendererData) RendererData;
		return this;
	}
	
	public G2DBaseControlImpl setMainFrameGraphics(final Graphics2D graphics) {
		this.mainFrameGraphics = graphics;
		return this;
	}
	
	@Override
	public BaseControl setX(final int x) {
		this.modified |= this.x != x;
		this.x = x;
		return this;
	}
	
	@Override
	public BaseControl setY(final int y) {
		this.modified |= this.y != y;
		this.y = y;
		return this;
	}
	
	@Override
	public BaseControl setWidth(final int width) {
		this.modified |= this.width != width;
		this.width = width;
		return this;
	}
	
	@Override
	public BaseControl setHeight(final int height) {
		this.modified |= this.height != height;
		this.height = height;
		return this;
	}
	
	@Override
	public BaseControl setFontSize(final int size) {
		this.modified |= this.fontSize != size;
		this.fontSize = size;
		return this;
	}
	
	@Override
	public BaseControl setControlGroup(final String controlGroup) {
		this.controlGroup = controlGroup;
		return this;
	}
	
	@Override
	public String getControlGroup() {
		return this.controlGroup;
	}
	
	@Override
	public BaseControl setVisible(final boolean visible) {
		this.modified |= visible != this.visible;
		this.visible = visible;
		return this;
	}
	
	public G2DBaseControlImpl setScaleFactor(final double scaleFactor) {
		this.modified = this.scaleFactor != scaleFactor;
		this.scaleFactor = scaleFactor;
		return this;
	}
	
	@Override
	public BaseControl setEnabled(final boolean enabled) {
		this.modified |= enabled != this.enabled;
		this.enabled = enabled;
		return this;
	}
	
	@Override
	public BaseControl setTooltip(final String tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	@Override
	public void render() {
		if (this.visible && this.bufferedImage != null) {
			this.mainFrameGraphics.drawImage(this.bufferedImage, this.x, this.y, null);
			this.drawToolTip();
		}
		
		if (this.modified) {
			this.modified = false;
		}
	}
	
	@Override
	public void build() {
		this.rebuildBufferedImage();
		this.modified = false;
		this.enabled = true;
	}
	
	@Override
	public void close() {
		this.disposeBufferedImage();
	}
	
	@Override
	public String toString() {
		return "BaseControlImpl [id=" + this.id + ", class=" + this.getClass().getSimpleName() + "]";
	}
	
	protected void rebuildBufferedImage() {
		this.disposeBufferedImage();
		this.bufferedImage = ((G2DRenderer) this.rendererData.getRenderer()).getMainFrame().getGraphicsConfiguration().createCompatibleImage(Math.max(1, this.width), Math.max(1, this.height), Transparency.TRANSLUCENT);
		this.bufferedImageGraphics = this.bufferedImage.createGraphics();
		this.bufferedImageGraphics.setRenderingHints(G2DRenderingHintFactory.createDefault());
	}
	
	protected void disposeBufferedImage() {
		if (this.bufferedImageGraphics != null) {
			this.bufferedImageGraphics.dispose();
			this.bufferedImageGraphics = null;
		}
		if (this.bufferedImage != null) {
			this.bufferedImage.flush();
			this.bufferedImage = null;
		}
	}
	
	protected void drawToolTip() {
		if (this.enabled &&
				this.tooltip != null &&
				this.tooltipMousePosition != null &&
				WorldUtils2.isInBounds(this.tooltipMousePosition, ImmutableCoordI2.create(Math.max(1, this.width), Math.max(1, this.height)))) {
			
			G2DDrawControlTooltip.TOOLTIP = new TooltipRecord(ImmutableCoordI2.create(this.x + 10 + this.tooltipMousePosition.getX(), this.y + 10 + this.tooltipMousePosition.getY()), this.tooltip);
		}
	}
	
}
