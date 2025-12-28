package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

public class ContemporaryThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Contemporary")
				.patternRendererName("modernPatternRenderer")
				.borderOuter(new HSBColor(0.88f, 0.12f, 0.75f))
				.borderInner(new HSBColor(0.88f, 0.10f, 0.92f))
				.borderInnerDisabled(new HSBColor(0.88f, 0.06f, 0.42f))
				.backgroundNormal(new HSBColor(0.92f, 0.55f, 0.22f))
				.backgroundSelected(new HSBColor(0.92f, 0.35f, 0.70f))
				.textNormal(new HSBColor(0.08f, 0.04f, 0.94f))
				.textDisabled(new HSBColor(0.08f, 0.02f, 0.62f))
				.selectionPrimary(new HSBColor(0.92f, 0.35f, 0.70f))
				.selectionSecondary(new HSBColor(0.58f, 0.25f, 0.95f))
				.windowBackground(new HSBColor(0.92f, 0.55f, 0.12f))
				.hoverBrightnessAdjustment(0.22f)
				.pressedBrightnessAdjustment(0.38f)
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
