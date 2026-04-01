package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class NeonThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Neon")
				.font("Geist-Regular.ttf")
				.patternRendererName("dreamPatternRenderer")
				.borderOuter(new HSBColor(0.83f, 0.80f, 0.95f))
				.borderInner(new HSBColor(0.52f, 0.85f, 0.92f))
				.borderInnerDisabled(new HSBColor(0.83f, 0.20f, 0.25f))
				.backgroundNormal(new HSBColor(0.74f, 0.80f, 0.09f))
				.backgroundSelected(new HSBColor(0.52f, 0.85f, 0.92f))
				.textNormal(new HSBColor(0.00f, 0.00f, 0.85f))
				.textDisabled(new HSBColor(0.00f, 0.00f, 0.70f))
				.selectionPrimary(new HSBColor(0.52f, 0.85f, 0.92f))
				.selectionSecondary(new HSBColor(0.16f, 0.85f, 0.95f))
				.windowBackground(new HSBColor(0.74f, 0.90f, 0.05f))
				.hoverBrightnessAdjustment(0.30f)
				.pressedBrightnessAdjustment(0.55f)
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
