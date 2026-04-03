package de.extio.game_engine.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.event.EventService;
import de.extio.game_engine.menu.HsbColorPickerModule.HsbColorPickerResponseEvent;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.ThemeManager;
import de.extio.game_engine.renderer.container.ScrollArea;
import de.extio.game_engine.renderer.container.Window;
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
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldData;
import de.extio.game_engine.renderer.model.bo.DrawEffectRenderingBo;
import de.extio.game_engine.renderer.model.bo.DrawEffectRenderingBoEffects;
import de.extio.game_engine.renderer.model.bo.DrawFontRenderingBo;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.color.HSBColor;
import de.extio.game_engine.renderer.model.color.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.event.MouseClickEvent;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.storage.StorageService;

public class ThemeEditorModule extends AbstractClientModule implements OptionsThemeEditorSupport {
	
	private static final List<String> CUSTOM_THEME_PATH = List.of("customThemes");
	
	private static final int WINDOW_WIDTH = 1200;
	
	private static final int WINDOW_HEIGHT = 820;
	
	private static final int ROW_HEIGHT = 60;
	
	private static final int LABEL_WIDTH = 320;
	
	private static final int CONTROL_WIDTH = 620;
	
	private static final int VALUE_WIDTH = 120;
	
	private static final int SPACING = 14;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private RenderingBoPool renderingBoPool;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private ThemeManager themeManager;
	
	@Autowired
	private StorageService storageService;
	
	@Autowired
	private StaticResourceService staticResourceService;
	
	@Autowired
	private HsbColorPickerModule hsbColorPickerModule;
	
	private Window editorWindow;
	
	private ScrollArea scrollArea;
	
	private Theme originalTheme;
	
	private String originalName;
	
	private String name;
	
	private String patternRendererName;
	
	private String font;
	
	private HSBColor borderOuter;
	
	private HSBColor borderInner;
	
	private HSBColor borderInnerDisabled;
	
	private HSBColor backgroundNormal;
	
	private HSBColor backgroundSelected;
	
	private HSBColor textNormal;
	
	private HSBColor textDisabled;
	
	private HSBColor selectionPrimary;
	
	private HSBColor selectionSecondary;
	
	private HSBColor windowBackground;
	
	private float hoverBrightnessAdjustment;
	
	private float pressedBrightnessAdjustment;
	
	private final Map<String, ColorSlot> colorSlotByControlId = new HashMap<>();
	
	private PopupTarget popupTarget;
	
	private String pendingColorRequestId;
	
