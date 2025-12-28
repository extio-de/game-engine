package de.extio.game_engine.renderer.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.bo.VerticalAlignment;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.MouseEvent;
import de.extio.game_engine.renderer.model.event.MouseMoveEvent;
import de.extio.game_engine.renderer.model.event.ViewportResizeEvent;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

@Component
@Scope("prototype")
public class Container extends AbstractClientModule implements InitializingBean {
	
	protected final static List<Container> DISPLAYED_CONTAINERS = Collections.synchronizedList(new ArrayList<>());
	
	protected final ModuleService moduleService;
	
	protected final RenderingBoPool renderingBoPool;
	
	protected final RendererControl rendererControl;
	
	protected final RendererWorkingSet rendererWorkingSet;
	
	protected final EventService eventService;
	
	protected final Area2 area = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	// Normalized area of this container relative to reference resolution, the actual position is automatically aligned based on alignment settings (e.g. if the user uses ultra-wide monitors). Global scale factors are not considered/handled by the renderer control.
	protected final Area2 normalizedArea = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	protected boolean draggable;
	
	protected boolean dragging;
	
	protected CoordI2 dragPrevPosition;
	
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
	
	protected VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
	
	protected short zIndex;
	
	protected Container parent;
	
	protected Set<Container> children = new HashSet<>();
	
	public Container(final ModuleService moduleService, final RenderingBoPool renderingBoPool, final RendererControl rendererControl, final EventService eventService, final RendererWorkingSet rendererWorkingSet) {
		this.moduleService = moduleService;
		this.renderingBoPool = renderingBoPool;
		this.rendererControl = rendererControl;
		this.eventService = eventService;
		this.rendererWorkingSet = rendererWorkingSet;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.moduleService.loadModule(this);
	}
	
	@Override
	public void onActivate() {
		this.eventService.register(ViewportResizeEvent.class, this.id, this::onViewportResize);
		this.eventService.register(MouseMoveEvent.class, this.id, this::onMouseMoveEvent);
		this.eventService.register(MouseClickEvent.class, this.id, this::onMouseClickEvent);
		this.onViewportResize(null);
	}
	
	@Override
	public void onShow() {
		DISPLAYED_CONTAINERS.add(this);
		this.zIndexToTop();
		this.draw();
	}
	
	@Override
	public void onHide() {
		DISPLAYED_CONTAINERS.remove(this);
	}
	
	protected void onViewportResize(final ViewportResizeEvent event) {
		this.setNormalizedPosition(this.normalizedArea.getPosition());
		this.setNormalizedDimension(this.normalizedArea.getDimension());
		this.adjustIfOutsideViewport();
		this.draw();
	}
	
	private boolean adjustIfOutsideViewport() {
		final var viewportDimension = this.rendererControl.getEffectiveViewportDimension();
		final var position = this.area.getPosition();
		final var dimension = this.area.getDimension();
		
		final var containerArea = this.area.getDimension().getX() * this.area.getDimension().getY();
		final var intersectionArea = this.calculateIntersectionArea(position, dimension, viewportDimension);
		final var intersectionPercentage = containerArea > 0 ? (double) intersectionArea / containerArea : 0.0;
		
		if (intersectionPercentage >= 0.1) {
			return false;
		}
		
		int adjustedX = position.getX();
		int adjustedY = position.getY();
		
		if (position.getX() >= viewportDimension.getX()) {
			adjustedX = viewportDimension.getX() - dimension.getX();
		}
		else if (position.getX() + dimension.getX() <= 0) {
			adjustedX = 0;
		}
		else if (position.getX() < 0) {
			adjustedX = 0;
		}
		else if (position.getX() + dimension.getX() > viewportDimension.getX()) {
			adjustedX = viewportDimension.getX() - dimension.getX();
		}
		
		if (position.getY() >= viewportDimension.getY()) {
			adjustedY = viewportDimension.getY() - dimension.getY();
		}
		else if (position.getY() + dimension.getY() <= 0) {
			adjustedY = 0;
		}
		else if (position.getY() < 0) {
			adjustedY = 0;
		}
		else if (position.getY() + dimension.getY() > viewportDimension.getY()) {
			adjustedY = viewportDimension.getY() - dimension.getY();
		}
		
		this.normalizedArea.getPosition().setXY(adjustedX, adjustedY);
		this.area.getPosition().setXY(adjustedX, adjustedY);
		
		this.LOGGER.debug("Adjusted container {} position to {},{} to remain visible in viewport", this.id, adjustedX, adjustedY);
		return true;
	}
	
