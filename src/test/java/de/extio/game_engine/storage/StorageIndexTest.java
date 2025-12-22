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
	void listReturnsItemsOnlyAtThatDirectory() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor rootItem = new StorageItemDescriptor(UUID.randomUUID(), "root", List.of());
		final StorageItemDescriptor aItem = new StorageItemDescriptor(UUID.randomUUID(), "a1", List.of("a"));
		final StorageItemDescriptor abItem = new StorageItemDescriptor(UUID.randomUUID(), "ab1", List.of("a", "b"));
		index.add(rootItem);
		index.add(aItem);
		index.add(abItem);
		assertThat(index.list(List.of())).containsExactlyInAnyOrder(rootItem);
		assertThat(index.list(List.of("a"))).containsExactlyInAnyOrder(aItem);
		assertThat(index.list(List.of("a", "b"))).containsExactlyInAnyOrder(abItem);
		assertThat(index.list(List.of("missing"))).isEmpty();
	}
	
	@Test
	void addNormalizesPathSegments() {
		final StorageIndex index = new StorageIndex();
		final StorageItemDescriptor descriptor = new StorageItemDescriptor(UUID.randomUUID(), "X", List.of(" ", "A", " ", "b "));
		index.add(descriptor);
		assertThat(index.find(List.of("a", "b"), "x")).isPresent();
		assertThat(index.find(List.of(" ", "A", "b "), "X")).isPresent();
		assertThat(index.list(List.of("a", "b"))).hasSize(1);
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
		assertThat(index.list(List.of("a", "b"))).isEmpty();
		assertThat(index.list(List.of("a"))).isEmpty();
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
}
