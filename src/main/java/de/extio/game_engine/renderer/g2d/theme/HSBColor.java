package de.extio.game_engine.renderer.g2d.theme;

import java.awt.Color;
import java.util.Objects;

/**
 * Represents a color in HSB (Hue, Saturation, Brightness) color space.
 * This allows for easier runtime color manipulation (e.g., brightening on hover).
 */
public final class HSBColor {
	
	private final float hue;
	private final float saturation;
	private final float brightness;
	
	/**
	 * Creates an HSB color.
	 *
	 * @param hue        Hue component (0.0 to 1.0)
	 * @param saturation Saturation component (0.0 to 1.0)
	 * @param brightness Brightness component (0.0 to 1.0)
	 */
	public HSBColor(final float hue, final float saturation, final float brightness) {
		this.hue = clamp(hue);
		this.saturation = clamp(saturation);
		this.brightness = clamp(brightness);
	}
	
	/**
	 * Creates an HSB color from an AWT Color.
	 *
	 * @param color The AWT color to convert
	 */
	public static HSBColor fromColor(final Color color) {
		final var hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new HSBColor(hsb[0], hsb[1], hsb[2]);
	}
	
	/**
	 * Converts this HSB color to an AWT Color.
	 *
	 * @return The AWT Color
	 */
	public Color toColor() {
		return Color.getHSBColor(this.hue, this.saturation, this.brightness);
	}
	
	/**
	 * Creates a new HSBColor with adjusted brightness.
	 *
	 * @param delta The amount to add to brightness (-1.0 to 1.0)
	 * @return A new HSBColor with adjusted brightness
	 */
	public HSBColor adjustBrightness(final float delta) {
		return new HSBColor(this.hue, this.saturation, this.brightness + delta);
	}
	
	/**
	 * Creates a new HSBColor with adjusted saturation.
	 *
	 * @param delta The amount to add to saturation (-1.0 to 1.0)
	 * @return A new HSBColor with adjusted saturation
	 */
	public HSBColor adjustSaturation(final float delta) {
		return new HSBColor(this.hue, this.saturation + delta, this.brightness);
	}
	
	public float getHue() {
		return this.hue;
	}
	
	public float getSaturation() {
		return this.saturation;
	}
	
	public float getBrightness() {
		return this.brightness;
	}
	
	private static float clamp(final float value) {
		return Math.max(0.0f, Math.min(1.0f, value));
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		final HSBColor hsbColor = (HSBColor) o;
		return Float.compare(hsbColor.hue, this.hue) == 0 &&
				Float.compare(hsbColor.saturation, this.saturation) == 0 &&
				Float.compare(hsbColor.brightness, this.brightness) == 0;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.hue, this.saturation, this.brightness);
	}
	
	@Override
	public String toString() {
		return String.format("HSB(%.2f, %.2f, %.2f)", this.hue, this.saturation, this.brightness);
	}
}
