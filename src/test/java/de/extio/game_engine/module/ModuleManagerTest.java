package de.extio.game_engine.module;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import de.extio.game_engine.renderer.work.RendererWorkingSet;

public class ModuleManagerTest {
	
	private ModuleServiceImpl moduleManager;
	
	private TestModule testModule;
	
	private TestClientModule testClientModule;
	
	@BeforeEach
	public void setUp() throws Exception {
		this.testModule = new TestModule();
		this.testClientModule = new TestClientModule();
		
		final ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansOfType(AbstractModule.class, false, false))
			.thenReturn(Map.of("testModule", this.testModule, "testClientModule", this.testClientModule));
		
		this.moduleManager = new ModuleServiceImpl(ctx, mock(RendererWorkingSet.class));
		this.moduleManager.onApplicationEvent(new ContextRefreshedEvent(ctx));
	}
	
	@Test
	public void testOnApplicationEvent() {
		assertEquals(2, this.moduleManager.getModulesAll().size());
		assertTrue(this.testModule.onLoadCalled);
		assertTrue(this.testClientModule.onLoadCalled);
	}
	
	@Test
	public void testOnApplicationEvent_EmptyModules() throws Exception {
		final ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansOfType(AbstractModule.class, false, false)).thenReturn(Map.of());
		final ModuleServiceImpl emptyManager = new ModuleServiceImpl(ctx, mock(RendererWorkingSet.class));
		emptyManager.onApplicationEvent(new ContextRefreshedEvent(ctx));
		
		assertTrue(emptyManager.getModulesAll().isEmpty());
	}
	
	@Test
	public void testOnApplicationEvent_NullModules() throws Exception {
		final ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansOfType(AbstractModule.class, false, false)).thenReturn(null);
		final ModuleServiceImpl nullManager = new ModuleServiceImpl(ctx, mock(RendererWorkingSet.class));
		nullManager.onApplicationEvent(new ContextRefreshedEvent(ctx));
		
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
		final var newModule = new AlwaysDisplayModule();
		this.moduleManager.loadModule(newModule);
		
		assertEquals(3, this.moduleManager.getModulesAll().size());
		assertTrue(this.moduleManager.getModulesAll().stream().anyMatch(m -> m instanceof AlwaysDisplayModule));
	}
	
	@Test
	public void testLoadModule_AlreadyLoaded() {
		final int initialSize = this.moduleManager.getModulesAll().size();
		
		this.moduleManager.loadModule(this.testModule);
		
		assertEquals(initialSize, this.moduleManager.getModulesAll().size());
	}
	
	@Test
	public void testUnloadModule() {
		this.moduleManager.unloadModule(this.testModule.getId());
		
		assertEquals(1, this.moduleManager.getModulesAll().size());
		assertFalse(this.moduleManager.getModulesAll().contains(this.testModule));
		assertTrue(this.testModule.onUnloadCalled);
	}
	
	@Test
	public void testUnloadModule_NotLoaded() {
		final int initialSize = this.moduleManager.getModulesAll().size();
		
		this.moduleManager.unloadModule("NonExistentModule");
		
		assertEquals(initialSize, this.moduleManager.getModulesAll().size());
	}
	
	@Test
	public void testUnloadModule_DeactivatesFirst() {
		this.moduleManager.changeActiveState(this.testModule.getId(), true);
		assertTrue(this.moduleManager.getModulesActive().contains(this.testModule));
		
		this.moduleManager.unloadModule(this.testModule.getId());
		
		assertFalse(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeActiveState_Activate() {
		this.moduleManager.changeActiveState(this.testModule.getId(), true);
		
		assertTrue(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onActivateCalled);
	}
	
	@Test
	public void testChangeActiveState_Deactivate() {
		this.moduleManager.changeActiveState(this.testModule.getId(), true);
		this.testModule.onActivateCalled = false;
		
		this.moduleManager.changeActiveState(this.testModule.getId(), false);
		
		assertFalse(this.moduleManager.getModulesActive().contains(this.testModule));
		assertTrue(this.testModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeActiveState_AlreadyActive() {
		this.moduleManager.changeActiveState(this.testModule.getId(), true);
		this.testModule.onActivateCalled = false;
		
		this.moduleManager.changeActiveState(this.testModule.getId(), true);
		
		assertFalse(this.testModule.onActivateCalled);
	}
	
	@Test
	public void testChangeActiveState_AlreadyInactive() {
		this.moduleManager.changeActiveState(this.testModule.getId(), false);
		
		assertFalse(this.testModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeActiveState_AlwaysDisplayModule() {
		final var alwaysDisplayModule = new AlwaysDisplayModule();
		this.moduleManager.loadModule(alwaysDisplayModule);
		
		this.moduleManager.changeActiveState(alwaysDisplayModule.getId(), true);
		
		assertTrue(this.moduleManager.isDisplayed(alwaysDisplayModule.getId()));
	}
	
	@Test
	public void testDeactivateAll() {
		this.moduleManager.changeActiveState(this.testModule.getId(), true);
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		
		this.moduleManager.deactivateAll();
		
		assertTrue(this.moduleManager.getModulesActive().isEmpty());
		assertTrue(this.testModule.onDeactivateCalled);
		assertTrue(this.testClientModule.onDeactivateCalled);
	}
	
	@Test
	public void testChangeDisplayState_Show() {
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		
		assertTrue(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		assertTrue(this.testClientModule.onShowCalled);
	}
	
	@Test
	public void testChangeDisplayState_Hide() {
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), false);
		
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testChangeDisplayState_AlreadyShown() {
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		
		assertFalse(this.testClientModule.onShowCalled, "onShow should not be called when already shown");
	}
	
	@Test
	public void testChangeDisplayState_AlreadyHidden() {
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), false);
		
		assertFalse(this.testClientModule.onHideCalled, "onHide should not be called when already hidden");
	}
	
	@Test
	public void testChangeDisplayState_AlwaysDisplayModule() {
		final var alwaysDisplayModule = new AlwaysDisplayModule();
		this.moduleManager.loadModule(alwaysDisplayModule);
		this.moduleManager.changeActiveState(alwaysDisplayModule.getId(), true);
		
		this.moduleManager.changeDisplayState(alwaysDisplayModule.getId(), false);
		
		assertTrue(this.moduleManager.isDisplayed(alwaysDisplayModule.getId()));
	}
	
	@Test
	public void testHideAll() {
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.hideAll();
		
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testHideAll_DoesNotHideAlwaysDisplay() {
		final var alwaysDisplayModule = new AlwaysDisplayModule();
		this.moduleManager.loadModule(alwaysDisplayModule);
		this.moduleManager.changeActiveState(alwaysDisplayModule.getId(), true);
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		
		this.moduleManager.hideAll();
		
		assertTrue(this.moduleManager.isDisplayed(alwaysDisplayModule.getId()));
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
	}
	
	@Test
	public void testHideExcept() {
		final var alwaysDisplayModule = new AlwaysDisplayModule();
		this.moduleManager.loadModule(alwaysDisplayModule);
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.moduleManager.changeActiveState(alwaysDisplayModule.getId(), true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.hideExcept(alwaysDisplayModule.getId());
		
		assertTrue(this.moduleManager.isDisplayed(alwaysDisplayModule.getId()));
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testHideExcept_MultipleExceptions() {
		final var alwaysDisplayModule = new AlwaysDisplayModule();
		final var highPriorityModule = new HighPriorityModule();
		this.moduleManager.loadModule(alwaysDisplayModule);
		this.moduleManager.loadModule(highPriorityModule);
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.moduleManager.changeActiveState(alwaysDisplayModule.getId(), true);
		this.moduleManager.changeActiveState(highPriorityModule.getId(), true);
		this.moduleManager.changeDisplayState(highPriorityModule.getId(), true);
		
		this.moduleManager.hideExcept(
				alwaysDisplayModule.getId(),
				highPriorityModule.getId()
		);
		
		assertTrue(this.moduleManager.isDisplayed(alwaysDisplayModule.getId()));
		assertTrue(this.moduleManager.isDisplayed(highPriorityModule.getId()));
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
	}
	
	@Test
	public void testRestoreVisibility() {
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		
		this.moduleManager.hideExcept();
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		this.testClientModule.onShowCalled = false;
		
		this.moduleManager.restoreVisibility();
		
		assertTrue(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		assertTrue(this.testClientModule.onShowCalled);
	}
	
	@Test
	public void testRestoreVisibility_NoState() {
		this.moduleManager.restoreVisibility();
		
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
	}
	
	@Test
	public void testIsModal() {
		assertFalse(this.moduleManager.isModal());
		
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.moduleManager.hideExcept();
		
		assertTrue(this.moduleManager.isModal());
		
		this.moduleManager.restoreVisibility();
		
		assertFalse(this.moduleManager.isModal());
	}
	
	@Test
	public void testModuleSorting_Priority() {
		final var highPriorityModule = new HighPriorityModule();
		this.moduleManager.loadModule(highPriorityModule);
		
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
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.testClientModule.onHideCalled = false;
		
		this.moduleManager.changeActiveState(this.testClientModule.getId(), false);
		
		assertFalse(this.moduleManager.isDisplayed(this.testClientModule.getId()));
		assertTrue(this.testClientModule.onHideCalled);
	}
	
	@Test
	public void testDeactivateAll_ClearsVisibilityStack() {
		this.moduleManager.changeActiveState(this.testClientModule.getId(), true);
		this.moduleManager.changeDisplayState(this.testClientModule.getId(), true);
		this.moduleManager.hideExcept();
		
		assertTrue(this.moduleManager.isModal());
		
		this.moduleManager.deactivateAll();
		
		assertFalse(this.moduleManager.isModal());
	}
	
	@Test
	public void testGetSubscribersForCallback_NoSubscribers() {
		final List<AbstractModule> subscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		
		assertTrue(subscribers.isEmpty());
	}
	
	@Test
	public void testGetSubscribersForCallback_WithSubscriber() {
		final var callbackSubscriberModule = new CallbackSubscriberModule();
		this.moduleManager.loadModule(callbackSubscriberModule);
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), true);
		
		final List<AbstractModule> subscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		
		assertEquals(1, subscribers.size());
		assertTrue(subscribers.stream().anyMatch(m -> m instanceof CallbackSubscriberModule));
	}
	
	@Test
	public void testGetSubscribersForCallback_MultipleCallbacks() {
		final var multiCallbackSubscriberModule = new MultiCallbackSubscriberModule();
		this.moduleManager.loadModule(multiCallbackSubscriberModule);
		this.moduleManager.changeActiveState(multiCallbackSubscriberModule.getId(), true);
		
		final List<AbstractModule> uiPreSubscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		final List<AbstractModule> runSubscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN);
		final List<AbstractModule> uiPostSubscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST);
		
		assertEquals(1, uiPreSubscribers.size());
		assertEquals(1, runSubscribers.size());
		assertEquals(1, uiPostSubscribers.size());
		
		assertTrue(uiPreSubscribers.get(0) instanceof MultiCallbackSubscriberModule);
		assertTrue(runSubscribers.get(0) instanceof MultiCallbackSubscriberModule);
		assertTrue(uiPostSubscribers.get(0) instanceof MultiCallbackSubscriberModule);
	}
	
	@Test
	public void testGetSubscribersForCallback_UnsubscribedCallbackEmpty() {
		final var callbackSubscriberModule = new CallbackSubscriberModule();
		this.moduleManager.loadModule(callbackSubscriberModule);
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), true);
		
		final List<AbstractModule> runSubscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN);
		
		assertTrue(runSubscribers.isEmpty());
	}
	
	@Test
	public void testGetSubscribersForCallback_RemovedOnDeactivate() {
		final var callbackSubscriberModule = new CallbackSubscriberModule();
		this.moduleManager.loadModule(callbackSubscriberModule);
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), true);
		
		List<AbstractModule> subscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		assertEquals(1, subscribers.size());
		
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), false);
		
		subscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		assertTrue(subscribers.isEmpty());
	}
	
	@Test
	public void testGetSubscribersForCallback_MultipleModules() {
		final var callbackSubscriberModule = new CallbackSubscriberModule();
		final var multiCallbackSubscriberModule = new MultiCallbackSubscriberModule();
		this.moduleManager.loadModule(callbackSubscriberModule);
		this.moduleManager.loadModule(multiCallbackSubscriberModule);
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), true);
		this.moduleManager.changeActiveState(multiCallbackSubscriberModule.getId(), true);
		
		final List<AbstractModule> uiPreSubscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		
		assertEquals(2, uiPreSubscribers.size());
		assertTrue(uiPreSubscribers.stream().anyMatch(m -> m instanceof CallbackSubscriberModule));
		assertTrue(uiPreSubscribers.stream().anyMatch(m -> m instanceof MultiCallbackSubscriberModule));
	}
	
	@Test
	public void testGetSubscribersForCallback_ReactivateModule() {
		final var callbackSubscriberModule = new CallbackSubscriberModule();
		this.moduleManager.loadModule(callbackSubscriberModule);
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), true);
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), false);
		
		List<AbstractModule> subscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		assertTrue(subscribers.isEmpty());
		
		this.moduleManager.changeActiveState(callbackSubscriberModule.getId(), true);
		
		subscribers = this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		assertEquals(1, subscribers.size());
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
	
	static class CallbackSubscriberModule extends AbstractModule {
		
		@Override
		public java.util.List<ModuleExecutorCallbacks> executorCallbackSubscriptions() {
			return java.util.List.of(ModuleExecutorCallbacks.UI_PRE);
		}
		
	}
	
	static class MultiCallbackSubscriberModule extends AbstractModule {
		
		@Override
		public java.util.List<ModuleExecutorCallbacks> executorCallbackSubscriptions() {
			return java.util.List.of(
				ModuleExecutorCallbacks.UI_PRE,
				ModuleExecutorCallbacks.RUN,
				ModuleExecutorCallbacks.UI_POST
			);
		}
		
	}
	
}
