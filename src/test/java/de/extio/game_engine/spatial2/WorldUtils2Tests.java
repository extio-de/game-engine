/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordD2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.Edge2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordD2;

public class WorldUtils2Tests {
	
	@Test
	public void testVectorAngle() {
		CoordD2 v0 = MutableCoordD2.create(0.0, 0.0);
		CoordD2 v1 = MutableCoordD2.create(0.0, 0.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(0.0, 0.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(0.0, 0.0);
		v1 = MutableCoordD2.create(-1.0, 0.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(0.5, 0.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(-1.0, 0.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(-1.0, 1.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(-0.5, 1.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(-1.0, 5.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(1.0, 0.0);
		v1 = MutableCoordD2.create(0.0, -1.0);
		this.calcAngle(v0, v1);
		
		v0 = MutableCoordD2.create(-0.31617965644035745, 0.21078643762690494);
		v1 = MutableCoordD2.create(-0.29595508588991065, 0.19730339059327376);
		this.calcAngle(v0, v1);
	}
	
	@Test
	public void testVectorAngle2() {
		for (double i = -1.0; i < 1.0; i += 0.2) {
			for (double ii = -1.0; ii < 1.0; ii += 0.2) {
				final CoordD2 v0 = MutableCoordD2.create(i, ii);
				final CoordD2 v1 = MutableCoordD2.create(ii, i);
				this.calcAngle(v0, v1);
			}
		}
	}
	
	@Test
	public void testWorldUtils2PointsToArea() {
		final CoordI2 point0 = ImmutableCoordI2.create(6, 8);
		final CoordI2 point1 = ImmutableCoordI2.create(6, 9);
		assertEquals(ImmutableCoordI2.create(6, 8), SpatialUtils2.pointsToArea(point0, point1).getPosition());
		assertEquals(ImmutableCoordI2.create(1, 2), SpatialUtils2.pointsToArea(point0, point1).getDimension());
		assertEquals(ImmutableCoordI2.create(6, 8), SpatialUtils2.pointsToArea(point1, point0).getPosition());
		assertEquals(ImmutableCoordI2.create(1, 2), SpatialUtils2.pointsToArea(point1, point0).getDimension());
	}
	
	@Test
	public void testPointInCircle() {
		for (double n = -Math.PI; n < Math.PI; n += 0.1) {
			System.out.println(n + " " + Math.cos(n) + " " + Math.sin(n));
		}
	}
	
	@Test
	public void testSector() {
		final Area2 area = new Area2(ImmutableCoordI2.create(100, 100), ImmutableCoordI2.create(20, 20));
		
		assertEquals(Edge2.RIGHT, SpatialUtils2.getSector(ImmutableCoordI2.create(120, 110), area));
		assertEquals(Edge2.LEFT, SpatialUtils2.getSector(ImmutableCoordI2.create(105, 110), area));
		assertEquals(Edge2.TOP, SpatialUtils2.getSector(ImmutableCoordI2.create(110, 100), area));
		assertEquals(Edge2.BOTTOM, SpatialUtils2.getSector(ImmutableCoordI2.create(110, 120), area));
	}
	
	@Test
	public void testPedalNaN() {
		final CoordD2 result = SpatialUtils2.getPedalOnLineSegmentAB(ImmutableCoordI2.create(-802, -905), ImmutableCoordI2.create(-805, -887), ImmutableCoordI2.create(-804, -893));
		assertNull(result);
	}
	
	private void calcAngle(final CoordD2 v0, final CoordD2 v1) {
		System.out.println(SpatialUtils2.getVectorAngle(v0, v1) + " " + v0 + " " + v1);
	}
	
}
