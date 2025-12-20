/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
