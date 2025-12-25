package de.extio.game_engine.renderer.work;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.color.RgbaColor;

public class RenderingBoPoolImplTest {

	private RenderingBoPoolImpl pool;
	private Map<Class<? extends RenderingBo>, Class<? extends RenderingBo>> mapping;
	private RendererData rendererData;

	@BeforeEach
	void setUp() {
		this.mapping = new HashMap<>();
		this.pool = new RenderingBoPoolImpl(this.mapping);
		this.rendererData = mock(RendererData.class);
		this.pool.setRendererData(this.rendererData);
	}

	@Test
	void testAcquireConcreteClass() {
		final TestRenderingBo acquired = this.pool.acquire("test-id", TestRenderingBo.class);

		assertNotNull(acquired);
		assertNotNull(acquired.getId());
		assertEquals("test-id", acquired.getId());
		assertSame(TestRenderingBo.class, acquired.getClass());
		assertSame(this.rendererData, acquired.getRendererData());
		assertEquals(0, acquired.getCloseCallCount());
	}

	@Test
	void testAcquireInterfaceWithMapping() {
		this.mapping.put(TestRenderingBoInterface.class, TestRenderingBoImpl.class);

		final TestRenderingBoInterface acquired = this.pool.acquire("interface-id", TestRenderingBoInterface.class);

		assertNotNull(acquired);
		assertNotNull(acquired.getId());
		assertEquals("interface-id", acquired.getId());
		assertTrue(acquired instanceof TestRenderingBoImpl);
		assertSame(TestRenderingBoImpl.class, acquired.getClass());
		assertSame(this.rendererData, ((TestRenderingBoImpl) acquired).getRendererData());
	}

