package de.extio.game_engine.module;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ModuleExecutorImplTest {

	@Mock
	private ModuleManager moduleManager;

	private ModuleExecutorImpl moduleExecutor;

	@BeforeEach
	public void setUp() {
		this.moduleExecutor = new ModuleExecutorImpl(this.moduleManager);
	}

	@Test
	public void testExecute_CallsAllPhasesForDisplayedClientModuleSubscribedToAllPhases() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_CallsOnlyRunForNonClientModule() {
		final AbstractModule module = mock(AbstractModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(module));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(module).run();
		verify(module, never()).onActivate();
	}

	@Test
	public void testExecute_SkipsUiPhaseForNotDisplayedClientModule() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(clientModule, never()).runUiPre();
		verify(clientModule).run();
		verify(clientModule, never()).runUiPost();
	}

	@Test
	public void testExecute_HandlesExceptionInRunPhase() {
		final AbstractModule module1 = mock(AbstractModule.class);
		final AbstractModule module2 = mock(AbstractModule.class);
		
		doThrow(new RuntimeException("Test Exception")).when(module1).run();
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(module1, module2));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(module1).run();
		verify(module2).run();
	}

	@Test
	public void testExecute_HandlesExceptionInUiPre() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		doThrow(new RuntimeException("UI Pre Exception")).when(clientModule).runUiPre();

		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_NoSubscribersForCallback() {
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(this.moduleManager).getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE);
		verify(this.moduleManager).getSubscribersForCallback(ModuleExecutorCallbacks.RUN);
		verify(this.moduleManager).getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST);
	}

	@Test
	public void testExecute_MultipleModulesWithMixedSubscriptions() {
		final AbstractClientModule clientModule1 = mock(AbstractClientModule.class);
		final AbstractClientModule clientModule2 = mock(AbstractClientModule.class);
		final AbstractModule module1 = mock(AbstractModule.class);

		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule1, clientModule2));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule1, clientModule2, module1));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule1));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule1));

		this.moduleExecutor.execute();

		verify(clientModule1).runUiPre();
		verify(clientModule1).run();
		verify(clientModule1).runUiPost();

		verify(clientModule2, never()).runUiPre();
		verify(clientModule2).run();
		verify(clientModule2, never()).runUiPost();

		verify(module1).run();
	}

	@Test
	public void testExecute_ModuleSubscribedOnlyToUiPre() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule, never()).run();
		verify(clientModule, never()).runUiPost();
	}

	@Test
	public void testExecute_ModuleSubscribedOnlyToRun() {
		final AbstractModule module = mock(AbstractModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(module));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(module).run();
	}

	@Test
	public void testExecute_ClientModuleSubscribedOnlyToUiPost() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule, never()).runUiPre();
		verify(clientModule, never()).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_ClientModuleSubscribedToUiPreAndRun_NotUiPost() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule, never()).runUiPost();
	}

	@Test
	public void testExecute_ClientModuleSubscribedToUiPreAndUiPost_NotRun() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule, never()).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_ClientModuleSubscribedToRunAndUiPost_NotUiPre() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule, never()).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_HandleExceptionInUiPost() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		doThrow(new RuntimeException("UI Post Exception")).when(clientModule).runUiPost();

		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_HandleExceptionInRun() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		doThrow(new RuntimeException("Run Exception")).when(clientModule).run();

		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_MultipleExceptionsInDifferentPhases() {
		final AbstractModule module1 = mock(AbstractModule.class);
		final AbstractModule module2 = mock(AbstractModule.class);
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);

		doThrow(new RuntimeException("Exception in module1")).when(module1).run();
		doThrow(new RuntimeException("Exception in clientModule")).when(clientModule).runUiPre();

		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(module1, module2));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(module1).run();
		verify(module2).run();
	}

	@Test
	public void testExecute_ClientModuleDisplayedButNotSubscribedToUiPhases() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(clientModule, never()).runUiPre();
		verify(clientModule).run();
		verify(clientModule, never()).runUiPost();
	}

	@Test
	public void testExecute_ClientModuleNotDisplayedButSubscribedToRunOnly() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of());
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(clientModule));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(clientModule, never()).runUiPre();
		verify(clientModule).run();
		verify(clientModule, never()).runUiPost();
	}

	@Test
	public void testExecute_MultipleNonClientModulesSubscribedToDifferentCallbacks() {
		final AbstractModule module1 = mock(AbstractModule.class);
		final AbstractModule module2 = mock(AbstractModule.class);
		final AbstractModule module3 = mock(AbstractModule.class);

		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_PRE)).thenReturn(List.of(module1));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.RUN)).thenReturn(List.of(module1, module2, module3));
		when(this.moduleManager.getSubscribersForCallback(ModuleExecutorCallbacks.UI_POST)).thenReturn(List.of(module3));

		this.moduleExecutor.execute();

		verify(module1).run();
		verify(module2).run();
		verify(module3).run();
	}
}
