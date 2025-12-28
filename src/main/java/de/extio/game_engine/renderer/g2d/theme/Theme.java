package de.extio.game_engine.renderer.g2d.theme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a complete UI theme with all color definitions and pattern renderer.
 * Themes are element-based rather than component-based, allowing consistent styling across different components.
 */
public class Theme {
	
	private final String name;
	private final String patternRendererName;
	
	// Element colors - Borders
	private final HSBColor borderOuter;
	private final HSBColor borderInner;
	private final HSBColor borderInnerDisabled;
	
	// Element colors - Backgrounds
	private final HSBColor backgroundNormal;
	private final HSBColor backgroundSelected;
	
	// Element colors - Text
	private final HSBColor textNormal;
	private final HSBColor textDisabled;
	
	// Element colors - Selection/Highlight
	private final HSBColor selectionPrimary;
	private final HSBColor selectionSecondary;
	
	// Element colors - Window/Panel
	private final HSBColor windowBackground;
	
	// Brightness adjustments for states
	private final float hoverBrightnessAdjustment;
	private final float pressedBrightnessAdjustment;
	
	@JsonCreator
	public Theme(
			@JsonProperty("name") final String name,
			@JsonProperty("patternRendererName") final String patternRendererName,
			@JsonProperty("borderOuter") final HSBColor borderOuter,
			@JsonProperty("borderInner") final HSBColor borderInner,
			@JsonProperty("borderInnerDisabled") final HSBColor borderInnerDisabled,
			@JsonProperty("backgroundNormal") final HSBColor backgroundNormal,
			@JsonProperty("backgroundSelected") final HSBColor backgroundSelected,
			@JsonProperty("textNormal") final HSBColor textNormal,
			@JsonProperty("textDisabled") final HSBColor textDisabled,
			@JsonProperty("selectionPrimary") final HSBColor selectionPrimary,
			@JsonProperty("selectionSecondary") final HSBColor selectionSecondary,
			@JsonProperty("windowBackground") final HSBColor windowBackground,
			@JsonProperty("hoverBrightnessAdjustment") final float hoverBrightnessAdjustment,
			@JsonProperty("pressedBrightnessAdjustment") final float pressedBrightnessAdjustment) {
		this.name = name;
		this.patternRendererName = patternRendererName;
		this.borderOuter = borderOuter;
		this.borderInner = borderInner;
		this.borderInnerDisabled = borderInnerDisabled;
		this.backgroundNormal = backgroundNormal;
		this.backgroundSelected = backgroundSelected;
		this.textNormal = textNormal;
		this.textDisabled = textDisabled;
		this.selectionPrimary = selectionPrimary;
		this.selectionSecondary = selectionSecondary;
		this.windowBackground = windowBackground;
		this.hoverBrightnessAdjustment = hoverBrightnessAdjustment;
		this.pressedBrightnessAdjustment = pressedBrightnessAdjustment;
	}
	
	private Theme(final Builder builder) {
		this(
				builder.name,
				builder.patternRendererName,
				builder.borderOuter,
				builder.borderInner,
				builder.borderInnerDisabled,
				builder.backgroundNormal,
				builder.backgroundSelected,
				builder.textNormal,
				builder.textDisabled,
				builder.selectionPrimary,
				builder.selectionSecondary,
				builder.windowBackground,
				builder.hoverBrightnessAdjustment,
				builder.pressedBrightnessAdjustment);
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPatternRendererName() {
		return this.patternRendererName;
	}
	
	public HSBColor getBorderOuter() {
		return this.borderOuter;
	}
	
	public HSBColor getBorderInner() {
		return this.borderInner;
	}
	
	public HSBColor getBorderInnerDisabled() {
		return this.borderInnerDisabled;
	}
	
	public HSBColor getBackgroundNormal() {
		return this.backgroundNormal;
	}
	
	public HSBColor getBackgroundSelected() {
		return this.backgroundSelected;
	}
	
	public HSBColor getTextNormal() {
		return this.textNormal;
	}
	
	public HSBColor getTextDisabled() {
		return this.textDisabled;
	}
	
	public HSBColor getSelectionPrimary() {
		return this.selectionPrimary;
	}
	
	public HSBColor getSelectionSecondary() {
		return this.selectionSecondary;
	}
	
	public HSBColor getWindowBackground() {
		return this.windowBackground;
	}
	
	public float getHoverBrightnessAdjustment() {
		return this.hoverBrightnessAdjustment;
	}
	
	public float getPressedBrightnessAdjustment() {
		return this.pressedBrightnessAdjustment;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String name;
		private String patternRendererName;
		private HSBColor borderOuter;
		private HSBColor borderInner;
		private HSBColor borderInnerDisabled;
		private HSBColor backgroundNormal;
		private HSBColor backgroundSelected;
		private HSBColor textNormal;
		private HSBColor textDisabled;
		private HSBColor selectionPrimary;
		private HSBColor selectionSecondary;
		private HSBColor windowBackground;
		private float hoverBrightnessAdjustment = 0.25f;
		private float pressedBrightnessAdjustment = 0.40f;
		
		public Builder name(final String name) {
			this.name = name;
			return this;
		}
		
		public Builder patternRendererName(final String patternRendererName) {
			this.patternRendererName = patternRendererName;
			return this;
		}
		
		public Builder borderOuter(final HSBColor borderOuter) {
			this.borderOuter = borderOuter;
			return this;
		}
		
		public Builder borderInner(final HSBColor borderInner) {
			this.borderInner = borderInner;
			return this;
		}
		
		public Builder borderInnerDisabled(final HSBColor borderInnerDisabled) {
			this.borderInnerDisabled = borderInnerDisabled;
			return this;
		}
		
		public Builder backgroundNormal(final HSBColor backgroundNormal) {
			this.backgroundNormal = backgroundNormal;
			return this;
		}
		
		public Builder backgroundSelected(final HSBColor backgroundSelected) {
			this.backgroundSelected = backgroundSelected;
			return this;
		}
		
		public Builder textNormal(final HSBColor textNormal) {
			this.textNormal = textNormal;
			return this;
		}
		
		public Builder textDisabled(final HSBColor textDisabled) {
			this.textDisabled = textDisabled;
			return this;
		}
		
		public Builder selectionPrimary(final HSBColor selectionPrimary) {
			this.selectionPrimary = selectionPrimary;
			return this;
		}
		
		public Builder selectionSecondary(final HSBColor selectionSecondary) {
			this.selectionSecondary = selectionSecondary;
			return this;
		}
		
		public Builder windowBackground(final HSBColor windowBackground) {
			this.windowBackground = windowBackground;
			return this;
		}
		
		public Builder hoverBrightnessAdjustment(final float hoverBrightnessAdjustment) {
			this.hoverBrightnessAdjustment = hoverBrightnessAdjustment;
			return this;
		}
		
		public Builder pressedBrightnessAdjustment(final float pressedBrightnessAdjustment) {
			this.pressedBrightnessAdjustment = pressedBrightnessAdjustment;
			return this;
		}
		
		public Theme build() {
			return new Theme(this);
		}
	}
}
