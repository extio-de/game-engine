package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

public class FantasyThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Fantasy")
				.font("CrimsonText-Regular.ttf")
				.patternRendererName("fantasyPatternRenderer")
				.borderOuter(new HSBColor(0.12f, 0.55f, 0.48f))
				.borderInner(new HSBColor(0.14f, 0.25f, 0.72f))
				.borderInnerDisabled(new HSBColor(0.12f, 0.25f, 0.25f))
				.backgroundNormal(new HSBColor(0.32f, 0.60f, 0.22f))
				.backgroundSelected(new HSBColor(0.13f, 0.65f, 0.68f))
				.textNormal(new HSBColor(0.12f, 0.10f, 0.96f))
				.textDisabled(new HSBColor(0.12f, 0.05f, 0.65f))
				.selectionPrimary(new HSBColor(0.13f, 0.65f, 0.70f))
				.selectionSecondary(new HSBColor(0.58f, 0.35f, 0.88f))
				.windowBackground(new HSBColor(0.32f, 0.70f, 0.08f))
				.hoverBrightnessAdjustment(0.18f)
				.pressedBrightnessAdjustment(0.34f)
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
