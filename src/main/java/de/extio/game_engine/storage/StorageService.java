package de.extio.game_engine.storage;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StorageService {
	
	boolean deleteById(UUID id);
	
	boolean deleteByPath(List<String> path, String name);
	
	List<StorageItemDescriptor> listAll();
	
	List<StorageItemDescriptor> listPath(List<String> path, boolean recursive);
	
	<T> Optional<T> loadById(Class<T> clazz, UUID id);
	
	<T> Optional<T> loadByPath(Class<T> clazz, List<String> path, String name);
	
	Optional<InputStream> loadStreamById(UUID id);
	
	Optional<InputStream> loadStreamByPath(List<String> path, String name);
	
	boolean moveById(UUID id, List<String> newPath, String newName);
	
	boolean moveByPath(List<String> oldPath, String oldName, List<String> newPath, String newName);
	
	Optional<StorageItemDescriptor> searchById(UUID id);
	
	Optional<StorageItemDescriptor> searchByPath(List<String> path, String name);
	
	List<StorageItemDescriptor> searchByPattern(List<String> basePath, String pattern, boolean recursive);
	
	UUID store(String name, List<String> path, Object obj);
	
	UUID storeStream(String name, List<String> path, InputStream inputStream);
	
	StorageOutputStream storeStream(String name, List<String> path);
	
}
