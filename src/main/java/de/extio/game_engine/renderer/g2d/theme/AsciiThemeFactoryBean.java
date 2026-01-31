package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

public class AsciiThemeFactoryBean implements FactoryBean<Theme> {

	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("ASCII")
				.font("Inconsolata-Regular.ttf")
				.patternRendererName("asciiPatternRenderer")
				.borderOuter(new HSBColor(0.00f, 0.00f, 0.30f))
				.borderInner(new HSBColor(0.00f, 0.00f, 0.50f))
				.borderInnerDisabled(new HSBColor(0.00f, 0.00f, 0.15f))
				.backgroundNormal(new HSBColor(0.00f, 0.00f, 0.10f))
				.backgroundSelected(new HSBColor(0.50f, 0.50f, 0.30f))
				.textNormal(new HSBColor(0.33f, 1.00f, 0.80f))
				.textDisabled(new HSBColor(0.33f, 0.50f, 0.50f))
				.selectionPrimary(new HSBColor(0.50f, 0.50f, 0.50f))
				.selectionSecondary(new HSBColor(0.50f, 0.70f, 0.70f))
				.windowBackground(new HSBColor(0.00f, 0.00f, 0.05f))
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