package de.extio.game_engine.renderer.work;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import de.extio.game_engine.renderer.RendererData;
import de.extio.game_engine.renderer.model.RenderingBo;

public class RenderingBoPoolImpl implements RenderingBoPool {
	
	public final Map<Class<? extends RenderingBo>, Class<? extends RenderingBo>> mapping;
	
	private final Map<Class<? extends RenderingBo>, Stack<RenderingBo>> pool = new HashMap<>();
	
	private final LastMapping lastMapping = new LastMapping();
	
	private final LastReverseMapping lastReverseMapping = new LastReverseMapping();
	
	private RendererData rendererData;
	
	private static class LastMapping {
		
		volatile Class<? extends RenderingBo> clazz;
		
		volatile Class<? extends RenderingBo> impl;
		
		volatile Stack<RenderingBo> pooled;
	}
	
	private static class LastReverseMapping {
		
		volatile Class<? extends RenderingBo> clazz;
		
		volatile Stack<RenderingBo> pooled;
	}
	
	public RenderingBoPoolImpl(final Map<Class<? extends RenderingBo>, Class<? extends RenderingBo>> mapping) {
		this.mapping = mapping;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T extends RenderingBo> T acquire(final String id, final Class<T> clazz) {
		Stack<RenderingBo> pooled;
		Class<? extends RenderingBo> impl;
		
		if (this.lastMapping != null && this.lastMapping.clazz == clazz) {
			impl = this.lastMapping.impl;
			pooled = this.lastMapping.pooled;
		}
		else {
			if (!clazz.isInterface()) {
				impl = clazz;
			}
			else {
				impl = mapping.get(clazz);
				if (impl == null) {
					throw new IllegalArgumentException(clazz.getSimpleName() + " not mapped");
				}
			}
			
			pooled = this.pool.get(impl);
			if (pooled == null) {
				pooled = new Stack<>();
				this.pool.put(impl, pooled);
			}
			
			this.lastMapping.clazz = clazz;
			this.lastMapping.impl = impl;
			this.lastMapping.pooled = pooled;
		}
		
		T instance;
		if (pooled.isEmpty()) {
			try {
				instance = clazz.cast(impl.getDeclaredConstructor().newInstance());
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			instance.setRendererData(this.rendererData);
		}
		else {
			instance = (T) pooled.pop();
		}
		
		instance.setId(id);
		return instance;
	}
	
	@Override
	public <T extends RenderingBo> T copy(final T original) {
		final T copy = this.acquire(original.getId(), (Class<T>) original.getClass());
		copy.apply(original);
		return copy;
	}
	
	@Override
	public synchronized void release(final RenderingBo obj) {
		try {
			obj.close();
			
			final Class<? extends RenderingBo> clazz = obj.getClass();
			Stack<RenderingBo> pooled;
			
			if (this.lastReverseMapping != null && this.lastReverseMapping.clazz == clazz) {
				pooled = this.lastReverseMapping.pooled;
			}
			else {
				pooled = this.pool.get(clazz);
				if (pooled == null) {
					pooled = new Stack<>();
					this.pool.put(clazz, pooled);
				}
				
				this.lastReverseMapping.clazz = clazz;
				this.lastReverseMapping.pooled = pooled;
			}
			
			pooled.push(obj);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void setRendererData(final RendererData rendererData) {
		this.rendererData = rendererData;
	}
	
}
