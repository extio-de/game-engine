package de.extio.game_engine.i18n;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.util.ObjectSerialization;

public class LocalizationServiceImpl implements LocalizationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationServiceImpl.class);
	
	public final static String NOT_FOUND_PREFIX = "{i18n.";
	
	private Localizations localizations = new Localizations();
	
	private Map<String, String> currentLanguage;
	
	private String currentLanguageName;
	
	private final Map<Integer, String> intLookup = new HashMap<>();
	
	@Override
	public void resetEntries() {
		this.localizations.getLanguages().forEach((lang, mapping) -> mapping.clear());
		this.intLookup.clear();
	}
	
	@Override
	public void reset() {
		this.localizations = new Localizations();
		this.currentLanguage = null;
		this.currentLanguageName = null;
		this.intLookup.clear();
	}
	
	@Override
	public void load(final InputStream stream) {
		Localizations localizations;
		try {
			localizations = ObjectSerialization.deserialize(Localizations.class, stream, false, false, null, null, null);
		}
		catch (final Exception exc) {
			LOGGER.error("An exception occured while loading localizations", exc);
			return;
		}
		
		if (localizations != null) {
			localizations.getLanguages().forEach((lang, newMapping) -> {
				final Map<String, String> localizationEntries = this.localizations.getLanguages().computeIfAbsent(lang, k -> new LinkedHashMap<>());
				for (final Entry<String, String> newEntry : newMapping.entrySet()) {
					if (!localizationEntries.containsKey(newEntry.getKey()) || (newEntry.getValue() != null && !newEntry.getValue().isEmpty())) {
						localizationEntries.put(newEntry.getKey(), newEntry.getValue());
					}
				}
			});
			this.localizations.getLanguagesInfo().putAll(localizations.getLanguagesInfo());
			this.localizations.getDescriptions().putAll(localizations.getDescriptions());
			
			int maxId = 0;
			for (final Map<String, String> entries : this.localizations.getLanguages().values()) {
				for (final String locId : entries.keySet()) {
					final int intId = parseNumericId(locId);
					if (intId > -1) {
						maxId = Math.max(maxId, intId);
					}
				}
			}
			this.localizations.setCurId(maxId);
			
			this.localizations.setPrefix(localizations.getPrefix());
			
			this.intLookup.clear();
		}
		
		if (this.currentLanguageName == null) {
			this.currentLanguageName = "en";
		}
		this.setLanguage(this.currentLanguageName);
	}
	
	@Override
	public void setLanguage(final String lang) {
		if (!this.localizations.getLanguages().containsKey(lang)) {
			return;
		}
		
		this.currentLanguageName = lang;
		this.currentLanguage = this.localizations.getLanguages().get(lang);
		this.intLookup.clear();
	}
	
	@Override
	public List<Language> getLanguages() {
		if (this.localizations.getLanguagesInfo() == null || this.localizations.getLanguagesInfo().isEmpty()) {
			return List.of();
		}
		return List.copyOf(this.localizations.getLanguagesInfo().values());
	}
	
	@Override
	public String getCurrentLanguage() {
		return this.currentLanguageName;
	}
	
	@Override
	public String translate(final Integer id) {
		String result = this.intLookup.get(id);
		if (result != null) {
			return result;
		}
		
		result = this.translate(String.valueOf(id));
		this.intLookup.put(id, result);
		return result;
	}
	
	@Override
	public String translate(final Integer id, final String defaultText) {
		String result = this.translate(id);
		if (result.startsWith(NOT_FOUND_PREFIX)) {
			result = defaultText;
		}
		return result;
	}
	
	@Override
	public String translate(final String id) {
		if (this.currentLanguage == null) {
			return NOT_FOUND_PREFIX + id + "}";
		}
		return this.currentLanguage.computeIfAbsent(id, key -> NOT_FOUND_PREFIX + key + "}");
	}
	
	@Override
	public String translate(final String id, final String defaultText) {
		String result = this.translate(id);
		if (result.startsWith(NOT_FOUND_PREFIX)) {
			result = defaultText;
		}
		return result;
	}
	
	@Override
	public void put(final String lang, final String id, final String value) {
		final Map<String, String> mapping = this.localizations.getLanguages().get(lang);
		if (mapping == null) {
			return;
		}
		
		mapping.put(id, value);
		if (!this.localizations.getLanguagesInfo().containsKey(lang)) {
			this.localizations.getLanguagesInfo().put(lang, new Language(lang, lang));
		}
		
		this.intLookup.clear();
	}
	
	@Override
	public void remove(final String id) {
		this.localizations.getLanguages().forEach((k, v) -> v.remove(id));
		this.intLookup.clear();
	}
	
	@Override
	public Integer getNextId() {
		final int result = this.localizations.getCurId() + 1;
		this.localizations.setCurId(result);
		return Integer.valueOf(result);
	}
	
	@Override
	public Localizations getLocalizations() {
		return this.localizations;
	}
	
	private static int parseNumericId(final String id) {
		try {
			return Integer.parseInt(id);
		}
		catch (final NumberFormatException e) {
			final int dashIndex = id.lastIndexOf("-");
			if (dashIndex == -1 || dashIndex == id.length() - 1) {
				return -1;
			}
			
			try {
				return Integer.parseInt(id.substring(dashIndex + 1));
			}
			catch (final NumberFormatException ex) {
				return -1;
			}
		}
	}
	
}
