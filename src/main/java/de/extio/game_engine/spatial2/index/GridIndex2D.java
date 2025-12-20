/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.index;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.HasPosition2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;

/**
 * Implements a grid index that can query objects with dimension 1 very fast at the expense of memory consumption. Use QuadTree instead for very large grids or 2 dimensional objects.
 */
public final class GridIndex2D<T extends HasPosition2> implements Iterable<T>, AutoCloseable {
	
	private final List<List<List<T>>> index = new ArrayList<>();
	
	private long modCnt;
	
	public GridIndex2D() {
		
	}
	
	public void add(final T obj) {
		this.ensureBounds(obj.getPosition());
		
		this.index.get(obj.getPosition().getX()).get(obj.getPosition().getY()).add(obj);
		this.modCnt++;
	}
	
	public void addAll(final Iterable<T> objs) {
		objs.forEach(this::add);
	}
	
	public void addAll(final Iterator<T> it) {
		it.forEachRemaining(this::add);
	}
	
	public void remove(final T obj) {
		if (!this.checkBounds(obj.getPosition())) {
			return;
		}
		
		this.removeInternal(obj);
		this.modCnt++;
	}
	
	public List<T> get(final HasPosition2 hasPosition2) {
		final CoordI2 coord = hasPosition2.getPosition();
		if (!this.checkBounds(coord)) {
			return List.of();
		}
		
		return this.index.get(coord.getX()).get(coord.getY());
	}
	
	/**
	 * Clears but keeps internal state allocated
	 */
	public void clear() {
		this.index.forEach(list -> list.forEach(List::clear));
		this.modCnt++;
	}
	
	/**
	 * Clears and frees internal state
	 */
	@Override
	public void close() {
		this.index.clear();
		this.modCnt = 0;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			
			int x;
			
			int y;
			
			int i;
			
			long modCnt;
			
			{
				this.modCnt = GridIndex2D.this.modCnt;
			}
			
			@Override
			public boolean hasNext() {
				return this.x < GridIndex2D.this.index.size();
			}
			
			@Override
			public T next() {
				if (this.modCnt != GridIndex2D.this.modCnt) {
					throw new ConcurrentModificationException();
				}
				
				final T result = GridIndex2D.this.index.get(this.x).get(this.y).get(this.i);
				this.forward();
				return result;
			}
			
			@Override
			public void remove() {
				GridIndex2D.this.removeInternal(GridIndex2D.this.index.get(this.x).get(this.y).get(this.i));
				if (!this.validate()) {
					this.forward();
				}
			}
			
			private void forward() {
				while (this.hasNext()) {
					if (++this.i >= GridIndex2D.this.index.get(this.x).get(this.y).size()) {
						this.i = 0;
						if (++this.y >= GridIndex2D.this.index.get(this.x).size()) {
							this.y = 0;
							this.x++;
						}
					}
					
					if (this.validate()) {
						break;
					}
				}
			}
			
			private boolean validate() {
				return this.x < GridIndex2D.this.index.size() && this.y < GridIndex2D.this.index.get(this.x).size() && this.i < GridIndex2D.this.index.get(this.x).get(this.y).size();
			}
		};
	}
	
	@Override
	public void forEach(final Consumer<? super T> action) {
		for (int x = 0; x < this.index.size(); x++) {
			for (int y = 0; y < this.index.get(x).size(); y++) {
				for (int i = 0; i < this.index.get(x).get(y).size(); i++) {
					action.accept(this.index.get(x).get(y).get(i));
				}
			}
		}
	}
	
	public void forEach(final BiConsumer<CoordI2, List<T>> action) {
		final CoordI2 coord = MutableCoordI2.create();
		
		for (int x = 0; x < this.index.size(); x++) {
			for (int y = 0; y < this.index.get(x).size(); y++) {
				if (this.index.get(x).get(y).size() > 0) {
					coord.setXY(x, y);
					action.accept(coord, this.index.get(x).get(y));
				}
			}
		}
	}
	
	private void ensureBounds(final CoordI2 coord) {
		int delta = this.index.size() - coord.getX();
		if (delta < 1) {
			for (delta = -delta; delta >= 0; delta--) {
				this.index.add(new ArrayList<>());
			}
		}
		
		delta = this.index.get(coord.getX()).size() - coord.getY();
		if (delta < 1) {
			for (delta = -delta; delta >= 0; delta--) {
				this.index.get(coord.getX()).add(new ArrayList<>());
			}
		}
	}
	
	private boolean checkBounds(final CoordI2 coord) {
		return coord.getX() >= 0 && coord.getY() >= 0 && coord.getX() < this.index.size() && coord.getY() < this.index.get(coord.getX()).size();
	}
	
	private void removeInternal(final T obj) {
		this.index.get(obj.getPosition().getX()).get(obj.getPosition().getY()).remove(obj);
	}
	
}
