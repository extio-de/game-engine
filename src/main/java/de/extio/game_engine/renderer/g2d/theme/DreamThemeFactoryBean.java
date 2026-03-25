package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class DreamThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Dream")
				.font("CrimsonText-Regular.ttf")
				.patternRendererName("dreamPatternRenderer")
				.borderOuter(new HSBColor(0.74f, 0.22f, 0.80f))
				.borderInner(new HSBColor(0.83f, 0.18f, 0.95f))
				.borderInnerDisabled(new HSBColor(0.74f, 0.10f, 0.35f))
				.backgroundNormal(new HSBColor(0.74f, 0.55f, 0.16f))
				.backgroundSelected(new HSBColor(0.78f, 0.35f, 0.65f))
				.textNormal(new HSBColor(0.12f, 0.03f, 0.96f))
				.textDisabled(new HSBColor(0.12f, 0.02f, 0.70f))
				.selectionPrimary(new HSBColor(0.78f, 0.35f, 0.65f))
				.selectionSecondary(new HSBColor(0.55f, 0.30f, 0.95f))
				.windowBackground(new HSBColor(0.74f, 0.70f, 0.07f))
				.hoverBrightnessAdjustment(0.28f)
				.pressedBrightnessAdjustment(0.45f)
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
