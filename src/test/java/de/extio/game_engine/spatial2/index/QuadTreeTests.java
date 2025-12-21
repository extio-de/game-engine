/*
 * Copyright (C) 2023 Stephan Birkl - All Rights Reserved. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.SpatialIndex2Capable;
import de.extio.game_engine.util.rng.XorShift128Random;

@SuppressWarnings("resource")
public class QuadTreeTests {
	
	@Test
	public void testQuadTreeDim1Inserts() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		for (int x = 0; x < 100; x++) {
			for (int y = 0; y < 100; y++) {
				index.add(new TestEntity(ImmutableCoordI2.create(x, y), ImmutableCoordI2.one()));
			}
		}
		
		for (int x = 0; x < 100; x++) {
			for (int y = 0; y < 100; y++) {
				final TestEntity entity = index.find(ImmutableCoordI2.create(x, y), ImmutableCoordI2.one()).get(0);
				assertEquals(x, entity.getPosition().getX());
				assertEquals(y, entity.getPosition().getY());
			}
		}
	}
	
	@Test
	public void testQuadTreeNDimInserts() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		for (int i = 10; i < 300; i += 20) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.create(i, i)));
		}
		for (int i = 290; i > 0; i -= 20) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.create(i, i)));
		}
		for (int i = 15; i < 300; i += 10) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.create(i, i)));
		}
		
		for (int i = 10; i < 300; i += 20) {
			final List<TestEntity> objs = index.find(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.create(i, i));
			final int iF = i;
			assertTrue(objs.stream().anyMatch(e -> e.getPosition().getX() == iF), String.valueOf(i));
		}
		for (int i = 290; i > 0; i -= 20) {
			final List<TestEntity> objs = index.find(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.create(i, i));
			final int iF = i;
			assertTrue(objs.stream().anyMatch(e -> e.getPosition().getX() == iF), String.valueOf(i));
		}
		for (int i = 15; i < 300; i += 10) {
			final List<TestEntity> objs = index.find(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.create(i, i));
			final int iF = i;
			assertTrue(objs.stream().anyMatch(e -> e.getPosition().getX() == iF), String.valueOf(i));
		}
	}
	
	@Test
	public void testRandomEntities() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		final Random random = new XorShift128Random(42);
		
		final List<TestEntity> testEntities = new ArrayList<>(100);
		for (int i = 0; i < 100; i++) {
			testEntities.add(new TestEntity(ImmutableCoordI2.create(random.nextInt(1000), random.nextInt(1000)), ImmutableCoordI2.create(random.nextInt(29) + 1, random.nextInt(29) + 1)));
			index.add(testEntities.get(i));
		}
		
		for (int i = 0; i < 100; i++) {
			final TestEntity subject = testEntities.get(i);
			final List<TestEntity> objs = index.find(subject);
			this.checkForDuplicates(objs);
			assertTrue(!objs.isEmpty(), String.valueOf(i));
			assertTrue(objs.stream().anyMatch(e -> e == subject));
		}
		
		for (int i = 0; i < 100; i++) {
			final TestEntity subject = testEntities.get(i);
			for (int x = subject.getPosition().getX(); x < subject.getPosition().getX() + subject.getDimension().getX(); x++) {
				for (int y = subject.getPosition().getY(); y < subject.getPosition().getY() + subject.getDimension().getY(); y++) {
					final List<TestEntity> objs = index.find(ImmutableCoordI2.create(x, y), ImmutableCoordI2.one());
					assertTrue(!objs.isEmpty(), String.valueOf(i));
					assertTrue(objs.stream().anyMatch(e -> e == subject), String.valueOf(i));
				}
			}
		}
		
		final List<TestEntity> testEntitiesForEach = new ArrayList<>(100);
		index.forEach(testEntitiesForEach::add);
		this.checkForDuplicates(testEntitiesForEach);
		assertEquals(testEntities.size(), testEntitiesForEach.size());
		for (int i = 0; i < 100; i++) {
			final TestEntity entity = testEntities.get(i);
			assertTrue(testEntitiesForEach.contains(entity));
		}
		
		final List<TestEntity> testEntitiesIterator = new ArrayList<>(100);
		final Iterator<TestEntity> it = index.iterator();
		while (it.hasNext()) {
			testEntitiesIterator.add(it.next());
		}
		this.checkForDuplicates(testEntitiesIterator);
		assertEquals(testEntities.size(), testEntitiesIterator.size());
		for (int i = 0; i < 100; i++) {
			final TestEntity entity = testEntities.get(i);
			assertTrue(testEntitiesIterator.contains(entity));
		}
	}
	
	private void checkForDuplicates(final List<TestEntity> in) {
		final List<TestEntity> entities = new ArrayList<>(in);
		entities.sort((e0, e1) -> Integer.compare(e0.hashCode(), e1.hashCode()));
		for (int i = 1; i < entities.size(); i++) {
			if (entities.get(i - 1).equals(entities.get(i))) {
				fail("Duplicate " + i);
			}
		}
	}
	
	@Test
	public void testQuadTreeDim1LinearInserts() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		for (int i = 1; i < 50; i++) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()));
		}
		for (int i = 0; i >= -50; i--) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()));
		}
		
		for (int i = -50; i < 50; i++) {
			final TestEntity entity = index.find(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()).get(0);
			assertEquals(i, entity.getPosition().getX());
		}
	}
	
	@Test
	public void testQuadTreeAddRemoveAdd() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		final List<TestEntity> testEntities = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			testEntities.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()));
			index.add(testEntities.get(i));
		}
		
		index.remove(testEntities.get(0));
		index.add(testEntities.get(0));
		
		for (int i = 0; i < 5; i++) {
			final List<TestEntity> result = index.find(testEntities.get(0).getPosition(), testEntities.get(0).getDimension());
			assertEquals(1, result.size());
			assertEquals(testEntities.get(0).getPosition().getX(), result.get(0).getPosition().getX());
			assertEquals(testEntities.get(0).getPosition().getY(), result.get(0).getPosition().getY());
		}
	}
	
	@Test
	public void testQuadTreeRemove() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		final List<TestEntity> testEntities = new ArrayList<>();
		for (int i = 0; i < 32; i++) {
			testEntities.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()));
			index.add(testEntities.get(i));
		}
		
		for (int i = 0; i < 32; i++) {
			index.remove(testEntities.get(i));
		}
	}
	
	@Test
	public void testQueryOutsideRange() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		for (int i = 0; i < 50; i++) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()));
		}
		
		assertEquals(50, index.find(new Area2(ImmutableCoordI2.create(0, 0), ImmutableCoordI2.create(50, 1))).size());
		assertEquals(50, index.find(new Area2(ImmutableCoordI2.create(-10, 0), ImmutableCoordI2.create(100, 1))).size());
		assertEquals(25, index.find(new Area2(ImmutableCoordI2.create(25, 0), ImmutableCoordI2.create(50, 1))).size());
		assertEquals(0, index.find(new Area2(ImmutableCoordI2.create(-10, 0), ImmutableCoordI2.create(5, 1))).size());
		assertEquals(0, index.find(new Area2(ImmutableCoordI2.create(100, 0), ImmutableCoordI2.create(5, 1))).size());
	}
	
	public void testQueryOutsideRange2() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		for (int i = 0; i < 50; i++) {
			index.add(new TestEntity(ImmutableCoordI2.create(i, 0), ImmutableCoordI2.one()));
		}
		
		assertEquals(50, index.find(new Area2(ImmutableCoordI2.create(-100000, -100000), ImmutableCoordI2.create(200000, 200000))).size());
		assertEquals(0, index.find(new Area2(ImmutableCoordI2.create(-100000, 0), ImmutableCoordI2.create(5, 1))).size());
		assertEquals(0, index.find(new Area2(ImmutableCoordI2.create(100000, 0), ImmutableCoordI2.create(5, 1))).size());
		assertEquals(0, index.find(new Area2(ImmutableCoordI2.create(0, -100000), ImmutableCoordI2.create(5, 1))).size());
		assertEquals(0, index.find(new Area2(ImmutableCoordI2.create(0, 100000), ImmutableCoordI2.create(5, 1))).size());
	}
	
	@Test
	public void testQuadTreeExpansion() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		for (int x = 0; x < 100000; x += 1000) {
			index.add(new TestEntity(ImmutableCoordI2.create(x, x), ImmutableCoordI2.one()));
		}
		
		for (int x = 0; x < 100000; x += 1000) {
			final TestEntity entity = index.find(ImmutableCoordI2.create(x, x), ImmutableCoordI2.one()).get(0);
			assertEquals(x, entity.getPosition().getX());
		}
	}
	
	@Test
	public void testQuadTreeExpansion2() {
		final SpatialIndex2D<TestEntity> index = new QuadTree<>();
		
		index.add(new TestEntity(ImmutableCoordI2.create(1000, 0), ImmutableCoordI2.one()));
		index.add(new TestEntity(ImmutableCoordI2.create(900, -5000), ImmutableCoordI2.create(1, 10000)));
		index.add(new TestEntity(ImmutableCoordI2.create(-50000, -50000), ImmutableCoordI2.create(100000, 100000)));
	}
	
	@Disabled
	@Test
	public void testCompareToSearchIndexes() {
		final int SIZE = 100000;
		final Random random = new XorShift128Random(42);
		final SpatialIndex2D<TestEntity> quadTree = new QuadTree<>();
		final SpatialIndex2D<TestEntity> linear = new LinearSearchIndex2D<>();
		
		final List<TestEntity> testEntities = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; i++) {
			testEntities.add(new TestEntity(ImmutableCoordI2.create(random.nextInt(10000) + 1, random.nextInt(10000) + 1), ImmutableCoordI2.create(random.nextInt(29) + 1, random.nextInt(29) + 1)));
		}
		final List<Area2> queries = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; i++) {
			queries.add(new Area2(ImmutableCoordI2.create(random.nextInt(10000), random.nextInt(10000)), ImmutableCoordI2.create(random.nextInt(499) + 1, random.nextInt(499) + 1)));
		}
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			quadTree.add(testEntities.get(i));
		}
		System.out.println("QT add took " + (System.currentTimeMillis() - start) + "ms");
		start = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			linear.add(testEntities.get(i));
		}
		System.out.println("LINEAR add took " + (System.currentTimeMillis() - start) + "ms");
		
		long totalQT = 0;
		start = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			final Area2 area = queries.get(i);
			final List<TestEntity> qtResult = quadTree.find(area);
			totalQT += qtResult.size();
		}
		System.out.println("Queried QT " + totalQT + " entities in " + (System.currentTimeMillis() - start) + "ms");
		
		long totalLinear = 0;
		start = System.currentTimeMillis();
		for (int i = 0; i < SIZE; i++) {
			final Area2 area = queries.get(i);
			final List<TestEntity> bsResult = linear.find(area);
			totalLinear += bsResult.size();
		}
		System.out.println("Queried LINEAR " + totalLinear + " entities in " + (System.currentTimeMillis() - start) + "ms");
		
		if (totalQT != totalLinear) {
			System.out.println("Error!");
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
		
		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("TestEntity [position=");
			builder.append(this.position);
			builder.append(", dimension=");
			builder.append(this.dimension);
			builder.append("]");
			return builder.toString();
		}
		
	}
}
