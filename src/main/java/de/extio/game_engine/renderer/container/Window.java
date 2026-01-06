package de.extio.game_engine.renderer.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.module.ModuleService;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowCloseButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowPanelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowPanelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.WindowCloseButtonData;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.bo.VerticalAlignment;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.MouseEvent;
import de.extio.game_engine.renderer.model.event.MouseMoveEvent;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.model.event.ViewportResizeEvent;
import de.extio.game_engine.renderer.work.RendererWorkingSet;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

public class Window extends AbstractClientModule implements InitializingBean {
	
	public static final int MARGIN_TOP = 30;
	public static final int MARGIN_LEFT = 20;
	public static final int MARGIN_RIGHT = 20;
	public static final int MARGIN_BOTTOM = 20;
	
	protected final static List<Window> DISPLAYED_CONTAINERS = Collections.synchronizedList(new ArrayList<>());
	
	protected final ModuleService moduleService;
	
	protected final RenderingBoPool renderingBoPool;
	
	protected final RendererControl rendererControl;
	
	protected final RendererWorkingSet rendererWorkingSet;
	
	protected final EventService eventService;
	
	protected final Area2 area = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	// Normalized area of this container relative to reference resolution, the actual position is automatically aligned based on alignment settings (e.g. if the user uses ultra-wide monitors). Global scale factors are not considered/handled by the renderer control.
	protected final Area2 normalizedArea = new Area2(MutableCoordI2.create(), MutableCoordI2.create());
	
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
	
	protected VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
	
	protected short zIndex;
	
	protected Window parent;
	
	protected Set<Window> children = new HashSet<>();
	
	protected Set<WindowComponent> components = new HashSet<>();
	
	protected boolean draggable;
	
	protected boolean dragging;
	
	protected CoordI2 dragPrevPosition;
	
	protected boolean closeButton;
	
	protected String closeButtonName = "";
	
	protected Runnable onCloseAction;
	
	public Window(final ModuleService moduleService, final RenderingBoPool renderingBoPool, final RendererControl rendererControl, final EventService eventService, final RendererWorkingSet rendererWorkingSet) {
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
		if (this.closeButton) {
			this.eventService.register(UiControlEvent.class, this.getId(), this::onCloseButtonControlEvent);
		}
		this.onViewportResize(null);
	}
	
	@Override
	public void onShow() {
		DISPLAYED_CONTAINERS.add(this);
		this.zIndexToTop();
		this.components.forEach(c -> c.onAddedToWindow());
		this.draw();
	}
	
