/*
 * (C) Copyright 2014-2016, by Dimitrios Michail
 *
 * JHeaps Library
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jheaps.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.Heap;
import org.junit.Test;

public abstract class AbstractHeapTest {

	private static final int SIZE = 100000;

	protected abstract Heap<Long> createHeap();

	protected abstract Heap<Long> createHeap(int capacity);

	@Test
	public void test() {
		Heap<Long> h = createHeap();

		for (long i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Long.valueOf(0), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin(), Long.valueOf(SIZE - i - 1));
			h.deleteMin();
		}
	}

	@Test
	public void testOnlyInsert() {
		Heap<Long> h = createHeap();

		for (long i = 0; i < SIZE; i++) {
			h.insert(SIZE - i);
			assertEquals(Long.valueOf(SIZE - i), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}
	}

	@Test
	public void testInsertFromZero() {
		Heap<Long> h = createHeap(0);

		for (long i = 0; i < SIZE; i++) {
			h.insert(SIZE - i);
			assertEquals(Long.valueOf(SIZE - i), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}
	}

	@Test(expected = NullPointerException.class)
	public void testBadInsert() {
		Heap<Long> h = createHeap();
		h.insert(null);
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadDeleteMin() {
		Heap<Long> h = createHeap();
		h.deleteMin();
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadFindMin() {
		Heap<Long> h = createHeap();
		h.findMin();
	}

	@Test
	public void testOnly4() {

		Heap<Long> h = createHeap();

		assertTrue(h.isEmpty());

		h.insert(780l);
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(780).longValue(), h.findMin().longValue());

		h.insert(-389l);
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(-389), h.findMin());

		h.insert(306l);
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(-389), h.findMin());

		h.insert(579l);
		assertEquals(h.size(), 4);
		assertEquals(Long.valueOf(-389), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(306), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(579), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(780), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 0);

		assertTrue(h.isEmpty());

	}

	@Test
	public void testSortRandomSeed1() {
		Heap<Long> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextLong());
		}

		Long prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.findMin();
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSort1RandomSeed1() {
		Heap<Long> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextLong());
		}

		Long prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSortRandomSeed2() {
		Heap<Long> h = createHeap();

		Random generator = new Random(2);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextLong());
		}

		Long prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.findMin();
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSort1RandomSeed2() {
		Heap<Long> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextLong());
		}

		Long prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testFindMinDeleteMinSameObject() {
		Heap<Long> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextLong());
		}

		while (!h.isEmpty()) {
			assertEquals(h.findMin(), h.deleteMin());
		}
	}

	@Test
	public void testSizeOneInitial() {
		Heap<Long> h = createHeap(1);

		for (long i = 0; i < 15; i++) {
			h.insert(i);
		}

		assertEquals(15, h.size());
	}

	@Test
	public void testClear() {
		Heap<Long> h = createHeap();

		for (long i = 0; i < 15; i++) {
			h.insert(i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@Test
	public void testComparator() {
		Heap<Long> h = createHeap();

		assertNull(h.comparator());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {
		Heap<Long> h = createHeap();

		for (long i = 0; i < 15; i++) {
			h.insert(i);
		}

		// write
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(h);
		oos.close();
		byte[] data = baos.toByteArray();

		// read
		h = null;

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		h = (Heap<Long>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Long.valueOf(i), h.findMin());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

}
