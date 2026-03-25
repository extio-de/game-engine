package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class UrbanThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Urban")
				.font("Geist-Regular.ttf")
				.patternRendererName("metroPatternRenderer")
				.borderOuter(new HSBColor(0.58f, 0.08f, 0.30f))
				.borderInner(new HSBColor(0.58f, 0.06f, 0.72f))
				.borderInnerDisabled(new HSBColor(0.58f, 0.04f, 0.22f))
				.backgroundNormal(new HSBColor(0.58f, 0.22f, 0.16f))
				.backgroundSelected(new HSBColor(0.03f, 0.08f, 0.78f))
				.textNormal(new HSBColor(0.00f, 0.00f, 0.94f))
				.textDisabled(new HSBColor(0.00f, 0.00f, 0.62f))
				.selectionPrimary(new HSBColor(0.03f, 0.08f, 0.78f))
				.selectionSecondary(new HSBColor(0.48f, 0.08f, 0.88f))
				.windowBackground(new HSBColor(0.58f, 0.28f, 0.09f))
				.hoverBrightnessAdjustment(0.18f)
				.pressedBrightnessAdjustment(0.32f)
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
