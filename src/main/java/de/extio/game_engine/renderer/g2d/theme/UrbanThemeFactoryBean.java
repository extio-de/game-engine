package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

public class UrbanThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Urban")
				.patternRendererName("urbanPatternRenderer")
				.borderOuter(new HSBColor(0.06f, 0.05f, 0.35f))
				.borderInner(new HSBColor(0.06f, 0.08f, 0.70f))
				.borderInnerDisabled(new HSBColor(0.06f, 0.04f, 0.22f))
				.backgroundNormal(new HSBColor(0.58f, 0.25f, 0.18f))
				.backgroundSelected(new HSBColor(0.02f, 0.75f, 0.70f))
				.textNormal(new HSBColor(0.12f, 0.05f, 0.92f))
				.textDisabled(new HSBColor(0.12f, 0.02f, 0.60f))
				.selectionPrimary(new HSBColor(0.02f, 0.75f, 0.70f))
				.selectionSecondary(new HSBColor(0.52f, 0.18f, 0.92f))
				.windowBackground(new HSBColor(0.58f, 0.30f, 0.10f))
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
