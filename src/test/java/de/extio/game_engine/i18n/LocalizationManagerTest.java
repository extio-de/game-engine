package de.extio.game_engine.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

class LocalizationManagerTest {
	
	@Mock
	private StorageService storageService;

	@Mock
	private StaticResourceService staticResourceService;
	
	private LocalizationServiceImpl localizationManager;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		this.localizationManager = new LocalizationServiceImpl(storageService, staticResourceService);
	}
	
	private static LocalizationLanguage language(final String shortName, final Map<String, String> entries) {
		final var language = new LocalizationLanguage(shortName);
		language.setEntries(new LinkedHashMap<>(entries));
		return language;
	}
	
	@Test
	void testReset() {
		this.localizationManager.reset();
		assertNotNull(this.localizationManager.getLocalizations());
		assertTrue(this.localizationManager.getLanguages().isEmpty());
	}
	
	@Test
	void testResetEntries() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.resetEntries();
		
		assertTrue(localizations.getLanguageEntries("en").isEmpty());
	}
	
	@Test
	void testLoadLocalizations() {
		final LocalizationMetadata metadata = new LocalizationMetadata();
		final Language english = new Language("English", "en");
		final Map<String, String> englishEntries = new LinkedHashMap<>(Map.of("greeting", "Hello", "farewell", "Goodbye"));
		metadata.getLanguagesInfo().put("en", english);
		metadata.setPrefix("i18n.");
		this.localizationManager.load(metadata, List.of(language("en", englishEntries)));
		
		final Localizations loaded = this.localizationManager.getLocalizations();
		assertNotNull(loaded);
		assertEquals("i18n.", loaded.getPrefix());
		
		final List<Language> languages = this.localizationManager.getLanguages();
		assertEquals(1, languages.size());
		assertEquals("en", languages.getFirst().getShortName());
		
		assertEquals("en", this.localizationManager.getCurrentLanguage());
		assertEquals("Hello", this.localizationManager.translate("greeting"));
		assertEquals("Goodbye", this.localizationManager.translate("farewell"));
	}
	
	@Test
	void testLoadLocalizationsFromDirectory(@TempDir final Path directory) throws Exception {
		final LocalizationMetadata metadata = new LocalizationMetadata();
		metadata.getLanguagesInfo().put("en", new Language("English", "en"));
		metadata.getLanguagesInfo().put("de", new Language("German", "de"));
		metadata.setPrefix("i18n.");
		Files.write(directory.resolve(LocalizationServiceImpl.METADATA_FILE_NAME), de.extio.game_engine.util.ObjectSerialization.serialize(metadata, false, false, false, null, digest -> {}));
		Files.write(directory.resolve(LocalizationServiceImpl.languageFileName("en")), de.extio.game_engine.util.ObjectSerialization.serialize(language("en", Map.of("i18n.greeting", "Hello")), false, false, false, null, digest -> {}));
		Files.write(directory.resolve(LocalizationServiceImpl.languageFileName("de")), de.extio.game_engine.util.ObjectSerialization.serialize(language("de", Map.of("i18n.greeting", "Hallo")), false, false, false, null, digest -> {}));
		
		this.localizationManager.loadWoService(directory);
		
		// this.localizationManager.setLanguage("en");
		assertEquals("Hello", this.localizationManager.translate("i18n.greeting"));
		this.localizationManager.setLanguage("de");
		assertEquals("Hallo", this.localizationManager.translate("i18n.greeting"));
	}
	
	@Test
	void testSetLanguage() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		final Language german = new Language("German", "de");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguagesInfo().put("de", german);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		localizations.ensureLanguageFile("de").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hallo")));
		
		this.localizationManager.setLanguage("de");
		
		assertEquals("de", this.localizationManager.getCurrentLanguage());
	}
	
	@Test
	void testSetLanguageInvalid() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.setLanguage("fr");
		
		assertEquals("en", this.localizationManager.getCurrentLanguage());
	}
	
	@Test
	void testGetLanguages() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		final Language german = new Language("German", "de");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguagesInfo().put("de", german);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		localizations.ensureLanguageFile("de").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hallo")));
		
		final List<Language> languages = this.localizationManager.getLanguages();
		
		assertEquals(2, languages.size());
		assertTrue(languages.stream().anyMatch(lang -> "en".equals(lang.getShortName())));
		assertTrue(languages.stream().anyMatch(lang -> "de".equals(lang.getShortName())));
	}
	
	@Test
	void testGetCurrentLanguage() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		assertEquals("en", this.localizationManager.getCurrentLanguage());
	}
	
	@Test
	void testTranslateWithStringId() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		this.localizationManager.setLanguage("en");
		
		final String result = this.localizationManager.translate("greeting");
		
		assertEquals("Hello", result);
	}
	
	@Test
	void testTranslateWithStringIdNotFound() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>());
		this.localizationManager.setLanguage("en");
		
		final String result = this.localizationManager.translate("unknown");
		
		assertEquals("unknown", result);
	}
	
	@Test
	void testTranslateWithStringIdAndDefault() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>());
		this.localizationManager.setLanguage("en");
		
		final String result = this.localizationManager.translate("unknown", "Default Text");
		
		assertEquals("Default Text", result);
	}
	
	@Test
	void testPut() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>());
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.put("en", "greeting", "Hello");
		final String result = this.localizationManager.translate("greeting");
		
		assertEquals("Hello", result);
	}
	
	@Test
	void testPutInvalidLanguage() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>());
		
		this.localizationManager.put("fr", "greeting", "Bonjour");
		
		assertTrue(localizations.getLanguageEntries("en").isEmpty());
	}
	
	@Test
	void testRemove() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		final Map<String, String> entries = new LinkedHashMap<>(Map.of("greeting", "Hello", "farewell", "Goodbye"));
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(entries);
		this.localizationManager.setLanguage("en");
		
		this.localizationManager.remove("greeting");
		
		assertFalse(localizations.getLanguageEntries("en").containsKey("greeting"));
		assertTrue(localizations.getLanguageEntries("en").containsKey("farewell"));
	}
	
	@Test
	void testGetNextId() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("test-5", "Hello")));
		this.localizationManager.load(localizations.getMetadata(), List.of(localizations.getLanguageFile("en")));
		
		final Integer nextId = this.localizationManager.getNextId();
		
		assertEquals(6, nextId);
	}
	
	@Test
	void testGetNextIdIncremental() {
		final Integer id1 = this.localizationManager.getNextId();
		final Integer id2 = this.localizationManager.getNextId();
		final Integer id3 = this.localizationManager.getNextId();
		
		assertEquals(1, id1);
		assertEquals(2, id2);
		assertEquals(3, id3);
	}
	
	@Test
	void testGetLocalizations() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		
		assertNotNull(localizations);
		assertEquals(localizations, this.localizationManager.getLocalizations());
	}
	
	@Test
	void testTranslateCaching() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("1", "Hello")));
		this.localizationManager.setLanguage("en");
		
		final String result1 = this.localizationManager.translate("1");
		final String result2 = this.localizationManager.translate("1");
		
		assertEquals("Hello", result1);
		assertEquals("Hello", result2);
		assertEquals(result1, result2);
	}
	
	@Test
	void testMultipleLanguages() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		final Language german = new Language("German", "de");
		localizations.getLanguagesInfo().put("en", english);
		localizations.getLanguagesInfo().put("de", german);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		localizations.ensureLanguageFile("de").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hallo")));
		
		this.localizationManager.setLanguage("en");
		final String englishGreeting = this.localizationManager.translate("greeting");
		
		this.localizationManager.setLanguage("de");
		final String germanGreeting = this.localizationManager.translate("greeting");
		
		assertEquals("Hello", englishGreeting);
		assertEquals("Hallo", germanGreeting);
	}
	
	@Test
	void testPersistCurrentLanguage() {
		final Localizations localizations = this.localizationManager.getLocalizations();
		final Language english = new Language("English", "en");
		localizations.getLanguagesInfo().put("en", english);
		localizations.ensureLanguageFile("en").setEntries(new LinkedHashMap<>(Map.of("greeting", "Hello")));
		
		this.localizationManager.setLanguage("en");
		
		verify(storageService).store(List.of("gameEngine"), "currentLanguage", "en");
	}
	
	@Test
	void testLoadPersistedLanguage() {
		when(storageService.loadByPath(String.class, List.of("gameEngine"), "currentLanguage"))
			.thenReturn(Optional.of("de"));
		
		final LocalizationMetadata metadata = new LocalizationMetadata();
		metadata.getLanguagesInfo().put("de", new Language("German", "de"));
		metadata.setPrefix("i18n.");
		this.localizationManager.load(metadata, List.of(language("de", Map.of("greeting", "Hallo"))));
		
		assertEquals("de", this.localizationManager.getCurrentLanguage());
	}
}
