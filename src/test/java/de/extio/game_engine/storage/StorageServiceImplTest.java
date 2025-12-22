package de.extio.game_engine.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.extio.game_engine.util.ObjectSerialization;

class StorageServiceImplTest {

	@TempDir
	static Path tempDir;

	@BeforeAll
	static void setUpStorageLocation() {
		System.setProperty("storagelocation", tempDir.resolve("storage").toString());
	}

	@AfterAll
	static void clearStorageLocation() {
		System.clearProperty("storagelocation");
	}

	@Test
	void loadIndexNoFileDoesNotFailAndKeepsIndexEmpty() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageServiceImpl service = new StorageServiceImpl();
		final StorageIndex index = this.getIndex(service);
		assertThat(index.isEmpty()).isTrue();
		assertThat(Files.exists(StorageServiceImpl.INDEX_LOCATION)).isFalse();
	}

	@Test
	void loadIndexDeserializesIndexFileAndAddsAllItems() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		Files.createDirectories(StorageServiceImpl.STORAGE_LOCATION);

		final StorageItemDescriptor d1 = new StorageItemDescriptor(UUID.randomUUID(), "File1.txt", List.of("A"));
		final StorageItemDescriptor d2 = new StorageItemDescriptor(UUID.randomUUID(), "File2.txt", List.of("A", "B"));
		final StorageItemDescriptor[] descriptors = new StorageItemDescriptor[] { d1, d2 };
		final byte[] bytes = ObjectSerialization.serialize(descriptors, false, false, false, new byte[0], ignored -> {
		});
		Files.write(StorageServiceImpl.INDEX_LOCATION, bytes);

		final StorageServiceImpl service = new StorageServiceImpl();
		final StorageIndex index = this.getIndex(service);
		assertThat(index.size()).isEqualTo(2);
		assertThat(index.find(List.of("a"), "file1.txt")).contains(d1);
		assertThat(index.find(List.of("a", "b"), "file2.txt")).contains(d2);
	}

	private StorageIndex getIndex(final StorageServiceImpl service) throws Exception {
		final Field field = StorageServiceImpl.class.getDeclaredField("index");
		field.setAccessible(true);
		return (StorageIndex) field.get(service);
	}
}
