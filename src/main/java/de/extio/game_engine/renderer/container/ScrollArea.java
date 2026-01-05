package de.extio.game_engine.renderer.container;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoHasDimension;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderData;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class ScrollArea implements WindowComponent {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ScrollArea.class);
	
	protected static final int SCROLLBAR_WIDTH = 20;
	
	protected static final int SCROLLBAR_WIDTH_WITH_MARGIN = 25;
	
	protected final RenderingBoPool renderingBoPool;
	
	protected final EventService eventService;
	
	protected final String id = Objects.toIdentityString(this);
	
	protected final String verticalScrollbarName = Objects.toIdentityString(this) + "_VerticalScrollbar";
	
	protected final String horizontalScrollbarName = Objects.toIdentityString(this) + "_HorizontalScrollbar";
	
	protected final MutableCoordI2 contentDimension = MutableCoordI2.create();
	
	protected final Set<String> renderingBoIds = new HashSet<>();
	
	protected final Area2 relativeArea = new Area2(ImmutableCoordI2.zero(), ImmutableCoordI2.one());
	
	protected Window parent;
	
	protected double scrollPositionVertical = 1.0;
	
	protected double scrollPositionHorizontal = 0.0;
	
	public ScrollArea(final RenderingBoPool renderingBoPool, final EventService eventService) {
		this.renderingBoPool = renderingBoPool;
		this.eventService = eventService;
	}
	
	@Override
	public void onAddedToWindow() {
		this.eventService.register(UiControlEvent.class, this.id, this::onScrollbarControlEvent);
		this.eventService.register(MouseClickEvent.class, this.id, this::onMouseWheelEvent);
	}
	
	@Override
	public void onRemovedFromWindow() {
		this.eventService.unregister(UiControlEvent.class, this.id);
		this.eventService.unregister(MouseClickEvent.class, this.id);
	}
	
	protected void onScrollbarControlEvent(final UiControlEvent event) {
		if (this.verticalScrollbarName.equals(event.getId())) {
			final double rawValue = ((Double) event.getPayload()).doubleValue();
			this.scrollPositionVertical = rawValue;
			this.parent.draw();
		}
		else if (this.horizontalScrollbarName.equals(event.getId())) {
			final double rawValue = ((Double) event.getPayload()).doubleValue();
			this.scrollPositionHorizontal = rawValue;
			this.parent.draw();
		}
	}
	
	protected void onMouseWheelEvent(final MouseClickEvent event) {
		if (event.getButton() == 4 || event.getButton() == 5) {
			if (!SpatialUtils2.intersects(
					this.relativeArea.getPosition().add(this.parent.getAbsolutePosition()),
					this.relativeArea.getDimension(),
					event.getScaledCoord(),
					ImmutableCoordI2.one())) {
				return;
			}
			
			final double scrollIncrement = 1.0 / Math.max(1, this.contentDimension.getY() / 50);
			final double delta = event.getButton() == 4 ? scrollIncrement : -scrollIncrement;
			this.scrollPositionVertical = Math.max(0.0, Math.min(1.0, this.scrollPositionVertical + delta));
			this.parent.draw();
		}
	}
	
	@Override
	public void draw() {
		final var renderingBos = this.parent.getRenderingBos().values();
		
		this.contentDimension.setXY(0, 0);
		for (final var bo : renderingBos) {
			if (bo instanceof final RenderingBoHasDimension boWithDim && this.renderingBoIds.contains(bo.getId())) {
				this.contentDimension.setX(Math.max(this.contentDimension.getX(), bo.getLocalX() + boWithDim.getWidth()));
				this.contentDimension.setY(Math.max(this.contentDimension.getY(), bo.getLocalY() + boWithDim.getHeight()));
			}
		}
		final var needsHorizontalScrollbar = this.contentDimension.getX() > this.relativeArea.getDimension().getX();
		final var needsVerticalScrollbar = this.contentDimension.getY() > this.relativeArea.getDimension().getY();
		
		if (needsHorizontalScrollbar || needsVerticalScrollbar) {
			for (final var bo : renderingBos) {
				if (this.renderingBoIds.contains(bo.getId())) {
					bo.withVisibleArea(
							this.parent.getAbsolutePosition().getX() + this.relativeArea.getPosition().getX(),
							this.parent.getAbsolutePosition().getY() + this.relativeArea.getPosition().getY(),
							this.relativeArea.getDimension().getX() - (needsVerticalScrollbar ? SCROLLBAR_WIDTH_WITH_MARGIN : 0),
							this.relativeArea.getDimension().getY() - (needsHorizontalScrollbar ? SCROLLBAR_WIDTH_WITH_MARGIN : 0));
					this.parent.putRenderingBo(bo);
				}
			}
			
			if (needsVerticalScrollbar) {
				final var verticalScrollbar = this.renderingBoPool.acquire(this.verticalScrollbarName, ControlRenderingBo.class)
						.setType(SliderControl.class)
						.setControlData(new SliderData(false, this.scrollPositionVertical, this.scrollPositionVertical, null))
						.setEnabled(true)
						.setVisible(true)
						.withDimensionAbsolute(SCROLLBAR_WIDTH, this.relativeArea.getDimension().getY() - SCROLLBAR_WIDTH)
						.withPositionRelative(this.relativeArea.getPosition().getX() + this.relativeArea.getDimension().getX() - SCROLLBAR_WIDTH, this.relativeArea.getPosition().getY())
						.setLayer(RenderingBoLayer.UI_TOP);
				this.parent.putRenderingBo(verticalScrollbar);
			}
			else {
				this.parent.removeRenderingBo(this.verticalScrollbarName);
			}
			
			if (needsHorizontalScrollbar) {
				final var horizontalScrollbar = this.renderingBoPool.acquire(this.horizontalScrollbarName, ControlRenderingBo.class)
						.setType(SliderControl.class)
						.setControlData(new SliderData(true, this.scrollPositionHorizontal, this.scrollPositionHorizontal, null))
						.setEnabled(true)
						.setVisible(true)
						.withDimensionAbsolute(this.relativeArea.getDimension().getX() - SCROLLBAR_WIDTH, SCROLLBAR_WIDTH)
						.withPositionRelative(this.relativeArea.getPosition().getX(), this.relativeArea.getPosition().getY() + this.relativeArea.getDimension().getY() - SCROLLBAR_WIDTH)
						.setLayer(RenderingBoLayer.UI_TOP);
				this.parent.putRenderingBo(horizontalScrollbar);
			}
			else {
				this.parent.removeRenderingBo(this.horizontalScrollbarName);
			}
		}
	}
	
	@Override
	public CoordI2 getRenderingBoExtraOffset(final RenderingBo renderingBo) {
		final int startX = (int) (this.scrollPositionHorizontal * (this.contentDimension.getX() - this.relativeArea.getDimension().getX() + SCROLLBAR_WIDTH_WITH_MARGIN));
		final int startY = (int) ((1.0 - this.scrollPositionVertical) * (this.contentDimension.getY() - this.relativeArea.getDimension().getY() + SCROLLBAR_WIDTH_WITH_MARGIN));
		return ImmutableCoordI2.create(-startX, -startY);
	}
	
	public void putRenderingBo(final RenderingBo renderingBo) {
		this.renderingBoIds.add(renderingBo.getId());
		this.parent.putRenderingBo(renderingBo);
	}
	
	public void removeRenderingBo(final String renderingBoId) {
		this.renderingBoIds.remove(renderingBoId);
		this.parent.removeRenderingBo(renderingBoId);
	}
	
	public double getScrollPositionVertical() {
		return this.scrollPositionVertical;
	}
	
	public void setScrollPositionVertical(final double scrollPositionVertical) {
		// this.scrollPositionVertical = Math.max(0.0, Math.min(1.0, scrollPositionVertical));
		// this.draw();
	}
	
	public double getScrollPositionHorizontal() {
		return this.scrollPositionHorizontal;
	}
	
	public void setScrollPositionHorizontal(final double scrollPositionHorizontal) {
		// this.scrollPositionHorizontal = Math.max(0.0, Math.min(1.0, scrollPositionHorizontal));
		// this.draw();
	}
	
	@Override
	public CoordI2 getRelativePosition() {
		return this.relativeArea.getPosition();
	}
	
	public void setRelativePosition(final CoordI2 relativePosition) {
		this.relativeArea.setPosition(relativePosition.toImmutableCoordI2());
	}
	
	public void setDimension(final CoordI2 dimension) {
		this.relativeArea.setDimension(dimension.toImmutableCoordI2());
	}
	
	public void setRelativeArea(final Area2 relativeArea) {
		this.relativeArea.setPosition(relativeArea.getPosition().toImmutableCoordI2());
		this.relativeArea.setDimension(relativeArea.getDimension().toImmutableCoordI2());
	}
	
	@Override
	public Set<String> getRenderingBoIds() {
		return this.renderingBoIds;
	}
	
	@Override
	public void setParent(final Window window) {
		this.parent = window;
	}
	
}
