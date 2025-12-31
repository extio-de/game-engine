package de.extio.game_engine.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.extio.game_engine.storage.StorageService;

class LocalizationManagerTest {
	
	@Mock
	private StorageService storageService;
	
	private LocalizationService localizationManager;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		this.localizationManager = new LocalizationServiceImpl(storageService);
	}
	
	@Test
	void testReset() {
		this.localizationManager.reset();
		assertNotNull(this.localizationManager.getLocalizations());
		assertTrue(this.localizationManager.getLanguages().isEmpty());
	}
	
	@Test
	void testResetEntries() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.resetEntries();
		
		assertTrue(localizations.getLanguages().get("en").isEmpty());
	}
	
	@Test
	void testLoadLocalizations() {
		Localizations localizationsToSave = new Localizations();
		Language english = new Language("English", "en");
		Map<String, String> englishEntries = new LinkedHashMap<>(Map.of("greeting", "Hello", "farewell", "Goodbye"));
		localizationsToSave.getLanguagesInfo().put("en", english);
		localizationsToSave.getLanguages().put("en", englishEntries);
		localizationsToSave.setPrefix("i18n.");
		localizationsToSave.setCurId(1);
		
		byte[] serialized = de.extio.game_engine.util.ObjectSerialization.serialize(
				localizationsToSave, false, false, false, null, digest -> {
				});
		
		InputStream stream = new ByteArrayInputStream(serialized);
		
		this.localizationManager.load(stream);
		
		Localizations loaded = this.localizationManager.getLocalizations();
		assertNotNull(loaded);
		assertEquals("i18n.", loaded.getPrefix());
		
		List<Language> languages = this.localizationManager.getLanguages();
		assertEquals(1, languages.size());
		assertEquals("en", languages.getFirst().getShortName());
		
		assertEquals("en", this.localizationManager.getCurrentLanguage());
		assertEquals("Hello", this.localizationManager.translate("greeting"));
		assertEquals("Goodbye", this.localizationManager.translate("farewell"));
	}
	
	@Test
	void testSetLanguage() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		Language german = new Language("German", "de");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguagesInfo().put("de", german);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		localizations.getLanguages().put("de", new LinkedHashMap<>(Map.of("greeting", "Hallo")));
		
		this.localizationManager.setLanguage("de");
		
		assertEquals("de", this.localizationManager.getCurrentLanguage());
	}
	
	@Test
	void testSetLanguageInvalid() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.setLanguage("fr");
		
		assertEquals("en", this.localizationManager.getCurrentLanguage());
	}
	
	@Test
	void testGetLanguages() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		Language german = new Language("German", "de");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguagesInfo().put("de", german);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		localizations.getLanguages().put("de", new LinkedHashMap<>(Map.of("greeting", "Hallo")));
		
		List<Language> languages = this.localizationManager.getLanguages();
		
		assertEquals(2, languages.size());
		assertTrue(languages.stream().anyMatch(lang -> "en".equals(lang.getShortName())));
		assertTrue(languages.stream().anyMatch(lang -> "de".equals(lang.getShortName())));
	}
	
	@Test
	void testGetCurrentLanguage() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		assertEquals("en", this.localizationManager.getCurrentLanguage());
	}
	
	@Test
	void testTranslateWithStringId() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		String result = this.localizationManager.translate("greeting");
		
		assertEquals("Hello", result);
	}
	
	@Test
	void testTranslateWithStringIdNotFound() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>());
		this.localizationManager.setLanguage("en");
		
		String result = this.localizationManager.translate("unknown");
		
		assertTrue(result.startsWith(LocalizationServiceImpl.NOT_FOUND_PREFIX));
	}
	
	@Test
	void testTranslateWithStringIdAndDefault() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>());
		this.localizationManager.setLanguage("en");
		
		String result = this.localizationManager.translate("unknown", "Default Text");
		
		assertEquals("Default Text", result);
	}
	
	@Test
	void testPut() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>());
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.put("en", "greeting", "Hello");
		String result = this.localizationManager.translate("greeting");
		
		assertEquals("Hello", result);
	}
	
	@Test
	void testPutInvalidLanguage() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>());
		
		this.localizationManager.put("fr", "greeting", "Bonjour");
		
		assertTrue(localizations.getLanguages().get("en").isEmpty());
	}
	
	@Test
	void testRemove() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		Map<String, String> entries = new LinkedHashMap<>(Map.of("greeting", "Hello", "farewell", "Goodbye"));
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", entries);
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.remove("greeting");
		
		assertFalse(localizations.getLanguages().get("en").containsKey("greeting"));
		assertTrue(localizations.getLanguages().get("en").containsKey("farewell"));
	}
	
	@Test
	void testGetNextId() {
		Localizations localizations = this.localizationManager.getLocalizations();
		localizations.setCurId(5);
		
		Integer nextId = this.localizationManager.getNextId();
		
		assertEquals(6, nextId);
		assertEquals(6, localizations.getCurId());
	}
	
	@Test
	void testGetNextIdIncremental() {
		Localizations localizations = this.localizationManager.getLocalizations();
		localizations.setCurId(0);
		
		Integer id1 = this.localizationManager.getNextId();
		Integer id2 = this.localizationManager.getNextId();
		Integer id3 = this.localizationManager.getNextId();
		
		assertEquals(1, id1);
		assertEquals(2, id2);
		assertEquals(3, id3);
	}
	
	@Test
	void testGetLocalizations() {
		Localizations localizations = this.localizationManager.getLocalizations();
		
		assertNotNull(localizations);
		assertEquals(localizations, this.localizationManager.getLocalizations());
	}
	
	@Test
	void testTranslateCaching() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("1", "Hello")));
		this.localizationManager.setLanguage("en");
		
		String result1 = this.localizationManager.translate("1");
		String result2 = this.localizationManager.translate("1");
		
		assertEquals("Hello", result1);
		assertEquals("Hello", result2);
		assertEquals(result1, result2);
	}
	
	@Test
	void testMultipleLanguages() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		Language german = new Language("German", "de");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguagesInfo().put("de", german);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		localizations.getLanguages().put("de", new LinkedHashMap<>(Map.of("greeting", "Hallo")));
		
		this.localizationManager.setLanguage("en");
		String englishGreeting = this.localizationManager.translate("greeting");
		
		this.localizationManager.setLanguage("de");
		String germanGreeting = this.localizationManager.translate("greeting");
		
		assertEquals("Hello", englishGreeting);
		assertEquals("Hallo", germanGreeting);
	}
	
	@Test
	void testPersistCurrentLanguage() {
		Localizations localizations = this.localizationManager.getLocalizations();
		Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguages().put("en", new LinkedHashMap<>(Map.of("greeting", "Hello")));
		
		this.localizationManager.setLanguage("en");
		
		verify(storageService).store(List.of("gameEngine"), "currentLanguage", "en");
	}
	
	@Test
	void testLoadPersistedLanguage() {
		when(storageService.loadByPath(String.class, List.of("gameEngine"), "currentLanguage"))
			.thenReturn(Optional.of("de"));
		
		// Load localizations with "de"
		Localizations localizationsToSave = new Localizations();
		Language german = new Language("German", "de");
		Map<String, String> germanEntries = new LinkedHashMap<>(Map.of("greeting", "Hallo"));
		localizationsToSave.getLanguagesInfo().put("de", german);
		localizationsToSave.getLanguages().put("de", germanEntries);
		localizationsToSave.setPrefix("i18n.");
		localizationsToSave.setCurId(1);
		
		byte[] serialized = de.extio.game_engine.util.ObjectSerialization.serialize(
			localizationsToSave, false, false, false, null, digest -> {});
		InputStream stream = new ByteArrayInputStream(serialized);
		
		this.localizationManager.load(stream);
		
		assertEquals("de", this.localizationManager.getCurrentLanguage());
	}
}