	@Override
	public void onLoad() {
		this.editorWindow = this.applicationContext.getBean(Window.class);
		this.editorWindow.setNormalizedDimension(ImmutableCoordI2.create(WINDOW_WIDTH, WINDOW_HEIGHT));
		this.editorWindow.setNormalizedPosition(this.centeredPosition(this.editorWindow.getNormalizedDimension()));
		this.editorWindow.setDraggable(true);
		this.editorWindow.setCloseButton(true);
		this.editorWindow.setOnCloseAction(this::onCancel);
		
		this.scrollArea = this.applicationContext.getBean(ScrollArea.class);
		this.editorWindow.addComponent(this.scrollArea);
	}
	
	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.editorWindow.getId());
	}
	
	@Override
	public void onActivate() {
		this.setupEditor();
		this.getModuleService().changeActiveState(this.editorWindow.getId(), true);
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
		this.eventService.register(HsbColorPickerResponseEvent.class, this.getId(), this::onColorPickerResponse);
		this.eventService.register(MouseClickEvent.class, this.getId(), event -> {
			if (event.isPressed() && (event.getButton() == 1 || event.getButton() == 3) && this.popupTarget != null) {
				this.hidePopup();
				this.editorWindow.draw();
			}
		});
	}
	
	@Override
	public void onDeactivate() {
		this.eventService.unregisterAll(this.getId());
		this.getModuleService().changeActiveState(this.editorWindow.getId(), false);
	}
	
	@Override
	public void onShow() {
		this.getModuleService().changeDisplayState(this.editorWindow.getId(), true);
	}
	
	@Override
	public void onHide() {
		this.getModuleService().changeDisplayState(this.editorWindow.getId(), false);
	}
	
	@Override
	public void openNew(final Theme baseTheme, final String proposedName, final Window parentWindow) {
		this.originalTheme = this.themeManager.getCurrentTheme();
		final var template = baseTheme != null ? baseTheme : this.themeManager.getCurrentTheme();
		this.originalName = null;
		this.name = proposedName;
		this.initializeFromTheme(template);
		// apply the template theme immediately for preview
		this.applyCurrentThemePreview();
		this.editorWindow.setParent(parentWindow);
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}
	
	@Override
	public void openEdit(final Theme theme, final Window parentWindow) {
		if (theme == null) {
			return;
		}
		this.originalTheme = this.themeManager.getCurrentTheme();
		this.originalName = theme.getName();
		this.name = theme.getName();
		this.initializeFromTheme(theme);
		// apply the selected theme immediately for preview
		this.applyCurrentThemePreview();
		this.editorWindow.setParent(parentWindow);
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}
	
	private void initializeFromTheme(final Theme theme) {
		this.patternRendererName = theme.getPatternRendererName();
		this.font = theme.getFont();
		this.borderOuter = theme.getBorderOuter();
		this.borderInner = theme.getBorderInner();
		this.borderInnerDisabled = theme.getBorderInnerDisabled();
		this.backgroundNormal = theme.getBackgroundNormal();
		this.backgroundSelected = theme.getBackgroundSelected();
		this.textNormal = theme.getTextNormal();
		this.textDisabled = theme.getTextDisabled();
		this.selectionPrimary = theme.getSelectionPrimary();
		this.selectionSecondary = theme.getSelectionSecondary();
		this.windowBackground = theme.getWindowBackground();
		this.hoverBrightnessAdjustment = theme.getHoverBrightnessAdjustment();
		this.pressedBrightnessAdjustment = theme.getPressedBrightnessAdjustment();
	}
	
	private void setupEditor() {
		this.editorWindow.clearRenderingBos();
		for (final var boId : this.scrollArea.getRenderingBoIds().toArray(new String[0])) {
			this.scrollArea.removeRenderingBo(boId);
		}
		this.colorSlotByControlId.clear();
		this.popupTarget = null;
		this.pendingColorRequestId = null;
		
		final var contentWidth = WINDOW_WIDTH - Window.MARGIN_LEFT - Window.MARGIN_RIGHT;
		final var bottomButtonsY = WINDOW_HEIGHT - Window.MARGIN_BOTTOM - 60;
		
		this.scrollArea.setRelativePosition(ImmutableCoordI2.create(Window.MARGIN_LEFT, Window.MARGIN_TOP));
		this.scrollArea.setDimension(ImmutableCoordI2.create(contentWidth, bottomButtonsY - Window.MARGIN_TOP - SPACING));
		
		var yPos = 10;
		
		var bo = this.renderingBoPool.acquire("ThemeEditor_Label_Header", DrawFontRenderingBo.class)
				.setText("Theme Editor")
				.setSize(36)
				.setAlignment(HorizontalAlignment.LEFT)
				.withDimensionAbsolute(600, 60)
				.withPositionRelative(10, yPos);
		this.scrollArea.putRenderingBo(bo);
		
		yPos += 80;
		
		yPos = this.addTextFieldRow("Name", "ThemeEditor_Field_Name", this.name, yPos);
		yPos = this.addPopupRow("Pattern Renderer", "ThemeEditor_Field_Pattern", this.patternRendererName, yPos);
		yPos = this.addPopupRow("Font", "ThemeEditor_Field_Font", this.font, yPos);
		yPos = this.addSliderRow("Hover Brightness", "ThemeEditor_Field_Hover", this.hoverBrightnessAdjustment, yPos);
		yPos = this.addSliderRow("Pressed Brightness", "ThemeEditor_Field_Pressed", this.pressedBrightnessAdjustment, yPos);
		
		yPos += 20;
		
		yPos = this.addColorRow("Border Outer", "ThemeEditor_Color_BorderOuter", this.borderOuter, yPos);
		yPos = this.addColorRow("Border Inner", "ThemeEditor_Color_BorderInner", this.borderInner, yPos);
		yPos = this.addColorRow("Border Inner Disabled", "ThemeEditor_Color_BorderInnerDisabled", this.borderInnerDisabled, yPos);
		yPos = this.addColorRow("Background Normal", "ThemeEditor_Color_BackgroundNormal", this.backgroundNormal, yPos);
		yPos = this.addColorRow("Background Selected", "ThemeEditor_Color_BackgroundSelected", this.backgroundSelected, yPos);
		yPos = this.addColorRow("Text Normal", "ThemeEditor_Color_TextNormal", this.textNormal, yPos);
		yPos = this.addColorRow("Text Disabled", "ThemeEditor_Color_TextDisabled", this.textDisabled, yPos);
		yPos = this.addColorRow("Selection Primary", "ThemeEditor_Color_SelectionPrimary", this.selectionPrimary, yPos);
		yPos = this.addColorRow("Selection Secondary", "ThemeEditor_Color_SelectionSecondary", this.selectionSecondary, yPos);
		yPos = this.addColorRow("Window Background", "ThemeEditor_Color_WindowBackground", this.windowBackground, yPos);
		
		final var buttonWidth = 200;
		final var buttonHeight = 60;
		
		bo = this.renderingBoPool.acquire("ThemeEditor_Button_Save", ControlRenderingBo.class)
				.setCaption("Save")
				.setFontSize(32)
				.setType(ButtonControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(buttonWidth, buttonHeight)
				.withPositionRelative(WINDOW_WIDTH - Window.MARGIN_RIGHT - buttonWidth * 2 - SPACING, bottomButtonsY);
		this.editorWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire("ThemeEditor_Button_Cancel", ControlRenderingBo.class)
				.setCaption("Cancel")
				.setFontSize(32)
				.setType(ButtonControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(buttonWidth, buttonHeight)
				.withPositionRelative(WINDOW_WIDTH - Window.MARGIN_RIGHT - buttonWidth, bottomButtonsY);
		this.editorWindow.putRenderingBo(bo);
		
		this.editorWindow.draw();
	}
	
	private int addTextFieldRow(final String label, final String fieldId, final String value, final int yPos) {
		var bo = this.renderingBoPool.acquire("ThemeEditor_Label_" + fieldId, ControlRenderingBo.class)
				.setCaption(label)
				.setFontSize(30)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(LABEL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10, yPos + 10);
		this.scrollArea.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire(fieldId, ControlRenderingBo.class)
				.setCaption(value != null ? value : "")
				.setFontSize(28)
				.setType(TextfieldControl.class)
				.setControlData(new TextfieldData(false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(CONTROL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10 + LABEL_WIDTH + SPACING, yPos);
		this.scrollArea.putRenderingBo(bo);
		
		return yPos + ROW_HEIGHT + SPACING;
	}
	
	private int addPopupRow(final String label, final String fieldId, final String value, final int yPos) {
		var bo = this.renderingBoPool.acquire("ThemeEditor_Label_" + fieldId, ControlRenderingBo.class)
				.setCaption(label)
				.setFontSize(30)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(LABEL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10, yPos + 10);
		this.scrollArea.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire(fieldId, ControlRenderingBo.class)
				.setCaption(value != null ? value : "")
				.setFontSize(28)
				.setType(ButtonControl.class)
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(CONTROL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10 + LABEL_WIDTH + SPACING, yPos);
		this.scrollArea.putRenderingBo(bo);
		
		return yPos + ROW_HEIGHT + SPACING;
	}
	
	private int addSliderRow(final String label, final String fieldId, final float value, final int yPos) {
		var bo = this.renderingBoPool.acquire("ThemeEditor_Label_" + fieldId, ControlRenderingBo.class)
				.setCaption(label)
				.setFontSize(30)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(LABEL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10, yPos + 10);
		this.scrollArea.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire(fieldId, ControlRenderingBo.class)
				.setType(SliderControl.class)
				.setControlData(new SliderData(true, value, value, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(CONTROL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10 + LABEL_WIDTH + SPACING, yPos);
		this.scrollArea.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire(fieldId + "_Value", ControlRenderingBo.class)
				.setCaption(this.formatValue(value))
				.setFontSize(26)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(VALUE_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10 + LABEL_WIDTH + SPACING + CONTROL_WIDTH + SPACING, yPos + 10);
		this.scrollArea.putRenderingBo(bo);
		
		return yPos + ROW_HEIGHT + SPACING;
	}
	
	private int addColorRow(final String label, final String fieldId, final HSBColor color, final int yPos) {
		var bo = this.renderingBoPool.acquire("ThemeEditor_Label_" + fieldId, ControlRenderingBo.class)
				.setCaption(label)
				.setFontSize(30)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(LABEL_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10, yPos + 10);
		this.scrollArea.putRenderingBo(bo);
		
		final int rectWidth = CONTROL_WIDTH - VALUE_WIDTH - SPACING;
		final DrawEffectRenderingBo rectBo = this.renderingBoPool.acquire("ThemeEditor_ColorRect_" + fieldId, DrawEffectRenderingBo.class);
		rectBo.setEffect(DrawEffectRenderingBoEffects.RECT_FILLED);
		rectBo.setRelativeCoordinates(List.of(ImmutableCoordI2.create(rectWidth, ROW_HEIGHT)));
		rectBo.setColor(this.toRgbaColor(color));
		rectBo.withPositionRelative(10 + LABEL_WIDTH + SPACING, yPos);
		this.scrollArea.putRenderingBo(rectBo);
		
		bo = this.renderingBoPool.acquire(fieldId, ControlRenderingBo.class)
				.setCaption("Edit")
				.setFontSize(26)
				.setType(ButtonControl.class)
				.setControlData(new ControlRenderingBo.ButtonData(null, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(VALUE_WIDTH, ROW_HEIGHT)
				.withPositionRelative(10 + LABEL_WIDTH + SPACING + rectWidth + SPACING, yPos);
		this.scrollArea.putRenderingBo(bo);
		
		this.colorSlotByControlId.put(fieldId, this.resolveColorSlot(fieldId));
		
		return yPos + ROW_HEIGHT + SPACING;
	}
	
	private void onUiControlEvent(final UiControlEvent event) {
		if (event.getId() == null) {
			return;
		}
		
		switch (event.getId()) {
			case "ThemeEditor_Button_Save" -> this.onSave();
			case "ThemeEditor_Button_Cancel" -> this.onCancel();
			case "ThemeEditor_Field_Name" -> this.onNameChanged(event.getPayload());
			case "ThemeEditor_Field_Pattern" -> this.openPatternPopup();
			case "ThemeEditor_Field_Font" -> this.openFontPopup();
			case "ThemeEditor_Field_Hover" -> this.onHoverChanged(event.getPayload());
			case "ThemeEditor_Field_Pressed" -> this.onPressedChanged(event.getPayload());
			case "ThemeEditor_Popup_Menu" -> this.onPopupSelected(event.getPayload());
			default -> this.handleColorButton(event.getId());
		}
	}
	
	private void onNameChanged(final Object payload) {
		if (payload instanceof final String text) {
			this.name = text.trim();
		}
	}
	
	private void onHoverChanged(final Object payload) {
		if (payload instanceof final Double value) {
			this.hoverBrightnessAdjustment = this.clamp01(value.floatValue());
			this.updateValueLabel("ThemeEditor_Field_Hover_Value", this.hoverBrightnessAdjustment);
			this.updateSliderValue("ThemeEditor_Field_Hover", value);
			this.editorWindow.draw();
			this.applyCurrentThemePreview();
		}
	}
	
	private void onPressedChanged(final Object payload) {
		if (payload instanceof final Double value) {
			this.pressedBrightnessAdjustment = this.clamp01(value.floatValue());
			this.updateValueLabel("ThemeEditor_Field_Pressed_Value", this.pressedBrightnessAdjustment);
			this.updateSliderValue("ThemeEditor_Field_Pressed", value);
			this.editorWindow.draw();
			this.applyCurrentThemePreview();
		}
	}
	
	private void handleColorButton(final String controlId) {
		final var slot = this.colorSlotByControlId.get(controlId);
		if (slot == null) {
			return;
		}
		final var color = this.getColor(slot);
		final var requestId = "ThemeEditor_ColorPicker_" + slot.name();
		this.pendingColorRequestId = requestId;
		this.hsbColorPickerModule.open(requestId, slot.displayName, color, this.editorWindow);
	}
	
	private void onColorPickerResponse(final HsbColorPickerResponseEvent event) {
		if (this.pendingColorRequestId == null || !this.pendingColorRequestId.equals(event.requestId())) {
			return;
		}
		if (!event.confirmed() || event.color() == null) {
			return;
		}
		final var slot = ColorSlot.valueOf(event.requestId().substring("ThemeEditor_ColorPicker_".length()));
		this.setColor(slot, event.color());
		this.updateColorButton(slot);
		this.applyCurrentThemePreview();
		this.editorWindow.draw();
	}
	
	private void openPatternPopup() {
		final List<String> items = this.themeManager.getPatternRendererNames();
		if (this.popupTarget == null) {
			this.popupTarget = PopupTarget.PATTERN_RENDERER;
			this.showPopup(items, "ThemeEditor_Field_Pattern");
		}
		else {
			this.hidePopup();
			this.editorWindow.draw();
		}
	}
	
	private void openFontPopup() {
		final List<String> items = this.getAvailableFonts();
		if (this.popupTarget == null) {
			this.popupTarget = PopupTarget.FONT;
			this.showPopup(items, "ThemeEditor_Field_Font");
		}
		else {
			this.hidePopup();
			this.editorWindow.draw();
		}
	}
	
	private void onPopupSelected(final Object payload) {
		final String value;
		if (payload instanceof final String id) {
			value = id;
		}
		else if (payload instanceof final PopupMenuItem item) {
			value = item.id();
		}
		else {
			return;
		}
		if (this.popupTarget == PopupTarget.PATTERN_RENDERER) {
			this.patternRendererName = value;
			this.updateButtonCaption("ThemeEditor_Field_Pattern", value);
		}
		else if (this.popupTarget == PopupTarget.FONT) {
			this.font = value;
			this.updateButtonCaption("ThemeEditor_Field_Font", value);
		}
		
		this.hidePopup();
		this.editorWindow.draw();
		this.applyCurrentThemePreview();
	}
	
	private void showPopup(final List<String> items, final String anchorId) {
		if (items == null || items.isEmpty()) {
			return;
		}
		final var bo = this.editorWindow.getRenderingBo(anchorId, ControlRenderingBo.class);
		if (!(bo instanceof final RenderingBoHasDimension boDim)) {
			return;
		}
		final var popupItems = items.stream()
				.map(item -> new PopupMenuItem(item, item, true))
				.toList();
		final var rowHeight = 32;
		final var padding = 8;
		final var width = Math.min(420, boDim.getWidth());
		final var height = (rowHeight * popupItems.size()) + (padding * 2);
		
		final var offset = this.scrollArea.getRelativePosition();
		final var x = offset.getX() + bo.getLocalX();
		final var y = offset.getY() + bo.getLocalY() + boDim.getHeight() + 6;
		
		final var data = new PopupMenuData(popupItems, null, null, null, rowHeight, padding);
		final var popup = this.renderingBoPool.acquire("ThemeEditor_Popup_Menu", ControlRenderingBo.class)
				.setType(PopupMenuControl.class)
				.setFontSize(22)
				.setVisible(true)
				.setEnabled(true)
				.setControlData(data)
				.withDimensionAbsolute(width, height)
				.setLayer(RenderingBoLayer.UI1)
				.withPositionRelative(x, y);
		this.editorWindow.putRenderingBo(popup);
		this.editorWindow.draw();
	}
	
	private void hidePopup() {
		this.editorWindow.removeRenderingBo("ThemeEditor_Popup_Menu");
		this.popupTarget = null;
	}
	
	private void updateButtonCaption(final String id, final String caption) {
		final var bo = this.editorWindow.getRenderingBo(id, ControlRenderingBo.class);
		if (bo != null) {
			bo.setCaption(caption != null ? caption : "");
			this.editorWindow.putRenderingBo(bo);
		}
	}
	
	private void updateValueLabel(final String id, final float value) {
		final var bo = this.editorWindow.getRenderingBo(id, ControlRenderingBo.class);
		if (bo != null) {
			bo.setCaption(this.formatValue(value));
			this.editorWindow.putRenderingBo(bo);
		}
	}
	
	private void updateSliderValue(final String id, final double value) {
		final var bo = this.editorWindow.getRenderingBo(id, ControlRenderingBo.class);
		if (bo != null) {
			final var sliderData = new SliderData(true, value, value, null);
			bo.setControlData(sliderData);
			this.editorWindow.putRenderingBo(bo);
		}
	}
	
	private void updateColorButton(final ColorSlot slot) {
		final var controlId = slot.controlId;
		final var rectBo = this.editorWindow.getRenderingBo("ThemeEditor_ColorRect_" + controlId, DrawEffectRenderingBo.class);
		if (rectBo != null) {
			rectBo.setColor(this.toRgbaColor(this.getColor(slot)));
			this.editorWindow.putRenderingBo(rectBo);
		}
	}
	
	private List<String> getAvailableFonts() {
		final var fonts = new ArrayList<String>();
		for (final var name : this.staticResourceService.listPath(List.of("renderer"))) {
			final var lower = name.toLowerCase();
			if (lower.endsWith(".ttf") || lower.endsWith(".otf")) {
				fonts.add(name);
			}
		}
		fonts.sort(String::compareToIgnoreCase);
		return fonts;
	}
	
	private void onSave() {
		if (this.name == null || this.name.isBlank()) {
			return;
		}
		final var theme = new Theme(
				this.name.trim(),
				this.patternRendererName,
				this.font,
				this.borderOuter,
				this.borderInner,
				this.borderInnerDisabled,
				this.backgroundNormal,
				this.backgroundSelected,
				this.textNormal,
				this.textDisabled,
				this.selectionPrimary,
				this.selectionSecondary,
				this.windowBackground,
				this.hoverBrightnessAdjustment,
				this.pressedBrightnessAdjustment);
		this.storageService.store(CUSTOM_THEME_PATH, theme.getName(), theme);
		if (this.originalName != null && !this.originalName.equalsIgnoreCase(theme.getName())) {
			this.storageService.deleteByPath(CUSTOM_THEME_PATH, this.originalName);
		}
		this.themeManager.setCurrentTheme(theme);
		this.eventService.fire(new OptionsThemeEditorSavedEvent(true, theme));
		this.close();
	}
	
	private void onCancel() {
		if (this.originalTheme != null) {
			this.themeManager.setCurrentTheme(this.originalTheme);
		}
		this.eventService.fire(new OptionsThemeEditorSavedEvent(false, null));
		this.close();
	}
	
	private void applyCurrentThemePreview() {
		final var previewTheme = new Theme(
				this.name != null && !this.name.isBlank() ? this.name : "Preview",
				this.patternRendererName,
				this.font,
				this.borderOuter,
				this.borderInner,
				this.borderInnerDisabled,
				this.backgroundNormal,
				this.backgroundSelected,
				this.textNormal,
				this.textDisabled,
				this.selectionPrimary,
				this.selectionSecondary,
				this.windowBackground,
				this.hoverBrightnessAdjustment,
				this.pressedBrightnessAdjustment);
		this.themeManager.setCurrentTheme(previewTheme);
	}
	
	private void close() {
		this.getModuleService().changeActiveState(this.getId(), false);
		this.getModuleService().changeDisplayState(this.getId(), false);
	}
	
	private float clamp01(final float value) {
		return Math.max(0.0f, Math.min(1.0f, value));
	}
	
	private String formatValue(final float value) {
		return String.format("%.2f", value);
	}
	
	private HSBColor getColor(final ColorSlot slot) {
		return switch (slot) {
			case BORDER_OUTER -> this.borderOuter;
			case BORDER_INNER -> this.borderInner;
			case BORDER_INNER_DISABLED -> this.borderInnerDisabled;
			case BACKGROUND_NORMAL -> this.backgroundNormal;
			case BACKGROUND_SELECTED -> this.backgroundSelected;
			case TEXT_NORMAL -> this.textNormal;
			case TEXT_DISABLED -> this.textDisabled;
			case SELECTION_PRIMARY -> this.selectionPrimary;
			case SELECTION_SECONDARY -> this.selectionSecondary;
			case WINDOW_BACKGROUND -> this.windowBackground;
		};
	}
	
	private void setColor(final ColorSlot slot, final HSBColor color) {
		switch (slot) {
			case BORDER_OUTER -> this.borderOuter = color;
			case BORDER_INNER -> this.borderInner = color;
			case BORDER_INNER_DISABLED -> this.borderInnerDisabled = color;
			case BACKGROUND_NORMAL -> this.backgroundNormal = color;
			case BACKGROUND_SELECTED -> this.backgroundSelected = color;
			case TEXT_NORMAL -> this.textNormal = color;
			case TEXT_DISABLED -> this.textDisabled = color;
			case SELECTION_PRIMARY -> this.selectionPrimary = color;
			case SELECTION_SECONDARY -> this.selectionSecondary = color;
			case WINDOW_BACKGROUND -> this.windowBackground = color;
		}
	}
	
	private ImmutableRgbaColor toRgbaColor(final HSBColor color) {
		return new ImmutableRgbaColor(color.toColor());
	}
	
	private ColorSlot resolveColorSlot(final String controlId) {
		return switch (controlId) {
			case "ThemeEditor_Color_BorderOuter" -> ColorSlot.BORDER_OUTER;
			case "ThemeEditor_Color_BorderInner" -> ColorSlot.BORDER_INNER;
			case "ThemeEditor_Color_BorderInnerDisabled" -> ColorSlot.BORDER_INNER_DISABLED;
			case "ThemeEditor_Color_BackgroundNormal" -> ColorSlot.BACKGROUND_NORMAL;
			case "ThemeEditor_Color_BackgroundSelected" -> ColorSlot.BACKGROUND_SELECTED;
			case "ThemeEditor_Color_TextNormal" -> ColorSlot.TEXT_NORMAL;
			case "ThemeEditor_Color_TextDisabled" -> ColorSlot.TEXT_DISABLED;
			case "ThemeEditor_Color_SelectionPrimary" -> ColorSlot.SELECTION_PRIMARY;
			case "ThemeEditor_Color_SelectionSecondary" -> ColorSlot.SELECTION_SECONDARY;
			case "ThemeEditor_Color_WindowBackground" -> ColorSlot.WINDOW_BACKGROUND;
			default -> null;
		};
	}
	
	private ImmutableCoordI2 centeredPosition(final de.extio.game_engine.spatial2.model.CoordI2 dimension) {
		final var referenceResolution = RendererControl.REFERENCE_RESOLUTION;
		final var x = Math.max(0, (referenceResolution.getX() - dimension.getX()) / 2);
		final var y = Math.max(0, (referenceResolution.getY() - dimension.getY()) / 2);
		return ImmutableCoordI2.create(x, y);
	}
	
	private enum PopupTarget {
		PATTERN_RENDERER,
		FONT
	}
	
	private enum ColorSlot {
		
		BORDER_OUTER("ThemeEditor_Color_BorderOuter", "Border Outer"),
		BORDER_INNER("ThemeEditor_Color_BorderInner", "Border Inner"),
		BORDER_INNER_DISABLED("ThemeEditor_Color_BorderInnerDisabled", "Border Inner Disabled"),
		BACKGROUND_NORMAL("ThemeEditor_Color_BackgroundNormal", "Background Normal"),
		BACKGROUND_SELECTED("ThemeEditor_Color_BackgroundSelected", "Background Selected"),
		TEXT_NORMAL("ThemeEditor_Color_TextNormal", "Text Normal"),
		TEXT_DISABLED("ThemeEditor_Color_TextDisabled", "Text Disabled"),
		SELECTION_PRIMARY("ThemeEditor_Color_SelectionPrimary", "Selection Primary"),
		SELECTION_SECONDARY("ThemeEditor_Color_SelectionSecondary", "Selection Secondary"),
		WINDOW_BACKGROUND("ThemeEditor_Color_WindowBackground", "Window Background");
		
		private final String controlId;
		
		private final String displayName;
		
		ColorSlot(final String controlId, final String displayName) {
			this.controlId = controlId;
			this.displayName = displayName;
		}
	}
}
