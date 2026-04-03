package de.extio.game_engine.menu;

import de.extio.game_engine.renderer.model.bo.ControlRenderingBo;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelControl;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.LabelData;
import de.extio.game_engine.renderer.model.bo.ControlRenderingBo.TextfieldControl;
import de.extio.game_engine.renderer.model.bo.HorizontalAlignment;
import de.extio.game_engine.renderer.model.event.UiControlEvent;

final class OptionsKeyboardTab extends AbstractOptionsTab implements OptionsTab {

	static final String TAB_ID = "keyboard";
	private static final String CONTROL_ID = "OptionsModule_Tab_Keyboard";

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
		return "ecyoa-86";
	}

	@Override
	public String fallbackCaption() {
		return "Keyboard";
	}

	@Override
	public int navigationOrder() {
		return 50;
	}

	@Override
	public boolean visibleInNavigation() {
		return false;
	}

	@Override
	public void render(final OptionsModuleContext context) {
		final var keycodeRegistrations = this.keycodeRegistry().getAll();

		var yPos = 10;

		var bo = this.renderingBoPool().acquire("OptionsModule_Keyboard_Label_Header", ControlRenderingBo.class)
				.setCaption(this.localizationService().translate("ecyoa-102", "Keyboard Shortcuts:"))
				.setFontSize(42)
				.setType(LabelControl.class)
				.setControlData(new LabelData(null, null, HorizontalAlignment.LEFT))
				.setVisible(true)
				.setEnabled(false)
				.withDimensionAbsolute(800, 80)
				.withPositionRelative(10, yPos);
		this.contentScrollArea(context).putRenderingBo(bo);

		yPos += 90;

		for (final var registration : keycodeRegistrations) {
			final var controlId = "OptionsModule_Keyboard_" + sanitizeId(registration.getQualifier());

			bo = this.renderingBoPool().acquire(controlId + "_Label", ControlRenderingBo.class)
					.setCaption(registration.getQualifier())
					.setFontSize(42)
					.setType(LabelControl.class)
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(400, 60)
					.withPositionRelative(10, yPos + 10);
			this.contentScrollArea(context).putRenderingBo(bo);

			bo = this.renderingBoPool().acquire(controlId + "_Key", ControlRenderingBo.class)
					.setCaption(registration.getKeyCodeDisplay() != null ? registration.getKeyCodeDisplay() : String.valueOf((char) registration.getCode()))
					.setFontSize(42)
					.setType(TextfieldControl.class)
					.setVisible(true)
					.setEnabled(false)
					.withDimensionAbsolute(400, 60)
					.withPositionRelative(420, yPos);
			this.contentScrollArea(context).putRenderingBo(bo);

			yPos += 70;
		}
	}

	@Override
	public boolean handle(final UiControlEvent event, final OptionsModuleContext context) {
		return false;
	}
}