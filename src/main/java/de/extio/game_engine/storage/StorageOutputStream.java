package de.extio.game_engine.storage;

import java.io.OutputStream;
import java.util.UUID;

/**
 * Wrapper that provides access to an OutputStream for storing data while
 * also returning the storage UUID associated with the data.
 */
public record StorageOutputStream(UUID id, OutputStream outputStream) {
}
