package de.extio.game_engine.i18n;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class LocalizationMetadata {
	
	private String prefix;
	
	private Map<String, Language> languagesInfo = new LinkedHashMap<>();
	
	public LocalizationMetadata() {
		
	}
	
	public LocalizationMetadata(final LocalizationMetadata other) {
		this.prefix = other.prefix;
		this.languagesInfo = new LinkedHashMap<>();
		for (final Entry<String, Language> entry : other.languagesInfo.entrySet()) {
			this.languagesInfo.put(entry.getKey(), new Language(entry.getValue()));
		}
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}
	
	public Map<String, Language> getLanguagesInfo() {
		return this.languagesInfo;
	}
	
	public void setLanguagesInfo(final Map<String, Language> languagesInfo) {
		this.languagesInfo = new LinkedHashMap<>();
		for (final Entry<String, Language> entry : languagesInfo.entrySet()) {
			this.languagesInfo.put(entry.getKey(), new Language(entry.getValue()));
		}
	}
	
}