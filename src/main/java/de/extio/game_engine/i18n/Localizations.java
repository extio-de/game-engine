package de.extio.game_engine.i18n;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is the container holding localized texts (i18n)
 */
public final class Localizations {
	
	private LocalizationMetadata metadata = new LocalizationMetadata();
	
	private Map<String, LocalizationLanguage> languageFiles = new LinkedHashMap<>();
	
	public Localizations() {
		
	}
	
	public Localizations(final Localizations other) {
		this.metadata = new LocalizationMetadata(other.metadata);
		this.languageFiles = new LinkedHashMap<>();
		for (final Entry<String, LocalizationLanguage> entry : other.languageFiles.entrySet()) {
			this.languageFiles.put(entry.getKey(), new LocalizationLanguage(entry.getValue()));
		}
	}
	
	public LocalizationMetadata getMetadata() {
		return this.metadata;
	}
	
	public void setMetadata(final LocalizationMetadata metadata) {
		this.metadata = metadata == null ? new LocalizationMetadata() : new LocalizationMetadata(metadata);
	}
	
	public Map<String, LocalizationLanguage> getLanguageFiles() {
		return this.languageFiles;
	}
	
	public void setLanguageFiles(final Map<String, LocalizationLanguage> languageFiles) {
		this.languageFiles = new LinkedHashMap<>();
		for (final Entry<String, LocalizationLanguage> entry : languageFiles.entrySet()) {
			this.languageFiles.put(entry.getKey(), new LocalizationLanguage(entry.getValue()));
		}
	}
	
	public String getPrefix() {
		return this.metadata.getPrefix();
	}
	
	public void setPrefix(final String prefix) {
		this.metadata.setPrefix(prefix);
	}
	
	public Map<String, Language> getLanguagesInfo() {
		return this.metadata.getLanguagesInfo();
	}
	
	public LocalizationLanguage getLanguageFile(final String shortName) {
		return this.languageFiles.get(shortName);
	}
	
	public LocalizationLanguage ensureLanguageFile(final String shortName) {
		return this.languageFiles.computeIfAbsent(shortName, key -> new LocalizationLanguage(key));
	}
	
	public Map<String, String> getLanguageEntries(final String shortName) {
		final var languageFile = this.getLanguageFile(shortName);
		return languageFile == null ? null : languageFile.getEntries();
	}
	
}
