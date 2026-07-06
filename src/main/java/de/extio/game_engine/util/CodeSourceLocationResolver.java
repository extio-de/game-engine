package de.extio.game_engine.util;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CodeSourceLocationResolver {
	
	private CodeSourceLocationResolver() {
	}
	
	public static Path resolvePath(final Class<?> clazz) throws Exception {
		final var codeSource = clazz.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new IllegalStateException("Could not determine code source for " + clazz.getName());
		}
		return resolvePath(codeSource.getLocation());
	}
	
	public static Path resolvePath(final URL url) throws Exception {
		var location = url.toExternalForm();
		if (location.startsWith("jar:nested:")) {
			location = location.substring("jar:nested:".length());
			final var nestedSeparator = location.indexOf("/!");
			if (nestedSeparator >= 0) {
				location = location.substring(0, nestedSeparator);
			}
			return decodePath(location).toAbsolutePath().normalize();
		}
		return Paths.get(url.toURI()).toAbsolutePath().normalize();
	}
	
	private static Path decodePath(final String location) {
		if (location.startsWith("file:")) {
			return Paths.get(URI.create(location));
		}
		if (location.matches("^[A-Za-z]:/.*")) {
			return Paths.get(URI.create("file:/" + location));
		}
		return Paths.get(URI.create("file:" + location));
	}
	
}