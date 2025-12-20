package de.extio.game_engine.renderer.model.event;

/**
 * This event is fired when a user interacted with a control in the UI, for example clicked on a button, entered text, ...
 */
public final class UiControlEvent {
	
	private final String id;
	
	private final Object payload;
	
	public UiControlEvent(final String id, final Object payload) {
		this.id = id;
		this.payload = payload;
	}
	
	/**
	 * Returns the control ID
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Payload depends on the type of action and the control
	 */
	public Object getPayload() {
		return this.payload;
	}
	
}
