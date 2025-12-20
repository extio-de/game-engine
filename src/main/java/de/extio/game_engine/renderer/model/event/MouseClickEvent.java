/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.renderer.model.event;

import de.extio.game_engine.spatial2.model.CoordI2;

/**
 * Message sent by game client engine on any mouse click
 */
public final class MouseClickEvent extends MouseEvent {
	
	private boolean pressed;
	
	private int button;
	
	public MouseClickEvent(final boolean pressed, final int modifiers, final int button, final CoordI2 coord) {
		super(coord, modifiers);
		this.pressed = pressed;
		this.button = button;
	}
	
	public boolean isPressed() {
		return this.pressed;
	}
	
	public void setPressed(final boolean pressed) {
		this.pressed = pressed;
	}
	
	public int getButton() {
		return this.button;
	}
	
	public void setButton(final int button) {
		this.button = button;
	}
}
