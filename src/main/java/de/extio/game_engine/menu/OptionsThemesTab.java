package de.extio.game_engine.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import de.extio.game_engine.renderer.model.RenderingBoHasDimension;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.PopupMenuControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.PopupMenuData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.PopupMenuItem;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

final class OptionsThemesTab extends AbstractOptionsTab implements OptionsTab {

	static final String TAB_ID = "themes";
	private static final String CONTROL_ID = "OptionsModule_Tab_Themes";

	private static final List<String> CUSTOM_THEME_PATH = List.of("customThemes");
	private static final String CUSTOM_THEME_MENU_ID = "OptionsModule_CustomTheme_Menu";
	private static final String CUSTOM_THEME_MENU_SELECT = "OptionsModule_CustomThemeMenu_Select";
	private static final String CUSTOM_THEME_MENU_EDIT = "OptionsModule_CustomThemeMenu_Edit";
	private static final String CUSTOM_THEME_MENU_DELETE = "OptionsModule_CustomThemeMenu_Delete";

	@Autowired
	private ObjectProvider<OptionsThemeEditorSupport> themeEditorSupportProvider;

	private final Map<String, String> themeSelectionByControlId = new HashMap<>();
	private final Map<String, String> customThemeSelectionByControlId = new HashMap<>();
	private String activeCustomThemeName;

	@Override
	public String tabId() {
		return TAB_ID;
	}

	@Override
	public String controlId() {
		return CONTROL_ID;
	}

	@Override
	public String navigationLocalizationKey() {
		return "ecyoa-87";
	}

	@Override
	public String fallbackCaption() {
		return "Themes";
	}

	@Override
	public int navigationOrder() {
		return 20;
	}

