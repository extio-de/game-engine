package de.extio.game_engine.spatial2.index;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;
import de.extio.game_engine.spatial2.model.RectI2;
import de.extio.game_engine.spatial2.model.SpatialIndex2Capable;

/**
 * This is the preferred SpatialIndex2D implementation to index bigger amounts of spatial data and query them efficiently.
 * You can consider using GridIndex2D instead for small grids with one-dimensional objects only.
 * A quadtree is a tree data structure in which each internal node has exactly four children. Quadtrees are the two-dimensional analog of octrees and are most often used to partition a two-dimensional space by recursively subdividing it into four quadrants or regions.
 */
public final class QuadTree<T extends SpatialIndex2Capable> implements SpatialIndex2D<T> {
	
	private static final int ORDER = 4;
	
	private static final int DEPTH_START = 8;
	
	private static final int RESULT_DIM_START = 6;
	
	private static final int RESULT_DIM_MAX = 96;
	
	private Node<T> root;
	
	private int depth;
	
	private int[] traverseStack;
	
	private Node<T>[] nodesCache;
	
	private int resultDim = RESULT_DIM_START;
	
	private final RectI2 rectCached = new RectI2(MutableCoordI2.create(), MutableCoordI2.create());
	
	private final AtomicReference<List<T>> resultHolder = new AtomicReference<>();
	
