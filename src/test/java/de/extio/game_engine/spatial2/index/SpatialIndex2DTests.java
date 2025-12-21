/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;
import de.extio.game_engine.spatial2.model.SpatialIndex2Capable;
import de.extio.game_engine.util.rng.XorShift128Random;

public class SpatialIndex2DTests {
	
	@Disabled
	@Test
	public void testQuadTreePerformance() {
		System.out.println("---");
		System.out.println("testQuadTreePerformance");
		
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		this.doTestPerformance(index);
	}
	
	@Disabled
	@Test
	public void testLinearPerformance() {
		System.out.println("---");
		System.out.println("testLinearPerformance");
		
		final SpatialIndex2D<TestEntity> index = new LinearSearchIndex2D<>();
		this.doTestPerformance(index);
	}
	
	private void doTestPerformance(final SpatialIndex2D<TestEntity> index) {
		final int SIZE = 100000;
		
		final Random random = new XorShift128Random(42);
		
		System.gc();
		try {
			Thread.sleep(5000l);
		}
		catch (final InterruptedException e) {
			e.printStackTrace();
		}
		
		// Add
		
		final List<TestEntity> testEntities = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; i++) {
			testEntities.add(new TestEntity(MutableCoordI2.create(random.nextInt(10000), random.nextInt(10000)), ImmutableCoordI2.create(random.nextInt(29) + 1, random.nextInt(29) + 1)));
		}
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			index.add(testEntities.get(i));
		}
		System.out.println("Add " + SIZE + " entities: " + (System.currentTimeMillis() - start));
		
		// Query
		
		final List<Area2> queries = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; i++) {
			queries.add(new Area2(ImmutableCoordI2.create(random.nextInt(10000), random.nextInt(10000)), ImmutableCoordI2.create(random.nextInt(499) + 1, random.nextInt(499) + 1)));
		}
		
		start = System.currentTimeMillis();
		long total = 0;
		for (int i = 0; i < SIZE; i++) {
			final Area2 area = queries.get(i);
			total += index.find(area).size();
		}
		System.out.println("Queried " + total + " entities: " + (System.currentTimeMillis() - start));
		
		// Move
		
		start = System.currentTimeMillis();
		final CoordI2 oldPosition = MutableCoordI2.create();
		for (int i = 0; i < SIZE; i++) {
			final TestEntity entity = testEntities.get(i);
			oldPosition.setXY(entity.getPosition());
			entity.getPosition().add(1);
			
			index.move(testEntities.get(i), oldPosition, entity.getDimension());
		}
		System.out.println("Moved all entities: " + (System.currentTimeMillis() - start));
		
		// Query
		
		start = System.currentTimeMillis();
		total = 0;
		for (int i = 0; i < SIZE; i++) {
			final Area2 area = queries.get(i);
			total += index.find(area).size();
		}
		System.out.println("Queried " + total + " entities: " + (System.currentTimeMillis() - start));
		
		// Remove
		
		start = System.currentTimeMillis();
		final int cntDelete = SIZE / 2;
		for (int i = 0; i < cntDelete; i++) {
			index.remove(testEntities.get(i));
		}
		System.out.println("Removed " + cntDelete + " entities: " + (System.currentTimeMillis() - start));
		
		// Query
		
		start = System.currentTimeMillis();
		total = 0;
		for (int i = 0; i < SIZE; i++) {
			final Area2 area = queries.get(i);
			total += index.find(area).size();
		}
		System.out.println("Queried " + total + " entities: " + (System.currentTimeMillis() - start));
		
		// Clear
		
		start = System.currentTimeMillis();
		index.clear();
		System.out.println("Clear all entities: " + (System.currentTimeMillis() - start));
		
		// Add
		
		start = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			index.add(testEntities.get(i));
		}
		System.out.println("Add " + SIZE + " entities: " + (System.currentTimeMillis() - start));
		
		// Query
		
		start = System.currentTimeMillis();
		total = 0;
		for (int i = 0; i < SIZE; i++) {
			final Area2 area = queries.get(i);
			total += index.find(area).size();
		}
		System.out.println("Queried " + total + " entities: " + (System.currentTimeMillis() - start));
		
		System.gc();
		try {
			Thread.sleep(5000l);
		}
		catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class TestEntity implements SpatialIndex2Capable {
		
		CoordI2 position;
		
		CoordI2 dimension;
		
		transient long bsi2Session;
		
		public TestEntity(final CoordI2 position, final CoordI2 dimension) {
			this.position = position;
			this.dimension = dimension;
		}
		
		@Override
		public CoordI2 getPosition() {
			return this.position;
		}
		
		@Override
		public CoordI2 getDimension() {
			return this.dimension;
		}
		
		@Override
		public boolean isInSpatialIndex2ResultSet(final long session) {
			return this.bsi2Session == session;
		}
		
		@Override
		public void includeInSpatialIndex2ResultSet(final long session) {
			this.bsi2Session = session;
		}
		
	}
	
}
