package de.extio.game_engine.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import de.extio.game_engine.util.ObjectSerialization;
import de.extio.game_engine.util.rng.FastRandomUUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageServiceImpl implements StorageService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceImpl.class);
	
	private final static String INDEX_FN = "index";
	
	private final static String DATA_DIR = "data/";
	
	public final static Path STORAGE_LOCATION;
	
	public final static Path DATA_LOCATION;
	
	public final static Path INDEX_LOCATION;
	
	static {
		String location = System.getProperty("storagelocation");
		if (location == null) {
			try {
				final URL url = StorageServiceImpl.class.getProtectionDomain().getCodeSource().getLocation();
				final Path jarPath = Paths.get(url.toURI()).getParent().toAbsolutePath().normalize();
				
				final String currentSubDir = jarPath.getFileName().toString();
				if ("bin".equals(currentSubDir) || "target".equals(currentSubDir)) {
					location = jarPath.getParent().resolve("storage").toString();
				}
				else {
					location = jarPath.resolve("storage").toString();
				}
			}
			catch (final Exception e) {
				LOGGER.error("Could not determine storage location, defaulting to 'storage'", e);
				location = "storage";
			}
		}
		
		STORAGE_LOCATION = Paths.get(location).toAbsolutePath().normalize();
		DATA_LOCATION = STORAGE_LOCATION.resolve(DATA_DIR);
		INDEX_LOCATION = STORAGE_LOCATION.resolve(INDEX_FN);
		LOGGER.info("Storage location: {}", STORAGE_LOCATION);
		
	}
	
	private static void forceMkdir(final Path p) {
		try {
			Files.createDirectories(p);
		}
		catch (final IOException e) {
			LOGGER.error("Could not create directory " + p.toString(), e);
		}
	}
	
	private final StorageIndex index;
	
	public StorageServiceImpl() {
		this.index = new StorageIndex();
		
		forceMkdir(STORAGE_LOCATION);
		forceMkdir(DATA_LOCATION);
		this.loadIndex();
	}
	
	private void loadIndex() {
		if (!Files.exists(INDEX_LOCATION)) {
			return;
		}
		try {
			if (Files.size(INDEX_LOCATION) == 0) {
				return;
			}
			
			final StorageItemDescriptor[] descriptors;
			try (final var in = Files.newInputStream(INDEX_LOCATION)) {
				descriptors = ObjectSerialization.deserialize(StorageItemDescriptor[].class, in, true, false, null, null, null);
			}
			if (descriptors == null || descriptors.length == 0) {
				return;
			}
			
			this.index.clear();
			for (final StorageItemDescriptor d : descriptors) {
				if (d != null) {
					this.index.add(d);
				}
			}
			
			LOGGER.info("Loaded {} items from index", this.index.size());
		}
		catch (final Exception e) {
			LOGGER.error("Could not load index from " + INDEX_LOCATION.toString(), e);
		}
	}
	
	private void saveIndex() {
		try {
			final StorageItemDescriptor[] descriptors = this.index.listAll().toArray(StorageItemDescriptor[]::new);
			try (var outputStream = Files.newOutputStream(INDEX_LOCATION)) {
				ObjectSerialization.serialize(descriptors, outputStream, true, false, false, null, null);
			}
			LOGGER.debug("Saved {} items to index", descriptors.length);
		}
		catch (final Exception e) {
			LOGGER.error("Could not save index to " + INDEX_LOCATION.toString(), e);
		}
	}
	
	private Path getFilePath(final UUID id) {
		return DATA_LOCATION.resolve(id.toString());
	}
	
	private void addToIndexAndSave(final UUID id, final String name, final List<String> path) {
		final StorageItemDescriptor descriptor = new StorageItemDescriptor(id, name, path == null ? List.of() : path);
		this.index.add(descriptor);
		this.saveIndex();
	}
	
	@Override
	public UUID store(final List<String> path, final String name, final Object obj) {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(obj, "obj");
		
		final Optional<StorageItemDescriptor> existing = this.index.find(path, name);
		final UUID id = existing.map(StorageItemDescriptor::id).orElse(FastRandomUUID.create());
		
		final Path filePath = getFilePath(id);
		try {
			try (var outputStream = Files.newOutputStream(filePath)) {
				ObjectSerialization.serialize(obj, outputStream, true, false, false, null, null);
			}
			
			addToIndexAndSave(id, name, path);
			LOGGER.info("Stored object with id {} at path {}/{}", id, path, name);
			return id;
		}
		catch (final Exception e) {
			LOGGER.error("Could not store object at " + filePath.toString(), e);
			throw new RuntimeException("Failed to store object", e);
		}
	}
	
	@Override
	public UUID storeStream(final List<String> path, final String name, final InputStream inputStream) {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(inputStream, "inputStream");
		
		final Optional<StorageItemDescriptor> existing = this.index.find(path, name);
		final UUID id = existing.map(StorageItemDescriptor::id).orElse(FastRandomUUID.create());
		
		final Path filePath = getFilePath(id);
		try {
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
			
			addToIndexAndSave(id, name, path);
			LOGGER.info("Stored stream with id {} at path {}/{}", id, path, name);
			return id;
		}
		catch (final Exception e) {
			LOGGER.error("Could not store stream at " + filePath.toString(), e);
			throw new RuntimeException("Failed to store stream", e);
		}
	}
	
	@Override
	public boolean deleteById(final UUID id) {
		Objects.requireNonNull(id, "id");
		
		final boolean removed = this.index.remove(id);
		if (!removed) {
			return false;
		}
		
		final Path filePath = DATA_LOCATION.resolve(id.toString());
		try {
			Files.deleteIfExists(filePath);
			this.saveIndex();
			LOGGER.debug("Deleted object with id {}", id);
			return true;
		}
		catch (final IOException e) {
			LOGGER.error("Could not delete file " + filePath.toString(), e);
			return false;
		}
	}
	
	@Override
	public boolean deleteByPath(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		
		final Optional<StorageItemDescriptor> descriptor = this.index.find(path, name);
		if (descriptor.isEmpty()) {
			return false;
		}
		
		return this.deleteById(descriptor.get().id());
	}
	
	@Override
	public boolean moveById(final UUID id, final List<String> newPath, final String newName) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(newName, "newName");
		
		final Optional<StorageItemDescriptor> existing = this.index.getById(id);
		if (existing.isEmpty()) {
			return false;
		}
		
		final StorageItemDescriptor newDescriptor = new StorageItemDescriptor(id, newName, newPath == null ? List.of() : newPath);
		this.index.add(newDescriptor);
		this.saveIndex();
		
		LOGGER.debug("Moved object with id {} to path {}/{}", id, newPath, newName);
		return true;
	}
	
	@Override
	public boolean moveByPath(final List<String> oldPath, final String oldName, final List<String> newPath, final String newName) {
		Objects.requireNonNull(oldName, "oldName");
		Objects.requireNonNull(newName, "newName");
		
		final Optional<StorageItemDescriptor> descriptor = this.index.find(oldPath, oldName);
		if (descriptor.isEmpty()) {
			return false;
		}
		
		return this.moveById(descriptor.get().id(), newPath, newName);
	}
	
	@Override
	public <T> Optional<T> loadById(final Class<T> clazz, final UUID id) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(id, "id");
		
		if (!this.index.getById(id).isPresent()) {
			return Optional.empty();
		}
		
		final Path filePath = getFilePath(id);
		if (!Files.exists(filePath)) {
			LOGGER.warn("Index references non-existent file for id {}", id);
			return Optional.empty();
		}
		
		try {
			try (final var in = Files.newInputStream(filePath)) {
				final T obj = ObjectSerialization.deserialize(clazz, in, true, false, null, null, null);
				if (obj != null) {
					LOGGER.info("Loaded object with id {}", id);
				}
				return Optional.ofNullable(obj);
			}
		}
		catch (final Exception e) {
			LOGGER.error("Could not load object from " + filePath.toString(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public <T> Optional<T> loadByPath(final Class<T> clazz, final List<String> path, final String name) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(name, "name");
		
		final Optional<StorageItemDescriptor> descriptor = this.index.find(path, name);
		if (descriptor.isEmpty()) {
			return Optional.empty();
		}
		
		return this.loadById(clazz, descriptor.get().id());
	}
	
	@Override
	public Optional<InputStream> loadStreamById(final UUID id) {
		Objects.requireNonNull(id, "id");
		
		if (!this.index.getById(id).isPresent()) {
			return Optional.empty();
		}
		
		final Path filePath = getFilePath(id);
		if (!Files.exists(filePath)) {
			LOGGER.warn("Index references non-existent file for id {}", id);
			return Optional.empty();
		}
		
		try {
			final InputStream stream = Files.newInputStream(filePath);
			LOGGER.info("Loaded stream with id {}", id);
			return Optional.of(stream);
		}
		catch (final Exception e) {
			LOGGER.error("Could not load stream from " + filePath.toString(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<InputStream> loadStreamByPath(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		
		final Optional<StorageItemDescriptor> descriptor = this.index.find(path, name);
		if (descriptor.isEmpty()) {
			return Optional.empty();
		}
		
		return this.loadStreamById(descriptor.get().id());
	}
	
	@Override
	public StorageOutputStream storeStream(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		
		final Optional<StorageItemDescriptor> existing = this.index.find(path, name);
		final UUID id = existing.map(StorageItemDescriptor::id).orElse(FastRandomUUID.create());
		
		final Path filePath = getFilePath(id);
		try {
			final OutputStream outputStream = Files.newOutputStream(filePath);
			
			return new StorageOutputStream(id, new OutputStream() {
				
				private boolean closed = false;
				
				@Override
				public void write(int b) throws IOException {
					outputStream.write(b);
				}
				
				@Override
				public void write(byte[] b) throws IOException {
					outputStream.write(b);
				}
				
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					outputStream.write(b, off, len);
				}
				
				@Override
				public void flush() throws IOException {
					outputStream.flush();
				}
				
				@Override
				public void close() throws IOException {
					if (closed) {
						return;
					}
					closed = true;
					outputStream.close();
					
					addToIndexAndSave(id, name, path);
					LOGGER.info("Stored stream with id {} at path {}/{}", id, path, name);
				}
			});
		}
		catch (final Exception e) {
			LOGGER.error("Could not create output stream at " + filePath.toString(), e);
			throw new RuntimeException("Failed to create output stream", e);
		}
	}
	
	@Override
	public Optional<StorageItemDescriptor> searchById(final UUID id) {
		Objects.requireNonNull(id, "id");
		return this.index.getById(id);
	}
	
	@Override
	public Optional<StorageItemDescriptor> searchByPath(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		return this.index.find(path, name);
	}
	
	@Override
	public List<StorageItemDescriptor> searchByPattern(final List<String> basePath, final String pattern, final boolean recursive) {
		Objects.requireNonNull(pattern, "pattern");
		return this.index.findByFilenamePattern(basePath, pattern, recursive);
	}
	
	@Override
	public List<StorageItemDescriptor> listPath(final List<String> path, final boolean recursive) {
		return this.index.list(path, recursive);
	}
	
	@Override
	public List<StorageItemDescriptor> listAll() {
		return this.index.listAll();
	}
	
}
