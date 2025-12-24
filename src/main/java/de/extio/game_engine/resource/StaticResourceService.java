package de.extio.game_engine.resource;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface StaticResourceService {
	
	<T> Optional<T> loadByPath(Class<T> clazz, List<String> path, String name);
	
	<T> Optional<T> loadByPath(Class<T> clazz, StaticResource resource);
	
	Optional<InputStream> loadStreamByPath(List<String> path, String name);
	
	Optional<InputStream> loadStreamByPath(StaticResource resource);
	
	boolean exists(List<String> path, String name);
	
	boolean exists(StaticResource resource);
	
	List<String> listPath(List<String> path);
	
	List<String> listPathRecursive(List<String> path);
	
}
