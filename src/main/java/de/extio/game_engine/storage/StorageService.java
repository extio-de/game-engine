package de.extio.game_engine.storage;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public interface StorageService {
	
	boolean deleteById(UUID id);
	
	boolean deleteByPath(List<String> path, String name);
	
	default boolean deleteByPath(final StorageResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.deleteByPath(resource.pathSegments(), resource.resourceName());
	}
	
	List<StorageItemDescriptor> listAll();
	
	List<StorageItemDescriptor> listPath(List<String> path, boolean recursive);
	
	<T> Optional<T> loadById(Class<T> clazz, UUID id);
	
	<T> Optional<T> loadByPath(Class<T> clazz, List<String> path, String name);
	
	default <T> Optional<T> loadByPath(final Class<T> clazz, final StorageResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.loadByPath(clazz, resource.pathSegments(), resource.resourceName());
	}
	
	Optional<InputStream> loadStreamById(UUID id);
	
	Optional<InputStream> loadStreamByPath(List<String> path, String name);
	
	default Optional<InputStream> loadStreamByPath(final StorageResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.loadStreamByPath(resource.pathSegments(), resource.resourceName());
	}
	
	boolean moveById(UUID id, List<String> newPath, String newName);
	
	default boolean moveById(final UUID id, final StorageResource newResource) {
		Objects.requireNonNull(newResource, "newResource");
		return this.moveById(id, newResource.pathSegments(), newResource.resourceName());
	}
	
	boolean moveByPath(List<String> oldPath, String oldName, List<String> newPath, String newName);
	
	default boolean moveByPath(final StorageResource oldResource, final StorageResource newResource) {
		Objects.requireNonNull(oldResource, "oldResource");
		Objects.requireNonNull(newResource, "newResource");
		return this.moveByPath(oldResource.pathSegments(), oldResource.resourceName(), newResource.pathSegments(), newResource.resourceName());
	}
	
	Optional<StorageItemDescriptor> searchById(UUID id);
	
	Optional<StorageItemDescriptor> searchByPath(List<String> path, String name);
	
	default Optional<StorageItemDescriptor> searchByPath(final StorageResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.searchByPath(resource.pathSegments(), resource.resourceName());
	}
	
	List<StorageItemDescriptor> searchByPattern(List<String> basePath, String pattern, boolean recursive);
	
	UUID store(List<String> path, String name, Object obj);
	
	default UUID store(final StorageResource resource, final Object obj) {
		Objects.requireNonNull(resource, "resource");
		return this.store(resource.pathSegments(), resource.resourceName(), obj);
	}
	
	UUID storeStream(List<String> path, String name, InputStream inputStream);
	
	default UUID storeStream(final StorageResource resource, final InputStream inputStream) {
		Objects.requireNonNull(resource, "resource");
		return this.storeStream(resource.pathSegments(), resource.resourceName(), inputStream);
	}
	
	StorageOutputStream storeStream(List<String> path, String name);
	
	default StorageOutputStream storeStream(final StorageResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.storeStream(resource.pathSegments(), resource.resourceName());
	}
	
}
