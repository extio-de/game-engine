package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

public class BlueprintThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Blueprint")
				.font("Inter_24pt-Regular.ttf")
				.patternRendererName("blueprintPatternRenderer")
				// Highlight/Border (White/Cyan)
				.borderOuter(new HSBColor(0.5f, 0.0f, 1.0f)) 
				// Inner Border (Faint Blue)
				.borderInner(new HSBColor(0.6f, 0.5f, 0.8f)) 
				// Disabled
				.borderInnerDisabled(new HSBColor(0.6f, 0.2f, 0.4f))
				// Background (Deep Blueprint Blue)
				.backgroundNormal(new HSBColor(0.62f, 0.90f, 0.40f))
				// Selected Background (Lighter Blue)
				.backgroundSelected(new HSBColor(0.62f, 0.70f, 0.60f))
				// Text (White)
				.textNormal(new HSBColor(0.0f, 0.0f, 1.0f))
				// Text Disabled (Light Blue)
				.textDisabled(new HSBColor(0.6f, 0.3f, 0.7f))
				// Selection Primary (Cyan)
				.selectionPrimary(new HSBColor(0.5f, 0.8f, 0.9f))
				// Selection Secondary (White)
				.selectionSecondary(new HSBColor(0.0f, 0.0f, 1.0f))
				// Window Background (Deep Blueprint Blue)
				.windowBackground(new HSBColor(0.62f, 0.90f, 0.40f))
				.hoverBrightnessAdjustment(0.15f)
				.pressedBrightnessAdjustment(0.30f)
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
