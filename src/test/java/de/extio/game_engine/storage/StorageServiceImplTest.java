package de.extio.game_engine.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
		final StorageService service = new StorageServiceImpl();
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
		final byte[] bytes = ObjectSerialization.serialize(descriptors, true, false, false, new byte[0], ignored -> {
		});
		Files.write(StorageServiceImpl.INDEX_LOCATION, bytes);
		
		final StorageService service = new StorageServiceImpl();
		final StorageIndex index = this.getIndex(service);
		assertThat(index.size()).isEqualTo(2);
		assertThat(index.find(List.of("a"), "file1.txt")).contains(d1);
		assertThat(index.find(List.of("a", "b"), "file2.txt")).contains(d2);
	}
	
	@Test
	void storeCreatesNewObjectAndReturnsUUID() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		final UUID id = service.store(List.of("folder", "subfolder"), "myfile.dat", data);
		
		assertThat(id).isNotNull();
		assertThat(Files.exists(StorageServiceImpl.DATA_LOCATION.resolve(id.toString()))).isTrue();
		
		final StorageIndex index = this.getIndex(service);
		final Optional<StorageItemDescriptor> descriptor = index.find(List.of("folder", "subfolder"), "myfile.dat");
		assertThat(descriptor).isPresent();
		assertThat(descriptor.get().id()).isEqualTo(id);
		assertThat(descriptor.get().name()).isEqualTo("myfile.dat");
		assertThat(descriptor.get().path()).containsExactly("folder", "subfolder");
	}
	
	@Test
	void storeStreamCreatesNewObjectAndReturnsUUID() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final String content = "Hello World";
		final InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		final UUID id = service.storeStream(List.of("streams"), "mystream.txt", inputStream);
		
		assertThat(id).isNotNull();
		assertThat(Files.exists(StorageServiceImpl.DATA_LOCATION.resolve(id.toString()))).isTrue();
		
		final StorageIndex index = this.getIndex(service);
		final Optional<StorageItemDescriptor> descriptor = index.find(List.of("streams"), "mystream.txt");
		assertThat(descriptor).isPresent();
		assertThat(descriptor.get().id()).isEqualTo(id);
	}
	
	@Test
	void loadStreamByIdReturnsInputStream() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final String content = "Stream Content";
		final InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		final UUID id = service.storeStream(List.of("streams"), "mystream.txt", inputStream);
		
		final Optional<InputStream> loaded = service.loadStreamById(id);
		assertThat(loaded).isPresent();
		try (InputStream in = loaded.get()) {
			final String loadedContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertThat(loadedContent).isEqualTo(content);
		}
	}
	
	@Test
	void loadStreamByPathReturnsInputStream() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final String content = "Path Stream Content";
		final InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		service.storeStream(List.of("path", "streams"), "pathstream.txt", inputStream);
		
		final Optional<InputStream> loaded = service.loadStreamByPath(List.of("path", "streams"), "pathstream.txt");
		assertThat(loaded).isPresent();
		try (InputStream in = loaded.get()) {
			final String loadedContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertThat(loadedContent).isEqualTo(content);
		}
	}
	
	@Test
	void storeStreamWithOutputStreamCreatesNewObjectAndReturnsUUIDAndOutputStream() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final StorageOutputStream storageOutput = service.storeStream(List.of("output", "streams"), "outstream.txt");
		assertThat(storageOutput.id()).isNotNull();
		assertThat(storageOutput.outputStream()).isNotNull();
		
		final String content = "Output Stream Content";
		try (OutputStream out = storageOutput.outputStream()) {
			out.write(content.getBytes(StandardCharsets.UTF_8));
		}
		
		assertThat(Files.exists(StorageServiceImpl.DATA_LOCATION.resolve(storageOutput.id().toString()))).isTrue();
		
		final StorageIndex index = this.getIndex(service);
		final Optional<StorageItemDescriptor> descriptor = index.find(List.of("output", "streams"), "outstream.txt");
		assertThat(descriptor).isPresent();
		assertThat(descriptor.get().id()).isEqualTo(storageOutput.id());
	}
	
	@Test
	void storeStreamWithOutputStreamCanBeReadBack() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final String content = "Output Stream Readback Content";
		final StorageOutputStream storageOutput = service.storeStream(List.of("readback"), "readback.txt");
		try (OutputStream out = storageOutput.outputStream()) {
			out.write(content.getBytes(StandardCharsets.UTF_8));
		}
		
		final Optional<InputStream> loaded = service.loadStreamById(storageOutput.id());
		assertThat(loaded).isPresent();
		try (InputStream in = loaded.get()) {
			final String loadedContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertThat(loadedContent).isEqualTo(content);
		}
	}
	
	@Test
	void storeReplacesExistingObjectWithSamePathAndName() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageServiceImpl service = new StorageServiceImpl();
		
		final TestData data1 = new TestData("first", 100);
		final UUID id1 = service.store(List.of("folder"), "myfile.dat", data1);
		
		final TestData data2 = new TestData("second", 200);
		final UUID id2 = service.store(List.of("folder"), "myfile.dat", data2);
		
		assertThat(id1).isEqualTo(id2);
		
		final Optional<TestData> loaded = service.loadById(TestData.class, id2);
		assertThat(loaded).isPresent();
		assertThat(loaded.get().value()).isEqualTo("second");
		assertThat(loaded.get().number()).isEqualTo(200);
		
		final StorageIndex index = this.getIndex(service);
		assertThat(index.size()).isEqualTo(1);
	}
	
	@Test
	void deleteByIdRemovesObjectAndFile() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		final UUID id = service.store(List.of("folder"), "myfile.dat", data);
		
		final Path filePath = StorageServiceImpl.DATA_LOCATION.resolve(id.toString());
		assertThat(Files.exists(filePath)).isTrue();
		
		final boolean deleted = service.deleteById(id);
		assertThat(deleted).isTrue();
		assertThat(Files.exists(filePath)).isFalse();
		
		final StorageIndex index = this.getIndex(service);
		assertThat(index.getById(id)).isEmpty();
	}
	
	@Test
	void deleteByIdReturnsFalseForNonExistentId() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final boolean deleted = service.deleteById(UUID.randomUUID());
		assertThat(deleted).isFalse();
	}
	
	@Test
	void deleteByPathRemovesObject() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		final UUID id = service.store(List.of("folder"), "myfile.dat", data);
		
		final boolean deleted = service.deleteByPath(List.of("folder"), "myfile.dat");
		assertThat(deleted).isTrue();
		
		final StorageIndex index = this.getIndex(service);
		assertThat(index.getById(id)).isEmpty();
	}
	
	@Test
	void deleteByPathReturnsFalseForNonExistentPath() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final boolean deleted = service.deleteByPath(List.of("nonexistent"), "file.dat");
		assertThat(deleted).isFalse();
	}
	
	@Test
	void moveByIdUpdatesPathAndName() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		final UUID id = service.store(List.of("old", "path"), "oldfile.dat", data);
		
		final boolean moved = service.moveById(id, List.of("new", "path"), "newfile.dat");
		assertThat(moved).isTrue();
		
		final StorageIndex index = this.getIndex(service);
		final Optional<StorageItemDescriptor> oldLocation = index.find(List.of("old", "path"), "oldfile.dat");
		assertThat(oldLocation).isEmpty();
		
		final Optional<StorageItemDescriptor> newLocation = index.find(List.of("new", "path"), "newfile.dat");
		assertThat(newLocation).isPresent();
		assertThat(newLocation.get().id()).isEqualTo(id);
	}
	
	@Test
	void moveByIdReturnsFalseForNonExistentId() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final boolean moved = service.moveById(UUID.randomUUID(), List.of("new"), "file.dat");
		assertThat(moved).isFalse();
	}
	
	@Test
	void moveByPathUpdatesLocation() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		service.store(List.of("old"), "oldfile.dat", data);
		
		final boolean moved = service.moveByPath(List.of("old"), "oldfile.dat", List.of("new"), "newfile.dat");
		assertThat(moved).isTrue();
		
		final StorageIndex index = this.getIndex(service);
		final Optional<StorageItemDescriptor> newLocation = index.find(List.of("new"), "newfile.dat");
		assertThat(newLocation).isPresent();
	}
	
	@Test
	void loadByIdDeserializesObject() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageServiceImpl service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 456);
		final UUID id = service.store(List.of("folder"), "myfile.dat", data);
		
		final Optional<TestData> loaded = service.loadById(TestData.class, id);
		assertThat(loaded).isPresent();
		assertThat(loaded.get().value()).isEqualTo("test");
		assertThat(loaded.get().number()).isEqualTo(456);
	}
	
	@Test
	void loadByIdReturnsEmptyForNonExistentId() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageServiceImpl service = new StorageServiceImpl();
		
		final Optional<TestData> loaded = service.loadById(TestData.class, UUID.randomUUID());
		assertThat(loaded).isEmpty();
	}
	
	@Test
	void loadByPathDeserializesObject() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageServiceImpl service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 789);
		service.store(List.of("folder", "sub"), "myfile.dat", data);
		
		final Optional<TestData> loaded = service.loadByPath(TestData.class, List.of("folder", "sub"), "myfile.dat");
		assertThat(loaded).isPresent();
		assertThat(loaded.get().value()).isEqualTo("test");
		assertThat(loaded.get().number()).isEqualTo(789);
	}
	
	@Test
	void searchByIdFindsDescriptor() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		final UUID id = service.store(List.of("folder"), "myfile.dat", data);
		
		final Optional<StorageItemDescriptor> descriptor = service.searchById(id);
		assertThat(descriptor).isPresent();
		assertThat(descriptor.get().id()).isEqualTo(id);
		assertThat(descriptor.get().name()).isEqualTo("myfile.dat");
	}
	
	@Test
	void searchByPathFindsDescriptor() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final TestData data = new TestData("test", 123);
		final UUID id = service.store(List.of("folder"), "myfile.dat", data);
		
		final Optional<StorageItemDescriptor> descriptor = service.searchByPath(List.of("folder"), "myfile.dat");
		assertThat(descriptor).isPresent();
		assertThat(descriptor.get().id()).isEqualTo(id);
	}
	
	@Test
	void searchByPatternFindsMatchingFiles() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of("docs"), "file1.txt", new TestData("a", 1));
		service.store(List.of("docs"), "file2.txt", new TestData("b", 2));
		service.store(List.of("docs"), "data.dat", new TestData("c", 3));
		
		final List<StorageItemDescriptor> matches = service.searchByPattern(List.of("docs"), "*.txt", false);
		assertThat(matches).hasSize(2);
		assertThat(matches.stream().map(StorageItemDescriptor::name)).containsExactlyInAnyOrder("file1.txt", "file2.txt");
	}
	
	@Test
	void searchByPatternRecursiveSearchesSubdirectories() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of(), "root.txt", new TestData("a", 1));
		service.store(List.of("level1"), "sub1.txt", new TestData("b", 2));
		service.store(List.of("level1", "level2"), "sub2.txt", new TestData("c", 3));
		
		final List<StorageItemDescriptor> matches = service.searchByPattern(List.of(), "*.txt", true);
		assertThat(matches).hasSize(3);
	}
	
	@Test
	void listPathReturnsAllItemsInPath() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of("docs"), "file1.txt", new TestData("a", 1));
		service.store(List.of("docs"), "file2.txt", new TestData("b", 2));
		service.store(List.of("other"), "other.txt", new TestData("c", 3));
		
		final List<StorageItemDescriptor> items = service.listPath(List.of("docs"), false);
		assertThat(items).hasSize(2);
		assertThat(items.stream().map(StorageItemDescriptor::name)).containsExactlyInAnyOrder("file1.txt", "file2.txt");
	}
	
	@Test
	void listPathNonRecursiveReturnsOnlyDirectItems() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of("docs"), "root.txt", new TestData("a", 1));
		service.store(List.of("docs", "subdir"), "sub1.txt", new TestData("b", 2));
		service.store(List.of("docs", "subdir", "deeper"), "sub2.txt", new TestData("c", 3));
		
		final List<StorageItemDescriptor> items = service.listPath(List.of("docs"), false);
		assertThat(items).hasSize(1);
		assertThat(items.get(0).name()).isEqualTo("root.txt");
	}
	
	@Test
	void listPathRecursiveReturnsAllItemsInSubdirectories() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of("docs"), "root.txt", new TestData("a", 1));
		service.store(List.of("docs", "subdir"), "sub1.txt", new TestData("b", 2));
		service.store(List.of("docs", "subdir", "deeper"), "sub2.txt", new TestData("c", 3));
		
		final List<StorageItemDescriptor> items = service.listPath(List.of("docs"), true);
		assertThat(items).hasSize(3);
		assertThat(items.stream().map(StorageItemDescriptor::name)).containsExactlyInAnyOrder("root.txt", "sub1.txt", "sub2.txt");
	}
	
	@Test
	void listPathRecursiveFromRootReturnsAllItems() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of(), "file1.txt", new TestData("a", 1));
		service.store(List.of("level1"), "file2.txt", new TestData("b", 2));
		service.store(List.of("level1", "level2"), "file3.txt", new TestData("c", 3));
		service.store(List.of("other"), "file4.txt", new TestData("d", 4));
		
		final List<StorageItemDescriptor> items = service.listPath(List.of(), true);
		assertThat(items).hasSize(4);
		assertThat(items.stream().map(StorageItemDescriptor::name)).containsExactlyInAnyOrder("file1.txt", "file2.txt", "file3.txt", "file4.txt");
	}
	
	@Test
	void listAllReturnsAllStoredItems() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		service.store(List.of("docs"), "file1.txt", new TestData("a", 1));
		service.store(List.of("data"), "file2.txt", new TestData("b", 2));
		service.store(List.of("docs", "sub"), "file3.txt", new TestData("c", 3));
		
		final List<StorageItemDescriptor> items = service.listAll();
		assertThat(items).hasSize(3);
		assertThat(items.stream().map(StorageItemDescriptor::name)).containsExactlyInAnyOrder("file1.txt", "file2.txt", "file3.txt");
	}
	
	@Test
	void listAllOnEmptyServiceReturnsEmpty() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final List<StorageItemDescriptor> items = service.listAll();
		assertThat(items).isEmpty();
	}
	
	@Test
	void listAllAfterDeletionReflectsRemovedItems() throws Exception {
		Files.deleteIfExists(StorageServiceImpl.INDEX_LOCATION);
		final StorageService service = new StorageServiceImpl();
		
		final UUID id = service.store(List.of("docs"), "file.txt", new TestData("test", 1));
		assertThat(service.listAll()).hasSize(1);
		
		service.deleteById(id);
		assertThat(service.listAll()).isEmpty();
	}
	
	private StorageIndex getIndex(final StorageService service) throws Exception {
		final Field field = StorageServiceImpl.class.getDeclaredField("index");
		field.setAccessible(true);
		return (StorageIndex) field.get(service);
	}
	
	public static record TestData(String value, int number) {
	}
}
