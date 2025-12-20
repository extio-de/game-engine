/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.model;

/**
 * A rectangle represented by top left (c0) and bottom right (c1) vertices
 */
public class RectI2 {
	
	private CoordI2 c0;
	
	private CoordI2 c1;
	
	public RectI2() {
		
	}
	
	public RectI2(final CoordI2 c0, final CoordI2 c1) {
		this.c0 = c0;
		this.c1 = c1;
	}
	
	public RectI2(final RectI2 other) {
		if (other == null) {
			return;
		}
		if (other.c0 != null) {
			if (other.c0 instanceof ImmutableCoordI2) {
				this.c0 = ImmutableCoordI2.create(other.c0);
			}
			else {
				this.c0 = MutableCoordI2.create(other.c0);
			}
		}
		if (other.c1 != null) {
			if (other.c1 instanceof ImmutableCoordI2) {
				this.c1 = ImmutableCoordI2.create(other.c1);
			}
			else {
				this.c1 = MutableCoordI2.create(other.c1);
			}
		}
	}
	
	public CoordI2 getC0() {
		return this.c0;
	}
	
	public void setC0(final CoordI2 c0) {
		this.c0 = c0;
	}
	
	public CoordI2 getC1() {
		return this.c1;
	}
	
	public void setC1(final CoordI2 c1) {
		this.c1 = c1;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.c0 == null) ? 0 : this.c0.hashCode());
		result = prime * result + ((this.c1 == null) ? 0 : this.c1.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		final RectI2 other = (RectI2) obj;
		if (this.c0 == null) {
			if (other.c0 != null) {
				return false;
			}
		}
		else if (!this.c0.equals(other.c0)) {
			return false;
		}
		if (this.c1 == null) {
			if (other.c1 != null) {
				return false;
			}
		}
		else if (!this.c1.equals(other.c1)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "RectI2 [c0=" + this.c0 + ", c1=" + this.c1 + "]";
	}
	
}
