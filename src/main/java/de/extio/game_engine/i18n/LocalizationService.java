package de.extio.game_engine.i18n;

import java.io.InputStream;
import java.util.List;

public interface LocalizationService {
	
	void resetEntries();
	
	void reset();
	
	void load(InputStream stream);
	
	void setLanguage(String lang);
	
	List<Language> getLanguages();
	
	String getCurrentLanguage();
	
	String translate(String id);
	
	String translate(String id, String defaultText);
	
	String translate(String id, List<String> params);
	
	void put(String lang, String id, String value);
	
	void remove(String id);
	
	Integer getNextId();
	
	Localizations getLocalizations();
	
}
