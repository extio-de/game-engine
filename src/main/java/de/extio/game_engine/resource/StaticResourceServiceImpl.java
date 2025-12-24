package de.extio.game_engine.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.storage.StorageServiceImpl;
import de.extio.game_engine.util.ObjectSerialization;

public class StaticResourceServiceImpl implements StaticResourceService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceServiceImpl.class);
	
	static Path DATA_LOCATION;
	
	static {
		String location = System.getProperty("datalocation");
		if (location == null) {
			try {
				final URL url = StorageServiceImpl.class.getProtectionDomain().getCodeSource().getLocation();
				final Path jarPath = Paths.get(url.toURI()).getParent().toAbsolutePath().normalize();
				
				final String currentSubDir = jarPath.getFileName().toString();
				if ("bin".equals(currentSubDir) || "target".equals(currentSubDir)) {
					location = jarPath.getParent().resolve("data").toString();
				}
				else {
					location = jarPath.resolve("data").toString();
				}
			}
			catch (final Exception e) {
				LOGGER.error("Could not determine data location, defaulting to 'data'", e);
				location = "data";
			}
		}
		
		DATA_LOCATION = Paths.get(location).toAbsolutePath().normalize();
		LOGGER.info("Data location: {}", DATA_LOCATION);
	}
	
	private Path resolvePath(final List<String> path, final String name) {
		Path result = DATA_LOCATION;
		if (path != null) {
			for (final String segment : path) {
				result = result.resolve(segment);
			}
		}
		if (name != null) {
			result = result.resolve(name);
		}
		result = result.normalize();
		
		if (!result.startsWith(DATA_LOCATION)) {
			throw new IllegalArgumentException("Path escapes data location: " + result);
		}
		
		return result;
	}
	
	@Override
	public <T> Optional<T> loadByPath(final Class<T> clazz, final List<String> path, final String name) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(name, "name");
		
		final Path filePath = resolvePath(path, name);
		
		if (!Files.exists(filePath)) {
			LOGGER.debug("Resource not found: {}", filePath);
			return Optional.empty();
		}
		
		if (!Files.isRegularFile(filePath)) {
			LOGGER.warn("Path is not a regular file: {}", filePath);
			return Optional.empty();
		}
		
		try {
			try (final var in = Files.newInputStream(filePath)) {
				final T obj = ObjectSerialization.deserialize(clazz, in, true, false, null, null, null);
				if (obj != null) {
					LOGGER.info("Loaded resource from {}", filePath);
				}
				return Optional.ofNullable(obj);
			}
		}
		catch (final Exception e) {
			LOGGER.error("Could not load resource from " + filePath.toString(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<InputStream> loadStreamByPath(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		
		final Path filePath = resolvePath(path, name);
		
		if (!Files.exists(filePath)) {
			LOGGER.debug("Resource not found: {}", filePath);
			return Optional.empty();
		}
		
		if (!Files.isRegularFile(filePath)) {
			LOGGER.warn("Path is not a regular file: {}", filePath);
			return Optional.empty();
		}
		
		try {
			final InputStream stream = Files.newInputStream(filePath);
			LOGGER.info("Loaded resource stream from {}", filePath);
			return Optional.of(stream);
		}
		catch (final Exception e) {
			LOGGER.error("Could not load resource stream from " + filePath.toString(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public boolean exists(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		
		final Path filePath = resolvePath(path, name);
		
		return Files.exists(filePath) && Files.isRegularFile(filePath);
	}
	
	@Override
	public List<String> listPath(final List<String> path) {
		final Path dirPath = resolvePath(path, null);
		
		if (!Files.exists(dirPath)) {
			LOGGER.debug("Directory not found: {}", dirPath);
			return List.of();
		}
		
		if (!Files.isDirectory(dirPath)) {
			LOGGER.warn("Path is not a directory: {}", dirPath);
			return List.of();
		}
		
		try (final Stream<Path> stream = Files.list(dirPath)) {
			return stream
				.filter(Files::isRegularFile)
				.map(Path::getFileName)
				.map(Path::toString)
				.sorted()
				.toList();
		}
		catch (final IOException e) {
			LOGGER.error("Could not list directory " + dirPath.toString(), e);
			return List.of();
		}
	}
	
	@Override
	public List<String> listPathRecursive(final List<String> path) {
		final Path dirPath = resolvePath(path, null);
		
		if (!Files.exists(dirPath)) {
			LOGGER.debug("Directory not found: {}", dirPath);
			return List.of();
		}
		
		if (!Files.isDirectory(dirPath)) {
			LOGGER.warn("Path is not a directory: {}", dirPath);
			return List.of();
		}
		
		try (final Stream<Path> stream = Files.walk(dirPath)) {
			return stream
				.filter(Files::isRegularFile)
				.map(dirPath::relativize)
				.map(Path::toString)
				.sorted()
				.toList();
		}
		catch (final IOException e) {
			LOGGER.error("Could not list directory recursively " + dirPath.toString(), e);
			return List.of();
		}
	}
	
}
