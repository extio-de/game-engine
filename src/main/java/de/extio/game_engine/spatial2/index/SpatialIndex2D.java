/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.spatial2.index;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.HasPositionAndDimension2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.RectI2;
import de.extio.game_engine.spatial2.model.SpatialIndex2Capable;

/**
 * Spatial indexes are used for spatial queries. See also QuadTree
 */
public interface SpatialIndex2D<T extends SpatialIndex2Capable> extends Iterable<T>, AutoCloseable {
	
	static AtomicLong SESSION = new AtomicLong(1);
	
	/**
	 * Adds an object to the index
	 */
	void add(T obj);
	
	/**
	 * Removes an object from the index
	 */
	void remove(T obj);
	
	/**
	 * Moves an indexed object to a new location. Implementations are usually faster than doing separate remove() and add() operations
	 */
	void move(T obj, CoordI2 oldPosition, CoordI2 oldDimension);
	
	/**
	 * Queries all object intersecting with a given area
	 */
	default Collection<T> find(final RectI2 rect) {
		return this.find(rect.getC0(), ImmutableCoordI2.create(rect.getC1()).substract(rect.getC0()), null);
	}
	
	/**
	 * Queries all object intersecting with a given area
	 */
	default List<T> find(final HasPositionAndDimension2 hasPositionAndDimension2) {
		return this.find(hasPositionAndDimension2.getPosition(), hasPositionAndDimension2.getDimension(), null);
	}
	
	/**
	 * Queries all object intersecting with a given area
	 */
	default List<T> find(final CoordI2 position, final CoordI2 dimension) {
		return this.find(position, dimension, null);
	}
	
	/**
	 * Queries all object intersecting with a given area. Objects can be pre-filtered which is usually faster than returning them first and then filter in your own code.
	 */
	List<T> find(CoordI2 position, CoordI2 dimension, Predicate<T> filter);
	
	/**
	 * Returns the first object at a given position
	 */
	default T findFirstAt(final CoordI2 position) {
		return this.findFirstAt(position, null);
	}
	
	/**
	 * Returns the first object at a given position. Objects can be pre-filtered which is usually faster than returning them first and then filter in your own code.
	 */
	T findFirstAt(CoordI2 position, Predicate<T> filter);
	
	/**
	 * Executes a consumer for all objects at a given position.
	 */
	default void executeAt(final CoordI2 position, final Consumer<T> consumer) {
		this.executeAt(position, consumer, null);
	}
	
	/**
	 * Executes a consumer for all objects at a given position. Objects can be pre-filtered which is usually faster than returning them first and then filter in your own code.
	 */
	void executeAt(CoordI2 position, Consumer<T> consumer, Predicate<T> filter);
	
	/**
	 * Clears and frees internal state
	 */
	@Override
	void close();
	
	/**
	 * Clears but keeps internal state allocated. Optimization for re-use.
	 */
	void clear();
	
}