	@Test
	void testAcquireUnmappedInterfaceThrows() {
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
			this.pool.acquire("id", TestRenderingBoInterface.class);
		});
		assertNotNull(ex.getMessage());
		assertTrue(ex.getMessage().contains("TestRenderingBoInterface"));
	}

	@Test
	void testAcquireMultipleInstancesPooling() {
		final TestRenderingBo first = this.pool.acquire("id1", TestRenderingBo.class);
		final TestRenderingBo second = this.pool.acquire("id2", TestRenderingBo.class);

		assertNotNull(first);
		assertNotNull(second);
		assertEquals("id1", first.getId());
		assertEquals("id2", second.getId());
		assertNotSame(first, second);
		assertSame(TestRenderingBo.class, first.getClass());
		assertSame(TestRenderingBo.class, second.getClass());
	}

	@Test
	void testReleaseAndReacquireFromPool() {
		final TestRenderingBo acquired1 = this.pool.acquire("id1", TestRenderingBo.class);
		final int id1 = System.identityHashCode(acquired1);
		assertEquals("id1", acquired1.getId());
		assertEquals(0, acquired1.getCloseCallCount());

		this.pool.release(acquired1);
		assertEquals(1, acquired1.getCloseCallCount());

		final TestRenderingBo acquired2 = this.pool.acquire("id2", TestRenderingBo.class);
		final int id2 = System.identityHashCode(acquired2);

		assertEquals(id1, id2);
		assertEquals("id2", acquired2.getId());
		assertEquals(1, acquired2.getCloseCallCount());
		assertSame(acquired1, acquired2);
	}

	@Test
	void testReleaseCallsCloseOnObject() throws Exception {
		final TestRenderingBo bo = this.pool.acquire("id", TestRenderingBo.class);
		assertEquals(0, bo.getCloseCallCount());
		
		this.pool.release(bo);
		assertEquals(1, bo.getCloseCallCount());
		
		this.pool.release(bo);
		assertEquals(2, bo.getCloseCallCount());
	}

	@Test
	void testCopyObject() {
		final TestRenderingBo original = this.pool.acquire("original-id", TestRenderingBo.class);
		original.testData = "test-value";
		assertNotNull(original.getId());

		final TestRenderingBo copy = this.pool.copy(original);

		assertNotNull(copy);
		assertNotSame(original, copy);
		assertEquals("original-id", copy.getId());
		assertEquals("test-value", copy.testData);
		assertSame(TestRenderingBo.class, copy.getClass());
	}

	@Test
	void testSetRendererData() {
		final TestRenderingBo boOld = this.pool.acquire("id1", TestRenderingBo.class);
		assertSame(this.rendererData, boOld.getRendererData());
		
		final RendererData newData = mock(RendererData.class);
		this.pool.setRendererData(newData);

		final TestRenderingBo boNew = this.pool.acquire("id2", TestRenderingBo.class);
		assertSame(newData, boNew.getRendererData());
		assertNotSame(this.rendererData, boNew.getRendererData());
	}

	@Test
	void testLastMappingCaching() {
		this.mapping.put(TestRenderingBoInterface.class, TestRenderingBoImpl.class);

		final TestRenderingBoInterface first = this.pool.acquire("id1", TestRenderingBoInterface.class);
		assertNotNull(first);
		assertEquals("id1", first.getId());
		
		final TestRenderingBoInterface second = this.pool.acquire("id2", TestRenderingBoInterface.class);
		assertNotNull(second);
		assertEquals("id2", second.getId());
		
		assertNotSame(first, second);
		assertSame(first.getClass(), second.getClass());
		assertTrue(first.getClass() == TestRenderingBoImpl.class);
	}

	@Test
	void testLastReverseMappingCaching() {
		final TestRenderingBo first = this.pool.acquire("id1", TestRenderingBo.class);
		assertNotNull(first);
		assertEquals(0, first.getCloseCallCount());
		
		final TestRenderingBo second = this.pool.acquire("id2", TestRenderingBo.class);
		assertNotNull(second);
		assertEquals(0, second.getCloseCallCount());
		assertNotSame(first, second);

		this.pool.release(first);
		assertEquals(1, first.getCloseCallCount());
		
		this.pool.release(second);
		assertEquals(1, second.getCloseCallCount());

		final TestRenderingBo reacquired = this.pool.acquire("id3", TestRenderingBo.class);

		assertNotNull(reacquired);
		assertEquals("id3", reacquired.getId());
		assertTrue(reacquired == first || reacquired == second);
	}

	@Test
	void testConcurrentAccess() throws InterruptedException {
		final int[] thread1Count = {0};
		final int[] thread2Count = {0};
		
		final Thread thread1 = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				final TestRenderingBo bo = this.pool.acquire("id-" + i, TestRenderingBo.class);
				assertNotNull(bo);
				assertNotNull(bo.getId());
				this.pool.release(bo);
				thread1Count[0]++;
			}
		});

		final Thread thread2 = new Thread(() -> {
			for (int i = 10; i < 20; i++) {
				final TestRenderingBo bo = this.pool.acquire("id-" + i, TestRenderingBo.class);
				assertNotNull(bo);
				assertNotNull(bo.getId());
				this.pool.release(bo);
				thread2Count[0]++;
			}
		});

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		assertEquals(10, thread1Count[0]);
		assertEquals(10, thread2Count[0]);
		
		final TestRenderingBo bo = this.pool.acquire("final-id", TestRenderingBo.class);
		assertNotNull(bo);
		assertEquals("final-id", bo.getId());
	}

	public interface TestRenderingBoInterface extends RenderingBo {
	}

	public static class TestRenderingBoImpl implements TestRenderingBoInterface {

		private String id;
		private RendererData rendererData;
		private int closeCallCount;

		@Override
		public void setId(final String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public RenderingBo setColor(final RgbaColor color) {
			return this;
		}

		@Override
		public int getX() {
			return 0;
		}

		@Override
		public int getY() {
			return 0;
		}

		@Override
		public int getLocalX() {
			return 0;
		}

		@Override
		public int getLocalY() {
			return 0;
		}

		@Override
		public RenderingBo setLayer(final RenderingBoLayer layer) {
			return this;
		}

		@Override
		public RenderingBoLayer getLayer() {
			return null;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopLeft(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopLeft(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopRight(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopRight(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomLeft(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomLeft(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomRight(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomRight(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionPercentual(final double x, final double y) {
			return this;
		}

		@Override
		public RenderingBo withPositionIncrementalAbsolute(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionIncrementalAbsolute(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionIncrementalPercentual(final double x, final double y) {
			return this;
		}

		@Override
		public RenderingBo withPositionRelative(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionRelative(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public void setRendererData(final RendererData rendererData) {
			this.rendererData = rendererData;
		}

		@Override
		public void apply(final RenderingBo source) {
		}

		@Override
		public void closeStatic() {
		}

		@Override
		public void close() {
			this.closeCallCount++;
		}

		public RendererData getRendererData() {
			return this.rendererData;
		}

		public int getCloseCallCount() {
			return this.closeCallCount;
		}
	}

	public static class TestRenderingBo implements RenderingBo {

		private String id;
		private RendererData rendererData;
		private int closeCallCount;
		public String testData;

		@Override
		public void setId(final String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public RenderingBo setColor(final RgbaColor color) {
			return this;
		}

		@Override
		public int getX() {
			return 0;
		}

		@Override
		public int getY() {
			return 0;
		}

		@Override
		public int getLocalX() {
			return 0;
		}

		@Override
		public int getLocalY() {
			return 0;
		}

		@Override
		public RenderingBo setLayer(final RenderingBoLayer layer) {
			return this;
		}

		@Override
		public RenderingBoLayer getLayer() {
			return null;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopLeft(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopLeft(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopRight(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorTopRight(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomLeft(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomLeft(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomRight(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomRight(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionPercentual(final double x, final double y) {
			return this;
		}

		@Override
		public RenderingBo withPositionIncrementalAbsolute(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionIncrementalAbsolute(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public RenderingBo withPositionIncrementalPercentual(final double x, final double y) {
			return this;
		}

		@Override
		public RenderingBo withPositionRelative(final int x, final int y) {
			return this;
		}

		@Override
		public RenderingBo withPositionRelative(final de.extio.game_engine.spatial2.model.HasPosition2 position) {
			return this;
		}

		@Override
		public void setRendererData(final RendererData rendererData) {
			this.rendererData = rendererData;
		}

		@Override
		public void apply(final RenderingBo source) {
			if (source instanceof TestRenderingBo) {
				this.testData = ((TestRenderingBo) source).testData;
			}
		}

		@Override
		public void closeStatic() {
		}

		@Override
		public void close() {
			this.closeCallCount++;
		}

		public RendererData getRendererData() {
			return this.rendererData;
		}

		public int getCloseCallCount() {
			return this.closeCallCount;
		}
	}
}
