package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class SteampunkThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Steampunk")
				.font("Lora-Regular.ttf")
				.patternRendererName("steampunkPatternRenderer")
				.borderOuter(new HSBColor(0.08f, 0.5f, 0.2f))
				.borderInner(new HSBColor(0.12f, 0.8f, 0.6f))
				.borderInnerDisabled(new HSBColor(0.08f, 0.3f, 0.3f))
				.backgroundNormal(new HSBColor(0.08f, 0.4f, 0.3f))
				.backgroundSelected(new HSBColor(0.12f, 0.6f, 0.7f))
				.textNormal(new HSBColor(0.0f, 0.0f, 0.9f))
				.textDisabled(new HSBColor(0.0f, 0.0f, 0.5f))
				.selectionPrimary(new HSBColor(0.12f, 0.8f, 0.8f))
				.selectionSecondary(new HSBColor(0.08f, 0.6f, 0.5f))
				.windowBackground(new HSBColor(0.08f, 0.3f, 0.4f))
				.hoverBrightnessAdjustment(0.2f)
				.pressedBrightnessAdjustment(0.3f)
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