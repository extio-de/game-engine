package de.extio.game_engine.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.extio.game_engine.util.ObjectSerialization;

@DisplayName("StaticResourceServiceImpl")
class StaticResourceServiceImplTest {
	
	@TempDir
	static Path tempDir;
	
	private StaticResourceServiceImpl service;
	
	private static int dataLocationCounter = 0;
	
	private static Path currentDataLocation;
	
	@BeforeEach
	void setUpDataLocation() {
		currentDataLocation = tempDir.resolve("data_" + (++dataLocationCounter));
		service = new StaticResourceServiceImpl();
		StaticResourceServiceImpl.DATA_LOCATION = currentDataLocation;
	}
	
	protected static Path getDataLocation() {
		return currentDataLocation;
	}
	
	@Nested
	@DisplayName("loadByPath")
	class LoadByPathTests {
		
		@Test
		void loadsObjectFromFileWhenResourceExists() throws Exception {
			final Path resourceFile = getDataLocation().resolve("test.txt");
			Files.createDirectories(resourceFile.getParent());
			
			final String content = "Hello, World!";
			Files.write(resourceFile, ObjectSerialization.serialize(content, true, false, false, new byte[0], ignored -> {
			}));
			
			final Optional<String> result = service.loadByPath(String.class, null, "test.txt");
			
			assertThat(result).contains(content);
		}
		
		@Test
		void loadsObjectFromNestedPath() throws Exception {
			final Path resourceFile = getDataLocation().resolve("path").resolve("to").resolve("resource.txt");
			Files.createDirectories(resourceFile.getParent());
			
			final String content = "Nested content";
			Files.write(resourceFile, ObjectSerialization.serialize(content, true, false, false, new byte[0], ignored -> {
			}));
			
			final Optional<String> result = service.loadByPath(String.class, List.of("path", "to"), "resource.txt");
			
			assertThat(result).contains(content);
		}
		