	@Override
	public void render(final OptionsModuleContext context) {
		this.themeSelectionByControlId.clear();
		this.customThemeSelectionByControlId.clear();

		final var themeEditorSupport = this.themeEditorSupportProvider.getIfAvailable();
		final var customThemeNames = this.getCustomThemeNames();
		final var themeNames = this.applicationContext().getBeansOfType(Theme.class).values().stream()
				.map(Theme::getName)
				.sorted()
				.toList();
		final var currentTheme = this.themeManager().getCurrentTheme();
		final var currentThemeName = currentTheme != null ? currentTheme.getName() : null;

		var yPos = 10;

		var bo = this.renderingBoPool().acquire("OptionsModule_Themes_Label_Header", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-103", "Available Themes:"))
				.setFontSize(42)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 80)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		final var columnWidth = 330;
		final var columnSpacing = 20;
		var xPos = 10;
		var columnIndex = 0;
		var emptyRow = false;

		for (final var themeName : themeNames) {
			emptyRow = false;
			final var controlId = "OptionsModule_Theme_" + sanitizeId(themeName);
			this.themeSelectionByControlId.put(controlId, themeName);

			bo = this.renderingBoPool().acquire(controlId, ControlRenderingBo.class)
					.setCaption(themeName)
					.setFontSize(32)
					.setType(ToggleButtonControl.class)
					.setControlGroup("OptionsModule_ThemeGroup")
					.setControlData(new ToggleButtonData(themeName.equals(currentThemeName), true, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(columnWidth, 60)
					.withPositionRelative(xPos, yPos);
			this.contentScrollArea(context).putRenderingBo(bo);

			columnIndex++;
			if (columnIndex >= 4) {
				columnIndex = 0;
				xPos = 10;
				yPos += 70;
				emptyRow = true;
			}
			else {
				xPos += columnWidth + columnSpacing;
			}
		}

		yPos += emptyRow ? 20 : 90;
		xPos = 10;
		columnIndex = 0;

		bo = this.renderingBoPool().acquire("OptionsModule_Themes_Label_Custom", ControlRenderingBo.class)
				.setCaption("Custom Themes")
				.setFontSize(40)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(600, 70)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		for (final var themeName : customThemeNames) {
			final var controlId = "OptionsModule_CustomTheme_" + sanitizeId(themeName);
			this.customThemeSelectionByControlId.put(controlId, themeName);

			bo = this.renderingBoPool().acquire(controlId, ControlRenderingBo.class)
					.setCaption(themeName)
					.setFontSize(32)
					.setType(ToggleButtonControl.class)
					.setControlGroup("OptionsModule_CustomThemeGroup")
					.setControlData(new ToggleButtonData(themeName.equals(currentThemeName), true, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(columnWidth, 60)
					.withPositionRelative(xPos, yPos);
			this.contentScrollArea(context).putRenderingBo(bo);

			columnIndex++;
			if (columnIndex >= 4) {
				columnIndex = 0;
				xPos = 10;
				yPos += 70;
			}
			else {
				xPos += columnWidth + columnSpacing;
			}
		}

		if (themeEditorSupport != null) {
			final var addBo = this.renderingBoPool().acquire("OptionsModule_CustomTheme_Add", ControlRenderingBo.class)
					.setCaption("+")
					.setFontSize(36)
					.setType(ButtonControl.class)
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(columnWidth, 60)
					.withPositionRelative(xPos, yPos);
			this.contentScrollArea(context).putRenderingBo(addBo);
		}
	}

	@Override
	public boolean handle(final UiControlEvent event, final OptionsModuleContext context) {
		final var controlId = event.getId();
		if (controlId == null) {
			return false;
		}

		switch (controlId) {
			case "OptionsModule_CustomTheme_Add" -> {
				this.openNewThemeEditor(context);
				return true;
			}
			case CUSTOM_THEME_MENU_ID -> {
				this.handleCustomThemeMenuSelection(event.getPayload(), context);
				return true;
			}
			default -> {
			}
		}

		final var customThemeName = this.customThemeSelectionByControlId.get(controlId);
		if (customThemeName != null) {
			final var bo = this.optionsWindow(context).getRenderingBo(controlId, ControlRenderingBo.class);
			if (bo != null) {
				this.showCustomThemeMenu(bo, customThemeName, context);
			}
			return true;
		}

		final var themeName = this.themeSelectionByControlId.get(controlId);
		if (themeName != null) {
			this.themeManager().setCurrentTheme(themeName);
			this.refreshContent(context);
			return true;
		}

		return false;
	}

	@Override
	public void onContentCleared(final OptionsModuleContext context) {
		this.hideCustomThemeMenu(context);
		this.themeSelectionByControlId.clear();
		this.customThemeSelectionByControlId.clear();
	}

	private List<String> getCustomThemeNames() {
		final var descriptors = this.storageService().listPath(CUSTOM_THEME_PATH, false);
		final var names = new ArrayList<String>(descriptors.size());
		for (final var descriptor : descriptors) {
			if (descriptor != null && descriptor.name() != null && !descriptor.name().isBlank()) {
				names.add(descriptor.name());
			}
		}
		names.sort(String::compareToIgnoreCase);
		return List.copyOf(names);
	}

	private void showCustomThemeMenu(final ControlRenderingBo bo, final String themeName, final OptionsModuleContext context) {
		if (themeName.equals(this.activeCustomThemeName)
				&& this.optionsWindow(context).getRenderingBo(CUSTOM_THEME_MENU_ID, ControlRenderingBo.class) != null) {
			this.hideCustomThemeMenu(context);
			final var controlId = "OptionsModule_CustomTheme_" + sanitizeId(themeName);
			final var control = this.optionsWindow(context).getRenderingBo(controlId, ControlRenderingBo.class);
			if (control != null) {
				final var currentTheme = this.themeManager().getCurrentTheme();
				control.setControlData(new ToggleButtonData(currentTheme != null && themeName.equals(currentTheme.getName()), true, null));
				this.optionsWindow(context).putRenderingBo(control);
			}
			this.optionsWindow(context).draw();
			return;
		}

		this.hideCustomThemeMenu(context);
		this.activeCustomThemeName = themeName;

		if (!(bo instanceof final RenderingBoHasDimension boDim)) {
			return;
		}

		final var items = new ArrayList<PopupMenuItem>();
		items.add(new PopupMenuItem(CUSTOM_THEME_MENU_SELECT, "Select", true));
		if (this.themeEditorSupportProvider.getIfAvailable() != null) {
			items.add(new PopupMenuItem(CUSTOM_THEME_MENU_EDIT, "Edit", true));
		}
		items.add(new PopupMenuItem(CUSTOM_THEME_MENU_DELETE, "Delete", true));

		final var rowHeight = 32;
		final var padding = 8;
		final var width = Math.min(240, boDim.getWidth());
		final var height = (rowHeight * items.size()) + (padding * 2);
		final var offset = this.contentScrollArea(context).getRelativePosition();
		final var x = offset.getX() + bo.getLocalX();
		final var y = offset.getY() + bo.getLocalY() + boDim.getHeight() + 6;

		final var data = new PopupMenuData(List.copyOf(items), null, null, null, rowHeight, padding);
		final var popup = this.renderingBoPool().acquire(CUSTOM_THEME_MENU_ID, ControlRenderingBo.class)
				.setType(PopupMenuControl.class)
				.setFontSize(22)
				.setVisible(true)
				.setEnabled(true)
				.setControlData(data)
				.withDimensionAbsolute(width, height)
				.setLayer(RenderingBoLayer.UI1)
				.withPositionRelative(x, y);
		this.optionsWindow(context).putRenderingBo(popup);
		this.optionsWindow(context).draw();
	}

	private void handleCustomThemeMenuSelection(final Object payload, final OptionsModuleContext context) {
		if (this.activeCustomThemeName == null) {
			return;
		}

		final String actionId;
		if (payload instanceof final String id) {
			actionId = id;
		}
		else if (payload instanceof final PopupMenuItem item) {
			actionId = item.id();
		}
		else {
			return;
		}

		switch (actionId) {
			case CUSTOM_THEME_MENU_SELECT -> this.applyCustomTheme(this.activeCustomThemeName, context);
			case CUSTOM_THEME_MENU_EDIT -> this.editCustomTheme(this.activeCustomThemeName, context);
			case CUSTOM_THEME_MENU_DELETE -> this.deleteCustomTheme(this.activeCustomThemeName, context);
			default -> {
				return;
			}
		}

		this.refreshContent(context);
	}

	private void hideCustomThemeMenu(final OptionsModuleContext context) {
		this.optionsWindow(context).removeRenderingBo(CUSTOM_THEME_MENU_ID);
		this.activeCustomThemeName = null;
	}

	private void applyCustomTheme(final String themeName, final OptionsModuleContext context) {
		final var theme = this.loadCustomTheme(themeName);
		if (theme == null) {
			return;
		}
		this.themeManager().setCurrentTheme(theme);
		this.refreshContent(context);
	}

	private void editCustomTheme(final String themeName, final OptionsModuleContext context) {
		final var themeEditorSupport = this.themeEditorSupportProvider.getIfAvailable();
		if (themeEditorSupport == null) {
			return;
		}
		final var theme = this.loadCustomTheme(themeName);
		if (theme == null) {
			return;
		}
		themeEditorSupport.openEdit(theme, this.optionsWindow(context));
	}

	private void deleteCustomTheme(final String themeName, final OptionsModuleContext context) {
		this.storageService().deleteByPath(CUSTOM_THEME_PATH, themeName);
		final var currentTheme = this.themeManager().getCurrentTheme();
		if (currentTheme != null && themeName.equalsIgnoreCase(currentTheme.getName())) {
			this.themeManager().setCurrentTheme((String) null);
		}
		this.refreshContent(context);
	}

	private Theme loadCustomTheme(final String themeName) {
		return this.storageService().loadByPath(Theme.class, CUSTOM_THEME_PATH, themeName).orElse(null);
	}

	private void openNewThemeEditor(final OptionsModuleContext context) {
		final var themeEditorSupport = this.themeEditorSupportProvider.getIfAvailable();
		if (themeEditorSupport == null) {
			return;
		}
		final var name = this.generateNewThemeName();
		themeEditorSupport.openNew(this.themeManager().getCurrentTheme(), name, this.optionsWindow(context));
	}

	private String generateNewThemeName() {
		final var existing = this.getCustomThemeNames();
		final var base = "Custom Theme";
		if (!existing.contains(base)) {
			return base;
		}
		var index = 2;
		while (existing.contains(base + " " + index)) {
			index++;
		}
		return base + " " + index;
	}
}