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
 * Message sent by game client engine when mouse has been moved 
 */
public final class MouseMoveEvent extends MouseEvent {
	
	private boolean drag;
	
	private int button;
	
	public MouseMoveEvent(final boolean drag, final int modifiers, final CoordI2 coord, final int button) {
		super(coord, modifiers);
		this.drag = drag;
		this.button = button;
	}
	
	public boolean isDrag() {
		return this.drag;
	}
	
	public void setDrag(final boolean drag) {
		this.drag = drag;
	}
	
	public int getButton() {
		return this.button;
	}
	
	public void setButton(final int button) {
		this.button = button;
	}
	
}