	@Override
	public void onHide() {
		DISPLAYED_CONTAINERS.remove(this);
		this.components.forEach(WindowComponent::onRemovedFromWindow);
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
			final Window containerUnderCursor = getContainerUnderCursor(event);
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
		final Window containerUnderCursor = this.getContainerUnderCursor(event);
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
	
	public void draw() {
		if (this.area.getDimension().getX() <= 0 || this.area.getDimension().getY() <= 0) {
			return;
		}
		if (this.area.getPosition().getX() + this.area.getDimension().getX() < 0 || this.area.getPosition().getY() + this.area.getDimension().getY() < 0) {
			return;
		}
		if (this.area.getPosition().getX() > this.rendererControl.getAbsoluteViewportDimension().getX() || this.area.getPosition().getY() > this.rendererControl.getAbsoluteViewportDimension().getY()) {
			return;
		}
		
		final var windowPanel = this.renderingBoPool.acquire(this.id + "_WindowPanel", ControlRenderingBo.class)
				.setType(WindowPanelControl.class)
				.setControlData(new WindowPanelData(true, null))
				.setEnabled(true)
				.setVisible(true)
				.withDimensionAbsolute(this.area.getDimension())
				.withPositionAbsoluteAnchorTopLeft(this.area.getPosition())
				.setZIndex(this.zIndex)
				.setLayer(RenderingBoLayer.UI_BGR);
		this.rendererWorkingSet.put(this.id, windowPanel);
		
		if (this.closeButton) {
			this.closeButtonName = this.id + "_CloseButton";
			final var closeButtonBo = this.renderingBoPool.acquire(this.closeButtonName, ControlRenderingBo.class)
					.setType(WindowCloseButtonControl.class)
					.setControlData(new WindowCloseButtonData(true))
					.setEnabled(true)
					.setVisible(true)
					.withDimensionAbsolute(this.area.getDimension())
					.withPositionAbsoluteAnchorTopLeft(this.area.getPosition())
					.setZIndex(this.zIndex)
					.setLayer(RenderingBoLayer.UI_TOP);
			this.rendererWorkingSet.put(this.id, closeButtonBo);
		}
		
		for (final var component : this.components) {
			component.draw();
		}
		
		final var uncommitted = this.rendererWorkingSet.getUncommittedWork(this.id);
		for (final RenderingBo bo : uncommitted.values()) {
			bo.setZIndex(this.zIndex);
			
			CoordI2 componentOffset = ImmutableCoordI2.zero();
			CoordI2 componentExtraOffset = ImmutableCoordI2.zero();
			for (final var component : this.components) {
				if (component.getRenderingBoIds().contains(bo.getId())) {
					componentOffset = component.getRelativePosition();
					componentExtraOffset = component.getRenderingBoExtraOffset(bo);
					break;
				}
			}
			bo.withPositionAbsoluteAnchorTopLeft(
					this.area.getPosition()
							.toMutableCoordI2()
							.add(bo.getLocalX(), bo.getLocalY())
							.add(componentOffset)
							.add(componentExtraOffset));
			
			this.rendererWorkingSet.put(this.id, bo);
		}
		
		this.rendererWorkingSet.commit(this.id, true);
	}
	
	/**
	 * Returns an unmodifiable DEEP copy of rendering BOs belonging to this container.
	 * If a RenderingBo is modified after this call, the changes will NOT be reflected in the active working set. You must call this.{@link #putRenderingBo(RenderingBo)} to update the working set.
	 */
	public Map<String, RenderingBo> getRenderingBos() {
		return this.rendererWorkingSet.getUncommittedWork(this.id);
	}

	public <T extends RenderingBo> T getRenderingBo(final String renderingBoId, final Class<T> type) {
		return this.rendererWorkingSet.get(this.id, renderingBoId, type);
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
	
	/**
	 * Normalized position of this container relative to reference resolution. Global scale factors are not considered/handled by the renderer control.
	 */
	public CoordI2 getNormalizedDimension() {
		return this.normalizedArea.getDimension().toImmutableCoordI2();
	}
	
	private Window getContainerUnderCursor(final MouseEvent event) {
		Window containerUnderCursor = null;
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
			final List<Window> path = this.getPathToRoot(this);
			final Window root = path.get(0);
			final Set<Window> pathSet = new HashSet<>(path);
			
			final Set<Window> displayed = new HashSet<>(DISPLAYED_CONTAINERS);
			
			final List<Window> others = new ArrayList<>();
			for (final Window c : DISPLAYED_CONTAINERS) {
				if (!isDescendantOrSelf(root, c)) {
					others.add(c);
				}
			}
			others.sort((c1, c2) -> Short.compare(c1.getzIndex(), c2.getzIndex()));
			
			final Map<Window, Short> subtreeMinDisplayedZIndex = new IdentityHashMap<>();
			final Map<Window, Boolean> subtreeHasDisplayed = new IdentityHashMap<>();
			this.computeSubtreeInfo(root, displayed, subtreeMinDisplayedZIndex, subtreeHasDisplayed, new HashSet<>());
			
			final List<Window> hierarchyDisplayed = new ArrayList<>();
			if (Boolean.TRUE.equals(subtreeHasDisplayed.get(root))) {
				this.flattenDisplayedSubtree(root, displayed, pathSet, subtreeMinDisplayedZIndex, subtreeHasDisplayed, hierarchyDisplayed, new HashSet<>());
			}
			
			short z = 0;
			for (final Window c : others) {
				if (c.getzIndex() != z) {
					c.setzIndex(z);
					c.draw();
				}
				z++;
			}
			for (final Window c : hierarchyDisplayed) {
				if (c.getzIndex() != z) {
					c.setzIndex(z);
					c.draw();
				}
				z++;
			}
		}
	}
	
	private List<Window> getPathToRoot(final Window container) {
		final List<Window> path = new ArrayList<>();
		Window current = container;
		while (current != null) {
			path.add(0, current);
			current = current.parent;
		}
		return path;
	}
	
	private boolean isDescendantOrSelf(final Window root, final Window c) {
		Window curr = c;
		while (curr != null) {
			if (curr == root) {
				return true;
			}
			curr = curr.parent;
		}
		return false;
	}
	
	private void computeSubtreeInfo(final Window node, final Set<Window> displayedNodes, final Map<Window, Short> subtreeMinDisplayedZIndex, final Map<Window, Boolean> subtreeHasDisplayed, final Set<Window> visited) {
		if (!visited.add(node)) {
			return;
		}
		
		short minZ = displayedNodes.contains(node) ? node.getzIndex() : Short.MAX_VALUE;
		boolean hasDisplayed = displayedNodes.contains(node);
		
		for (final Window child : node.children) {
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
	
	private void flattenDisplayedSubtree(final Window node, final Set<Window> displayedNodes, final Set<Window> pathSet, final Map<Window, Short> subtreeMinDisplayedZIndex, final Map<Window, Boolean> subtreeHasDisplayed, final List<Window> result, final Set<Window> visited) {
		if (!visited.add(node)) {
			return;
		}
		if (!Boolean.TRUE.equals(subtreeHasDisplayed.get(node))) {
			return;
		}
		
		if (displayedNodes.contains(node)) {
			result.add(node);
		}
		
		final List<Window> childrenSorted = new ArrayList<>(node.children);
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
		
		for (final Window child : childrenSorted) {
			if (Boolean.TRUE.equals(subtreeHasDisplayed.get(child))) {
				this.flattenDisplayedSubtree(child, displayedNodes, pathSet, subtreeMinDisplayedZIndex, subtreeHasDisplayed, result, visited);
			}
		}
	}
	
	public void setParent(final Window parent) {
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
	
	public void addComponent(final WindowComponent component) {
		this.components.add(Objects.requireNonNull(component));
		component.setParent(this);
		if (this.isDisplayed()) {
			component.onAddedToWindow();
		}
	}
	
	public void removeComponent(final WindowComponent component) {
		if (this.components.remove(component) && this.isDisplayed()) {
			component.onRemovedFromWindow();
		}
	}
	
	protected void onCloseButtonControlEvent(final UiControlEvent event) {
		if (this.closeButton && this.closeButtonName.equals(event.getId())) {
			if (this.onCloseAction != null) {
				this.onCloseAction.run();
			}
			else {
				this.getModuleService().changeActiveState(this.getId(), false);
			}
		}
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
	
	public boolean hasCloseButton() {
		return closeButton;
	}
	
	/**
	 * Sets whether the window has a close button. Must be set before activation.
	 */
	public void setCloseButton(final boolean closeButton) {
		this.closeButton = closeButton;
	}
	
	public Runnable getOnCloseAction() {
		return onCloseAction;
	}
	
	/**
	 * Sets the action to be performed when the close button is pressed.
	 * If not set, the window module will simply be deactivated on close button press.
	 * 
	 * @param onCloseAction The action to be performed on close button press.
	 */
	public void setOnCloseAction(final Runnable onCloseAction) {
		this.onCloseAction = onCloseAction;
	}
	
	public CoordI2 getAbsolutePosition() {
		return this.area.getPosition().toImmutableCoordI2();
	}
	
}