	public QuadTree() {
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void add(final T obj) {
		final var rect = this.toRectI2(obj.getPosition(), obj.getDimension());
		this.expandTree(rect);
		
		var node = this.root;
		traversal: while (true) {
			if (node.branches == null) {
				// Add leaf to fresh branch 
				this.addLeave(node, obj);
				if (node.leaves.size() < ORDER) {
					break;
				}
				
				// Overflow -> Split if not already at lowest level
				if (node.createBranches()) {
					final var toSplit = node.leaves;
					if (toSplit != null) {
						node.leaves = null;
						
						splitLeaves: for (var l = 0; l < toSplit.size(); l++) {
							final var leaf = toSplit.get(l);
							
							for (var b = 0; b < ORDER; b++) {
								if (this.contains(node.branches[b], leaf)) {
									this.addLeave(node.branches[b], leaf);
									continue splitLeaves;
								}
							}
							
							this.addLeave(node, leaf);
						}
					}
				}
				
				break;
			}
			else {
				// Traverse
				for (var i = 0; i < ORDER; i++) {
					if (this.contains(node.branches[i], rect)) {
						node = node.branches[i];
						continue traversal;
					}
				}
			}
			
			// Node does not fit into branches -> add to current level
			this.addLeave(node, obj);
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void expandTree(final RectI2 rect) {
		if (this.root == null) {
			final var side = 1 << DEPTH_START;
			this.root = new Node<>(rect.getC0().getX() - side, rect.getC0().getY() - side, side << 1);
			this.root.createBranches();
			this.recalcDepth();
		}
		
		for (var i = 0; !this.contains(this.root, rect); i++) {
			final var expansion = new RectI2(MutableCoordI2.create(), MutableCoordI2.create());
			final var branchesIntersect = new int[4];
			var intersects = 0;
			var containsBranch = -1;
			
			expansion.getC0().setXY(this.root.xMin, this.root.yMin);
			expansion.getC1().setXY(Integer.MAX_VALUE, Integer.MAX_VALUE);
			if (this.contains(expansion, rect)) {
				containsBranch = 0;
			}
			else {
				if (this.intersects(expansion, rect)) {
					branchesIntersect[intersects++] = 0;
				}
				expansion.getC0().setXY(Integer.MIN_VALUE, this.root.yMin);
				expansion.getC1().setXY(this.root.xMax, Integer.MAX_VALUE);
				if (this.contains(expansion, rect)) {
					containsBranch = 2;
				}
				else {
					if (this.intersects(expansion, rect)) {
						branchesIntersect[intersects++] = 2;
					}
					expansion.getC0().setXY(Integer.MIN_VALUE, Integer.MIN_VALUE);
					expansion.getC1().setXY(this.root.xMax, this.root.yMax);
					if (this.contains(expansion, rect)) {
						containsBranch = 3;
					}
					else {
						if (this.intersects(expansion, rect)) {
							branchesIntersect[intersects++] = 3;
						}
						expansion.getC0().setXY(this.root.xMin, Integer.MIN_VALUE);
						expansion.getC1().setXY(Integer.MAX_VALUE, this.root.yMax);
						if (this.contains(expansion, rect)) {
							containsBranch = 1;
						}
						else if (this.intersects(expansion, rect)) {
							branchesIntersect[intersects++] = 1;
						}
					}
				}
			}
			
			Node<T> rootNew = null;
			final var branch = containsBranch != -1 ? containsBranch : branchesIntersect[i % intersects];
			final var size = this.root.xMax - this.root.xMin + 1;
			switch (branch) {
				case 0:
					rootNew = new Node<>(this.root.xMin, this.root.yMin, size << 1);
					break;
				case 1:
					rootNew = new Node<>(this.root.xMin, this.root.yMin - size, size << 1);
					break;
				case 2:
					rootNew = new Node<>(this.root.xMin - size, this.root.yMin, size << 1);
					break;
				case 3:
					rootNew = new Node<>(this.root.xMin - size, this.root.yMin - size, size << 1);
					break;
			}
			
			rootNew.createBranches();
			rootNew.branches[branch] = this.root;
			rootNew.branches[branch].parent = rootNew;
			
			this.root = rootNew;
			this.recalcDepth();
		}
		
		// Alternative approach, center around the origin. In principle very elegant but the tree always expands to all directions whereas the data may only expand to a specific direction
		//		while (!this.contains(this.root, rect)) {
		//			final int side = this.root.xMax - this.root.xMin + 1;
		//			final Node<T> rootNew = new Node<>(this.root.xMin - (side >> 1), this.root.yMin - (side >> 1), side << 1);
		//			rootNew.createBranches();
		//			for (int i = 0; i < ORDER; i++) {
		//				rootNew.branches[i].createBranches();
		//			}
		//			rootNew.branches[0].branches[3] = this.root.branches[0];
		//			rootNew.branches[0].branches[3].parent = rootNew.branches[0];
		//			rootNew.branches[1].branches[2] = this.root.branches[1];
		//			rootNew.branches[1].branches[2].parent = rootNew.branches[1];
		//			rootNew.branches[2].branches[1] = this.root.branches[2];
		//			rootNew.branches[2].branches[1].parent = rootNew.branches[2];
		//			rootNew.branches[3].branches[0] = this.root.branches[3];
		//			rootNew.branches[3].branches[0].parent = rootNew.branches[3];
		//			rootNew.leaves = this.root.leaves;
		//			
		//			this.root = rootNew;
		//			this.recalcDepth();
		//		}
	}
	
	@SuppressWarnings("unchecked")
	private void recalcDepth() {
		this.depth = Integer.numberOfTrailingZeros(Integer.highestOneBit(this.root.xMax - this.root.xMin)) + 1;
		this.traverseStack = new int[this.depth];
		this.nodesCache = new Node[this.depth];
	}
	
	private void addLeave(final Node<T> node, final T obj) {
		if (node.leaves == null) {
			node.leaves = new ArrayList<>();
		}
		node.leaves.add(obj);
	}
	
	@Override
	public void remove(final T obj) {
		final var rect = this.toRectI2Bounded(obj.getPosition(), obj.getDimension());
		if (rect == null) {
			return;
		}
		
		this.remove(obj, rect);
	}
	
	@SuppressWarnings("unchecked")
	private void remove(final T obj, final RectI2 rect) {
		var node = this.root;
		traversal: while (true) {
			if (node.leaves != null) {
				node.leaves.remove(obj);
				if (node.leaves.isEmpty()) {
					node.leaves = null;
				}
				
				this.shrink(node);
			}
			
			if (node.branches != null) {
				for (var i = 0; i < ORDER; i++) {
					if (this.contains(node.branches[i], rect)) {
						node = node.branches[i];
						continue traversal;
					}
				}
			}
			
			break;
		}
	}
	
	private void shrink(final Node<T> node_) {
		var node = node_;
		while (node.parent != null) {
			for (var i = 0; i < ORDER; i++) {
				if (node.parent.branches[i].leaves != null || node.parent.branches[i].branches != null) {
					return;
				}
			}
			
			node.parent.branches = null;
			node = node.parent;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void move(final T obj, final CoordI2 oldPosition, final CoordI2 oldDimension) {
		final var rect = this.toRectI2Bounded(oldPosition, oldDimension);
		if (rect != null) {
			var nodes = 0;
			var node = this.root;
			traversal: while (true) {
				if (node.leaves != null) {
					this.nodesCache[nodes++] = node;
				}
				
				if (node.branches != null) {
					for (var i = 0; i < ORDER; i++) {
						if (this.contains(node.branches[i], rect)) {
							node = node.branches[i];
							continue traversal;
						}
					}
				}
				
				if (!this.contains(node, obj)) {
					while (nodes-- > 0) {
						final var curNode = this.nodesCache[nodes];
						if (curNode.leaves != null) {
							curNode.leaves.remove(obj);
							if (curNode.leaves.isEmpty()) {
								curNode.leaves = null;
							}
						}
					}
					this.shrink(node);
					
					this.add(obj);
				}
				
				break;
			}
		}
		else {
			this.add(obj);
		}
	}
	
	@Override
	public List<T> find(final CoordI2 position, final CoordI2 dimension, final Predicate<T> filter) {
		if (dimension.equals(ImmutableCoordI2.one())) {
			return this.findDim1(position, filter);
		}
		
		final var rect = this.toRectI2Bounded(position, dimension);
		if (rect == null) {
			return List.of();
		}
		
		return this.findDimN(rect, filter);
	}
	
	private List<T> findDim1(final CoordI2 position, final Predicate<T> filter) {
		this.resultHolder.set(null);
		
		this.executeAt(position, obj -> {
			if (this.resultHolder.get() == null) {
				this.resultHolder.set(new ArrayList<>(RESULT_DIM_START));
			}
			this.resultHolder.get().add(obj);
		}, filter);
		
		final var result = this.resultHolder.get();
		if (result == null) {
			return List.of();
		}
		this.resultHolder.set(null);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private List<T> findDimN(final RectI2 rect, final Predicate<T> filter) {
		List<T> result = null;
		
		var level = 0;
		var branch = 0;
		var node = this.root;
		
		traversal: while (true) {
			if (node.branches != null) {
				for (var b = branch; b < ORDER; b++) {
					if (this.intersects(node.branches[b], rect)) {
						node = node.branches[b];
						this.traverseStack[level++] = b + 1;
						branch = 0;
						
						continue traversal;
					}
				}
			}
			
			if (node.leaves != null) {
				for (var i = 0; i < node.leaves.size(); i++) {
					final var obj = node.leaves.get(i);
					if (this.intersects(obj, rect) && (filter == null || filter.test((T) obj))) {
						if (result == null) {
							result = new ArrayList<>(this.resultDim);
						}
						result.add(obj);
					}
				}
			}
			
			if (node.parent == null) {
				break;
			}
			node = node.parent;
			branch = this.traverseStack[--level];
		}
		
		if (result == null) {
			return List.of();
		}
		if (result.size() > this.resultDim) {
			this.resultDim = Math.min(result.size(), RESULT_DIM_MAX);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T findFirstAt(final CoordI2 position, final Predicate<T> filter) {
		final var rect = this.toRectI2Bounded(position, ImmutableCoordI2.one());
		if (rect == null) {
			return null;
		}
		
		var node = this.root;
		traversal: while (true) {
			if (node.leaves != null) {
				for (var i = 0; i < node.leaves.size(); i++) {
					final var obj = node.leaves.get(i);
					if (this.contains(obj, rect) && (filter == null || filter.test((T) obj))) {
						return obj;
					}
				}
			}
			
			if (node.branches != null) {
				for (var i = 0; i < ORDER; i++) {
					if (this.contains(node.branches[i], rect)) {
						node = node.branches[i];
						continue traversal;
					}
				}
			}
			
			break;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAt(final CoordI2 position, final Consumer<T> consumer, final Predicate<T> filter) {
		final var rect = this.toRectI2Bounded(position, ImmutableCoordI2.one());
		if (rect == null) {
			return;
		}
		
		var node = this.root;
		traversal: while (true) {
			if (node.leaves != null) {
				for (var i = 0; i < node.leaves.size(); i++) {
					final var obj = node.leaves.get(i);
					if (this.contains(obj, rect) && (filter == null || filter.test((T) obj))) {
						consumer.accept(obj);
					}
				}
			}
			
			if (node.branches != null) {
				for (var i = 0; i < ORDER; i++) {
					if (this.contains(node.branches[i], rect)) {
						node = node.branches[i];
						continue traversal;
					}
				}
			}
			
			break;
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			
			private final Deque<Integer> branchStack = new ArrayDeque<>(QuadTree.this.depth);
			
			private T next;
			
			private Node<T> node = QuadTree.this.root;
			
			private int branch;
			
			private int leaf;
			
			@SuppressWarnings("unchecked")
			@Override
			public boolean hasNext() {
				if (this.next != null) {
					return true;
				}
				
				traversal: while (true) {
					if (this.node.branches != null) {
						if (this.branch < ORDER) {
							this.node = this.node.branches[this.branch];
							this.branchStack.addFirst(this.branch + 1);
							this.branch = 0;
							continue traversal;
						}
					}
					
					if (this.node.leaves != null) {
						if (this.leaf < this.node.leaves.size()) {
							final var obj = this.node.leaves.get(this.leaf++);
							this.next = obj;
							break traversal;
						}
						this.leaf = 0;
					}
					
					if (this.node.parent == null) {
						break;
					}
					this.node = this.node.parent;
					this.branch = this.branchStack.removeFirst();
				}
				
				return this.next != null;
			}
			
			@Override
			public T next() {
				try {
					return this.next;
				}
				finally {
					this.next = null;
				}
			}
			
			@Override
			public void remove() {
				if (this.leaf > 0 && this.leaf <= this.node.leaves.size()) {
					this.node.leaves.remove(this.leaf-- - 1);
				}
			}
		};
	}
	
	@Override
	public void forEach(final Consumer<? super T> action) {
		this.traverseAll(node -> {
			if (node.leaves != null) {
				for (var i = 0; i < node.leaves.size(); i++) {
					final var obj = node.leaves.get(i);
					action.accept(obj);
				}
			}
		});
	}
	
	@Override
	public void close() {
		this.resultDim = RESULT_DIM_START;
		this.traverseStack = null;
		this.nodesCache = null;
		this.root = null;
	}
	
	@Override
	public void clear() {
		this.traverseAll(node -> {
			if (node.leaves != null) {
				node.leaves = null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private void traverseAll(final Consumer<Node<T>> consumer) {
		if (this.root == null) {
			return;
		}
		
		var level = 0;
		var branch = 0;
		var node = this.root;
		
		traversal: while (true) {
			if (node.branches != null && branch < ORDER) {
				node = node.branches[branch];
				this.traverseStack[level++] = branch + 1;
				branch = 0;
				
				continue traversal;
			}
			
			consumer.accept(node);
			
			if (node.parent == null) {
				break;
			}
			node = node.parent;
			branch = this.traverseStack[--level];
		}
	}
	
	private RectI2 toRectI2(final CoordI2 position, final CoordI2 dimension) {
		this.rectCached.getC0().setXY(position.getX(), position.getY());
		this.rectCached.getC1().setXY(position.getX() + dimension.getX() - 1, position.getY() + dimension.getY() - 1);
		
		return this.rectCached;
	}
	
	private RectI2 toRectI2Bounded(final CoordI2 position, final CoordI2 dimension) {
		if (this.root == null) {
			return null;
		}
		
		this.rectCached.getC0().setXY(Math.max(position.getX(), this.root.xMin), Math.max(position.getY(), this.root.yMin));
		this.rectCached.getC1().setXY(Math.min(position.getX() + dimension.getX() - 1, this.root.xMax), Math.min(position.getY() + dimension.getY() - 1, this.root.yMax));
		
		if (this.rectCached.getC1().getX() - this.rectCached.getC0().getX() < 0 || this.rectCached.getC1().getY() - this.rectCached.getC0().getY() < 0) {
			return null;
		}
		return this.rectCached;
	}
	
	private boolean contains(final Node<T> node, final RectI2 rect) {
		return rect.getC0().getX() >= node.xMin &&
				rect.getC0().getY() >= node.yMin &&
				rect.getC1().getX() <= node.xMax &&
				rect.getC1().getY() <= node.yMax;
	}
	
	private boolean contains(final Node<T> node, final T obj) {
		return obj.getPosition().getX() >= node.xMin &&
				obj.getPosition().getY() >= node.yMin &&
				obj.getPosition().getX() + obj.getDimension().getX() - 1 <= node.xMax &&
				obj.getPosition().getY() + obj.getDimension().getY() - 1 <= node.yMax;
	}
	
	private boolean contains(final T obj, final RectI2 rect) {
		return rect.getC0().getX() >= obj.getPosition().getX() &&
				rect.getC0().getY() >= obj.getPosition().getY() &&
				rect.getC1().getX() <= obj.getPosition().getX() + obj.getDimension().getX() - 1 &&
				rect.getC1().getY() <= obj.getPosition().getY() + obj.getDimension().getY() - 1;
	}
	
	private boolean contains(final RectI2 rect0, final RectI2 rect1) {
		return rect1.getC0().getX() >= rect0.getC0().getX() &&
				rect1.getC0().getY() >= rect0.getC0().getY() &&
				rect1.getC1().getX() <= rect0.getC1().getX() &&
				rect1.getC1().getY() <= rect0.getC1().getY();
	}
	
	public boolean intersects(final Node<T> node, final RectI2 rect) {
		return ((node.xMax + 1 < node.xMin || node.xMax >= rect.getC0().getX()) &&
				(node.yMax + 1 < node.yMin || node.yMax >= rect.getC0().getY()) &&
				(rect.getC1().getX() + 1 < rect.getC0().getX() || rect.getC1().getX() >= node.xMin) &&
				(rect.getC1().getY() + 1 < rect.getC0().getY() || rect.getC1().getY() >= node.yMin));
	}
	
	public boolean intersects(final T obj, final RectI2 rect) {
		final var oUpperX = obj.getDimension().getX() + obj.getPosition().getX();
		final var oUpperY = obj.getDimension().getY() + obj.getPosition().getY();
		return ((oUpperX < obj.getPosition().getX() || oUpperX > rect.getC0().getX()) &&
				(oUpperY < obj.getPosition().getY() || oUpperY > rect.getC0().getY()) &&
				(rect.getC1().getX() + 1 < rect.getC0().getX() || rect.getC1().getX() >= obj.getPosition().getX()) &&
				(rect.getC1().getY() + 1 < rect.getC0().getY() || rect.getC1().getY() >= obj.getPosition().getY()));
	}
	
	public boolean intersects(final RectI2 rect0, final RectI2 rect1) {
		return ((rect0.getC1().getX() + 1 < rect0.getC0().getX() || rect0.getC1().getX() >= rect1.getC0().getX()) &&
				(rect0.getC1().getY() + 1 < rect0.getC0().getY() || rect0.getC1().getY() >= rect1.getC0().getY()) &&
				(rect1.getC1().getX() + 1 < rect1.getC0().getX() || rect1.getC1().getX() >= rect0.getC0().getX()) &&
				(rect1.getC1().getY() + 1 < rect1.getC0().getY() || rect1.getC1().getY() >= rect0.getC0().getY()));
	}
	
	private static class Node<T extends SpatialIndex2Capable> {
		
		int xMin;
		
		int xMax;
		
		int yMin;
		
		int yMax;
		
		List<T> leaves;
		
		@SuppressWarnings("rawtypes")
		Node[] branches;
		
		Node<T> parent;
		
		Node(final int xMin, final int yMin, final int size) {
			this.xMin = xMin;
			this.xMax = xMin + size - 1;
			this.yMin = yMin;
			this.yMax = yMin + size - 1;
		}
		
		Node(final int xMin, final int xMax, final int yMin, final int yMax, final Node<T> parent) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.parent = parent;
		}
		
		boolean createBranches() {
			final var partition = (this.xMax - this.xMin + 1) >> 1;
			if (partition == 0) {
				return false;
			}
			
			this.branches = new Node[ORDER];
			this.branches[0] = new Node<>(this.xMin, this.xMin + partition - 1, this.yMin, this.yMin + partition - 1, this);
			this.branches[1] = new Node<>(this.xMin, this.xMin + partition - 1, this.yMin + partition, this.yMax, this);
			this.branches[2] = new Node<>(this.xMin + partition, this.xMax, this.yMin, this.yMin + partition - 1, this);
			this.branches[3] = new Node<>(this.xMin + partition, this.xMax, this.yMin + partition, this.yMax, this);
			
			return true;
		}
		
		@Override
		public String toString() {
			final var builder = new StringBuilder();
			builder.append("Node [xMin=");
			builder.append(this.xMin);
			builder.append(", xMax=");
			builder.append(this.xMax);
			builder.append(", yMin=");
			builder.append(this.yMin);
			builder.append(", yMax=");
			builder.append(this.yMax);
			builder.append("]");
			return builder.toString();
		}
	}
	
}
