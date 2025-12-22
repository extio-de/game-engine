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
	public void testExecute_CallsAllPhasesForActiveAndDisplayedClientModule() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getModulesActive()).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_CallsOnlyRunForActiveNonClientModule() {
		final AbstractModule module = mock(AbstractModule.class);
		when(this.moduleManager.getModulesActive()).thenReturn(List.of(module));

		this.moduleExecutor.execute();

		verify(module).run();
		verify(module, never()).onActivate(); // Just checking it doesn't call other things
	}

	@Test
	public void testExecute_CallsOnlyRunForActiveButNotDisplayedClientModule() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		when(this.moduleManager.getModulesActive()).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(clientModule, never()).runUiPre();
		verify(clientModule).run();
		verify(clientModule, never()).runUiPost();
	}

	@Test
	public void testExecute_HandlesExceptionInModule() {
		final AbstractModule module1 = mock(AbstractModule.class);
		final AbstractModule module2 = mock(AbstractModule.class);
		
		doThrow(new RuntimeException("Test Exception")).when(module1).run();
		when(this.moduleManager.getModulesActive()).thenReturn(List.of(module1, module2));

		this.moduleExecutor.execute();

		verify(module1).run();
		verify(module2).run();
	}

	@Test
	public void testExecute_HandlesExceptionInUiPre() {
		final AbstractClientModule clientModule = mock(AbstractClientModule.class);
		doThrow(new RuntimeException("UI Pre Exception")).when(clientModule).runUiPre();

		when(this.moduleManager.getModulesActive()).thenReturn(List.of(clientModule));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule));

		this.moduleExecutor.execute();

		verify(clientModule).runUiPre();
		verify(clientModule).run();
		verify(clientModule).runUiPost();
	}

	@Test
	public void testExecute_EmptyModules() {
		when(this.moduleManager.getModulesActive()).thenReturn(List.of());

		this.moduleExecutor.execute();

		verify(this.moduleManager, times(3)).getModulesActive();
	}

	@Test
	public void testExecute_MultipleModules() {
		final AbstractClientModule clientModule1 = mock(AbstractClientModule.class);
		final AbstractClientModule clientModule2 = mock(AbstractClientModule.class);
		final AbstractModule module1 = mock(AbstractModule.class);

		when(this.moduleManager.getModulesActive()).thenReturn(List.of(clientModule1, clientModule2, module1));
		when(this.moduleManager.getModulesDisplayedClientModules()).thenReturn(List.of(clientModule1));

		this.moduleExecutor.execute();

		// clientModule1: displayed, so all phases
		verify(clientModule1).runUiPre();
		verify(clientModule1).run();
		verify(clientModule1).runUiPost();

		// clientModule2: active but not displayed, so only run
		verify(clientModule2, never()).runUiPre();
		verify(clientModule2).run();
		verify(clientModule2, never()).runUiPost();

		// module1: only run
		verify(module1).run();
	}
}
