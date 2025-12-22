package de.extio.game_engine.storage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.extio.game_engine.util.ObjectSerialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageServiceImpl {
	
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
				descriptors = ObjectSerialization.deserialize(StorageItemDescriptor[].class, in, false, false, null, null, null);
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
	
}
