package de.extio.game_engine.module;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.extio.game_engine.renderer.work.RendererWorkingSet;

public class ModuleManagerTest {
	
	private ModuleManagerImpl moduleManager;
	
	private TestModule testModule;
	
	private TestClientModule testClientModule;
	
	@BeforeEach
	public void setUp() throws Exception {
		this.testModule = new TestModule();
		this.testClientModule = new TestClientModule();
		
		final List<AbstractModule> initialModules = new ArrayList<>();
		initialModules.add(this.testModule);
		initialModules.add(this.testClientModule);
		
		this.moduleManager = new ModuleManagerImpl(initialModules, mock(RendererWorkingSet.class));
		this.moduleManager.afterPropertiesSet();
	}
	
	@Test
	public void testAfterPropertiesSet() {
		assertEquals(2, this.moduleManager.getModulesAll().size());
		assertTrue(this.testModule.onLoadCalled);
		assertTrue(this.testClientModule.onLoadCalled);
	}
	
	@Test
	public void testAfterPropertiesSet_EmptyModules() throws Exception {
		final ModuleManagerImpl emptyManager = new ModuleManagerImpl(List.of(), mock(RendererWorkingSet.class));
		emptyManager.afterPropertiesSet();
		
		assertTrue(emptyManager.getModulesAll().isEmpty());
	}
	
	@Test
	public void testAfterPropertiesSet_NullModules() throws Exception {
		final ModuleManagerImpl nullManager = new ModuleManagerImpl(null, mock(RendererWorkingSet.class));
		nullManager.afterPropertiesSet();
		
		assertTrue(nullManager.getModulesAll().isEmpty());
	}
	
	@Test
	public void testGetModulesAll() {
		final List<AbstractModule> modules = this.moduleManager.getModulesAll();
		
		assertEquals(2, modules.size());
		assertTrue(modules.contains(this.testModule));
		assertTrue(modules.contains(this.testClientModule));
	}
	
	@Test
	public void testGetModulesActive_InitiallyEmpty() {
		assertTrue(this.moduleManager.getModulesActive().isEmpty());
	}
	
	@Test
	public void testLoadModule() {
		this.moduleManager.loadModule(AlwaysDisplayModule.class);
		
		assertEquals(3, this.moduleManager.getModulesAll().size());
		assertTrue(this.moduleManager.getModulesAll().stream().anyMatch(m -> m instanceof AlwaysDisplayModule));
	}
	
	@Test
	public void testLoadModule_AlreadyLoaded() {
		final int initialSize = this.moduleManager.getModulesAll().size();
		
		this.moduleManager.loadModule(TestModule.class);
		
		assertEquals(initialSize, this.moduleManager.getModulesAll().size());
	}
	
	@Test
	public void testUnloadModule() {
		this.moduleManager.unloadModule(TestModule.class);
		
		assertEquals(1, this.moduleManager.getModulesAll().size());
		assertFalse(this.moduleManager.getModulesAll().contains(this.testModule));
		assertTrue(this.testModule.onUnloadCalled);
	}
	
	@Test
	public void testUnloadModule_NotLoaded() {
		final int initialSize = this.moduleManager.getModulesAll().size();
		
		this.moduleManager.unloadModule(AlwaysDisplayModule.class);
		
		assertEquals(initialSize, this.moduleManager.getModulesAll().size());
	}
	
