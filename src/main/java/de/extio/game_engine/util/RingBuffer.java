package de.extio.game_engine.util;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A fixed-size circular buffer implementation that implements {@link java.util.Queue}.
 * When the buffer is full, adding new elements will overwrite the oldest elements.
 * 
 * <p>This implementation is not thread-safe. If multiple threads access a RingBuffer
 * concurrently, it must be synchronized externally.</p>
 * 
 * @param <E> the type of elements held in this collection
 */
public class RingBuffer<E> extends AbstractQueue<E> {
	
	private final E[] buffer;
	
	private int head;
	
	private int tail;
	
	private int size;
	
	private final int capacity;
	
	/**
	 * Creates a RingBuffer with the specified capacity.
	 * 
	 * @param capacity the maximum number of elements the buffer can hold
	 * @throws IllegalArgumentException if capacity is not positive
	 */
	@SuppressWarnings("unchecked")
	public RingBuffer(final int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Capacity must be positive");
		}
		this.capacity = capacity;
		this.buffer = (E[]) new Object[capacity];
		this.head = 0;
		this.tail = 0;
		this.size = 0;
	}
	
	@Override
	public boolean offer(final E e) {
		if (e == null) {
			throw new NullPointerException("Null elements are not allowed");
		}
		
		this.buffer[this.tail] = e;
		this.tail = (this.tail + 1) % this.capacity;
		
		if (this.size < this.capacity) {
			this.size++;
		}
		else {
			// Buffer is full, overwrite oldest element
			this.head = (this.head + 1) % this.capacity;
		}
		
		return true;
	}
	
	@Override
	public E poll() {
		if (this.size == 0) {
			return null;
		}
		
		final var element = this.buffer[this.head];
		this.buffer[this.head] = null; // Help GC
		this.head = (this.head + 1) % this.capacity;
		this.size--;
		
		return element;
	}
	
	@Override
	public E peek() {
		if (this.size == 0) {
			return null;
		}
		return this.buffer[this.head];
	}
	
	@Override
	public Iterator<E> iterator() {
		return new RingBufferIterator();
	}
	
	@Override
	public int size() {
		return this.size;
	}
	
	@Override
	public void clear() {
		for (var i = 0; i < this.capacity; i++) {
			this.buffer[i] = null;
		}
		this.head = 0;
		this.tail = 0;
		this.size = 0;
	}
	
	/**
	 * Returns the maximum capacity of this ring buffer.
	 * 
	 * @return the capacity
	 */
	public int capacity() {
		return this.capacity;
	}
	
	/**
	 * Returns true if the buffer is at full capacity.
	 * 
	 * @return true if the buffer is full
	 */
	public boolean isFull() {
		return this.size == this.capacity;
	}
	
	/**
	 * Retrieves the element at the specified index in the buffer.
	 * Index 0 refers to the oldest element (head), index size()-1 refers to the newest element.
	 * 
	 * @param index the index of the element to retrieve
	 * @return the element at the specified position
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public E get(final int index) {
		if (index < 0 || index >= this.size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
		}
		final var actualIndex = (this.head + index) % this.capacity;
		return this.buffer[actualIndex];
	}
	
	private class RingBufferIterator implements Iterator<E> {
		
		private int current = 0;
		
		private final int expectedSize = RingBuffer.this.size;
		
		@Override
		public boolean hasNext() {
			return this.current < this.expectedSize;
		}
		
		@Override
		public E next() {
			if (this.current >= this.expectedSize) {
				throw new NoSuchElementException();
			}
			final var element = RingBuffer.this.get(this.current);
			this.current++;
			return element;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported");
		}
	}
}
