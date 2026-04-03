package de.extio.game_engine.menu;

import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SliderData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.SwitchData;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

final class OptionsAudioTab extends AbstractOptionsTab implements OptionsTab {

	static final String TAB_ID = "audio";
	private static final String CONTROL_ID = "OptionsModule_Tab_Audio";

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
		return "ecyoa-85";
	}

	@Override
	public String fallbackCaption() {
		return "Audio";
	}

	@Override
	public int navigationOrder() {
		return 40;
	}

	@Override
	public void render(final OptionsModuleContext context) {
		var yPos = 10;

		var bo = this.renderingBoPool().acquire("OptionsModule_Audio_Label_SFX", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-97", "Sound Effects:"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 60;

		final var sfxEnabled = this.audioControl().getAudioOptions().getSfxOptions().isEnable();

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_SFX_Enable", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-98", "Enable"))
				.setFontSize(42)
				.setType(SwitchControl.class)
				.setControlData(new SwitchData(sfxEnabled, false, null, true))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(620, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 80;

		final var sfxVolume = this.audioControl().getAudioOptions().getSfxOptions().getVolume();

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_Label_SFX_Volume", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-99", "Volume:"))
				.setFontSize(42)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(200, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_SFX_Volume_Slider", ControlRenderingBo.class)
				.setCaption(String.format("%.2f", sfxVolume))
				.setFontSize(42)
				.setType(SliderControl.class)
				.setControlData(new SliderData(true, sfxVolume, 0.0, null))
				.setVisible(true)
				.setEnabled(sfxEnabled)
				.withDimensionAbsolute(400, 60)
				.withPositionRelative(220, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 70;

		final var sfxSoftwareMixing = this.audioControl().getAudioOptions().getSfxOptions().isSoftwareMixing();

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_SFX_SoftwareMixing", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-100", "Software Mixing"))
				.setFontSize(42)
				.setType(SwitchControl.class)
				.setControlData(new SwitchData(sfxSoftwareMixing, false, null, true))
				.setVisible(true)
				.setEnabled(sfxEnabled)
				.withDimensionAbsolute(620, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 110;

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_Label_Music", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-101", "Music:"))
				.setFontSize(48)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 60;

		final var musicEnabled = this.audioControl().getAudioOptions().getMusicOptions().isEnable();

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_Music_Enable", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-98", "Enable"))
				.setFontSize(42)
				.setType(SwitchControl.class)
				.setControlData(new SwitchData(musicEnabled, false, null, true))
				.setVisible(true)
				.setEnabled(true)
				.withDimensionAbsolute(620, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 80;

		final var musicVolume = this.audioControl().getAudioOptions().getMusicOptions().getVolume();

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_Label_Music_Volume", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-99", "Volume:"))
				.setFontSize(42)
				.setType(LabelControl.class)
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(200, 60)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		bo = this.renderingBoPool().acquire("OptionsModule_Audio_Music_Volume_Slider", ControlRenderingBo.class)
				.setCaption(String.format("%.2f", musicVolume))
				.setFontSize(42)
				.setType(SliderControl.class)
				.setControlData(new SliderData(true, musicVolume, 0.0, null))
				.setVisible(true)
				.setEnabled(musicEnabled)
				.withDimensionAbsolute(400, 60)
				.withPositionRelative(220, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);
	}

	@Override
	public boolean handle(final UiControlEvent event, final OptionsModuleContext context) {
		final var controlId = event.getId();
		if (controlId == null) {
			return false;
		}

		switch (controlId) {
			case "OptionsModule_Audio_SFX_Enable" -> {
				final var currentValue = this.audioControl().getAudioOptions().getSfxOptions().isEnable();
				this.audioControl().getAudioOptions().getSfxOptions().setEnable(!currentValue);
				this.audioControl().applyAudioOptions(this.audioControl().getAudioOptions());
				this.refreshContent(context);
				return true;
			}
			case "OptionsModule_Audio_Music_Enable" -> {
				final var currentValue = this.audioControl().getAudioOptions().getMusicOptions().isEnable();
				this.audioControl().getAudioOptions().getMusicOptions().setEnable(!currentValue);
				this.audioControl().applyAudioOptions(this.audioControl().getAudioOptions());
				this.refreshContent(context);
				return true;
			}
			case "OptionsModule_Audio_SFX_SoftwareMixing" -> {
				final var currentValue = this.audioControl().getAudioOptions().getSfxOptions().isSoftwareMixing();
				this.audioControl().getAudioOptions().getSfxOptions().setSoftwareMixing(!currentValue);
				this.audioControl().applyAudioOptions(this.audioControl().getAudioOptions());
				this.refreshContent(context);
				return true;
			}
			case "OptionsModule_Audio_Music_SoftwareMixing" -> {
				final var currentValue = this.audioControl().getAudioOptions().getMusicOptions().isSoftwareMixing();
				this.audioControl().getAudioOptions().getMusicOptions().setSoftwareMixing(!currentValue);
				this.audioControl().applyAudioOptions(this.audioControl().getAudioOptions());
				this.refreshContent(context);
				return true;
			}
			default -> {
				return false;
			}
		}
	}
}