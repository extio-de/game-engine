package de.extio.game_engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RingBufferTest {

	private RingBuffer<String> buffer;
	private static final int CAPACITY = 3;

	@BeforeEach
	void setUp() {
		this.buffer = new RingBuffer<>(CAPACITY);
	}

	@Test
	void testConstructor() {
		assertEquals(CAPACITY, this.buffer.capacity());
		assertEquals(0, this.buffer.size());
		assertFalse(this.buffer.isFull());
		assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(0));
		assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(-1));
	}

	@Test
	void testOffer() {
		assertTrue(this.buffer.offer("A"));
		assertEquals(1, this.buffer.size());
		assertEquals("A", this.buffer.peek());

		assertTrue(this.buffer.offer("B"));
		assertTrue(this.buffer.offer("C"));
		assertEquals(3, this.buffer.size());
		assertTrue(this.buffer.isFull());

		// Overwrite oldest
		assertTrue(this.buffer.offer("D"));
		assertEquals(3, this.buffer.size());
		assertEquals("B", this.buffer.peek());
		assertEquals("B", this.buffer.get(0));
		assertEquals("C", this.buffer.get(1));
		assertEquals("D", this.buffer.get(2));
	}

	@Test
	void testOfferNull() {
		assertThrows(NullPointerException.class, () -> this.buffer.offer(null));
	}

	@Test
	void testPoll() {
		assertNull(this.buffer.poll());

		this.buffer.offer("A");
		this.buffer.offer("B");

		assertEquals("A", this.buffer.poll());
		assertEquals(1, this.buffer.size());
		assertEquals("B", this.buffer.peek());

		assertEquals("B", this.buffer.poll());
		assertEquals(0, this.buffer.size());
		assertNull(this.buffer.poll());
	}

	@Test
	void testPeek() {
		assertNull(this.buffer.peek());

		this.buffer.offer("A");
		assertEquals("A", this.buffer.peek());
		assertEquals(1, this.buffer.size());

		this.buffer.offer("B");
		assertEquals("A", this.buffer.peek());
		assertEquals(2, this.buffer.size());
	}

	@Test
	void testGet() {
		this.buffer.offer("A");
		this.buffer.offer("B");
		this.buffer.offer("C");

		assertEquals("A", this.buffer.get(0));
		assertEquals("B", this.buffer.get(1));
		assertEquals("C", this.buffer.get(2));

		assertThrows(IndexOutOfBoundsException.class, () -> this.buffer.get(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> this.buffer.get(3));

		this.buffer.offer("D"); // Overwrites A
		assertEquals("B", this.buffer.get(0));
		assertEquals("C", this.buffer.get(1));
		assertEquals("D", this.buffer.get(2));
	}

	@Test
	void testClear() {
		this.buffer.offer("A");
		this.buffer.offer("B");
		this.buffer.clear();

		assertEquals(0, this.buffer.size());
		assertNull(this.buffer.peek());
		assertFalse(this.buffer.isFull());
	}

	@Test
	void testIterator() {
		this.buffer.offer("A");
		this.buffer.offer("B");
		this.buffer.offer("C");
		this.buffer.offer("D"); // B, C, D

		final Iterator<String> it = this.buffer.iterator();
		assertTrue(it.hasNext());
		assertEquals("B", it.next());
		assertTrue(it.hasNext());
		assertEquals("C", it.next());
		assertTrue(it.hasNext());
		assertEquals("D", it.next());
		assertFalse(it.hasNext());
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void testIteratorRemoveUnsupported() {
		this.buffer.offer("A");
		final Iterator<String> it = this.buffer.iterator();
		assertThrows(UnsupportedOperationException.class, it::remove);
	}
}
