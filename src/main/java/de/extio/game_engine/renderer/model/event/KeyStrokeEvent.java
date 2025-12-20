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
 * Message sent by game client engine on any key stroke. You may want to use the respective callback provided by ClientModule instead. 
 */
public final class KeyStrokeEvent {
	
	private boolean released;
	
	private int code;
	
	private String key;
	
	private int modifiers;
	
	public KeyStrokeEvent(final boolean released, final int code, final String key, final int modifiers) {
		this.released = released;
		this.code = code;
		this.key = key;
		this.modifiers = modifiers;
	}
	
	public boolean isReleased() {
		return this.released;
	}
	
	public void setReleased(final boolean released) {
		this.released = released;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(final String key) {
		this.key = key;
	}
	
	public int getModifiers() {
		return this.modifiers;
	}
	
	public void setModifiers(final int modifiers) {
		this.modifiers = modifiers;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public void setCode(final int code) {
		this.code = code;
	}
	
}
