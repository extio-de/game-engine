package de.extio.game_engine.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.i18n.LocalizationService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.ScrollArea;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class OptionsModule extends AbstractClientModule implements OptionsModuleContext {

	private static final String TAB_GROUP = "OptionsModule_TabGroup";
	private static final int TAB_BUTTON_HEIGHT = 60;
	private static final int TAB_BUTTON_WIDTH = 220;
	private static final int CONTENT_START_Y = 100;

	private final ApplicationContext applicationContext;

	private final RenderingBoPool renderingBoPool;

	private final EventService eventService;

	private final LocalizationService localizationService;

	@Autowired
	private List<OptionsTab> discoveredTabs;

	OptionsModule(final ApplicationContext applicationContext, final RenderingBoPool renderingBoPool, final EventService eventService,
			final LocalizationService localizationService) {
		this.applicationContext = applicationContext;
		this.renderingBoPool = renderingBoPool;
		this.eventService = eventService;
		this.localizationService = localizationService;
	} 

	private List<OptionsTab> tabs = List.of();
	private Map<String, OptionsTab> tabsById = Map.of();
	private Map<String, OptionsTab> tabsByControlId = Map.of();

	private Window optionsWindow;
	private ScrollArea contentScrollArea;
	private String activeTabId;

	@Override
	public void onLoad() {
		this.initializeTabs();
		this.optionsWindow = this.applicationContext.getBean(Window.class);
		this.optionsWindow.setNormalizedDimension(RendererControl.REFERENCE_RESOLUTION.divide(9).multiply(7));
		this.optionsWindow.setNormalizedPosition(centeredPosition(this.optionsWindow.getNormalizedDimension()));
		this.optionsWindow.setDraggable(false);
		this.optionsWindow.setCloseButton(true);
		this.optionsWindow.setOnCloseAction(this::closeOptions);

		this.contentScrollArea = this.applicationContext.getBean(ScrollArea.class);
		this.optionsWindow.addComponent(this.contentScrollArea);
	}

	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.optionsWindow.getId());
	}

	@Override
	public void onActivate() {
		this.setupOptionsWindow();
		this.getModuleService().changeActiveState(this.optionsWindow.getId(), true);
		this.getModuleService().hideExcept(this.getId(), this.optionsWindow.getId());
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
		this.eventService.register(OptionsThemeEditorSavedEvent.class, this.getId(), this::onThemeEditorSaved);
		this.eventService.register(MouseClickEvent.class, this.getId(), event -> {
			if (event.isPressed() && (event.getButton() == 1 || event.getButton() == 3)) {
				this.refreshContent();
			}
		});
		this.eventService.register(OpenOptionsModuleEvent.class, this.getId(), event -> {
			if (event.tab() != null && !event.tab().isBlank()) {
				this.openTab(event.tab());
			} else {
				this.getModuleService().changeActiveState(this.getId(), true);
				this.getModuleService().changeDisplayState(this.getId(), true);
			}
		});
	}

	@Override
	public void onDeactivate() {
		this.eventService.unregisterAll(this.getId());
		this.getModuleService().changeActiveState(this.optionsWindow.getId(), false);
		this.getModuleService().restoreVisibility();
	}

	@Override
	public void onShow() {
		this.getModuleService().changeDisplayState(this.optionsWindow.getId(), true);
	}

	@Override
	public void onHide() {
		this.getModuleService().changeDisplayState(this.optionsWindow.getId(), false);
	}

	public void openTab(final String tabId) {
		this.initializeTabs();
		if (this.tabsById.containsKey(tabId)) {
			this.activeTabId = tabId;
		}
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}

	@Override
	public void rebuildWindow() {
		this.setupOptionsWindow();
	}

	@Override
	public void refreshContent() {
		for (final var boId : this.contentScrollArea.getRenderingBoIds().toArray(new String[0])) {
			this.contentScrollArea.removeRenderingBo(boId);
		}
		for (final var tab : this.tabs) {
			tab.onContentCleared(this);
		}
		this.activeTab().render(this);
		this.optionsWindow.draw();
	}

	@Override
	public Window optionsWindow() {
		return this.optionsWindow;
	}

	@Override
	public ScrollArea contentScrollArea() {
		return this.contentScrollArea;
	}

	private void setupOptionsWindow() {
		final var windowWidth = this.optionsWindow.getNormalizedDimension().getX();

		final var tabsY = Window.MARGIN_TOP;
		final var tabSpacing = 10;
		var tabX = Window.MARGIN_LEFT;

		for (final var tab : this.tabs) {
			if (!tab.visibleInNavigation()) {
				continue;
			}
			final var bo = this.renderingBoPool.acquire(tab.controlId(), ControlRenderingBo.class)
					.setCaption(this.localizationService.translate(tab.navigationLocalizationKey(), tab.fallbackCaption()))
					.setFontSize(36)
					.setType(ToggleButtonControl.class)
					.setControlGroup(TAB_GROUP)
					.setControlData(new ToggleButtonData(tab.tabId().equals(this.activeTabId), false, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT)
					.withPositionRelative(tabX, tabsY);
			this.optionsWindow.putRenderingBo(bo);
			tabX += TAB_BUTTON_WIDTH + tabSpacing;
		}

		final var bottomButtonsY = this.optionsWindow.getNormalizedDimension().getY() - Window.MARGIN_BOTTOM - 60;
		final var buttonWidth = 200;
		final var buttonHeight = 60;

		final var closeButton = this.renderingBoPool.acquire("OptionsModule_Button_Close", ControlRenderingBo.class)
				.setCaption(this.localizationService.translate("ecyoa-67", "Cancel"))
				.setFontSize(36)
				.setType(ButtonControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(buttonWidth, buttonHeight)
				.withPositionRelative(windowWidth - Window.MARGIN_RIGHT - buttonWidth, bottomButtonsY);
		this.optionsWindow.putRenderingBo(closeButton);

		this.contentScrollArea.setRelativePosition(ImmutableCoordI2.create(Window.MARGIN_LEFT, CONTENT_START_Y));
		this.contentScrollArea.setDimension(ImmutableCoordI2.create(
				windowWidth - Window.MARGIN_LEFT - Window.MARGIN_RIGHT,
				bottomButtonsY - CONTENT_START_Y - 20));

		this.refreshContent();
		this.optionsWindow.draw();
	}

	private void initializeTabs() {
		if (!this.tabs.isEmpty()) {
			return;
		}

		final var orderedTabs = new ArrayList<>(this.discoveredTabs);
		orderedTabs.sort(Comparator.comparingInt(OptionsTab::navigationOrder));
		this.tabs = List.copyOf(orderedTabs);

		final var tabsById = new LinkedHashMap<String, OptionsTab>();
		final var tabsByControlId = new LinkedHashMap<String, OptionsTab>();
		for (final var tab : this.tabs) {
			if (tabsById.put(tab.tabId(), tab) != null) {
				throw new IllegalStateException("Duplicate options tab id: " + tab.tabId());
			}
			if (tabsByControlId.put(tab.controlId(), tab) != null) {
				throw new IllegalStateException("Duplicate options tab control id: " + tab.controlId());
			}
		}
		this.tabsById = Map.copyOf(tabsById);
		this.tabsByControlId = Map.copyOf(tabsByControlId);

		if (this.activeTabId == null && !this.tabs.isEmpty()) {
			this.activeTabId = this.tabs.stream()
					.filter(OptionsTab::visibleInNavigation)
					.findFirst()
					.orElse(this.tabs.getFirst())
					.tabId();
		}
	}

	private OptionsTab activeTab() {
		this.initializeTabs();
		final var activeTab = this.tabsById.get(this.activeTabId);
		if (activeTab != null) {
			return activeTab;
		}
		return this.tabs.getFirst();
	}

	private void onUiControlEvent(final UiControlEvent event) {
		final var controlId = event.getId();
		if (controlId == null) {
			return;
		}

		final var selectedTab = this.tabsByControlId.get(controlId);
		if (selectedTab != null) {
			this.activeTabId = selectedTab.tabId();
			this.setupOptionsWindow();
			return;
		}

		switch (controlId) {
			case "OptionsModule_Button_Close" -> this.closeOptions();
			default -> this.activeTab().handle(event, this);
		}
	}

	private void onThemeEditorSaved(final OptionsThemeEditorSavedEvent event) {
		if (event.saved()) {
			this.refreshContent();
		}
	}

	private void closeOptions() {
		this.getModuleService().changeActiveState(this.getId(), false);
		this.getModuleService().changeDisplayState(this.getId(), false);
	}

	private static ImmutableCoordI2 centeredPosition(final CoordI2 dimension) {
		final var referenceResolution = RendererControl.REFERENCE_RESOLUTION;
		final var x = Math.max(0, (referenceResolution.getX() - dimension.getX()) / 2);
		final var y = Math.max(0, (referenceResolution.getY() - dimension.getY()) / 2);
		return ImmutableCoordI2.create(x, y);
	}
}