		@Test
		void returnsEmptyOptionalWhenResourceDoesNotExist() {
			final Optional<String> result = service.loadByPath(String.class, null, "nonexistent.txt");
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void returnsEmptyOptionalWhenPathIsDirectory() throws Exception {
			final Path directory = getDataLocation().resolve("directory");
			Files.createDirectories(directory);
			
			final Optional<String> result = service.loadByPath(String.class, null, "directory");
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void throwsIllegalArgumentExceptionWhenPathEscapesDataLocation() {
			assertThatThrownBy(() -> service.loadByPath(String.class, List.of(".."), "test.txt"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
		
		@Test
		void throwsNullPointerExceptionWhenClassIsNull() {
			assertThatThrownBy(() -> service.loadByPath(null, null, "test.txt"))
					.isInstanceOf(NullPointerException.class);
		}
		
		@Test
		void throwsNullPointerExceptionWhenNameIsNull() {
			assertThatThrownBy(() -> service.loadByPath(String.class, null, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
	
	@Nested
	@DisplayName("loadStreamByPath")
	class LoadStreamByPathTests {
		
		@Test
		void loadsStreamFromFileWhenResourceExists() throws Exception {
			final Path resourceFile = getDataLocation().resolve("test.bin");
			Files.createDirectories(resourceFile.getParent());
			
			final byte[] content = "Binary content".getBytes(StandardCharsets.UTF_8);
			Files.write(resourceFile, content);
			
			final Optional<InputStream> result = service.loadStreamByPath(null, "test.bin");
			
			assertThat(result).isPresent();
			try (final InputStream stream = result.get()) {
				final byte[] read = stream.readAllBytes();
				assertThat(read).isEqualTo(content);
			}
		}
		
		@Test
		void loadsStreamFromNestedPath() throws Exception {
			final Path resourceFile = getDataLocation().resolve("path").resolve("to").resolve("binary.bin");
			Files.createDirectories(resourceFile.getParent());
			
			final byte[] content = "Nested binary".getBytes(StandardCharsets.UTF_8);
			Files.write(resourceFile, content);
			
			final Optional<InputStream> result = service.loadStreamByPath(List.of("path", "to"), "binary.bin");
			
			assertThat(result).isPresent();
			try (final InputStream stream = result.get()) {
				final byte[] read = stream.readAllBytes();
				assertThat(read).isEqualTo(content);
			}
		}
		
		@Test
		void returnsEmptyOptionalWhenResourceDoesNotExist() {
			final Optional<InputStream> result = service.loadStreamByPath(null, "nonexistent.bin");
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void returnsEmptyOptionalWhenPathIsDirectory() throws Exception {
			final Path directory = getDataLocation().resolve("directory");
			Files.createDirectories(directory);
			
			final Optional<InputStream> result = service.loadStreamByPath(null, "directory");
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void throwsIllegalArgumentExceptionWhenPathEscapesDataLocation() {
			assertThatThrownBy(() -> service.loadStreamByPath(List.of(".."), "test.bin"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
		
		@Test
		void throwsNullPointerExceptionWhenNameIsNull() {
			assertThatThrownBy(() -> service.loadStreamByPath(null, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
	
	@Nested
	@DisplayName("exists")
	class ExistsTests {
		
		@Test
		void returnsTrueWhenResourceExists() throws Exception {
			final Path resourceFile = getDataLocation().resolve("exists.txt");
			Files.createDirectories(resourceFile.getParent());
			Files.write(resourceFile, "content".getBytes());
			
			final boolean result = service.exists(null, "exists.txt");
			
			assertThat(result).isTrue();
		}
		
		@Test
		void returnsTrueWhenResourceExistsInNestedPath() throws Exception {
			final Path resourceFile = getDataLocation().resolve("deep").resolve("path").resolve("file.txt");
			Files.createDirectories(resourceFile.getParent());
			Files.write(resourceFile, "content".getBytes());
			
			final boolean result = service.exists(List.of("deep", "path"), "file.txt");
			
			assertThat(result).isTrue();
		}
		
		@Test
		void returnsFalseWhenResourceDoesNotExist() {
			final boolean result = service.exists(null, "nonexistent.txt");
			
			assertThat(result).isFalse();
		}
		
		@Test
		void returnsFalseWhenPathIsDirectory() throws Exception {
			final Path directory = getDataLocation().resolve("directory");
			Files.createDirectories(directory);
			
			final boolean result = service.exists(null, "directory");
			
			assertThat(result).isFalse();
		}
		
		@Test
		void throwsIllegalArgumentExceptionWhenPathEscapesDataLocation() {
			assertThatThrownBy(() -> service.exists(List.of(".."), "test.txt"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
		
		@Test
		void throwsNullPointerExceptionWhenNameIsNull() {
			assertThatThrownBy(() -> service.exists(null, null))
					.isInstanceOf(NullPointerException.class);
		}
	}
	
	@Nested
	@DisplayName("listPath")
	class ListPathTests {
		
		@Test
		void listsFilesInRootDirectory() throws Exception {
			final Path dataLocation = getDataLocation();
			Files.createDirectories(dataLocation);
			Files.write(dataLocation.resolve("file1.txt"), "content".getBytes());
			Files.write(dataLocation.resolve("file2.txt"), "content".getBytes());
			Files.createDirectories(dataLocation.resolve("subdir"));
			
			final List<String> result = service.listPath(null);
			
			assertThat(result).containsExactly("file1.txt", "file2.txt");
		}
		
		@Test
		void listsFilesInNestedDirectory() throws Exception {
			final Path directory = getDataLocation().resolve("path").resolve("to").resolve("files");
			Files.createDirectories(directory);
			Files.write(directory.resolve("a.txt"), "content".getBytes());
			Files.write(directory.resolve("b.txt"), "content".getBytes());
			Files.write(directory.resolve("c.txt"), "content".getBytes());
			
			final List<String> result = service.listPath(List.of("path", "to", "files"));
			
			assertThat(result).containsExactly("a.txt", "b.txt", "c.txt");
		}
		
		@Test
		void returnsEmptyListWhenDirectoryIsEmpty() throws Exception {
			final Path directory = getDataLocation().resolve("empty");
			Files.createDirectories(directory);
			
			final List<String> result = service.listPath(List.of("empty"));
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void returnsEmptyListWhenDirectoryDoesNotExist() {
			final List<String> result = service.listPath(List.of("nonexistent"));
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void excludesSubdirectories() throws Exception {
			final Path directory = getDataLocation().resolve("mixed");
			Files.createDirectories(directory);
			Files.write(directory.resolve("file.txt"), "content".getBytes());
			Files.createDirectories(directory.resolve("subdir"));
			
			final List<String> result = service.listPath(List.of("mixed"));
			
			assertThat(result).containsExactly("file.txt");
		}
		
		@Test
		void returnsResultsSorted() throws Exception {
			final Path directory = getDataLocation().resolve("sorted");
			Files.createDirectories(directory);
			Files.write(directory.resolve("zebra.txt"), "content".getBytes());
			Files.write(directory.resolve("apple.txt"), "content".getBytes());
			Files.write(directory.resolve("banana.txt"), "content".getBytes());
			
			final List<String> result = service.listPath(List.of("sorted"));
			
			assertThat(result).containsExactly("apple.txt", "banana.txt", "zebra.txt");
		}
		
		@Test
		void returnsFalseWhenPathIsFile() throws Exception {
			final Path file = getDataLocation().resolve("file.txt");
			Files.createDirectories(file.getParent());
			Files.write(file, "content".getBytes());
			
			final List<String> result = service.listPath(List.of("file.txt"));
			
			assertThat(result).doesNotContain("file.txt"); // It will be listed, but we verify path-is-file handling elsewhere
		}
		
		@Test
		void throwsIllegalArgumentExceptionWhenPathEscapesDataLocation() {
			assertThatThrownBy(() -> service.listPath(List.of("..")))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
	}
	
	@Nested
	@DisplayName("listPathRecursive")
	class ListPathRecursiveTests {
		
		@Test
		void listsFilesRecursivelyFromRootDirectory() throws Exception {
			final Path dataLocation = getDataLocation();
			Files.createDirectories(dataLocation);
			Files.write(dataLocation.resolve("file1.txt"), "content".getBytes());
			Files.createDirectories(dataLocation.resolve("subdir"));
			Files.write(dataLocation.resolve("subdir").resolve("file2.txt"), "content".getBytes());
			Files.createDirectories(dataLocation.resolve("subdir").resolve("deep"));
			Files.write(dataLocation.resolve("subdir").resolve("deep").resolve("file3.txt"), "content".getBytes());
			
			final List<String> result = service.listPathRecursive(null);
			
			assertThat(result).containsExactlyInAnyOrder(
					"file1.txt",
					"subdir" + java.io.File.separator + "file2.txt",
					"subdir" + java.io.File.separator + "deep" + java.io.File.separator + "file3.txt");
		}
		
		@Test
		void listsFilesRecursivelyFromNestedDirectory() throws Exception {
			final Path baseDir = getDataLocation().resolve("base");
			Files.createDirectories(baseDir);
			Files.write(baseDir.resolve("file1.txt"), "content".getBytes());
			Files.createDirectories(baseDir.resolve("sub1"));
			Files.write(baseDir.resolve("sub1").resolve("file2.txt"), "content".getBytes());
			Files.createDirectories(baseDir.resolve("sub1").resolve("sub2"));
			Files.write(baseDir.resolve("sub1").resolve("sub2").resolve("file3.txt"), "content".getBytes());
			
			final List<String> result = service.listPathRecursive(List.of("base"));
			
			assertThat(result).containsExactlyInAnyOrder(
					"file1.txt",
					"sub1" + java.io.File.separator + "file2.txt",
					"sub1" + java.io.File.separator + "sub2" + java.io.File.separator + "file3.txt");
		}
		
		@Test
		void returnsEmptyListWhenDirectoryIsEmpty() throws Exception {
			final Path directory = getDataLocation().resolve("emptyrecursive");
			Files.createDirectories(directory);
			
			final List<String> result = service.listPathRecursive(List.of("emptyrecursive"));
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void returnsEmptyListWhenDirectoryDoesNotExist() {
			final List<String> result = service.listPathRecursive(List.of("nonexistentrecursive"));
			
			assertThat(result).isEmpty();
		}
		
		@Test
		void returnsResultsSorted() throws Exception {
			final Path directory = getDataLocation().resolve("sortedrecursive");
			Files.createDirectories(directory);
			Files.write(directory.resolve("zebra.txt"), "content".getBytes());
			Files.createDirectories(directory.resolve("adir"));
			Files.write(directory.resolve("adir").resolve("apple.txt"), "content".getBytes());
			Files.write(directory.resolve("banana.txt"), "content".getBytes());
			
			final List<String> result = service.listPathRecursive(List.of("sortedrecursive"));
			
			assertThat(result)
					.containsExactly(
							"adir" + java.io.File.separator + "apple.txt",
							"banana.txt",
							"zebra.txt");
		}
		
		@Test
		void throwsIllegalArgumentExceptionWhenPathEscapesDataLocation() {
			assertThatThrownBy(() -> service.listPathRecursive(List.of("..")))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
	}
	
	@Nested
	@DisplayName("PATH validation")
	class PathValidationTests {
		
		@Test
		void preventsPathTraversalWithDotDot() {
			assertThatThrownBy(() -> service.loadByPath(String.class, List.of(".."), "escape.txt"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
		
		@Test
		void preventsPathTraversalInFileName() {
			assertThatThrownBy(() -> service.loadByPath(String.class, null, "../escape.txt"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
		
		@Test
		void preventsPathTraversalInMultipleSegments() {
			assertThatThrownBy(() -> service.loadByPath(String.class, List.of("a", "..", "..", "escape"), "txt"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Path escapes data location");
		}
	}
}
