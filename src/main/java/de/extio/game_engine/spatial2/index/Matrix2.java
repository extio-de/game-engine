package de.extio.game_engine.spatial2.index;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.Edge2;
import de.extio.game_engine.spatial2.model.HasPosition2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

/**
 * Implements a 2D matrix
 */
public final class Matrix2<T> implements Iterable<T> {
	
	private List<List<T>> matrix = new ArrayList<>();
	
	private final CoordI2 dimension = MutableCoordI2.create();
	
	private long modCnt;
	
	public Matrix2() {
		
	}
	
	public Matrix2(final int x, final int y) {
		this.allocate(Math.max(0, x - 1), Math.max(0, y - 1));
	}
	
	public Matrix2(final Matrix2<T> other) {
		this.dimension.setX(other.matrix.size());
		
		this.matrix = new ArrayList<>(other.matrix);
		for (var x = 0; x < this.matrix.size(); x++) {
			final var column = other.matrix.get(x);
			this.matrix.set(x, new ArrayList<>(column));
			
			if (column.size() > this.dimension.getY()) {
				this.dimension.setY(column.size());
			}
		}
	}
	
	/**
	 * Clears and frees internal state
	 */
	public void close() {
		this.matrix.clear();
		this.dimension.setXY(0, 0);
		this.modCnt++;
	}
	
	/**
	 * Clears but keeps internal state allocated
	 */
	public void clear() {
		this.matrix.forEach(col -> col.replaceAll((t) -> null));
		this.modCnt++;
	}
	
	public void put(final HasPosition2 hasPosition, final T obj) {
		this.put(hasPosition.getPosition().getX(), hasPosition.getPosition().getY(), obj);
	}
	
	public void put(final int x, final int y, final T obj) {
		if (x < 0 || y < 0) {
			return;
		}
		
		this.allocate(x, y);
		
		this.matrix.get(x).set(y, obj);
		this.modCnt++;
	}
	
	public void remove(final HasPosition2 hasPosition) {
		this.remove(hasPosition.getPosition().getX(), hasPosition.getPosition().getY());
	}
	
	public void remove(final int x, final int y) {
		if (!this.checkBounds(x, y)) {
			return;
		}
		
		this.matrix.get(x).set(y, null);
		this.modCnt++;
	}
	
	public void removeAll() {
		for (var x = 0; x < this.matrix.size(); x++) {
			for (var y = 0; y < this.matrix.get(x).size(); y++) {
				this.matrix.get(x).set(y, null);
			}
		}
		this.modCnt++;
	}
	
	public T get(final HasPosition2 hasPosition) {
		return this.get(hasPosition.getPosition().getX(), hasPosition.getPosition().getY());
	}
	
	public T get(final int x, final int y) {
		if (!this.checkBounds(x, y)) {
			return null;
		}
		
		return this.matrix.get(x).get(y);
	}
	
	public boolean contains(final HasPosition2 hasPosition) {
		return this.contains(hasPosition.getPosition().getX(), hasPosition.getPosition().getY());
	}
	
	public boolean contains(final int x, final int y) {
		return this.checkBounds(x, y) && this.matrix.get(x).get(y) != null;
	}
	
	// x and y are indices, not the dimension or size!
	public void allocate(final int x, final int y) {
		if (x < 0 || y < 0) {
			return;
		}
		
		var delta = this.matrix.size() - x;
		if (delta < 1) {
			for (delta = -delta; delta >= 0; delta--) {
				final List<T> row = new ArrayList<>(y + 1);
				for (var i = 0; i <= y; i++) {
					row.add(null);
				}
				this.matrix.add(row);
			}
			
			if (x >= this.dimension.getX()) {
				this.dimension.setX(x + 1);
			}
			if (y >= this.dimension.getY()) {
				this.dimension.setY(y + 1);
			}
		}
		
		delta = this.matrix.get(x).size() - y;
		if (delta < 1) {
			for (delta = -delta; delta >= 0; delta--) {
				this.matrix.get(x).add(null);
			}
			
			if (y >= this.dimension.getY()) {
				this.dimension.setY(y + 1);
			}
		}
	}
	
	public void rotate(final Edge2 direction) {
		final List<List<T>> original = new ArrayList<>(this.matrix);
		final CoordI2 origDim = ImmutableCoordI2.create(this.dimension);
		
		this.close();
		this.allocate(origDim.getY() - 1, origDim.getX() - 1);
		
		final CoordI2 posSrc = MutableCoordI2.create();
		for (var x = 0; x < origDim.getY(); x++) {
			for (var y = 0; y < origDim.getX(); y++) {
				if (direction == Edge2.LEFT) {
					posSrc.setXY(origDim.getX() - y - 1, x);
				}
				else {
					posSrc.setXY(y, origDim.getY() - x - 1);
				}
				
				final var originalObjs = original.get(posSrc.getX());
				if (originalObjs.size() > posSrc.getY()) {
					this.matrix.get(x).set(y, originalObjs.get(posSrc.getY()));
				}
			}
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			
			private int x = 0;
			
			private int y = 0;
			
			private T obj = null;
			
			private long modCnt;
			
			{
				this.modCnt = Matrix2.this.modCnt;
			}
			
			@Override
			public boolean hasNext() {
				return this.findNext(false) != null;
			}
			
			@Override
			public T next() {
				try {
					if (this.modCnt != Matrix2.this.modCnt) {
						throw new ConcurrentModificationException();
					}
					
					return this.findNext(true);
				}
				finally {
					this.obj = null;
				}
			}
			
			private T findNext(final boolean nseExc) {
				if (this.obj != null) {
					return this.obj;
				}
				
				var x = this.x;
				var y = this.y;
				for (; x < Matrix2.this.matrix.size(); x++) {
					for (; y < Matrix2.this.matrix.get(x).size(); y++) {
						this.obj = Matrix2.this.matrix.get(x).get(y);
						if (this.obj != null) {
							this.x = x;
							this.y = y + 1;
							if (this.y >= Matrix2.this.matrix.get(x).size()) {
								this.y = 0;
								this.x++;
							}
							
							return this.obj;
						}
					}
					y = 0;
				}
				
				if (nseExc) {
					throw new NoSuchElementException();
				}
				return null;
			}
		};
	}
	
	@Override
	public void forEach(final Consumer<? super T> action) {
		for (var x = 0; x < this.matrix.size(); x++) {
			for (var y = 0; y < this.matrix.get(x).size(); y++) {
				final var obj = this.matrix.get(x).get(y);
				if (obj != null) {
					action.accept(obj);
				}
			}
		}
	}
	
	public void forEach(final BiConsumer<CoordI2, T> consumer) {
		final CoordI2 coord = MutableCoordI2.create();
		
		for (var x = 0; x < this.matrix.size(); x++) {
			for (var y = 0; y < this.matrix.get(x).size(); y++) {
				final var obj = this.matrix.get(x).get(y);
				if (obj != null) {
					coord.setXY(x, y);
					consumer.accept(coord, obj);
				}
			}
		}
	}
	
	public CoordI2 getDimension() {
		return ImmutableCoordI2.create(this.dimension);
	}
	
	private boolean checkBounds(final int x, final int y) {
		return x >= 0 && y >= 0 && x < this.matrix.size() && y < this.matrix.get(x).size();
	}
}
