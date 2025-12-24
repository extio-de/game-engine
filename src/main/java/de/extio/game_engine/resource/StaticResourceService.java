package de.extio.game_engine.resource;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface StaticResourceService {
	
	<T> Optional<T> loadByPath(Class<T> clazz, List<String> path, String name);
	
	default <T> Optional<T> loadByPath(final Class<T> clazz, final StaticResource resource) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(resource, "resource");
		return this.loadByPath(clazz, resource.pathSegments(), resource.resourceName());
	}
	
	Optional<InputStream> loadStreamByPath(List<String> path, String name);
	
	default Optional<InputStream> loadStreamByPath(final StaticResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.loadStreamByPath(resource.pathSegments(), resource.resourceName());
	}
	
	boolean exists(List<String> path, String name);
	
	default boolean exists(final StaticResource resource) {
		Objects.requireNonNull(resource, "resource");
		return this.exists(resource.pathSegments(), resource.resourceName());
	}
	
	List<String> listPath(List<String> path);
	
	List<String> listPathRecursive(List<String> path);
	
}
