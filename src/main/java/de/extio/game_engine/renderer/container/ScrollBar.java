package de.extio.game_engine.renderer.container;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Helper for scrollbars.
 * I recommend to check for code examples to learn more about the usage of this helper and how to integrate in client modules.
 */
public class ScrollBar {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScrollBar.class);
	
	private final String name;
	
	private double scrollPosition;
	
	private int lastNumElements;
	
	private RenderingBoLayer layer = RenderingBoLayer.UI0_1;
	
	public ScrollBar(final String nameSuffix) {
		this.name = "Scrollbar_" + nameSuffix;
	}
	
	public void onEvent(final UiControlEvent event, final Predicate<CoordI2> positionIntersectsPredicate) {
		final var uiControlEvent = (UiControlEvent) event;
		
		if (this.name.equals(uiControlEvent.getId())) {
			this.scrollPosition = 1.0D - ((Double) uiControlEvent.getPayload()).doubleValue();
		}
	}
	
	public void onMouseScroll(final int button, final CoordI2 absolutePos, final Predicate<CoordI2> positionIntersectsPredicate) {
		if ((button == 4 || button == 5) &&
				this.lastNumElements > 0 &&
				positionIntersectsPredicate.test(absolutePos)) {
			
			final var perElem = (button == 4 ? -1.0 : 1.0) / this.lastNumElements;
			this.scrollPosition = Math.min(1.0, Math.max(0.0, this.scrollPosition + perElem));
		}
	}
	
	public void render(final List<RenderingBo> renderingBo, final Area2 renderingArea, final int numElements, final RenderingBoPool renderingBoPool) {
		this.lastNumElements = numElements;
		
		final var bo = renderingBoPool.acquire(this.name + "_Slider", ControlRenderingBo.class)
				.setControlId(this.name)
				.setType(SliderControl.class)
				.setCustomData(Boolean.FALSE)
				.setCustomData2(Double.valueOf(1.0D - this.scrollPosition))
				.setCustomData3(Double.valueOf(1.0D - this.scrollPosition))
				.setEnabled(numElements > 0)
				.setVisible(true)
				.withDimensionAbsolute(20, renderingArea.getDimension().getY())
				.withPositionAbsoluteAnchorTopLeft(renderingArea.getPosition().getX() + renderingArea.getDimension().getX() - 20, renderingArea.getPosition().getY())
				.setColor(RgbaColor.LIGHT_GRAY)
				.setLayer(this.layer);
		renderingBo.add(bo);
	}
	
	public double getScrollPosition() {
		return this.scrollPosition;
	}
	
	public void setScrollPosition(final double scrollPosition) {
		this.scrollPosition = scrollPosition;
	}
	
	public String getName() {
		return this.name;
	}
	
	public RenderingBoLayer getLayer() {
		return this.layer;
	}
	
	public void setLayer(final RenderingBoLayer layer) {
		this.layer = layer;
	}
	
}
