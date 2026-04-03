package de.extio.game_engine.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.extio.game_engine.event.Event;
import de.extio.game_engine.event.EventService;
import de.extio.game_engine.module.AbstractClientModule;
import de.extio.game_engine.renderer.RendererControl;
import de.extio.game_engine.renderer.container.Window;
import de.extio.game_engine.renderer.model.RenderingBoLayer;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderData;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.color.HSBColor;
import de.extio.game_engine.renderer.model.color.ImmutableRgbaColor;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.work.RenderingBoPool;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;

public class HsbColorPickerModule extends AbstractClientModule {
	
	public record HsbColorPickerResponseEvent(String requestId, boolean confirmed, HSBColor color) implements Event {
	}
	
	private static final int WINDOW_WIDTH = 700;
	
	private static final int WINDOW_HEIGHT = 520;
	
	private static final int LABEL_HEIGHT = 34;
	
	private static final int SLIDER_HEIGHT = 34;
	
	private static final int BUTTON_HEIGHT = 40;
	
	private static final int BUTTON_WIDTH = 110;
	
	private static final int SPACING = 12;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private RenderingBoPool renderingBoPool;
	
	@Autowired
	private EventService eventService;
	
	private Window dialogWindow;
	
	private String requestId;
	
	private String title;
	
	private float hue;
	
	private float saturation;
	
	private float brightness;
	
	@Override
	public void onLoad() {
		this.dialogWindow = this.applicationContext.getBean(Window.class);
		this.dialogWindow.setNormalizedDimension(ImmutableCoordI2.create(WINDOW_WIDTH, WINDOW_HEIGHT));
		this.dialogWindow.setNormalizedPosition(this.centeredPosition(this.dialogWindow.getNormalizedDimension()));
		this.dialogWindow.setDraggable(true);
		this.dialogWindow.setCloseButton(true);
		this.dialogWindow.setOnCloseAction(this::onCancel);
	}
	
	@Override
	public void onUnload() {
		this.getModuleService().unloadModule(this.dialogWindow.getId());
	}
	
	@Override
	public void onActivate() {
		this.setupDialog();
		this.getModuleService().changeActiveState(this.dialogWindow.getId(), true);
		this.eventService.register(UiControlEvent.class, this.getId(), this::onUiControlEvent);
	}
	
	@Override
	public void onDeactivate() {
		this.eventService.unregisterAll(this.getId());
		this.getModuleService().changeActiveState(this.dialogWindow.getId(), false);
	}
	
	@Override
	public void onShow() {
		this.getModuleService().changeDisplayState(this.dialogWindow.getId(), true);
	}
	
	@Override
	public void onHide() {
		this.getModuleService().changeDisplayState(this.dialogWindow.getId(), false);
	}
	
	public void open(final String requestId, final String title, final HSBColor initialColor, final Window parentWindow) {
		this.requestId = requestId;
		this.title = title;
		if (initialColor != null) {
			this.hue = initialColor.getHue();
			this.saturation = initialColor.getSaturation();
			this.brightness = initialColor.getBrightness();
		}
		else {
			this.hue = 0.0f;
			this.saturation = 0.0f;
			this.brightness = 0.0f;
		}
		this.dialogWindow.setParent(parentWindow);
		this.getModuleService().changeActiveState(this.getId(), true);
		this.getModuleService().changeDisplayState(this.getId(), true);
	}
	
	private void onUiControlEvent(final UiControlEvent event) {
		if (event.getId() == null) {
			return;
		}
		
		switch (event.getId()) {
			case "HsbColorPicker_Button_Ok" -> this.onOk();
			case "HsbColorPicker_Button_Cancel" -> this.onCancel();
			case "HsbColorPicker_Slider_Hue" -> this.updateSliderValue(event, SliderTarget.HUE);
			case "HsbColorPicker_Slider_Saturation" -> this.updateSliderValue(event, SliderTarget.SATURATION);
			case "HsbColorPicker_Slider_Brightness" -> this.updateSliderValue(event, SliderTarget.BRIGHTNESS);
		}
	}
	
	private void updateSliderValue(final UiControlEvent event, final SliderTarget target) {
		if (!(event.getPayload() instanceof final Double rawValue)) {
			return;
		}
		final float value = Math.max(0.0f, Math.min(1.0f, rawValue.floatValue()));
		switch (target) {
			case HUE -> this.hue = value;
			case SATURATION -> this.saturation = value;
			case BRIGHTNESS -> this.brightness = value;
		}
		this.refreshPreview();
	}
	