	private int calculateIntersectionArea(final CoordI2 position, final CoordI2 dimension, final CoordI2 viewportDimension) {
		final int x1 = Math.max(0, position.getX());
		final int y1 = Math.max(0, position.getY());
		final int x2 = Math.min(viewportDimension.getX(), position.getX() + dimension.getX());
		final int y2 = Math.min(viewportDimension.getY(), position.getY() + dimension.getY());
		
		final int width = Math.max(0, x2 - x1);
		final int height = Math.max(0, y2 - y1);
		
		return width * height;
	}
	
	protected void onMouseMoveEvent(final MouseMoveEvent event) {
		if (this.draggable && event.isDrag()) {
			final Container containerUnderCursor = getContainerUnderCursor(event);
			if (containerUnderCursor == this) {
				if (!this.dragging) {
					this.dragging = true;
					this.dragPrevPosition = null;
					this.zIndexToTop();
					this.LOGGER.debug("Started dragging container {}", this.id);
				}
				if (this.dragPrevPosition != null) {
					final var delta = event.getScaledCoord().substract(this.dragPrevPosition);
					if (!ImmutableCoordI2.zero().equals(delta)) {
						this.setNormalizedPosition(this.getNormalizedPosition().add(delta));
					}
				}
				this.dragPrevPosition = event.getScaledCoord();
			}
		}
	}
	
	protected void onMouseClickEvent(final MouseClickEvent event) {
		final Container containerUnderCursor = this.getContainerUnderCursor(event);
		if (containerUnderCursor == this) {
			if (event.isPressed() && !this.dragging) {
				this.zIndexToTop();
			}
		}
		
		if (!event.isPressed() && this.dragging) {
			this.dragging = false;
			this.dragPrevPosition = null;
			if (this.adjustIfOutsideViewport()) {
				this.draw();
			}
			this.LOGGER.debug("Stopped dragging container {}", this.id);
		}
	}
	
	/**
	 * Override in subclasses, but make sure to call super.draw() at the end if overridden
	 */
	public void draw() {
		final var uncommitted = this.rendererWorkingSet.getUncommittedWork(this.id);
		for (final RenderingBo bo : uncommitted.values()) {
			bo
					.setZIndex(this.zIndex)
					.withPositionAbsoluteAnchorTopLeft(this.area.getPosition()
							.toMutableCoordD2()
							.add(bo.getLocalX(), bo.getLocalY())
							.toImmutableCoordI2());
			this.rendererWorkingSet.put(this.id, bo);
		}
		
		this.rendererWorkingSet.commit(this.id, true);
	}
	
	public void putRenderingBo(final RenderingBo renderingBo) {
		this.rendererWorkingSet.put(this.id, renderingBo);
	}
	
	public void removeRenderingBo(final String renderingBoId) {
		this.rendererWorkingSet.remove(this.id, renderingBoId);
	}
	
	public void clearRenderingBos() {
		this.rendererWorkingSet.clearNext(this.id);
	}
	
