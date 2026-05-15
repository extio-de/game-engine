package de.extio.game_engine.i18n;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LocalizationLanguage {
	
	private String shortName;
	
	private Map<String, String> entries = new LinkedHashMap<>();
	
	public LocalizationLanguage() {
		
	}
	
	public LocalizationLanguage(final String shortName) {
		this.shortName = shortName;
	}
	
	public LocalizationLanguage(final LocalizationLanguage other) {
		this.shortName = other.shortName;
		this.entries = new LinkedHashMap<>(other.entries);
	}
	
	public String getShortName() {
		return this.shortName;
	}
	
	public void setShortName(final String shortName) {
		this.shortName = shortName;
	}
	
	public Map<String, String> getEntries() {
		return this.entries;
	}
	
	public void setEntries(final Map<String, String> entries) {
		this.entries = new LinkedHashMap<>(entries);
	}
	
}