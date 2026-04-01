package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class VintageThemeFactoryBean implements FactoryBean<Theme> {

	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Vintage")
				.font("LibreBaskerville-Regular.ttf")
				.patternRendererName("vintagePatternRenderer")
				.borderOuter(new HSBColor(0.08f, 0.55f, 0.22f))
				.borderInner(new HSBColor(0.10f, 0.30f, 0.55f))
				.borderInnerDisabled(new HSBColor(0.08f, 0.20f, 0.18f))
				.backgroundNormal(new HSBColor(0.085f, 0.45f, 0.28f))
				.backgroundSelected(new HSBColor(0.11f, 0.55f, 0.55f))
				.textNormal(new HSBColor(0.12f, 0.10f, 0.82f))
				.textDisabled(new HSBColor(0.12f, 0.05f, 0.60f))
				.selectionPrimary(new HSBColor(0.15f, 0.55f, 0.60f))
				.selectionSecondary(new HSBColor(0.11f, 0.70f, 0.80f))
				.windowBackground(new HSBColor(0.08f, 0.60f, 0.12f))
				.hoverBrightnessAdjustment(0.15f)
				.pressedBrightnessAdjustment(0.30f)
				.build();
	}

	@Override
	public Class<?> getObjectType() {
		return Theme.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
