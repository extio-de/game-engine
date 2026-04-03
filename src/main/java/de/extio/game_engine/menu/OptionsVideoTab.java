package de.extio.game_engine.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.ToggleButtonData;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;
import de.extio.game_engine.renderer.model.options.VideoOptions.VideoOptionsVideoMode;

final class OptionsVideoTab extends AbstractOptionsTab implements OptionsTab {

	static final String TAB_ID = "video";
	private static final String CONTROL_ID = "OptionsModule_Tab_Video";

	private final Map<String, Integer> framerateSelectionByControlId = new HashMap<>();
	private final Map<String, Integer> screenSelectionByControlId = new HashMap<>();
	private final Map<String, Double> scaleFactorSelectionByControlId = new HashMap<>();

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
		return "ecyoa-84";
	}

	@Override
	public String fallbackCaption() {
		return "Video";
	}

	@Override
	public int navigationOrder() {
		return 30;
	}

	@Override
	public void render(final OptionsModuleContext context) {
		this.framerateSelectionByControlId.clear();
		this.screenSelectionByControlId.clear();
		this.scaleFactorSelectionByControlId.clear();

		var yPos = 10;

		var bo = this.renderingBoPool().acquire("OptionsModule_Video_Label_Mode", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-91", "Video Mode:"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 80)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		final var currentMode = this.rendererControl().getVideoOptions().getVideoMode();

		bo = this.renderingBoPool().acquire("OptionsModule_VideoMode_Window", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-92", "Windowed"))
				.setFontSize(42)
				.setType(ToggleButtonControl.class)
				.setControlGroup("OptionsModule_VideoModeGroup")
				.setControlData(new ToggleButtonData(currentMode == VideoOptionsVideoMode.WINDOW, false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(300, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		bo = this.renderingBoPool().acquire("OptionsModule_VideoMode_Borderless", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-93", "Borderless"))
				.setFontSize(42)
				.setType(ToggleButtonControl.class)
				.setControlGroup("OptionsModule_VideoModeGroup")
				.setControlData(new ToggleButtonData(currentMode == VideoOptionsVideoMode.BORDERLESS, false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(300, 60)
				.withPositionRelative(320, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		bo = this.renderingBoPool().acquire("OptionsModule_VideoMode_Fullscreen", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-94", "Fullscreen"))
				.setFontSize(42)
				.setType(ToggleButtonControl.class)
				.setControlGroup("OptionsModule_VideoModeGroup")
				.setControlData(new ToggleButtonData(currentMode == VideoOptionsVideoMode.FULLSCREEN, false, null))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(300, 60)
				.withPositionRelative(630, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 100;

		bo = this.renderingBoPool().acquire("OptionsModule_Video_Label_Screen", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-105", "Screen:"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 80)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		final var screenDevices = this.rendererControl().getScreenDevicesCount();
		final var currentScreen = this.rendererControl().getVideoOptions().getFullScreenNumber();

		var screenXPos = 10;
		for (var i = 0; i < screenDevices; i++) {
			final var controlId = "OptionsModule_Video_Screen_" + i;
			this.screenSelectionByControlId.put(controlId, i);

			bo = this.renderingBoPool().acquire(controlId, ControlRenderingBo.class)
					.setCaption(String.valueOf(i + 1))
					.setFontSize(42)
					.setType(ToggleButtonControl.class)
					.setControlGroup("OptionsModule_ScreenGroup")
					.setControlData(new ToggleButtonData(currentScreen == i, false, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(80, 60)
					.withPositionRelative(screenXPos, yPos);
			this.contentScrollArea(context).putRenderingBo(bo);

			screenXPos += 90;
		}

		yPos += 100;

		bo = this.renderingBoPool().acquire("OptionsModule_Video_Label_ScaleFactor", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-95", "Scale Factor:"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 80)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		final var currentScaleFactor = this.rendererControl().getVideoOptions().getScaleFactorModifier();
		final var scaleFactors = List.of(0.75, 0.90, 1.00, 1.15, 1.25);

		var scaleXPos = 10;
		for (final var scaleFactor : scaleFactors) {
			final var controlId = "OptionsModule_Video_ScaleFactor_" + (int) (scaleFactor * 100);
			this.scaleFactorSelectionByControlId.put(controlId, scaleFactor);

			bo = this.renderingBoPool().acquire(controlId, ControlRenderingBo.class)
					.setCaption((int) (scaleFactor * 100) + "%")
					.setFontSize(42)
					.setType(ToggleButtonControl.class)
					.setControlGroup("OptionsModule_ScaleFactorGroup")
					.setControlData(new ToggleButtonData(Math.abs(currentScaleFactor - scaleFactor) < 0.01, false, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(150, 60)
					.withPositionRelative(scaleXPos, yPos);
			this.contentScrollArea(context).putRenderingBo(bo);

			scaleXPos += 160;
		}

		yPos += 100;

		bo = this.renderingBoPool().acquire("OptionsModule_Video_Label_Framerate", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-96", "Target Framerate:"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 80)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		final var currentFramerate = this.rendererControl().getVideoOptions().getFrameRateTarget();
		final var framerates = List.of(30, 60, 120, 144, 165, 200);

		var xPos = 10;
		for (final var framerate : framerates) {
			final var controlId = "OptionsModule_Video_Framerate_" + framerate;
			this.framerateSelectionByControlId.put(controlId, framerate);

			bo = this.renderingBoPool().acquire(controlId, ControlRenderingBo.class)
					.setCaption(framerate + " FPS")
					.setFontSize(42)
					.setType(ToggleButtonControl.class)
					.setControlGroup("OptionsModule_FramerateGroup")
					.setControlData(new ToggleButtonData(currentFramerate == framerate, false, null))
					.setVisible(true)
					.setEnabled(true)
					.withDimensionAbsolute(220, 60)
					.withPositionRelative(xPos, yPos);
			this.contentScrollArea(context).putRenderingBo(bo);

			xPos += 230;
		}
	}

	@Override
	public boolean handle(final UiControlEvent event, final OptionsModuleContext context) {
		final var controlId = event.getId();
		if (controlId == null) {
			return false;
		}

		switch (controlId) {
			case "OptionsModule_VideoMode_Window" -> {
				this.rendererControl().getVideoOptions().setVideoMode(VideoOptionsVideoMode.WINDOW);
				this.rendererControl().applyVideoOptions();
				this.refreshContent(context);
				return true;
			}
			case "OptionsModule_VideoMode_Borderless" -> {
				this.rendererControl().getVideoOptions().setVideoMode(VideoOptionsVideoMode.BORDERLESS);
				this.rendererControl().applyVideoOptions();
				this.refreshContent(context);
				return true;
			}
			case "OptionsModule_VideoMode_Fullscreen" -> {
				this.rendererControl().getVideoOptions().setVideoMode(VideoOptionsVideoMode.FULLSCREEN);
				this.rendererControl().applyVideoOptions();
				this.refreshContent(context);
				return true;
			}
			default -> {
			}
		}

		final var framerate = this.framerateSelectionByControlId.get(controlId);
		if (framerate != null) {
			this.rendererControl().getVideoOptions().setFrameRateTarget(framerate);
			this.rendererControl().setFrameRate(framerate);
			this.refreshContent(context);
			return true;
		}

		final var screenNumber = this.screenSelectionByControlId.get(controlId);
		if (screenNumber != null) {
			this.rendererControl().getVideoOptions().setFullScreenNumber(screenNumber);
			this.rendererControl().applyVideoOptions();
			this.refreshContent(context);
			return true;
		}

		final var scaleFactor = this.scaleFactorSelectionByControlId.get(controlId);
		if (scaleFactor != null) {
			this.rendererControl().getVideoOptions().setScaleFactorModifier(scaleFactor);
			this.rendererControl().setScaleFactorModifier(scaleFactor);
			this.refreshContent(context);
			return true;
		}

		return false;
	}

	@Override
	public void onContentCleared(final OptionsModuleContext context) {
		this.framerateSelectionByControlId.clear();
		this.screenSelectionByControlId.clear();
		this.scaleFactorSelectionByControlId.clear();
	}
}