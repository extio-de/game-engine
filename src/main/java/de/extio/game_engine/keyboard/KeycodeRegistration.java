package de.extio.game_engine.keyboard;

import java.util.Objects;

public final class KeycodeRegistration {
	
	private String qualifier;
	
	private String displayText;
	
	private int code;
	
	private int modifier;
	
	private String keyCodeDisplay;
	
	public KeycodeRegistration() {
		
	}
	
	public KeycodeRegistration(final String qualifier, final String displayText, final int code, final int modifier, final String keyCodeDisplay) {
		this.qualifier = qualifier;
		this.displayText = displayText;
		this.code = code;
		this.modifier = modifier;
		this.keyCodeDisplay = keyCodeDisplay;
	}
	
	public KeycodeRegistration(final KeycodeRegistration other) {
		this.qualifier = other.qualifier;
		this.displayText = other.displayText;
		this.code = other.code;
		this.modifier = other.modifier;
		this.keyCodeDisplay = other.keyCodeDisplay;
	}
	
	public String getQualifier() {
		return this.qualifier;
	}
	
	public void setQualifier(final String qualifier) {
		this.qualifier = qualifier;
	}
	
	public int getCode() {
		return this.code;
	}
	
	public void setCode(final int code) {
		this.code = code;
	}
	
	public int getModifier() {
		return this.modifier;
	}
	
	public void setModifier(final int modifier) {
		this.modifier = modifier;
	}
	
	public String getDisplayText() {
		return this.displayText;
	}
	
	public void setDisplayText(final String displayText) {
		this.displayText = displayText;
	}
	
	public String getKeyCodeDisplay() {
		return this.keyCodeDisplay;
	}
	
	public void setKeyCodeDisplay(final String keyCodeDisplay) {
		this.keyCodeDisplay = keyCodeDisplay;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.code, this.displayText, this.keyCodeDisplay, this.modifier, this.qualifier);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof KeycodeRegistration)) {
			return false;
		}
		final KeycodeRegistration other = (KeycodeRegistration) obj;
		return this.code == other.code && Objects.equals(this.displayText, other.displayText) && Objects.equals(this.keyCodeDisplay, other.keyCodeDisplay) && this.modifier == other.modifier && Objects.equals(this.qualifier, other.qualifier);
	}
	
}
