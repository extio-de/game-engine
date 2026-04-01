package de.extio.game_engine.renderer.g2d.theme;

import org.springframework.beans.factory.FactoryBean;

import de.extio.game_engine.renderer.model.Theme;
import de.extio.game_engine.renderer.model.color.HSBColor;

public class BevelDarkThemeFactoryBean implements FactoryBean<Theme> {
	
	@Override
	public Theme getObject() {
		return Theme.builder()
				.name("Bevel Dark")
				.font("Roboto-Regular.ttf")
				.patternRendererName("bevelPatternRenderer")
				// Highlight (Light Grey)
				.borderOuter(new HSBColor(0.0f, 0.0f, 0.6f)) 
				// Shadow (Black)
				.borderInner(new HSBColor(0.0f, 0.0f, 0.0f)) 
				// Disabled Shadow
				.borderInnerDisabled(new HSBColor(0.0f, 0.0f, 0.3f))
				// Background (Dark Grey)
				.backgroundNormal(new HSBColor(0.0f, 0.0f, 0.25f))
				// Selected Background (Dark Blue)
				.backgroundSelected(new HSBColor(0.6f, 0.8f, 0.4f))
				// Text (White)
				.textNormal(new HSBColor(0.0f, 0.0f, 0.85f))
				// Text Disabled (Grey)
				.textDisabled(new HSBColor(0.0f, 0.0f, 0.5f))
				// Selection Primary (Dark Blue)
				.selectionPrimary(new HSBColor(0.6f, 0.2f, 0.5f))
				// Selection Secondary (Darker Blue)
				.selectionSecondary(new HSBColor(0.6f, 0.1f, 0.3f))
				// Window Background (Dark Grey)
				.windowBackground(new HSBColor(0.0f, 0.0f, 0.15f))
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
