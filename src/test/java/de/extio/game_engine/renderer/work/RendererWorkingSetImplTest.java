package de.extio.game_engine.renderer.work;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	private TestModuleA moduleA;
	
	private TestModuleB moduleB;
	
	private TestModuleC moduleC;
	
	@BeforeEach
	void setUp() {
		this.renderingBoPool = mock(RenderingBoPool.class);
		this.workingSet = new RendererWorkingSetImpl(this.renderingBoPool);
		this.moduleA = new TestModuleA();
		this.moduleB = new TestModuleB();
		this.moduleC = new TestModuleC();
	}
	
	@Test
	void addSingleAddsToUncommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		this.workingSet.put(this.moduleA.getId(), bo1);
		
		final var uncommitted = this.workingSet.getUncommittedWork(this.moduleA.getId());
		assertNotNull(uncommitted);
		assertEquals(Map.of("bo1", bo1), uncommitted);
	}
	
	@Test
	void addListAddsAllToUncommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.put(this.moduleA.getId(), List.of(bo1, bo2));
		
		final var uncommitted = this.workingSet.getUncommittedWork(this.moduleA.getId());
		assertEquals(Map.of("bo1", bo1, "bo2", bo2), uncommitted);
	}
	
	@Test
	void commitWithoutCloneMovesUncommittedToLiveAndClearsUncommitted() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.put(this.moduleA.getId(), bo2);
		
		final var returnedUncommitted = this.workingSet.commit(this.moduleA.getId(), false);
		final var currentUncommitted = this.workingSet.getUncommittedWork(this.moduleA.getId());
		
		assertTrue(returnedUncommitted.isEmpty());
		assertTrue(currentUncommitted.isEmpty());
		assertEquals(returnedUncommitted, currentUncommitted);
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live, null);
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
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.put(this.moduleA.getId(), bo2);
		
		final var returnedUncommitted = this.workingSet.commit(this.moduleA.getId(), true);
		final var currentUncommitted = this.workingSet.getUncommittedWork(this.moduleA.getId());
		
		assertEquals(currentUncommitted, returnedUncommitted);
		assertEquals(Map.of("bo1", bo1, "bo2", bo2), currentUncommitted);
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live, null);
		assertEquals(2, live.size());
		assertTrue(live.containsAll(List.of(bo1, bo2)));
		
		this.workingSet.put(this.moduleA.getId(), bo3);
		final List<RenderingBo> liveAfterAdd = new ArrayList<>();
		this.workingSet.getLiveSet(liveAfterAdd, null);
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
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.put(this.moduleA.getId(), bo2);
		this.workingSet.put(this.moduleB.getId(), bo3);
		
		this.workingSet.commit(this.moduleA.getId(), false);
		this.workingSet.commit(this.moduleB.getId(), false);
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live, null);
		assertEquals(3, live.size());
		assertTrue(live.containsAll(List.of(bo1, bo2, bo3)));
	}
	
	@Test
	void commitOnUnknownProducerCreatesEmptyState() {
		final var returnedUncommitted = this.workingSet.commit(this.moduleC.getId(), false);
		assertNotNull(returnedUncommitted);
		assertTrue(returnedUncommitted.isEmpty());
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live, null);
		assertTrue(live.isEmpty());
	}
	
	@Test
	void clearRemovesProducerAndReleasesWork() {
		final var bo1 = new TestBoA();
		bo1.setId("bo1");
		final var bo2 = new TestBoB();
		bo2.setId("bo2");
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.commit(this.moduleA.getId(), false);
		this.workingSet.put(this.moduleA.getId(), bo2);
		
		// Before clear
		final List<RenderingBo> liveBefore = new ArrayList<>();
		this.workingSet.getLiveSet(liveBefore, null);
		assertTrue(liveBefore.contains(bo1));
		assertNotNull(this.workingSet.get(this.moduleA.getId(), "bo2"));
		
		this.workingSet.clear(this.moduleA.getId());
		
		// After clear
		final List<RenderingBo> liveAfter = new ArrayList<>();
		this.workingSet.getLiveSet(liveAfter, null);
		assertTrue(liveAfter.isEmpty());
		
		final var uncommittedAfter = this.workingSet.getUncommittedWork(this.moduleA.getId());
		assertTrue(uncommittedAfter.isEmpty());
		
		verify(this.renderingBoPool, times(1)).release(bo1);
		verify(this.renderingBoPool, times(1)).release(bo2);
	}
	
	@Test
	void clearOnUnknownProducerDoesNothing() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.commit(this.moduleA.getId(), false);
		
		this.workingSet.clear(this.moduleC.getId());
		
		final List<RenderingBo> live = new ArrayList<>();
		this.workingSet.getLiveSet(live, null);
		assertEquals(1, live.size());
		assertTrue(live.contains(bo1));
	}
	
	@Test
	void secondCommitReleasesPreviousLiveWork() {
		final var bo1 = new TestBoA();
		bo1.setId("bo1");
		final var bo2 = new TestBoA();
		bo2.setId("bo2");
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.put(this.moduleA.getId(), bo2);
		this.workingSet.commit(this.moduleA.getId(), false);
		
		final var bo3 = new TestBoB();
		bo3.setId("bo3");
		this.workingSet.put(this.moduleA.getId(), bo3);
		this.workingSet.commit(this.moduleA.getId(), false);
		
		verify(this.renderingBoPool, times(1)).release(bo1);
		verify(this.renderingBoPool, times(1)).release(bo2);
	}
	
	@Test
	void getReturnsRenderingBoFromUncommittedWork() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo2 = mock(RenderingBo.class);
		when(bo2.getId()).thenReturn("bo2");
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.put(this.moduleA.getId(), bo2);
		
		final var retrieved1 = this.workingSet.get(this.moduleA.getId(), "bo1");
		final var retrieved2 = this.workingSet.get(this.moduleA.getId(), "bo2");
		final var notFound = this.workingSet.get(this.moduleA.getId(), "nonexistent");
		
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
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		this.workingSet.put(this.moduleA.getId(), bo2);
		final var committedWork = this.workingSet.commit(this.moduleA.getId(), false);
		
		// After commit without clone, the uncommitted work should be empty
		assertTrue(committedWork.isEmpty());
		
		// get() only retrieves from next set, not live set, so it returns null after commit
		final var retrieved1 = this.workingSet.get(this.moduleA.getId(), "bo1");
		final var retrieved2 = this.workingSet.get(this.moduleA.getId(), "bo2");
		
		assertTrue(retrieved1 == null);
		assertTrue(retrieved2 == null);
	}
	
	@Test
	void addWithSameIdReplacesExistingRenderingBo() {
		final RenderingBo bo1 = mock(RenderingBo.class);
		when(bo1.getId()).thenReturn("bo1");
		final RenderingBo bo1_replacement = mock(RenderingBo.class);
		when(bo1_replacement.getId()).thenReturn("bo1");
		
		this.workingSet.put(this.moduleA.getId(), bo1);
		final var uncommittedBefore = this.workingSet.getUncommittedWork(this.moduleA.getId());
		assertEquals(1, uncommittedBefore.size());
		assertSame(bo1, uncommittedBefore.get("bo1"));
		
		this.workingSet.put(this.moduleA.getId(), bo1_replacement);
		final var uncommittedAfter = this.workingSet.getUncommittedWork(this.moduleA.getId());
		assertEquals(1, uncommittedAfter.size());
		assertSame(bo1_replacement, uncommittedAfter.get("bo1"));
	}

	@Test
	void getLiveSetFiltersOutNotDisplayedModules() {
		final var boA1 = new TestBoA();
		boA1.setId("boA1");
		final var boB1 = new TestBoB();
		boB1.setId("boB1");
		final var boA2 = new TestBoA();
		boA2.setId("boA2");
		
		this.workingSet.put(this.moduleA.getId(), boA1);
		this.workingSet.put(this.moduleB.getId(), boB1);
		this.workingSet.put(this.moduleA.getId(), boA2);
		
		this.workingSet.commit(this.moduleA.getId(), false);
		this.workingSet.commit(this.moduleB.getId(), false);
		
		final List<RenderingBo> allLive = new ArrayList<>();
		this.workingSet.getLiveSet(allLive, null);
		assertEquals(3, allLive.size());
		assertTrue(allLive.containsAll(List.of(boA1, boB1, boA2)));
		
		final List<RenderingBo> filteredLive = new ArrayList<>();
		this.workingSet.getLiveSet(filteredLive, this.moduleA.getId()::equals);
		assertEquals(2, filteredLive.size());
		assertTrue(filteredLive.containsAll(List.of(boA1, boA2)));
		assertFalse(filteredLive.contains(boB1));
	}

	@Test
	void getWithTypeReturnsTypedInstanceAndNullForUnknown() {
		final var boA = new TestBoA();
		boA.setId("bo1");
		this.workingSet.put(this.moduleA.getId(), boA);

		final var retrieved = this.workingSet.get(this.moduleA.getId(), "bo1", TestBoA.class);
		assertSame(boA, retrieved);

		final var notFound = this.workingSet.get(this.moduleA.getId(), "nonexistent", TestBoA.class);
		assertTrue(notFound == null);
	}

	@Test
	void getWithTypeThrowsClassCastForWrongType() {
		final var boA = new TestBoA();
		boA.setId("bo1");
		this.workingSet.put(this.moduleA.getId(), boA);

		assertThrows(ClassCastException.class, () -> this.workingSet.get(this.moduleA.getId(), "bo1", TestBoB.class));
	}

	@Test
	void getOrAcquireReturnsUncommittedWhenPresent() {
		final var boA = new TestBoA();
		boA.setId("bo1");
		this.workingSet.put(this.moduleA.getId(), boA);

		final var retrieved = this.workingSet.getOrAcquire(this.moduleA.getId(), "bo1", TestBoA.class);
		assertSame(boA, retrieved);
	}

	@Test
	void getOrAcquireAcquiresFromPoolWhenMissing() {
		final var pooledBo = new TestBoA();
		pooledBo.setId("pooled");
		when(this.renderingBoPool.acquire("pooled", TestBoA.class)).thenReturn(pooledBo);

		final var retrieved = this.workingSet.getOrAcquire(this.moduleA.getId(), "pooled", TestBoA.class);
		assertSame(pooledBo, retrieved);
		verify(this.renderingBoPool, times(1)).acquire("pooled", TestBoA.class);
	}

	private static abstract class TestBoBase implements RenderingBo {
		
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
