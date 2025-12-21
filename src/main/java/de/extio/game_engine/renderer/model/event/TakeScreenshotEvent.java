package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.event.Event;

/**
 * This event contains a screenshot created by the renderer. Screenshot creation can be triggered via RendererControl
 */
public final class TakeScreenshotEvent implements Event {
	
	private final String id;
	
	private final byte[] payload;
	
	public TakeScreenshotEvent(final String id, final byte[] payload) {
		this.id = id;
		this.payload = payload;
	}
	
	public String getId() {
		return this.id;
	}
	
	public byte[] getPayload() {
		return this.payload;
	}
}