	private void setupDialog() {
		this.dialogWindow.clearRenderingBos();
		
		final var contentWidth = WINDOW_WIDTH - Window.MARGIN_LEFT - Window.MARGIN_RIGHT;
		var yOffset = Window.MARGIN_TOP;
		
		if (this.title != null && !this.title.isBlank()) {
			final var titleBo = this.renderingBoPool.acquire("HsbColorPicker_Title", ControlRenderingBo.class)
					.setType(LabelControl.class)
					.setCaption(this.title)
					.setFontSize(28)
					.setControlData(new LabelData(null, null, HorizontalAlignment.CENTER))
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(contentWidth, LABEL_HEIGHT)
					.withPositionRelative(Window.MARGIN_LEFT, yOffset)
					.setLayer(RenderingBoLayer.UI0);
			this.dialogWindow.putRenderingBo(titleBo);
			yOffset += LABEL_HEIGHT + SPACING;
		}
		
		final var preview = this.renderingBoPool.acquire("HsbColorPicker_Preview", ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(" ")
				.setFontSize(20)
				.setControlData(new LabelData(this.toRgbaColor(this.getCurrentColor()), null, HorizontalAlignment.CENTER))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(contentWidth, 80)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(preview);
		
		yOffset += 80 + SPACING * 2;
		
		yOffset = this.addSliderRow("Hue", "HsbColorPicker_Slider_Hue", "HsbColorPicker_Value_Hue", this.hue, yOffset, contentWidth);
		yOffset = this.addSliderRow("Saturation", "HsbColorPicker_Slider_Saturation", "HsbColorPicker_Value_Saturation", this.saturation, yOffset, contentWidth);
		yOffset = this.addSliderRow("Brightness", "HsbColorPicker_Slider_Brightness", "HsbColorPicker_Value_Brightness", this.brightness, yOffset, contentWidth);
		
		yOffset += SPACING * 2;
		
		final var buttonY = yOffset;
		final var okButton = this.renderingBoPool.acquire("HsbColorPicker_Button_Ok", ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption("OK")
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT + contentWidth - (BUTTON_WIDTH * 2 + SPACING), buttonY)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(okButton);
		
		final var cancelButton = this.renderingBoPool.acquire("HsbColorPicker_Button_Cancel", ControlRenderingBo.class)
				.setType(ButtonControl.class)
				.setCaption("Cancel")
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(BUTTON_WIDTH, BUTTON_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT + contentWidth - BUTTON_WIDTH, buttonY)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(cancelButton);
		
		this.dialogWindow.draw();
	}
	
	private int addSliderRow(final String label, final String sliderId, final String valueId, final float value, final int yOffset, final int contentWidth) {
		final var labelWidth = 160;
		final var valueWidth = 100;
		final var sliderWidth = contentWidth - labelWidth - valueWidth - SPACING * 2;
		
		var bo = this.renderingBoPool.acquire("HsbColorPicker_Label_" + label, ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(label)
				.setFontSize(24)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(labelWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire(sliderId, ControlRenderingBo.class)
				.setType(SliderControl.class)
				.setControlData(new SliderData(true, value, value, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(sliderWidth, SLIDER_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT + labelWidth + SPACING, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(bo);
		
		bo = this.renderingBoPool.acquire(valueId, ControlRenderingBo.class)
				.setType(LabelControl.class)
				.setCaption(this.formatValue(value))
				.setFontSize(22)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(valueWidth, LABEL_HEIGHT)
				.withPositionRelative(Window.MARGIN_LEFT + labelWidth + SPACING + sliderWidth + SPACING, yOffset)
				.setLayer(RenderingBoLayer.UI0);
		this.dialogWindow.putRenderingBo(bo);
		
		return yOffset + LABEL_HEIGHT + SPACING;
	}
	
	private void refreshPreview() {
		final var preview = this.dialogWindow.getRenderingBo("HsbColorPicker_Preview", ControlRenderingBo.class);
		if (preview != null) {
			preview.setControlData(new LabelData(this.toRgbaColor(this.getCurrentColor()), null, HorizontalAlignment.CENTER));
			this.dialogWindow.putRenderingBo(preview);
		}
		this.updateValueLabel("HsbColorPicker_Value_Hue", this.hue);
		this.updateValueLabel("HsbColorPicker_Value_Saturation", this.saturation);
		this.updateValueLabel("HsbColorPicker_Value_Brightness", this.brightness);
		this.dialogWindow.draw();
	}
	
	private void updateValueLabel(final String id, final float value) {
		final var bo = this.dialogWindow.getRenderingBo(id, ControlRenderingBo.class);
		if (bo != null) {
			bo.setCaption(this.formatValue(value));
			this.dialogWindow.putRenderingBo(bo);
		}
	}
	
	private String formatValue(final float value) {
		return String.format("%.2f", value);
	}
	
	private HSBColor getCurrentColor() {
		return new HSBColor(this.hue, this.saturation, this.brightness);
	}
	
	private ImmutableRgbaColor toRgbaColor(final HSBColor color) {
		return new ImmutableRgbaColor(color.toColor());
	}
	
	private void onOk() {
		this.eventService.fire(new HsbColorPickerResponseEvent(this.requestId, true, this.getCurrentColor()));
		this.close();
	}
	
	private void onCancel() {
		this.eventService.fire(new HsbColorPickerResponseEvent(this.requestId, false, null));
		this.close();
	}
	
	private void close() {
		this.getModuleService().changeActiveState(this.getId(), false);
		this.getModuleService().changeDisplayState(this.getId(), false);
	}
	
	private ImmutableCoordI2 centeredPosition(final de.extio.game_engine.spatial2.model.CoordI2 dimension) {
		final var referenceResolution = RendererControl.REFERENCE_RESOLUTION;
		final var x = (referenceResolution.getX() - dimension.getX()) / 2;
		final var y = (referenceResolution.getY() - dimension.getY()) / 2;
		return ImmutableCoordI2.create(x, y);
	}
	
	private enum SliderTarget {
		HUE,
		SATURATION,
		BRIGHTNESS
	}
}
