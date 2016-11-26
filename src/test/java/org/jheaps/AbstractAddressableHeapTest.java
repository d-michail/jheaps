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
package org.jheaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.AddressableHeap.Handle;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractAddressableHeapTest {

	protected static final int SIZE = 100000;

	protected static Comparator<Integer> comparator;

	@BeforeClass
	public static void setUpClass() {
		comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1 < o2) {
					return 1;
				} else if (o1 > o2) {
					return -1;
				} else {
					return 0;
				}
			}
		};
	}

	protected abstract AddressableHeap<Integer, Void> createHeap();

	protected abstract AddressableHeap<Integer, Void> createHeap(Comparator<Integer> comparator);

	protected abstract AddressableHeap<Integer, String> createHeapWithStringValues();

	@Test
	public void test() {
		AddressableHeap<Integer, Void> h = createHeap();

		for (int i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Integer.valueOf(0), h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin().getKey(), Integer.valueOf(SIZE - i - 1));
			h.deleteMin();
		}
	}

	@Test
	public void testOnlyInsert() {
		AddressableHeap<Integer, Void> h = createHeap();

		for (int i = 0; i < SIZE; i++) {
			h.insert(SIZE - i);
			assertEquals(Integer.valueOf(SIZE - i), h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}
	}

	@Test
	public void testComparator() {
		AddressableHeap<Integer, Void> h = createHeap(comparator);
		int i;

		for (i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Integer.valueOf(i), h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin().getKey(), Integer.valueOf(i));
			h.deleteMin();
		}
	}

	@Test
	public void testGetComparator() {
		AddressableHeap<Integer, Void> h = createHeap(comparator);
		assertEquals(comparator, h.comparator());
	}

	@Test
	public void testOnly4() {

		AddressableHeap<Integer, Void> h = createHeap();

		assertTrue(h.isEmpty());

		h.insert(780);
		assertEquals(h.size(), 1);
		assertEquals(Integer.valueOf(780), h.findMin().getKey());

		h.insert(-389);
		assertEquals(h.size(), 2);
		assertEquals(Integer.valueOf(-389), h.findMin().getKey());

		h.insert(306);
		assertEquals(h.size(), 3);
		assertEquals(Integer.valueOf(-389), h.findMin().getKey());

		h.insert(579);
		assertEquals(h.size(), 4);
		assertEquals(Integer.valueOf(-389), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 3);
		assertEquals(Integer.valueOf(306), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 2);
		assertEquals(Integer.valueOf(579), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 1);
		assertEquals(Integer.valueOf(780), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 0);

		assertTrue(h.isEmpty());

	}

	@Test
	public void testOnly4Reverse() {
		AddressableHeap<Integer, Void> h = createHeap(comparator);

		assertTrue(h.isEmpty());

		h.insert(780);
		assertEquals(h.size(), 1);
		assertEquals(Integer.valueOf(780), h.findMin().getKey());

		h.insert(-389);
		assertEquals(h.size(), 2);
		assertEquals(Integer.valueOf(780), h.findMin().getKey());

		h.insert(306);
		assertEquals(h.size(), 3);
		assertEquals(Integer.valueOf(780), h.findMin().getKey());

		h.insert(579);
		assertEquals(h.size(), 4);
		assertEquals(Integer.valueOf(780), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 3);
		assertEquals(Integer.valueOf(579), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 2);
		assertEquals(Integer.valueOf(306), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 1);
		assertEquals(Integer.valueOf(-389), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 0);

		assertTrue(h.isEmpty());
	}

	@Test
	public void testSortRandomSeed1() {
		AddressableHeap<Integer, Void> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextInt());
		}

		Integer prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.findMin().getKey();
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSort1RandomSeed1() {
		AddressableHeap<Integer, Void> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextInt());
		}

		Integer prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMin().getKey();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSortRandomSeed2() {
		AddressableHeap<Integer, Void> h = createHeap();

		Random generator = new Random(2);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextInt());
		}

		Integer prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.findMin().getKey();
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testSort2RandomSeed2() {
		AddressableHeap<Integer, Void> h = createHeap();

		Random generator = new Random(2);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextInt());
		}

		Integer prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMin().getKey();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testFindMinDeleteMinSameObject() {
		AddressableHeap<Integer, Void> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextInt());
		}

		while (!h.isEmpty()) {
			assertEquals(h.findMin(), h.deleteMin());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete() {
		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(i);
		}

		array[5].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(Integer.valueOf(3), h.findMin().getKey());
		array[3].delete();
		assertEquals(Integer.valueOf(4), h.findMin().getKey());
		array[9].delete();
		assertEquals(Integer.valueOf(4), h.findMin().getKey());
		array[4].delete();
		assertEquals(Integer.valueOf(6), h.findMin().getKey());
		array[8].delete();
		assertEquals(Integer.valueOf(6), h.findMin().getKey());
		array[11].delete();
		assertEquals(Integer.valueOf(6), h.findMin().getKey());
		array[6].delete();
		assertEquals(Integer.valueOf(10), h.findMin().getKey());
		array[12].delete();
		assertEquals(Integer.valueOf(10), h.findMin().getKey());
		array[10].delete();
		assertEquals(Integer.valueOf(13), h.findMin().getKey());
		array[13].delete();
		assertEquals(Integer.valueOf(14), h.findMin().getKey());
		array[14].delete();
		assertTrue(h.isEmpty());

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete1() {
		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[8];
		for (int i = 0; i < 8; i++) {
			array[i] = h.insert(i);
		}

		array[5].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(Integer.valueOf(3), h.findMin().getKey());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDelete() {
		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert(i);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			array[i].delete();
			if (i > 0) {
				assertEquals(Integer.valueOf(0), h.findMin().getKey());
			}
		}
		assertTrue(h.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDeleteComparator() {
		AddressableHeap<Integer, Void> h = createHeap(comparator);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert(i);
		}

		for (int i = 0; i < SIZE; i++) {
			array[i].delete();
			if (i < SIZE - 1) {
				assertEquals(Integer.valueOf(SIZE - 1), h.findMin().getKey());
			}
		}
		assertTrue(h.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDecreaseKeyDeleteMin() {
		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert(i);
		}

		for (int i = SIZE / 2; i < SIZE / 2 + 10; i++) {
			array[i].decreaseKey(i / 2);
		}

		array[0].delete();

		for (int i = SIZE / 2 + 10; i < SIZE / 2 + 20; i++) {
			array[i].decreaseKey(0);
		}

		assertEquals(0, h.deleteMin().getKey().intValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDecreaseKeyDeleteMinComparator() {
		AddressableHeap<Integer, Void> h = createHeap(comparator);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert(i);
		}

		for (int i = SIZE / 2; i < SIZE / 2 + 10; i++) {
			array[i].decreaseKey(SIZE-1);
		}

		array[SIZE-1].delete();

		for (int i = SIZE / 2 + 10; i < SIZE / 2 + 20; i++) {
			array[i].decreaseKey(SIZE-1);
		}

		assertEquals(SIZE-1, h.deleteMin().getKey().intValue());
	}

	@Test
	public void testClear() {
		AddressableHeap<Integer, Void> h = createHeap();

		for (int i = 0; i < 15; i++) {
			h.insert(i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTwice() {

		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(i);
		}

		array[5].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(Integer.valueOf(3), h.findMin().getKey());
		array[3].delete();
		assertEquals(Integer.valueOf(4), h.findMin().getKey());
		array[9].delete();
		assertEquals(Integer.valueOf(4), h.findMin().getKey());
		array[4].delete();
		assertEquals(Integer.valueOf(6), h.findMin().getKey());

		// again
		array[2].delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice() {
		AddressableHeap<Integer, Void> h = createHeap();
		AddressableHeap.Handle<Integer, Void> e1 = h.insert(50);
		h.insert(100);
		h.deleteMin();
		e1.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice1() {
		AddressableHeap<Integer, Void> h = createHeap();

		for (int i = 100; i < 200; i++) {
			h.insert(i);
		}

		h.deleteMin().delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDecreaseKey() {
		AddressableHeap<Integer, Void> h = createHeap();

		for (int i = 100; i < 200; i++) {
			h.insert(i);
		}
		h.deleteMin().decreaseKey(0);
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoElementFindMin() {
		AddressableHeap<Integer, Void> h = createHeap();
		h.findMin();
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoElementDeleteMin() {
		AddressableHeap<Integer, Void> h = createHeap();
		h.deleteMin();
	}

	@Test(expected = NullPointerException.class)
	public void testInsertNull() {
		AddressableHeap<Integer, Void> h = createHeap();
		h.insert(null, null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {

		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(i + 100);
		}

		assertEquals(Integer.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(5);
		assertEquals(Integer.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(50);
		assertEquals(Integer.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(20);
		assertEquals(Integer.valueOf(5), h.findMin().getKey());
		array[5].delete();
		assertEquals(Integer.valueOf(20), h.findMin().getKey());
		array[10].decreaseKey(3);
		assertEquals(Integer.valueOf(3), h.findMin().getKey());
		array[0].decreaseKey(0);
		assertEquals(Integer.valueOf(0), h.findMin().getKey());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey1() {
		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[1000];
		for (int i = 0; i < 1000; i++) {
			array[i] = h.insert(2000 + i);
		}

		for (int i = 999; i >= 0; i--) {
			array[i].decreaseKey(array[i].getKey() - 2000);
		}

		for (int i = 0; i < 1000; i++) {
			assertEquals(i, h.deleteMin().getKey().intValue());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKeyWithComparator() {
		AddressableHeap<Integer, Void> h = createHeap(comparator);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[1000];
		for (int i = 0; i < 1000; i++) {
			array[i] = h.insert(i);
		}

		for (int i = 0; i < 1000; i++) {
			array[i].decreaseKey(array[i].getKey() + 2000);
		}

		for (int i = 999; i >= 0; i--) {
			assertEquals(i + 2000, h.deleteMin().getKey().intValue());
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {

		AddressableHeap<Integer, Void> h = createHeap();

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(i + 100);
		}

		assertEquals(Integer.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(5);
		assertEquals(Integer.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(102);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {

		AddressableHeap<Integer, Void> h = createHeap();

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
		h = (AddressableHeap<Integer, Void>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Integer.valueOf(i), h.findMin().getKey());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());
	}

	@Test
	public void testSameKey() {
		AddressableHeap<Integer, Void> h = createHeap();

		assertTrue(h.isEmpty());

		Handle<Integer, Void> handle = h.insert(780);
		handle.decreaseKey(780);
		assertEquals(780, h.deleteMin().getKey().intValue());
		assertTrue(h.isEmpty());
	}

	@Test
	public void testGetValue() {
		AddressableHeap<Integer, String> h = createHeapWithStringValues();

		assertTrue(h.isEmpty());

		Handle<Integer, String> handle = h.insert(1, "1");
		assertEquals("1", handle.getValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMeldGeneric() {
		AddressableHeap<Integer, Void> h1 = createHeap();

		if (h1 instanceof MergeableHeap) {
			for (int i = 0; i < SIZE; i++) {
				h1.insert(2 * i);
			}

			AddressableHeap<Integer, Void> h2 = createHeap();
			for (int i = 0; i < SIZE; i++) {
				h2.insert(2 * i + 1);
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertEquals(h1.size(), SIZE * 2);
			assertEquals(h2.size(), 0);

			Integer prev = null, cur;
			while (!h1.isEmpty()) {
				cur = h1.findMin().getKey();
				h1.deleteMin();
				if (prev != null) {
					assertTrue(prev.compareTo(cur) <= 0);
				}
				prev = cur;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMeldGeneric1() {
		AddressableHeap<Integer, Void> h1 = createHeap();

		if (h1 instanceof MergeableHeap) {
			AddressableHeap<Integer, Void> h2 = createHeap();
			for (int i = 0; i < SIZE; i++) {
				h2.insert(i);
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertEquals(h1.size(), SIZE);
			assertEquals(h2.size(), 0);

			Integer prev = null, cur;
			while (!h1.isEmpty()) {
				cur = h1.findMin().getKey();
				h1.deleteMin();
				if (prev != null) {
					assertTrue(prev.compareTo(cur) <= 0);
				}
				prev = cur;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMeldGeneric2() {
		AddressableHeap<Integer, Void> h1 = createHeap();

		if (h1 instanceof MergeableHeap) {
			AddressableHeap<Integer, Void> h2 = createHeap();
			for (int i = 0; i < SIZE; i++) {
				h1.insert(i);
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertEquals(h1.size(), SIZE);
			assertEquals(h2.size(), 0);

			Integer prev = null, cur;
			while (!h1.isEmpty()) {
				cur = h1.findMin().getKey();
				h1.deleteMin();
				if (prev != null) {
					assertTrue(prev.compareTo(cur) <= 0);
				}
				prev = cur;
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMeld() {
		AddressableHeap<Integer, Void> h1 = createHeap();
		AddressableHeap<Integer, Void> h2 = createHeap();

		if (h1 instanceof MergeableHeap) {
			for (int i = 0; i < SIZE; i++) {
				if (i % 2 == 0) {
					h1.insert(i);
				} else {
					h2.insert(i);
				}
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertTrue(h2.isEmpty());
			assertEquals(0, h2.size());

			for (int i = 0; i < SIZE; i++) {
				assertEquals(Integer.valueOf(i), h1.findMin().getKey());
				h1.deleteMin();
			}
			assertTrue(h1.isEmpty());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMeldWithComparatorSmallerFirst() {
		AddressableHeap<Integer, Void> h1 = createHeap(comparator);
		AddressableHeap<Integer, Void> h2 = createHeap(comparator);

		if (h1 instanceof MergeableHeap) {

			h1.insert(0);
			h1.insert(1);
			h1.insert(2);
			h1.insert(3);

			for (int i = 4; i < SIZE; i++) {
				h2.insert(i);
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertTrue(h2.isEmpty());
			assertEquals(0, h2.size());

			for (int i = 0; i < SIZE; i++) {
				assertEquals(Integer.valueOf(SIZE - i - 1), h1.findMin().getKey());
				h1.deleteMin();
			}
			assertTrue(h1.isEmpty());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMeldWithComparator() {
		AddressableHeap<Integer, Void> h1 = createHeap(comparator);
		AddressableHeap<Integer, Void> h2 = createHeap(comparator);

		if (h1 instanceof MergeableHeap) {
			for (int i = 0; i < SIZE; i++) {
				if (i % 2 == 0) {
					h1.insert(i);
				} else {
					h2.insert(i);
				}
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertTrue(h2.isEmpty());
			assertEquals(0, h2.size());

			for (int i = 0; i < SIZE; i++) {
				assertEquals(Integer.valueOf(SIZE - i - 1), h1.findMin().getKey());
				h1.deleteMin();
			}
			assertTrue(h1.isEmpty());
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testMeldWrong() {
		AddressableHeap<Integer, Void> h1 = createHeap();
		AddressableHeap<Integer, Void> h2 = createHeap(comparator);

		if (h1 instanceof MergeableHeap) {
			for (int i = 0; i < SIZE; i++) {
				if (i % 2 == 0) {
					h1.insert(i);
				} else {
					h2.insert(i);
				}
			}

			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

			assertTrue(h2.isEmpty());
			assertEquals(0, h2.size());

			for (int i = 0; i < SIZE; i++) {
				assertEquals(Integer.valueOf(i), h1.findMin().getKey());
				h1.deleteMin();
			}
			assertTrue(h1.isEmpty());
		} else {
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testMeldWrong1() throws IOException, ClassNotFoundException {
		AddressableHeap<Integer, Void> h1 = createHeap(comparator);
		AddressableHeap<Integer, Void> h2 = createHeap();

		if (h1 instanceof MergeableHeap) {
			((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);
		} else {
			throw new IllegalArgumentException();
		}
	}

}
