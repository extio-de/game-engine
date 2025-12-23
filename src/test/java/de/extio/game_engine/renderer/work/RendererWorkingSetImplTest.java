package de.extio.game_engine.renderer.work;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.RenderingBo;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.color.RgbaColor;
import de.extio.game_engine.spatial2.model.HasPosition2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RendererWorkingSetImplTest {
	
	private RendererWorkingSetImpl workingSet;
	
	private RenderingBoPool renderingBoPool;
	
	@BeforeEach
	void setUp() {
		this.renderingBoPool = mock(RenderingBoPool.class);
		this.workingSet = new RendererWorkingSetImpl(this.renderingBoPool);
	}
	
	@Test
	void addSingleAddsToUncommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		this.workingSet.add(TestModuleA.class, bo1);
		
		final var uncommitted = this.workingSet.getUncommittedWork(TestModuleA.class);
		assertNotNull(uncommitted);
		assertEquals(Map.of("bo1", bo1), uncommitted);
	}
	
	@Test
	void addListAddsAllToUncommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.add(TestModuleA.class, List.of(bo1, bo2));
		
		final var uncommitted = this.workingSet.getUncommittedWork(TestModuleA.class);
		assertEquals(Map.of("bo1", bo1, "bo2", bo2), uncommitted);
	}
	
	@Test
	void commitWithoutCloneMovesUncommittedToLiveAndClearsUncommitted() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.add(TestModuleA.class, bo2);
		
		final var returnedUncommitted = this.workingSet.commit(TestModuleA.class, false);
		final var currentUncommitted = this.workingSet.getUncommittedWork(TestModuleA.class);
		
		assertSame(currentUncommitted, returnedUncommitted);
		assertTrue(currentUncommitted.isEmpty());
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live);
		assertEquals(2, live.size());
		assertTrue(live.containsAll(List.of(bo1, bo2)));
	}
	
	@Test
	void commitWithCloneCopiesUncommittedIntoNewUncommitted() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		final RenderingBo bo3 = mock(RenderingBo.class);
		when(bo3.getId()).thenReturn("bo3");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.add(TestModuleA.class, bo2);
		
		final var returnedUncommitted = this.workingSet.commit(TestModuleA.class, true);
		final var currentUncommitted = this.workingSet.getUncommittedWork(TestModuleA.class);
		
		assertSame(currentUncommitted, returnedUncommitted);
		assertEquals(Map.of("bo1", bo1, "bo2", bo2), currentUncommitted);
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live);
		assertEquals(2, live.size());
		assertTrue(live.containsAll(List.of(bo1, bo2)));
		
		this.workingSet.add(TestModuleA.class, bo3);
		final List<RenderingBo> liveAfterAdd = new ArrayList<>();
		this.workingSet.getLiveSet(liveAfterAdd);
		assertEquals(2, liveAfterAdd.size());
		assertTrue(liveAfterAdd.containsAll(List.of(bo1, bo2)));
	}
	
	@Test
	void liveSetContainsAllProducersCommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		final RenderingBo bo3 = mock(RenderingBo.class);
		when(bo3.getId()).thenReturn("bo3");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.add(TestModuleA.class, bo2);
		this.workingSet.add(TestModuleB.class, bo3);
		
		this.workingSet.commit(TestModuleA.class, false);
		this.workingSet.commit(TestModuleB.class, false);
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live);
		assertEquals(3, live.size());
		assertTrue(live.containsAll(List.of(bo1, bo2, bo3)));
	}
	
	@Test
	void commitOnUnknownProducerCreatesEmptyState() {
		final var returnedUncommitted = this.workingSet.commit(TestModuleC.class, false);
		assertNotNull(returnedUncommitted);
		assertTrue(returnedUncommitted.isEmpty());
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live);
		assertTrue(live.isEmpty());
	}
	
	@Test
	void clearRemovesProducerAndReleasesWork() {
		final var bo1 = new TestBoA();
		bo1.setId("bo1");
		final var bo2 = new TestBoB();
		bo2.setId("bo2");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.commit(TestModuleA.class, false);
		this.workingSet.add(TestModuleA.class, bo2);
		
		// Before clear
		final List<RenderingBo> liveBefore = new ArrayList<>();
		this.workingSet.getLiveSet(liveBefore);
		assertTrue(liveBefore.contains(bo1));
		assertNotNull(this.workingSet.get(TestModuleA.class, "bo2"));
		
		this.workingSet.clear(TestModuleA.class);
		
		// After clear
		final List<RenderingBo> liveAfter = new ArrayList<>();
		this.workingSet.getLiveSet(liveAfter);
		assertTrue(liveAfter.isEmpty());
		
		final var uncommittedAfter = this.workingSet.getUncommittedWork(TestModuleA.class);
		assertTrue(uncommittedAfter.isEmpty());
		
		verify(this.renderingBoPool, times(1)).release(bo1);
		verify(this.renderingBoPool, times(1)).release(bo2);
		assertEquals(1, bo1.closeStaticCalls());
		assertEquals(1, bo2.closeStaticCalls());
	}
	
	@Test
	void clearOnUnknownProducerDoesNothing() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.commit(TestModuleA.class, false);
		
		this.workingSet.clear(TestModuleC.class);
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live);
		assertEquals(1, live.size());
		assertTrue(live.contains(bo1));
	}
	
	@Test
	void secondCommitReleasesPreviousLiveWorkAndCallsCloseStaticOncePerClass() {
		final var bo1 = new TestBoA();
		bo1.setId("bo1");
		final var bo2 = new TestBoA();
		bo2.setId("bo2");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.add(TestModuleA.class, bo2);
		this.workingSet.commit(TestModuleA.class, false);
		
		final var bo3 = new TestBoB();
		bo3.setId("bo3");
		this.workingSet.add(TestModuleA.class, bo3);
		this.workingSet.commit(TestModuleA.class, false);
		
		verify(this.renderingBoPool, times(1)).release(bo1);
		verify(this.renderingBoPool, times(1)).release(bo2);
		
		// closeStatic() is called exactly once per class, so one of bo1 or bo2 has it called
		final int totalCloseStaticCallsForTestBoA = bo1.closeStaticCalls() + bo2.closeStaticCalls();
		assertEquals(1, totalCloseStaticCallsForTestBoA);
		assertEquals(0, bo3.closeStaticCalls());
	}
	
	@Test
	void getReturnsRenderingBoFromUncommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.add(TestModuleA.class, bo2);
		
		final var retrieved1 = this.workingSet.get(TestModuleA.class, "bo1");
		final var retrieved2 = this.workingSet.get(TestModuleA.class, "bo2");
		final var notFound = this.workingSet.get(TestModuleA.class, "nonexistent");
		
		assertSame(bo1, retrieved1);
		assertSame(bo2, retrieved2);
		assertTrue(notFound == null);
	}
	
	@Test
	void getRetrievesFromUncommittedWorkOnly() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.add(TestModuleA.class, bo1);
		this.workingSet.add(TestModuleA.class, bo2);
		final var committedWork = this.workingSet.commit(TestModuleA.class, false);
		
		// After commit without clone, the uncommitted work should be empty
		assertTrue(committedWork.isEmpty());
		
		// get() only retrieves from next set, not live set, so it returns null after commit
		final var retrieved1 = this.workingSet.get(TestModuleA.class, "bo1");
		final var retrieved2 = this.workingSet.get(TestModuleA.class, "bo2");
		
		assertTrue(retrieved1 == null);
		assertTrue(retrieved2 == null);
	}
	
	@Test
	void addWithSameIdReplacesExistingRenderingBo() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo1_replacement = mock(RenderingBo.class);
		when(bo1_replacement.getId()).thenReturn("bo1");
		
		this.workingSet.add(TestModuleA.class, bo1);
		final var uncommittedBefore = this.workingSet.getUncommittedWork(TestModuleA.class);
		assertEquals(1, uncommittedBefore.size());
		assertSame(bo1, uncommittedBefore.get("bo1"));
		
		this.workingSet.add(TestModuleA.class, bo1_replacement);
		final var uncommittedAfter = this.workingSet.getUncommittedWork(TestModuleA.class);
		assertEquals(1, uncommittedAfter.size());
		assertSame(bo1_replacement, uncommittedAfter.get("bo1"));
	}
	
	private static abstract class TestBoBase implements RenderingBo {
		
		private final AtomicInteger closeStaticCalls = new AtomicInteger();
		
		private String id;
		
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
			return RenderingBoLayer.UI0_0;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorTopLeft(final int x, final int y) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorTopLeft(final HasPosition2 position) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorTopRight(final int x, final int y) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorTopRight(final HasPosition2 position) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomLeft(final int x, final int y) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomLeft(final HasPosition2 position) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomRight(final int x, final int y) {
			return this;
		}
		
		@Override
		public RenderingBo withPositionAbsoluteAnchorBottomRight(final HasPosition2 position) {
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
		public RenderingBo withPositionIncrementalAbsolute(final HasPosition2 position) {
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
		public RenderingBo withPositionRelative(final HasPosition2 position) {
			return this;
		}
		
		@Override
		public void closeStatic() {
			this.closeStaticCalls.incrementAndGet();
		}
		
		public int closeStaticCalls() {
			return this.closeStaticCalls.get();
		}
		
		@Override
		public void setRendererData(final RendererData RendererData) {
		}
		
		@Override
		public void close() {
		}
	}
	
	private static final class TestBoA extends TestBoBase {
		
	}
	
	private static final class TestBoB extends TestBoBase {
		
	}
	
	private static final class TestModuleA extends AbstractClientModule {
		
	}
	
	private static final class TestModuleB extends AbstractClientModule {
		
	}
	
	private static final class TestModuleC extends AbstractClientModule {
		
	}
}
