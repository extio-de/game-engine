package de.extio.game_engine.i18n;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is the container holding localized texts (i18n)
 */
public final class Localizations {
	
	private String prefix;
	
	private Map<String, Map<String, String>> languages = new LinkedHashMap<>();
	
	private Map<String, Language> languagesInfo = new LinkedHashMap<>();
	
	private int curId;
	
	public Localizations() {
		
	}
	
	public Localizations(final Localizations other) {
		this.languages = new LinkedHashMap<>();
		for (final Entry<String, Map<String, String>> entry : other.languages.entrySet()) {
			this.languages.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
		}
		this.languagesInfo = new LinkedHashMap<>();
		for (final Entry<String, Language> entry : other.languagesInfo.entrySet()) {
			this.languagesInfo.put(entry.getKey(), new Language(entry.getValue()));
		}
		this.curId = other.curId;
		this.prefix = other.prefix;
	}
	
	public Map<String, Map<String, String>> getLanguages() {
		return this.languages;
	}
	
	public void setLanguages(final Map<String, Map<String, String>> languages) {
		this.languages = new LinkedHashMap<>(languages);
	}
	
	public Map<String, Language> getLanguagesInfo() {
		return this.languagesInfo;
	}
	
	public void setLanguagesInfo(final Map<String, Language> languagesInfo) {
		this.languagesInfo = new LinkedHashMap<>(languagesInfo);
	}
	
	public int getCurId() {
		return this.curId;
	}
	
	public void setCurId(final int curId) {
		this.curId = curId;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}
	
}
