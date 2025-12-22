package de.extio.game_engine.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class StorageIndexTest {
	
	@Test
	void addAndFindAtPath() {
		final StorageIndex index = new StorageIndex();
		final UUID id = UUID.randomUUID();
		final StorageItemDescriptor descriptor = new StorageItemDescriptor(id, "File.TXT", List.of("A", "B"));
		index.add(descriptor);
		assertThat(index.size()).isEqualTo(1);
		assertThat(index.find(List.of("a", "b"), "file.txt")).contains(descriptor);
		assertThat(index.find(List.of("A", "B"), "FILE.txt")).contains(descriptor);
		assertThat(index.find(List.of("a"), "file.txt")).isEmpty();
	}
	
	@Test
	void listNonRecursiveReturnsItemsOnlyAtThatDirectory() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor rootItem = new StorageItemDescriptor(UUID.randomUUID(), "root", List.of());
		final StorageItemDescriptor aItem = new StorageItemDescriptor(UUID.randomUUID(), "a1", List.of("a"));
		final StorageItemDescriptor abItem = new StorageItemDescriptor(UUID.randomUUID(), "ab1", List.of("a", "b"));
		index.add(rootItem);
		index.add(aItem);
		index.add(abItem);
		assertThat(index.list(List.of(), false)).containsExactlyInAnyOrder(rootItem);
		assertThat(index.list(List.of("a"), false)).containsExactlyInAnyOrder(aItem);
		assertThat(index.list(List.of("a", "b"), false)).containsExactlyInAnyOrder(abItem);
		assertThat(index.list(List.of("missing"), false)).isEmpty();
	}
	
	@Test
	void addNormalizesPathSegments() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor descriptor = new StorageItemDescriptor(UUID.randomUUID(), "X", List.of(" ", "A", " ", "b "));
		index.add(descriptor);
		assertThat(index.find(List.of("a", "b"), "x")).isPresent();
		assertThat(index.find(List.of(" ", "A", "b "), "X")).isPresent();
		assertThat(index.list(List.of("a", "b"), false)).hasSize(1);
	}
	
	@Test
	void addRejectsNullPathSegment() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor descriptor = new StorageItemDescriptor(UUID.randomUUID(), "x", Arrays.asList("a", null));
		assertThatThrownBy(() -> index.add(descriptor)).isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	void addReplacesExistingById() {
		final StorageIndex index = new StorageIndex();
		final UUID id = UUID.randomUUID();
		final StorageItemDescriptor first = new StorageItemDescriptor(id, "one", List.of("a"));
		final StorageItemDescriptor second = new StorageItemDescriptor(id, "two", List.of("b"));
		index.add(first);
		index.add(second);
		assertThat(index.size()).isEqualTo(1);
		assertThat(index.find(List.of("a"), "one")).isEmpty();
		assertThat(index.find(List.of("b"), "two")).contains(second);
		assertThat(index.getById(id)).contains(second);
	}
	
	@Test
	void addReplacesExistingByNameWithinSameDirectory() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor first = new StorageItemDescriptor(UUID.randomUUID(), "same", List.of("a"));
		final StorageItemDescriptor second = new StorageItemDescriptor(UUID.randomUUID(), "same", List.of("a"));
		index.add(first);
		index.add(second);
		assertThat(index.size()).isEqualTo(1);
		assertThat(index.find(List.of("a"), "same")).contains(second);
		assertThat(index.getById(first.id())).isEmpty();
		assertThat(index.getById(second.id())).contains(second);
	}
	
	@Test
	void removeByIdRemovesAndPrunesEmptyDirectories() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor leaf = new StorageItemDescriptor(UUID.randomUUID(), "leaf", List.of("a", "b"));
		index.add(leaf);
		assertThat(index.remove(leaf.id())).isTrue();
		assertThat(index.size()).isZero();
		assertThat(index.find(List.of("a", "b"), "leaf")).isEmpty();
		assertThat(index.list(List.of("a", "b"), false)).isEmpty();
		assertThat(index.list(List.of("a"), false)).isEmpty();
	}
	
	@Test
	void removeByPathAndNameRemovesAndKeepsOtherSiblings() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor a = new StorageItemDescriptor(UUID.randomUUID(), "a", List.of("x"));
		final StorageItemDescriptor b = new StorageItemDescriptor(UUID.randomUUID(), "b", List.of("x"));
		index.add(a);
		index.add(b);
		assertThat(index.remove(List.of("x"), "a")).isTrue();
		assertThat(index.find(List.of("x"), "a")).isEmpty();
		assertThat(index.find(List.of("x"), "b")).contains(b);
		assertThat(index.size()).isEqualTo(1);
	}
	
	@Test
	void findByFilenamePatternNonRecursive() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor inBase1 = new StorageItemDescriptor(UUID.randomUUID(), "hero.png", List.of("assets"));
		final StorageItemDescriptor inBase2 = new StorageItemDescriptor(UUID.randomUUID(), "hero.txt", List.of("assets"));
		final StorageItemDescriptor inChild = new StorageItemDescriptor(UUID.randomUUID(), "hero.jpg", List.of("assets", "portraits"));
		index.add(inBase1);
		index.add(inBase2);
		index.add(inChild);
		final var matches = index.findByFilenamePattern(List.of("ASSETS"), "glob:*.png", false);
		assertThat(matches).containsExactlyInAnyOrder(inBase1);
	}
	
	@Test
	void findByFilenamePatternRecursive() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor inBase = new StorageItemDescriptor(UUID.randomUUID(), "HERO.PNG", List.of("assets"));
		final StorageItemDescriptor inChild1 = new StorageItemDescriptor(UUID.randomUUID(), "hero.jpg", List.of("assets", "portraits"));
		final StorageItemDescriptor inChild2 = new StorageItemDescriptor(UUID.randomUUID(), "villain.png", List.of("assets", "portraits", "alt"));
		final StorageItemDescriptor outside = new StorageItemDescriptor(UUID.randomUUID(), "hero.png", List.of("other"));
		index.add(inBase);
		index.add(inChild1);
		index.add(inChild2);
		index.add(outside);
		final var matches = index.findByFilenamePattern(List.of("assets"), "glob:*.png", true);
		assertThat(matches).containsExactlyInAnyOrder(inBase, inChild2);
	}
	
	@Test
	void listRecursiveReturnsAllItemsInDirectoryAndSubdirectories() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor rootItem = new StorageItemDescriptor(UUID.randomUUID(), "root", List.of());
		final StorageItemDescriptor aItem = new StorageItemDescriptor(UUID.randomUUID(), "a1", List.of("a"));
		final StorageItemDescriptor abItem = new StorageItemDescriptor(UUID.randomUUID(), "ab1", List.of("a", "b"));
		final StorageItemDescriptor abcItem = new StorageItemDescriptor(UUID.randomUUID(), "abc1", List.of("a", "b", "c"));
		final StorageItemDescriptor otherItem = new StorageItemDescriptor(UUID.randomUUID(), "other", List.of("other"));
		index.add(rootItem);
		index.add(aItem);
		index.add(abItem);
		index.add(abcItem);
		index.add(otherItem);
		assertThat(index.list(List.of(), true)).containsExactlyInAnyOrder(rootItem, aItem, abItem, abcItem, otherItem);
		assertThat(index.list(List.of("a"), true)).containsExactlyInAnyOrder(aItem, abItem, abcItem);
		assertThat(index.list(List.of("a", "b"), true)).containsExactlyInAnyOrder(abItem, abcItem);
		assertThat(index.list(List.of("a", "b", "c"), true)).containsExactlyInAnyOrder(abcItem);
		assertThat(index.list(List.of("missing"), true)).isEmpty();
	}
	
	@Test
	void listRecursiveNonExistentPathReturnsEmpty() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor item = new StorageItemDescriptor(UUID.randomUUID(), "file", List.of("a"));
		index.add(item);
		assertThat(index.list(List.of("nonexistent"), true)).isEmpty();
	}
	
	@Test
	void listRecursiveAndNonRecursiveAreConsistent() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor aItem = new StorageItemDescriptor(UUID.randomUUID(), "a1", List.of("a"));
		final StorageItemDescriptor abItem = new StorageItemDescriptor(UUID.randomUUID(), "ab1", List.of("a", "b"));
		index.add(aItem);
		index.add(abItem);
		assertThat(index.list(List.of("a"), false)).containsExactly(aItem);
		assertThat(index.list(List.of("a"), true)).containsExactlyInAnyOrder(aItem, abItem);
	}
	
	@Test
	void listAllReturnsAllItemsInIndex() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor root = new StorageItemDescriptor(UUID.randomUUID(), "root", List.of());
		final StorageItemDescriptor a = new StorageItemDescriptor(UUID.randomUUID(), "a", List.of("a"));
		final StorageItemDescriptor ab = new StorageItemDescriptor(UUID.randomUUID(), "ab", List.of("a", "b"));
		final StorageItemDescriptor other = new StorageItemDescriptor(UUID.randomUUID(), "other", List.of("x", "y"));
		index.add(root);
		index.add(a);
		index.add(ab);
		index.add(other);
		assertThat(index.listAll()).containsExactlyInAnyOrder(root, a, ab, other);
	}
	
	@Test
	void listAllOnEmptyIndexReturnsEmpty() {
		final StorageIndex index = new StorageIndex();
		assertThat(index.listAll()).isEmpty();
	}
	
	@Test
	void listAllIsCopyOfInternalState() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor item = new StorageItemDescriptor(UUID.randomUUID(), "file", List.of("a"));
		index.add(item);
		final var list1 = index.listAll();
		index.add(new StorageItemDescriptor(UUID.randomUUID(), "file2", List.of("b")));
		final var list2 = index.listAll();
		assertThat(list1).hasSize(1);
		assertThat(list2).hasSize(2);
	}
}