	public void setNormalizedPosition(final CoordI2 normalizedPosition) {
		this.normalizedArea.getPosition().setXY(normalizedPosition);
		switch (this.horizontalAlignment) {
			case LEFT -> this.area.getPosition().setX(normalizedPosition.getX());
			case CENTER -> this.area.getPosition().setX(normalizedPosition.getX() + (this.rendererControl.getEffectiveViewportDimension().getX() - RendererControl.REFERENCE_RESOLUTION.getX()) / 2);
			case RIGHT -> this.area.getPosition().setX(normalizedPosition.getX() + this.rendererControl.getEffectiveViewportDimension().getX() - RendererControl.REFERENCE_RESOLUTION.getX());
		}
		switch (this.verticalAlignment) {
			case TOP -> this.area.getPosition().setY(normalizedPosition.getY());
			case CENTER -> this.area.getPosition().setY(normalizedPosition.getY() + (this.rendererControl.getEffectiveViewportDimension().getY() - RendererControl.REFERENCE_RESOLUTION.getY()) / 2);
			case BOTTOM -> this.area.getPosition().setY(normalizedPosition.getY() + this.rendererControl.getEffectiveViewportDimension().getY() - RendererControl.REFERENCE_RESOLUTION.getY());
		}
		this.draw();
	}
	
	/**
	 * Normalized position of this container relative to reference resolution, the actual position is automatically aligned based on alignment settings (e.g. if the user uses ultra-wide monitors). Global scale factors are not considered/handled by the renderer control.
	 */
	public CoordI2 getNormalizedPosition() {
		return this.normalizedArea.getPosition().toImmutableCoordI2();
	}
	
	public void setNormalizedDimension(final CoordI2 normalizedDimension) {
		this.normalizedArea.getDimension().setXY(normalizedDimension);
		this.area.getDimension().setXY(normalizedDimension);
		this.draw();
	}
	
	private Container getContainerUnderCursor(final MouseEvent event) {
		Container containerUnderCursor = null;
		synchronized (DISPLAYED_CONTAINERS) {
			short zIndexHighest = -1;
			for (final var container : DISPLAYED_CONTAINERS) {
				if (container.dragging) {
					containerUnderCursor = container;
					break;
				}
				if (SpatialUtils2.intersects(container.area.getPosition(), container.area.getDimension(), event.getScaledCoord(), ImmutableCoordI2.one())) {
					if (container.zIndex > zIndexHighest) {
						zIndexHighest = container.zIndex;
						containerUnderCursor = container;
					}
				}
			}
		}
		return containerUnderCursor;
	}
	
	private void zIndexToTop() {
		if (!this.draggable) {
			return;
		}
		
		synchronized (DISPLAYED_CONTAINERS) {
			final List<Container> path = this.getPathToRoot(this);
			final Container root = path.get(0);
			final Set<Container> pathSet = new HashSet<>(path);
			
			final Set<Container> displayed = new HashSet<>(DISPLAYED_CONTAINERS);
			
			final List<Container> others = new ArrayList<>();
			for (final Container c : DISPLAYED_CONTAINERS) {
				if (!isDescendantOrSelf(root, c)) {
					others.add(c);
				}
			}
			others.sort((c1, c2) -> Short.compare(c1.getzIndex(), c2.getzIndex()));
			
			final Map<Container, Short> subtreeMinDisplayedZIndex = new IdentityHashMap<>();
			final Map<Container, Boolean> subtreeHasDisplayed = new IdentityHashMap<>();
			this.computeSubtreeInfo(root, displayed, subtreeMinDisplayedZIndex, subtreeHasDisplayed, new HashSet<>());
			
			final List<Container> hierarchyDisplayed = new ArrayList<>();
			if (Boolean.TRUE.equals(subtreeHasDisplayed.get(root))) {
				this.flattenDisplayedSubtree(root, displayed, pathSet, subtreeMinDisplayedZIndex, subtreeHasDisplayed, hierarchyDisplayed, new HashSet<>());
			}
			
			short z = 0;
			for (final Container c : others) {
				if (c.getzIndex() != z) {
					c.setzIndex(z);
					c.draw();
				}
				z++;
			}
			for (final Container c : hierarchyDisplayed) {
				if (c.getzIndex() != z) {
					c.setzIndex(z);
					c.draw();
				}
				z++;
			}
		}
	}
	
	private List<Container> getPathToRoot(final Container container) {
		final List<Container> path = new ArrayList<>();
		Container current = container;
		while (current != null) {
			path.add(0, current);
			current = current.parent;
		}
		return path;
	}
	
