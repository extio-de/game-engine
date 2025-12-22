package de.extio.game_engine.storage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class StorageIndex {
	
	private final Node root;
	private final Map<UUID, IndexedItem> byId;

	public StorageIndex() {
		this.root = new Node();
		this.byId = new HashMap<>();
	}
	
	public int size() {
		return this.byId.size();
	}
	
	public boolean isEmpty() {
		return this.byId.isEmpty();
	}
	
	public void clear() {
		this.root.clear();
		this.byId.clear();
	}
	
	public void add(final StorageItemDescriptor descriptor) {
		Objects.requireNonNull(descriptor, "descriptor");
		Objects.requireNonNull(descriptor.id(), "descriptor.id");
		Objects.requireNonNull(descriptor.name(), "descriptor.name");
		final String normalizedName = this.normalizeName(descriptor.name());
		final List<String> normalizedPath = this.normalizePath(descriptor.path());
		final IndexedItem indexedItem = new IndexedItem(descriptor, normalizedPath, normalizedName);
		this.insert(indexedItem);
	}
	
	public Optional<StorageItemDescriptor> getById(final UUID id) {
		Objects.requireNonNull(id, "id");
		return Optional.ofNullable(this.byId.get(id)).map(IndexedItem::descriptor);
	}
	
	public Optional<StorageItemDescriptor> find(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		final List<String> normalizedPath = this.normalizePath(path);
		final String normalizedName = this.normalizeName(name);
		final Node node = this.getNode(normalizedPath);
		if (node == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(node.itemsByNormalizedName.get(normalizedName)).map(IndexedItem::descriptor);
	}
	
	public List<StorageItemDescriptor> list(final List<String> path) {
		final List<String> normalizedPath = this.normalizePath(path);
		final Node node = this.getNode(normalizedPath);
		if (node == null) {
			return List.of();
		}
		if (node.itemsByNormalizedName.isEmpty()) {
			return List.of();
		}
		return node.itemsByNormalizedName.values().stream().map(IndexedItem::descriptor).toList();
	}
	
	public List<StorageItemDescriptor> findByFilenamePattern(final List<String> basePath, final String pattern, final boolean recursive) {
		Objects.requireNonNull(pattern, "pattern");
		final List<String> normalizedBasePath = this.normalizePath(basePath);
		final Node baseNode = this.getNode(normalizedBasePath);
		if (baseNode == null) {
			return List.of();
		}
		final PathMatcher matcher = this.createCaseInsensitiveFilenameMatcher(pattern);
		final List<StorageItemDescriptor> matches = new ArrayList<>();
		if (!recursive) {
			this.collectMatches(baseNode, matcher, matches);
			return List.copyOf(matches);
		}
		final Deque<Node> work = new ArrayDeque<>();
		work.addLast(baseNode);
		while (!work.isEmpty()) {
			final Node n = work.removeFirst();
			this.collectMatches(n, matcher, matches);
			work.addAll(n.children.values());
		}
		return List.copyOf(matches);
	}
	
	public boolean remove(final UUID id) {
		Objects.requireNonNull(id, "id");
		final IndexedItem indexedItem = this.byId.remove(id);
		if (indexedItem == null) {
			return false;
		}
		final Node node = this.getNode(indexedItem.normalizedPath());
		if (node != null) {
			final IndexedItem existing = node.itemsByNormalizedName.get(indexedItem.normalizedName());
			if (existing != null && existing.descriptor().id().equals(id)) {
				node.itemsByNormalizedName.remove(indexedItem.normalizedName());
				this.pruneEmptyParents(indexedItem.normalizedPath());
			}
		}
		return true;
	}
	
	public boolean remove(final List<String> path, final String name) {
		Objects.requireNonNull(name, "name");
		final List<String> normalizedPath = this.normalizePath(path);
		final String normalizedName = this.normalizeName(name);
		final Node node = this.getNode(normalizedPath);
		if (node == null) {
			return false;
		}
		final IndexedItem removed = node.itemsByNormalizedName.remove(normalizedName);
		if (removed == null) {
			return false;
		}
		this.byId.remove(removed.descriptor().id());
		this.pruneEmptyParents(normalizedPath);
		return true;
	}
	
	private void insert(final IndexedItem indexedItem) {
		final IndexedItem existingById = this.byId.get(indexedItem.descriptor().id());
		if (existingById != null) {
			this.remove(existingById.descriptor().id());
		}
		final Node node = this.getOrCreateNode(indexedItem.normalizedPath());
		final IndexedItem existingByName = node.itemsByNormalizedName.get(indexedItem.normalizedName());
		if (existingByName != null) {
			this.byId.remove(existingByName.descriptor().id());
		}
		node.itemsByNormalizedName.put(indexedItem.normalizedName(), indexedItem);
		this.byId.put(indexedItem.descriptor().id(), indexedItem);
	}
	
	private List<String> normalizePath(final List<String> path) {
		if (path == null || path.isEmpty()) {
			return List.of();
		}
		final List<String> normalized = new ArrayList<>(path.size());
		for (final String segment : path) {
			if (segment == null) {
				throw new IllegalArgumentException("path segment must not be null");
			}
			final String trimmed = segment.trim();
			if (!trimmed.isEmpty()) {
				normalized.add(trimmed.toLowerCase(Locale.ROOT));
			}
		}
		return Collections.unmodifiableList(normalized);
	}
	
	private String normalizeName(final String name) {
		final String trimmed = Objects.requireNonNull(name, "name").trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("name must not be empty");
		}
		return trimmed.toLowerCase(Locale.ROOT);
	}
	
	private PathMatcher createCaseInsensitiveFilenameMatcher(final String pattern) {
		final String trimmed = pattern.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("pattern must not be empty");
		}
		final String syntax;
		final String body;
		if (trimmed.startsWith("glob:") || trimmed.startsWith("regex:")) {
			final int sep = trimmed.indexOf(':');
			syntax = trimmed.substring(0, sep);
			body = trimmed.substring(sep + 1);
		}
		else {
			syntax = "glob";
			body = trimmed;
		}
		final String normalized;
		if ("glob".equals(syntax)) {
			normalized = "glob:" + body.toLowerCase(Locale.ROOT);
		}
		else {
			final String regexBody = body.startsWith("(?") ? body : "(?iu)" + body;
			normalized = "regex:" + regexBody;
		}
		return FileSystems.getDefault().getPathMatcher(normalized);
	}
	
	private void collectMatches(final Node node, final PathMatcher matcher, final List<StorageItemDescriptor> matches) {
		for (final IndexedItem item : node.itemsByNormalizedName.values()) {
			if (matcher.matches(Path.of(item.normalizedName()))) {
				matches.add(item.descriptor());
			}
		}
	}
	
	private Node getNode(final List<String> path) {
		Node current = this.root;
		for (final String segment : path) {
			current = current.children.get(segment);
			if (current == null) {
				return null;
			}
		}
		return current;
	}
	
	private Node getOrCreateNode(final List<String> path) {
		Node current = this.root;
		for (final String segment : path) {
			current = current.children.computeIfAbsent(segment, ignored -> new Node());
		}
		return current;
	}
	
	private void pruneEmptyParents(final List<String> path) {
		if (path.isEmpty()) {
			return;
		}
		final Deque<ParentLink> parents = new ArrayDeque<>();
		Node current = this.root;
		for (final String segment : path) {
			final Node child = current.children.get(segment);
			if (child == null) {
				return;
			}
			parents.addLast(new ParentLink(current, segment, child));
			current = child;
		}
		while (!parents.isEmpty()) {
			final ParentLink link = parents.removeLast();
			if (!link.child.isEmpty()) {
				return;
			}
			link.parent.children.remove(link.segment);
		}
	}
	
	private record ParentLink(Node parent, String segment, Node child) {
	}
	
	private record IndexedItem(StorageItemDescriptor descriptor, List<String> normalizedPath, String normalizedName) {
	}
	
	private static final class Node {
		private final Map<String, Node> children;
		private final Map<String, IndexedItem> itemsByNormalizedName;
		
		private Node() {
			this.children = new HashMap<>();
			this.itemsByNormalizedName = new HashMap<>();
		}
		
		private boolean isEmpty() {
			return this.children.isEmpty() && this.itemsByNormalizedName.isEmpty();
		}
		
		private void clear() {
			this.children.clear();
			this.itemsByNormalizedName.clear();
		}
	}
}
