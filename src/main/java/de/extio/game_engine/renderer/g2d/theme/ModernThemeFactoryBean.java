package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class ModernThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Modern")
				.font("Inter_24pt-Regular.ttf")
				.patternRendererName("modernPatternRenderer")
				.borderOuter(new HSBColor(0.58f, 0.08f, 0.78f))
				.borderInner(new HSBColor(0.58f, 0.10f, 0.88f))
				.borderInnerDisabled(new HSBColor(0.58f, 0.05f, 0.45f))
				.backgroundNormal(new HSBColor(0.60f, 0.55f, 0.20f))
				.backgroundSelected(new HSBColor(0.14f, 0.20f, 0.78f))
				.textNormal(new HSBColor(0.00f, 0.00f, 0.82f))
				.textDisabled(new HSBColor(0.00f, 0.00f, 0.65f))
				.selectionPrimary(new HSBColor(0.14f, 0.20f, 0.78f))
				.selectionSecondary(new HSBColor(0.50f, 0.20f, 0.98f))
				.windowBackground(new HSBColor(0.60f, 0.45f, 0.10f))
				.hoverBrightnessAdjustment(0.20f)
				.pressedBrightnessAdjustment(0.35f)
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
