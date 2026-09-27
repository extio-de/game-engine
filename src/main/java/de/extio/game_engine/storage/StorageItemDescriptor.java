package de.extio.game_engine.storage;

import java.util.List;
import java.util.UUID;

public record StorageItemDescriptor(UUID id, String name, List<String> path, Long lastModified) {

	public StorageItemDescriptor(final UUID id, final String name, final List<String> path) {
		this(id, name, path, null);
	}

}
