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
	
	String translate(Integer id);
	
	String translate(Integer id, String defaultText);
	
	String translate(String id);
	
	String translate(String id, String defaultText);
	
	void put(String lang, String id, String value);
	
	void remove(String id);
	
	Integer getNextId();
	
	Localizations getLocalizations();
	
}
