package de.extio.game_engine.spatial2.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.extio.game_engine.spatial2.SpatialUtils2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.SpatialIndex2Capable;

/**
 * LinearSearchIndex2D doing linear search only. Use it only for very special use cases. Implementation is mainly intended for performance comparison.
 */
public final class LinearSearchIndex2D<T extends SpatialIndex2Capable> implements SpatialIndex2D<T> {
	
	private static final int RESULT_DIM_START = 8;
	
	private static final int RESULT_DIM_MAX = 96;
	
	private List<T> objects = new ArrayList<>();
	
	private int resultDim = RESULT_DIM_START;
	
	@Override
	public Iterator<T> iterator() {
		return this.objects.iterator();
	}
	
	@Override
	public void add(final T obj) {
		this.objects.add(obj);
	}
	
	@Override
	public void remove(final T obj) {
		this.objects.remove(obj);
	}
	
	@Override
	public void move(final T obj, final CoordI2 oldPosition, final CoordI2 oldDimension) {
		
	}
	
	@Override
	public List<T> find(final CoordI2 position, final CoordI2 dimension, final Predicate<T> filter) {
		List<T> result = null;
		
		for (var i = 0; i < this.objects.size(); i++) {
			final var obj = this.objects.get(i);
			
			if (SpatialUtils2.intersects(position, dimension, obj.getPosition(), obj.getDimension())) {
				if (filter == null || filter.test(obj)) {
					if (result == null) {
						result = new ArrayList<>(dimension.equals(ImmutableCoordI2.one()) ? RESULT_DIM_START : this.resultDim);
					}
					result.add(obj);
				}
			}
		}
		
		if (result == null) {
			return List.of();
		}
		if (result.size() > this.resultDim) {
			this.resultDim = Math.min(result.size(), RESULT_DIM_MAX);
		}
		
		return result;
	}
	
	@Override
	public T findFirstAt(final CoordI2 position, final Predicate<T> filter) {
		for (var i = 0; i < this.objects.size(); i++) {
			final var obj = this.objects.get(i);
			
			if (SpatialUtils2.intersects(position, ImmutableCoordI2.one(), obj.getPosition(), obj.getDimension())) {
				if (filter == null || filter.test(obj)) {
					return obj;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void executeAt(final CoordI2 position, final Consumer<T> consumer, final Predicate<T> filter) {
		for (var i = 0; i < this.objects.size(); i++) {
			final var obj = this.objects.get(i);
			
			if (SpatialUtils2.intersects(position, ImmutableCoordI2.one(), obj.getPosition(), obj.getDimension())) {
				if (filter == null || filter.test(obj)) {
					consumer.accept(obj);
				}
			}
		}
	}
	
	@Override
	public void close() {
		this.objects = new ArrayList<>();
		this.resultDim = RESULT_DIM_START;
	}
	
	@Override
	public void clear() {
		this.objects.clear();
	}
	
}
