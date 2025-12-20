package de.extio.game_engine.renderer.container;

import java.util.function.Function;

import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.RenderingBoPool;
import de.extio.game_engine.spatial2.model.Area2;

/**
 * Helper to position, draw and manage virtual windows in the UI.
 * EnhancedWindow holds a state to switch between different representations and also can persist the window position.
 */
public class EnhancedWindow<T> extends Window {
	
	public final static String PERSISTENT_STATE_PREFIX = "ENHANCEDWINDOW_POSITION_";
	
	private final boolean persistentPosition;
	
	private T state;
	
	private final T initialState;
	
	private final Function<T, Area2> areaFunction;
	
	public static void resetWindows() {
		// boolean removed = false;
		// final Iterator<String> it = EngineFacade.instance().getPersistentClientState().state.keySet().iterator();
		// while (it.hasNext()) {
		// 	final String key = it.next();
		// 	if (key.startsWith(EnhancedWindow.PERSISTENT_STATE_PREFIX)) {
		// 		it.remove();
		// 		removed = true;
		// 	}
		// }
		// if (removed) {
		// 	EngineFacade.instance().storePersistentClientState();
		// }
		
		synchronized (WINDOWS) {
			for (final Window window : WINDOWS) {
				if (window instanceof EnhancedWindow) {
					window.reset();
				}
			}
		}
	}
	
	public EnhancedWindow(final String name, final boolean draggable, final boolean persistentPosition, final T initialState, final RenderingBoPool renderingBoPool, final RendererControl rendererControl, final Function<T, Area2> areaFunction) {
		super(name, draggable, renderingBoPool, rendererControl);
		
		this.initialState = initialState;
		this.areaFunction = areaFunction;
		this.persistentPosition = persistentPosition;
		
		this.reset();
	}
	
	public String getName() {
		return this.name;
	}
	
	public T getState() {
		return this.state;
	}
	
	public void setState(final T state) {
		if (state == null) {
			this.reset();
			return;
		}
		else {
			this.state = state;
		}
		if (this.areaFunction != null) {
			this.updateArea();
			this.savePosition();
		}
	}
	
	@Override
	public void updateArea() {
		if (this.areaFunction != null) {
			final Area2 area = this.areaFunction.apply(this.state);
			this.setDimension(area.getDimension());
			//			this.resizeCentered(area.getDimension());
			if (!this.persistentPosition) {
				this.setPosition(area.getPosition());
			}
			this.updateRelativeScreenPosition();
		}
		
		super.updateArea();
	}
	
	@Override
	public void reset() {
		super.reset();
		this.state = this.initialState;
		this.updateArea();
		this.loadPosition();
	}
	
	@Override
	public void releaseDragging() {
		if (this.isDragging()) {
			this.savePosition();
		}
		super.releaseDragging();
	}
	
	private void loadPosition() {
		if (this.persistentPosition && this.name != null) {
			// final CoordD2 existingPosition = (CoordD2) EngineFacade.instance().getPersistentClientState().state.get(this.getPersistentStateKeyName());
			// if (existingPosition != null) {
			// 	LOGGER.debug(() -> "Position loaded: " + this.name + " " + existingPosition);
			// 	this.relativeScreenPosition = MutableCoordD2.create(existingPosition);
			// 	this.applyRelativeScreenPosition();
			// }
			/*else*/ if (this.areaFunction != null) {
				final Area2 area = this.areaFunction.apply(this.state);
				this.setPosition(area.getPosition());
			}
		}
	}
	
	private void savePosition() {
		if (!this.persistentPosition) {
			return;
		}
		
		// final CoordD2 existingPosition = (CoordD2) EngineFacade.instance().getPersistentClientState().state.get(this.getPersistentStateKeyName());
		// if (existingPosition == null || !this.relativeScreenPosition.equals(existingPosition)) {
		// 	EngineFacade.instance().getPersistentClientState().state.put(this.getPersistentStateKeyName(), MutableCoordD2.create(this.relativeScreenPosition));
		// 	EngineFacade.instance().storePersistentClientState();
		// 	LOGGER.debug(() -> "Position stored: " + this.name + " " + this.getPosition());
		// }
	}
	
	// private String getPersistentStateKeyName() {
	// 	return PERSISTENT_STATE_PREFIX.concat(this.name);
	// }
}
