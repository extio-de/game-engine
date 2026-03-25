package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class NoirThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Noir")
				.font("Lora-Regular.ttf")
				.patternRendererName("urbanPatternRenderer")
				.borderOuter(new HSBColor(0.00f, 0.00f, 0.18f))
				.borderInner(new HSBColor(0.00f, 0.00f, 0.45f))
				.borderInnerDisabled(new HSBColor(0.00f, 0.00f, 0.12f))
				.backgroundNormal(new HSBColor(0.62f, 0.20f, 0.08f))
				.backgroundSelected(new HSBColor(0.12f, 0.15f, 0.85f))
				.textNormal(new HSBColor(0.00f, 0.00f, 0.92f))
				.textDisabled(new HSBColor(0.00f, 0.00f, 0.55f))
				.selectionPrimary(new HSBColor(0.12f, 0.15f, 0.85f))
				.selectionSecondary(new HSBColor(0.58f, 0.12f, 0.75f))
				.windowBackground(new HSBColor(0.62f, 0.30f, 0.05f))
				.hoverBrightnessAdjustment(0.12f)
				.pressedBrightnessAdjustment(0.26f)
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
