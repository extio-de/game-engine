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
 * This event contains a screenshot created by the renderer. Screenshot creation can be triggered via RendererControl
 */
public final class TakeScreenshotEvent {
	
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
