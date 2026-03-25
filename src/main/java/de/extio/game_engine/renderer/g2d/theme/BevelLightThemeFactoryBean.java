package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class BevelLightThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Bevel Light")
				.font("Roboto-Regular.ttf")
				.patternRendererName("bevelPatternRenderer")
				// Highlight (White)
				.borderOuter(new HSBColor(0.0f, 0.0f, 1.0f)) 
				// Shadow (Dark Grey)
				.borderInner(new HSBColor(0.0f, 0.0f, 0.4f)) 
				// Disabled Shadow
				.borderInnerDisabled(new HSBColor(0.0f, 0.0f, 0.6f))
				// Background (Grey)
				.backgroundNormal(new HSBColor(0.0f, 0.0f, 0.6f))
				// Selected Background (Blueish)
				.backgroundSelected(new HSBColor(0.6f, 0.5f, 0.9f))
				// Text (Black)
				.textNormal(new HSBColor(0.0f, 0.0f, 0.05f))
				// Text Disabled (Grey)
				.textDisabled(new HSBColor(0.0f, 0.0f, 0.5f))
				// Selection Primary (Blue)
				.selectionPrimary(new HSBColor(0.6f, 0.1f, 0.3f))
				// Selection Secondary (Lighter Blue)
				.selectionSecondary(new HSBColor(0.6f, 0.15f, 0.4f))
				// Window Background (Light Grey)
				.windowBackground(new HSBColor(0.0f, 0.0f, 0.55f))
				.hoverBrightnessAdjustment(0.1f)
				.pressedBrightnessAdjustment(0.2f)
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
