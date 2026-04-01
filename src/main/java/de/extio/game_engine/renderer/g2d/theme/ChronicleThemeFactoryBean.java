package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class ChronicleThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Chronicle")
				.font("Lato-Regular.ttf")
				.patternRendererName("chroniclePatternRenderer")
				.borderOuter(new HSBColor(0.08f, 0.18f, 0.45f))
				.borderInner(new HSBColor(0.08f, 0.12f, 0.70f))
				.borderInnerDisabled(new HSBColor(0.08f, 0.06f, 0.40f))
				.backgroundNormal(new HSBColor(0.11f, 0.22f, 0.75f))
				.backgroundSelected(new HSBColor(0.06f, 0.30f, 0.70f))
				.textNormal(new HSBColor(0.07f, 0.10f, 0.14f))
				.textDisabled(new HSBColor(0.07f, 0.08f, 0.35f))
				.selectionPrimary(new HSBColor(0.00f, 0.30f, 0.62f))
				.selectionSecondary(new HSBColor(0.12f, 0.20f, 0.85f))
				.windowBackground(new HSBColor(0.11f, 0.18f, 0.6f))
				.hoverBrightnessAdjustment(0.12f)
				.pressedBrightnessAdjustment(0.24f)
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