	private boolean isDescendantOrSelf(final Container root, final Container c) {
		Container curr = c;
		while (curr != null) {
			if (curr == root) {
				return true;
			}
			curr = curr.parent;
		}
		return false;
	}
	
	private void computeSubtreeInfo(final Container node, final Set<Container> displayedNodes, final Map<Container, Short> subtreeMinDisplayedZIndex, final Map<Container, Boolean> subtreeHasDisplayed, final Set<Container> visited) {
		if (!visited.add(node)) {
			return;
		}
		
		short minZ = displayedNodes.contains(node) ? node.getzIndex() : Short.MAX_VALUE;
		boolean hasDisplayed = displayedNodes.contains(node);
		
		for (final Container child : node.children) {
			this.computeSubtreeInfo(child, displayedNodes, subtreeMinDisplayedZIndex, subtreeHasDisplayed, visited);
			if (Boolean.TRUE.equals(subtreeHasDisplayed.get(child))) {
				hasDisplayed = true;
				final Short childMin = subtreeMinDisplayedZIndex.get(child);
				if (childMin != null && childMin < minZ) {
					minZ = childMin;
				}
			}
		}
		
		subtreeHasDisplayed.put(node, hasDisplayed);
		if (hasDisplayed) {
			subtreeMinDisplayedZIndex.put(node, minZ);
		}
	}
	
	private void flattenDisplayedSubtree(final Container node, final Set<Container> displayedNodes, final Set<Container> pathSet, final Map<Container, Short> subtreeMinDisplayedZIndex, final Map<Container, Boolean> subtreeHasDisplayed, final List<Container> result, final Set<Container> visited) {
		if (!visited.add(node)) {
			return;
		}
		if (!Boolean.TRUE.equals(subtreeHasDisplayed.get(node))) {
			return;
		}
		
		if (displayedNodes.contains(node)) {
			result.add(node);
		}
		
		final List<Container> childrenSorted = new ArrayList<>(node.children);
		childrenSorted.sort((c1, c2) -> {
			final boolean c1OnPath = pathSet.contains(c1);
			final boolean c2OnPath = pathSet.contains(c2);
			if (c1OnPath != c2OnPath) {
				return c1OnPath ? 1 : -1;
			}
			final short c1Key = subtreeMinDisplayedZIndex.getOrDefault(c1, Short.MAX_VALUE);
			final short c2Key = subtreeMinDisplayedZIndex.getOrDefault(c2, Short.MAX_VALUE);
			final int cmp = Short.compare(c1Key, c2Key);
			if (cmp != 0) {
				return cmp;
			}
			return Integer.compare(System.identityHashCode(c1), System.identityHashCode(c2));
		});
		
		for (final Container child : childrenSorted) {
			if (Boolean.TRUE.equals(subtreeHasDisplayed.get(child))) {
				this.flattenDisplayedSubtree(child, displayedNodes, pathSet, subtreeMinDisplayedZIndex, subtreeHasDisplayed, result, visited);
			}
		}
	}
	
	public void setParent(final Container parent) {
		synchronized (DISPLAYED_CONTAINERS) {
			if (this.parent != null) {
				this.parent.children.remove(this);
			}
			this.parent = parent;
			if (parent != null) {
				parent.children.add(this);
			}
		}
	}
	
	/**
	 * Normalized position of this container relative to reference resolution. Global scale factors are not considered/handled by the renderer control.
	 */
	public CoordI2 getNormalizedDimension() {
		return this.normalizedArea.getDimension().toImmutableCoordI2();
	}
	
	public boolean isDraggable() {
		return draggable;
	}
	
	public void setDraggable(final boolean draggable) {
		this.draggable = draggable;
	}
	
	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}
	
	public void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}
	
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}
	
	public void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}
	
	public short getzIndex() {
		return zIndex;
	}
	
	public void setzIndex(final short zIndex) {
		this.zIndex = zIndex;
	}
	
}
