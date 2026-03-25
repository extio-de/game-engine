package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class TerminalThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Terminal")
				.font("GoogleSansCode-Regular.ttf")
				.patternRendererName("terminalPatternRenderer")
				.borderOuter(new HSBColor(0.10f, 0.45f, 0.45f))
				.borderInner(new HSBColor(0.10f, 0.30f, 0.70f))
				.borderInnerDisabled(new HSBColor(0.10f, 0.15f, 0.25f))
				.backgroundNormal(new HSBColor(0.08f, 0.65f, 0.12f))
				.backgroundSelected(new HSBColor(0.12f, 0.70f, 0.35f))
				.textNormal(new HSBColor(0.12f, 0.85f, 0.95f))
				.textDisabled(new HSBColor(0.12f, 0.45f, 0.60f))
				.selectionPrimary(new HSBColor(0.10f, 0.75f, 0.55f))
				.selectionSecondary(new HSBColor(0.14f, 0.80f, 0.85f))
				.windowBackground(new HSBColor(0.08f, 0.55f, 0.08f))
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
