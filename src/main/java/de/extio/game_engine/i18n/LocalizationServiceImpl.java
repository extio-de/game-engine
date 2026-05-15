package de.extio.game_engine.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;
import de.extio.game_engine.util.ObjectSerialization;

public class LocalizationServiceImpl implements LocalizationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationServiceImpl.class);
	
	public final static String NOT_FOUND_PREFIX = "{i18n.";
	
	public static final String METADATA_FILE_NAME = "metadata.yaml";
	
	public static final String LANGUAGE_FILE_SUFFIX = ".yaml";
	
	private static final List<String> LANGUAGE_STORAGE_PATH = List.of("gameEngine");
	
	private static final String LANGUAGE_STORAGE_NAME = "currentLanguage";
	
	private final StorageService storageService;

	private final StaticResourceService staticResourceService;
	
	private Localizations localizations = new Localizations();
	
	private Map<String, String> currentLanguage;
	
	private String currentLanguageName;
	
	private int nextId = 1;
	
	LocalizationServiceImpl(final StorageService storageService, final StaticResourceService staticResourceService) {
		this.storageService = storageService;
		this.staticResourceService = staticResourceService;
	}
	
	@Override
	public void resetEntries() {
		this.localizations.getLanguageFiles().values().forEach(language -> language.getEntries().clear());
		this.nextId = 1;
	}
	
	@Override
	public void reset() {
		this.localizations = new Localizations();
		this.currentLanguage = null;
		this.currentLanguageName = null;
		this.nextId = 1;
	}
	
	public void load(final Path metaDataPath) {
		final List<String> resourcePath = metaDataPath.getParent() == null ? List.of() : StreamSupport.stream(metaDataPath.getParent().spliterator(), false).map(Path::toString).toList();
		final var metadataFileName = metaDataPath.getFileName().toString();
		final var staticResource = new StaticResource(resourcePath.isEmpty() ? null : resourcePath, metadataFileName);
		staticResourceService.loadStreamByPath(staticResource).ifPresent(stream -> {
			try (stream) {
				final var metadata = ObjectSerialization.deserialize(LocalizationMetadata.class, stream, false, false, null, null, null);
				final var languages = new ArrayList<LocalizationLanguage>();
				for (final String fileName : staticResourceService.listPath(resourcePath)) {
					if (metadataFileName.equals(fileName) || !fileName.endsWith(LANGUAGE_FILE_SUFFIX)) {
						continue;
					}
					staticResourceService.loadStreamByPath(resourcePath, fileName).ifPresent(languageStream -> {
						try (languageStream) {
							final var language = ObjectSerialization.deserialize(LocalizationLanguage.class, languageStream, false, false, null, null, null);
							if (language != null) {
								languages.add(language);
							}
						}
						catch (final Exception e) {
							throw new RuntimeException(e);
						}
					});
				}
				this.reset();
				this.load(metadata, languages);
			}
			catch (final Exception e) {
				LOGGER.error("An exception occured while loading localizations from resource: " + metaDataPath, e);
			}
		});
	}
	
	void loadWoService(final Path directory) throws IOException {
		Objects.requireNonNull(directory, "directory");
		this.reset();
		try (final var metadataStream = Files.newInputStream(directory.resolve(METADATA_FILE_NAME))) {
			final var metadata = ObjectSerialization.deserialize(LocalizationMetadata.class, metadataStream, false, false, null, null, null);
			final List<LocalizationLanguage> languages;
			try (final Stream<Path> files = Files.list(directory)) {
				languages = files
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(fileName -> !METADATA_FILE_NAME.equals(fileName) && fileName.endsWith(LANGUAGE_FILE_SUFFIX))
					.sorted()
					.map(fileName -> this.loadLanguage(directory.resolve(fileName)))
					.toList();
			}
			this.load(metadata, languages);
		}
	}

	void load(final LocalizationMetadata metadata, final List<LocalizationLanguage> languages) {
		if (metadata != null) {
			this.localizations.setPrefix(metadata.getPrefix());
			metadata.getLanguagesInfo().forEach((shortName, language) -> this.localizations.getLanguagesInfo().put(shortName, new Language(language)));
			this.localizations.getLanguagesInfo().keySet().forEach(this.localizations::ensureLanguageFile);
		}
		for (final LocalizationLanguage language : languages) {
			if (language == null || language.getShortName() == null || language.getShortName().isBlank()) {
				continue;
			}
			final var localizationEntries = this.localizations.ensureLanguageFile(language.getShortName()).getEntries();
			for (final Entry<String, String> newEntry : language.getEntries().entrySet()) {
				if (!localizationEntries.containsKey(newEntry.getKey()) || (newEntry.getValue() != null && !newEntry.getValue().isEmpty())) {
					localizationEntries.put(newEntry.getKey(), newEntry.getValue());
				}
			}
			this.localizations.getLanguagesInfo().computeIfAbsent(language.getShortName(), shortName -> new Language(shortName, shortName));
		}
		this.recalculateNextId();
		LOGGER.info("Loaded localizations for {} languages", this.localizations.getLanguageFiles().size());
		this.loadPersistedLanguageName();
		if (this.currentLanguageName == null) {
			this.currentLanguageName = "en";
		}
		this.setLanguage(this.currentLanguageName);
	}
	
	public void save(final Path directory) throws IOException {
		Objects.requireNonNull(directory, "directory");
		Files.createDirectories(directory);
		try (final var metadataStream = Files.newOutputStream(directory.resolve(METADATA_FILE_NAME))) {
			ObjectSerialization.serialize(this.localizations.getMetadata(), metadataStream, false, false, false, null, null);
		}
		final var expectedFiles = new HashSet<String>();
		expectedFiles.add(METADATA_FILE_NAME);
		for (final LocalizationLanguage language : this.localizations.getLanguageFiles().values()) {
			if (language.getShortName() == null || language.getShortName().isBlank()) {
				continue;
			}
			final var fileName = languageFileName(language.getShortName());
			expectedFiles.add(fileName);
			try (final var languageStream = Files.newOutputStream(directory.resolve(fileName))) {
				ObjectSerialization.serialize(language, languageStream, false, false, false, null, null);
			}
		}
		try (final Stream<Path> files = Files.list(directory)) {
			for (final Path file : files.filter(Files::isRegularFile).toList()) {
				final var fileName = file.getFileName().toString();
				if (fileName.endsWith(LANGUAGE_FILE_SUFFIX) && !expectedFiles.contains(fileName)) {
					Files.deleteIfExists(file);
				}
			}
		}
	}
	
	@Override
	public void setLanguage(final String lang) {
		if (this.localizations.getLanguageFile(lang) == null) {
			return;
		}
		
		this.currentLanguageName = lang;
		this.currentLanguage = this.localizations.getLanguageEntries(lang);
		this.persistCurrentLanguageName();
	}
	
	@Override
	public List<Language> getLanguages() {
		if (this.localizations.getLanguagesInfo().isEmpty()) {
			return List.of();
		}
		return List.copyOf(this.localizations.getLanguagesInfo().values());
	}
	
	@Override
	public String getCurrentLanguage() {
		return this.currentLanguageName;
	}
	
	@Override
	public String translate(final String id) {
		if (this.currentLanguage == null) {
			return NOT_FOUND_PREFIX + id + "}";
		}
		try {
			return this.currentLanguage.computeIfAbsent(id, key -> key);
		}
		catch (final ConcurrentModificationException e) {
			LOGGER.warn("Concurrent modification while translating id '{}'", id, e);
			return NOT_FOUND_PREFIX + id + "}";
		}
	}
	
	@Override
	public String translate(final String id, final String defaultText) {
		if (this.currentLanguage == null) {
			return defaultText;
		}
		final var result = this.currentLanguage.get(id);
		if (result == null) {
			return defaultText;
		}
		return result;
	}
	
	@Override
	public String translate(final String id, final List<String> params) {
		String result = this.translate(id);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				result = result.replace("{" + i + "}", params.get(i));
			}
		}
		return result;
	}
	
	@Override
	public void put(final String lang, final String id, final String value) {
		final Map<String, String> mapping = this.localizations.getLanguageEntries(lang);
		if (mapping == null) {
			return;
		}
		
		mapping.put(id, value);
		this.updateNextId(id);
		if (!this.localizations.getLanguagesInfo().containsKey(lang)) {
			this.localizations.getLanguagesInfo().put(lang, new Language(lang, lang));
		}
	}
	
	@Override
	public void remove(final String id) {
		this.localizations.getLanguageFiles().values().forEach(language -> language.getEntries().remove(id));
		this.recalculateNextId();
	}
	
	@Override
	public Integer getNextId() {
		final int result = this.nextId;
		this.nextId++;
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
	
	public static String languageFileName(final String shortName) {
		return shortName + LANGUAGE_FILE_SUFFIX;
	}
	
	private LocalizationLanguage loadLanguage(final Path file) {
		try (final var languageStream = Files.newInputStream(file)) {
			return ObjectSerialization.deserialize(LocalizationLanguage.class, languageStream, false, false, null, null, null);
		}
		catch (final Exception e) {
			throw new RuntimeException("Could not load localization language file " + file, e);
		}
	}
	
	private void recalculateNextId() {
		int maxId = 0;
		for (final LocalizationLanguage language : this.localizations.getLanguageFiles().values()) {
			for (final String locId : language.getEntries().keySet()) {
				final int intId = parseNumericId(locId);
				if (intId > -1) {
					maxId = Math.max(maxId, intId);
				}
			}
		}
		this.nextId = maxId + 1;
	}
	
	private void updateNextId(final String id) {
		final int numericId = parseNumericId(id);
		if (numericId > -1) {
			this.nextId = Math.max(this.nextId, numericId + 1);
		}
	}
	
	private void persistCurrentLanguageName() {
		if (this.storageService == null) {
			return;
		}
		try {
			this.storageService.store(LANGUAGE_STORAGE_PATH, LANGUAGE_STORAGE_NAME, this.currentLanguageName);
		}
		catch (final Exception e) {
			LOGGER.warn("Could not persist current language name", e);
		}
	}
	
	private void loadPersistedLanguageName() {
		if (this.storageService == null) {
			return;
		}
		try {
			final Optional<String> loaded = this.storageService.loadByPath(String.class, LANGUAGE_STORAGE_PATH, LANGUAGE_STORAGE_NAME);
			if (loaded.isPresent()) {
				this.currentLanguageName = loaded.get();
			}
		}
		catch (final Exception e) {
			LOGGER.warn("Could not load persisted language name", e);
		}
	}
}
