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
package org.jheaps.monotone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

import org.jheaps.Heap;
import org.jheaps.MapHeap;
import org.jheaps.MapHeap.Entry;
import org.jheaps.monotone.IntegerRadixHeap;
import org.junit.Test;

public class IntegerRadixHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void test() {
		Heap<Integer> h = new IntegerRadixHeap<Integer>(0, SIZE).asHeap();

		for (int i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Integer.valueOf(0), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin().intValue(), Integer.valueOf(SIZE - i - 1).intValue());
			h.deleteMin();
		}
	}

	@Test
	public void testVerySmall() {
		Heap<Integer> h = new IntegerRadixHeap<Integer>(29, 36).asHeap();

		h.insert(29);
		h.insert(30);
		h.insert(31);
		h.insert(30);
		h.insert(33);
		h.insert(36);
		h.insert(35);

		assertEquals(h.size(), 7);
		assertEquals(h.findMin().intValue(), 29l);
		assertEquals(h.size(), 7);
		assertEquals(h.deleteMin().intValue(), 29l);
		assertEquals(h.size(), 6);
		assertEquals(h.findMin().intValue(), 30l);
		assertEquals(h.deleteMin().intValue(), 30l);
		assertEquals(h.findMin().intValue(), 30l);
		assertEquals(h.deleteMin().intValue(), 30l);
		assertEquals(h.findMin().intValue(), 31l);
		assertEquals(h.deleteMin().intValue(), 31l);
		assertEquals(h.findMin().intValue(), 33l);
		assertEquals(h.deleteMin().intValue(), 33l);
		assertEquals(h.findMin().intValue(), 35l);
		assertEquals(h.deleteMin().intValue(), 35l);
		assertEquals(h.findMin().intValue(), 36l);
		assertEquals(h.deleteMin().intValue(), 36l);
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void testSortRandomSeed1() {
		Heap<Integer> h = new IntegerRadixHeap<Integer>(0, SIZE + 1).asHeap();

		Random generator = new Random(1);

		int[] a = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (int) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
		}

		Integer prev = null, cur;
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
	public void testSort2RandomSeed1() {
		Heap<Integer> h = new IntegerRadixHeap<Boolean>(0, SIZE + 1).asHeap();

		Random generator = new Random(1);

		int[] a = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (int) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
		}

		Integer prev = null, cur;
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
		Heap<Integer> h = new IntegerRadixHeap<Boolean>(0, SIZE + 1).asHeap();

		Random generator = new Random(2);

		int[] a = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (int) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
		}

		Integer prev = null, cur;
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
	public void testSort2RandomSeed2() {
		Heap<Integer> h = new IntegerRadixHeap<Integer>(0, SIZE + 1).asHeap();

		Random generator = new Random(2);

		int[] a = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (int) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
		}

		Integer prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSort3RandomSeed1() {
		MapHeap<Integer, String> h = new IntegerRadixHeap<String>(0, SIZE + 1);

		Random generator = new Random(1);

		int[] a = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (int) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i], String.valueOf(a[i]));
		}

		Entry<Integer, String> prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMin();
			assertEquals(String.valueOf(cur.getKey()), cur.getValue());
			if (prev != null) {
				assertTrue(prev.getKey().compareTo(cur.getKey()) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSameMinMax() {
		Heap<Integer> h = new IntegerRadixHeap<Boolean>(100, 100).asHeap();

		for (int i = 0; i < 15; i++) {
			h.insert(100);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(100l, h.deleteMin().intValue());
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testMaxDifference() {
		Heap<Integer> h = new IntegerRadixHeap<Boolean>(0, Integer.MAX_VALUE).asHeap();

		h.insert(0);
		h.insert(Integer.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0, h.deleteMin().intValue());
		assertEquals(Integer.MAX_VALUE, h.deleteMin().intValue());
		assertEquals(0, h.size());
	}

	@Test
	public void testClear() {
		Heap<Integer> h = new IntegerRadixHeap<Boolean>(0, 15).asHeap();

		for (int i = 0; i < 15; i++) {
			h.insert(i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMonotone() {
		Heap<Integer> h = new IntegerRadixHeap<Boolean>(0, 1000).asHeap();
		h.insert(100);
		h.insert(99);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new IntegerRadixHeap<Integer>(-1, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new IntegerRadixHeap<Boolean>(100, 99);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSerializable() throws IOException, ClassNotFoundException {
		Heap<Integer> h = new IntegerRadixHeap<Integer>(0, 15).asHeap();

		for (int i = 0; i < 15; i++) {
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
		h = (Heap<Integer>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Integer.valueOf(i), h.findMin());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

}
