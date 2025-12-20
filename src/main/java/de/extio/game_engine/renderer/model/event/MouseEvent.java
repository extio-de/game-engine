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

public abstract class MouseEvent {
	
	private CoordI2 coord;
	
	private int modifiers;
	
	public MouseEvent(final CoordI2 coord, final int modifiers) {
		this.coord = coord;
		this.modifiers = modifiers;
	}
	
	public CoordI2 getCoord() {
		return this.coord;
	}
	
	public void setCoord(final CoordI2 coord) {
		this.coord = coord;
	}
	
	public int getModifiers() {
		return this.modifiers;
	}
	
	public void setModifiers(final int modifiers) {
		this.modifiers = modifiers;
	}
	
}