	@Test
	public void testUnloadModule_DeactivatesFirst() {
		this.moduleManager.changeActiveState(TestModule.class, true);
		assertTrue(this.moduleManager.getModulesActive().contains(this.testModule));
		
		this.moduleManager.unloadModule(TestModule.class);
		
		assertFalse(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeActiveState_Activate() {
		this.moduleManager.changeActiveState(TestModule.class, true);
		
		assertTrue(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onActivateCalled);
	}
	
	@Test
	public void testChangeActiveState_Deactivate() {
		this.moduleManager.changeActiveState(TestModule.class, true);
		this.testModule.onActivateCalled = false;
		
		this.moduleManager.changeActiveState(TestModule.class, false);
		
		assertFalse(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeActiveState_AlreadyActive() {
		this.moduleManager.changeActiveState(TestModule.class, true);
		this.testModule.onActivateCalled = false;
		
		this.moduleManager.changeActiveState(TestModule.class, true);
		
		assertFalse(this.testModule.onActivateCalled);
	}
	
	@Test
	public void testChangeActiveState_AlreadyInactive() {
		this.moduleManager.changeActiveState(TestModule.class, false);
		
		assertFalse(this.testModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeActiveState_ByClassName() {
		this.moduleManager.changeActiveState(TestModule.class.getName(), true);
		
		assertTrue(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onActivateCalled);
	}
	
	@Test
	public void testChangeActiveState_AlwaysDisplayModule() {
		this.moduleManager.loadModule(AlwaysDisplayModule.class);
		
		this.moduleManager.changeActiveState(AlwaysDisplayModule.class, true);
		
		assertTrue(this.moduleManager.isDisplayed(AlwaysDisplayModule.class));
	}
	
	@Test
	public void testDeactivateAll() {
		this.moduleManager.changeActiveState(TestModule.class, true);
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		
		this.moduleManager.deactivateAll();
		
		assertTrue(this.moduleManager.getModulesActive().isEmpty());
		assertTrue(this.testModule.onDeactivateCalled);
		assertTrue(this.testClientModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeDisplayState_Show() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		
		assertTrue(this.moduleManager.isDisplayed(TestClientModule.class));
		assertTrue(this.testClientModule.onShowCalled);
	}
	
	@Test
	public void testChangeDisplayState_Hide() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.changeDisplayState(TestClientModule.class, false);
		
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testChangeDisplayState_AlreadyShown() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		
		assertFalse(this.testClientModule.onShowCalled, "onShow should not be called when already shown");
	}
	
	@Test
	public void testChangeDisplayState_AlreadyHidden() {
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.changeDisplayState(TestClientModule.class, false);
		
		assertFalse(this.testClientModule.onHideCalled, "onHide should not be called when already hidden");
	}
	
	@Test
	public void testChangeDisplayState_ByClassName() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.changeDisplayState(TestClientModule.class.getName(), true);
		
		assertTrue(this.moduleManager.isDisplayed(TestClientModule.class));
	}
	
	@Test
	public void testChangeDisplayState_AlwaysDisplayModule() {
		this.moduleManager.loadModule(AlwaysDisplayModule.class);
		this.moduleManager.changeActiveState(AlwaysDisplayModule.class, true);
		
		this.moduleManager.changeDisplayState(AlwaysDisplayModule.class, false);
		
		assertTrue(this.moduleManager.isDisplayed(AlwaysDisplayModule.class));
	}
	
	@Test
	public void testIsDisplayed_ByClassName() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		
		assertTrue(this.moduleManager.isDisplayed(TestClientModule.class.getName()));
	}
	
	@Test
	public void testHideAll() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.hideAll();
		
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testHideAll_DoesNotHideAlwaysDisplay() {
		this.moduleManager.loadModule(AlwaysDisplayModule.class);
		this.moduleManager.changeActiveState(AlwaysDisplayModule.class, true);
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		
		this.moduleManager.hideAll();
		
		assertTrue(this.moduleManager.isDisplayed(AlwaysDisplayModule.class));
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
	}
	
	@Test
	public void testHideExcept() {
		this.moduleManager.loadModule(AlwaysDisplayModule.class);
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.moduleManager.changeActiveState(AlwaysDisplayModule.class, true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.hideExcept(AlwaysDisplayModule.class.getName());
		
		assertTrue(this.moduleManager.isDisplayed(AlwaysDisplayModule.class));
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testHideExcept_MultipleExceptions() {
		this.moduleManager.loadModule(AlwaysDisplayModule.class);
		this.moduleManager.loadModule(HighPriorityModule.class);
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.moduleManager.changeActiveState(AlwaysDisplayModule.class, true);
		this.moduleManager.changeActiveState(HighPriorityModule.class, true);
		this.moduleManager.changeDisplayState(HighPriorityModule.class, true);
		
		this.moduleManager.hideExcept(
				AlwaysDisplayModule.class.getName(),
				HighPriorityModule.class.getName()
		);
		
		assertTrue(this.moduleManager.isDisplayed(AlwaysDisplayModule.class));
		assertTrue(this.moduleManager.isDisplayed(HighPriorityModule.class));
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
	}
	
	@Test
	public void testRestoreVisibility() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		
		this.moduleManager.hideExcept();
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.restoreVisibility();
		
		assertTrue(this.moduleManager.isDisplayed(TestClientModule.class));
		assertTrue(this.testClientModule.onShowCalled);
	}
	
	@Test
	public void testRestoreVisibility_NoState() {
		this.moduleManager.restoreVisibility();
		
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
	}
	
	@Test
	public void testIsModal() {
		assertFalse(this.moduleManager.isModal());
		
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.moduleManager.hideExcept();
		
		assertTrue(this.moduleManager.isModal());
		
		this.moduleManager.restoreVisibility();
		
		assertFalse(this.moduleManager.isModal());
	}
	
	@Test
	public void testModuleSorting_Priority() {
		this.moduleManager.loadModule(HighPriorityModule.class);
		
		final List<AbstractModule> modules = this.moduleManager.getModulesAll();
		
		assertEquals(3, modules.size());
		
		int highPriorityIndex = -1;
		int normalPriorityIndex = -1;
		int nonClientIndex = -1;
		
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i) instanceof HighPriorityModule) {
				highPriorityIndex = i;
			}
			else if (modules.get(i) instanceof TestClientModule) {
				normalPriorityIndex = i;
			}
			else if (modules.get(i) instanceof TestModule) {
				nonClientIndex = i;
			}
		}
		
		assertTrue(highPriorityIndex >= 0);
		assertTrue(normalPriorityIndex >= 0);
		assertTrue(nonClientIndex >= 0);
		assertTrue(highPriorityIndex < normalPriorityIndex, 
				"High priority (" + highPriorityIndex + ") should come before normal (" + normalPriorityIndex + ")");
		assertTrue(nonClientIndex < normalPriorityIndex,
				"Non-client (" + nonClientIndex + ") should come before client (" + normalPriorityIndex + ")");
		assertTrue(nonClientIndex < highPriorityIndex,
				"Non-client (" + nonClientIndex + ") should come before high priority client (" + highPriorityIndex + ")");
	}
	
	@Test
	public void testDeactivate_HidesDisplayedClientModule() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.changeActiveState(TestClientModule.class, false);
		
		assertFalse(this.moduleManager.isDisplayed(TestClientModule.class));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testDeactivateAll_ClearsVisibilityStack() {
		this.moduleManager.changeActiveState(TestClientModule.class, true);
		this.moduleManager.changeDisplayState(TestClientModule.class, true);
		this.moduleManager.hideExcept();
		
		assertTrue(this.moduleManager.isModal());
		
		this.moduleManager.deactivateAll();
		
		assertFalse(this.moduleManager.isModal());
	}
	
	static class TestModule extends AbstractModule {
		
		boolean onLoadCalled = false;
		
		boolean onUnloadCalled = false;
		
		boolean onActivateCalled = false;
		
		boolean onDeactivateCalled = false;
		
		@Override
		public void onLoad() {
			this.onLoadCalled = true;
		}
		
		@Override
		public void onUnload() {
			this.onUnloadCalled = true;
		}
		
		@Override
		public void onActivate() {
			this.onActivateCalled = true;
		}
		
		@Override
		public void onDeactivate() {
			this.onDeactivateCalled = true;
		}
		
	}
	
	static class TestClientModule extends AbstractClientModule {
		
		boolean onLoadCalled = false;
		
		boolean onUnloadCalled = false;
		
		boolean onActivateCalled = false;
		
		boolean onDeactivateCalled = false;
		
		boolean onShowCalled = false;
		
		boolean onHideCalled = false;
		
		@Override
		public void onLoad() {
			this.onLoadCalled = true;
		}
		
		@Override
		public void onUnload() {
			this.onUnloadCalled = true;
		}
		
		@Override
		public void onActivate() {
			this.onActivateCalled = true;
		}
		
		@Override
		public void onDeactivate() {
			this.onDeactivateCalled = true;
		}
		
		@Override
		public void onShow() {
			this.onShowCalled = true;
		}
		
		@Override
		public void onHide() {
			this.onHideCalled = true;
		}
		
	}
	
	static class AlwaysDisplayModule extends AbstractClientModule {
		
		@Override
		public boolean isAlwaysDisplay() {
			return true;
		}
		
	}
	
	static class HighPriorityModule extends AbstractClientModule {
		
		@Override
		public int getPriority() {
			return MODULE_PRIORITY_HIGH;
		}
		
	}
	
}
