package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class SpacecraftThemeFactoryBean implements FactoryBean<Theme> {

	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Spacecraft")
				.font("Roboto-Regular.ttf")
				.patternRendererName("spacecraftPatternRenderer")
				.borderOuter(new HSBColor(0.71f, 0.12f, 0.50f))
				.borderInner(new HSBColor(0.71f, 0.12f, 0.80f))
				.borderInnerDisabled(new HSBColor(0.71f, 0.15f, 0.20f))
				.backgroundNormal(new HSBColor(0.71f, 0.92f, 0.30f))
				.backgroundSelected(new HSBColor(0.199f, 0.83f, 0.50f))
				.textNormal(new HSBColor(0.0f, 0.0f, 0.80f))
				.textDisabled(new HSBColor(0.0f, 0.0f, 0.50f))
				.selectionPrimary(new HSBColor(0.5f, 0.83f, 0.7f))
				.selectionSecondary(new HSBColor(0.199f, 0.83f, 0.90f))
				.windowBackground(new HSBColor(0.71f, 0.6f, 0.05f))
				.hoverBrightnessAdjustment(0.25f)
				.pressedBrightnessAdjustment(0.40f)
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
