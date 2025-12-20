package de.extio.game_engine.keyboard;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeycodeRegistryImpl implements KeycodeRegistry {
	
	private volatile Map<String, KeycodeRegistration> registry;
	
	@Override
	public void register(final KeycodeRegistration... keycodeRegistrations) {
		this.lazyLoadRegistry();
		
		for (final KeycodeRegistration keycodeRegistration : keycodeRegistrations) {
			this.registry.put(keycodeRegistration.getQualifier(), new KeycodeRegistration(keycodeRegistration));
		}
		
//		EngineFacade.instance().storePersistentClientState();
	}
	
	@Override
	public void registerDefault(final KeycodeRegistration... keycodeRegistrations) {
		this.lazyLoadRegistry();
		
		boolean added = false;
		for (final KeycodeRegistration keycodeRegistration : keycodeRegistrations) {
			added |= this.registry.putIfAbsent(keycodeRegistration.getQualifier(), new KeycodeRegistration(keycodeRegistration)) == null;
		}
		
		if (added) {
//			EngineFacade.instance().storePersistentClientState();
		}
	}
	
	@Override
	public void unregister(final String... qualifiers) {
		this.lazyLoadRegistry();
		
		for (final String qualifier : qualifiers) {
			this.registry.remove(qualifier);
		}
		
//		EngineFacade.instance().storePersistentClientState();
	}
	
	@Override
	public KeycodeRegistration get(final String qualifier) {
		this.lazyLoadRegistry();
		
		return this.registry.get(qualifier);
	}
	
	@Override
	public List<KeycodeRegistration> get(final int code) {
		this.lazyLoadRegistry();
		
		return this.registry.values()
				.stream()
				.filter(reg -> reg.getCode() == code)
				.toList();
	}
	
	@Override
	public Collection<KeycodeRegistration> getAll() {
		this.lazyLoadRegistry();
		
		return this.registry.values();
	}
	
	@Override
	public boolean check(final String qualifier, final int code, final int modifiers) {
		this.lazyLoadRegistry();
		
		final KeycodeRegistration reg = this.registry.get(qualifier);
		if (reg == null) {
			return false;
		}
		
		return reg.getCode() == code && reg.getModifier() == modifiers;
	}
	
	@SuppressWarnings("unchecked")
	private void lazyLoadRegistry() {
		if (this.registry == null) {
			synchronized (this) {
				if (this.registry == null) {
//					this.registry = (Map<String, KeycodeRegistration>) this.clientEngineData.getPersistentClientState().state.get("KeycodeRegistry");
//					if (this.registry == null) {
						this.registry = Collections.synchronizedMap(new LinkedHashMap<>());
//						this.clientEngineData.getPersistentClientState().state.put("KeycodeRegistry", this.registry);
//						EngineFacade.instance().storePersistentClientState();
//					}
				}
			}
		}
	}
	
}
