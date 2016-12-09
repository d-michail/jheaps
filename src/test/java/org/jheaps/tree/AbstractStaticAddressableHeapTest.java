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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Random;

import org.jheaps.AddressableHeap;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractStaticAddressableHeapTest {

	private static final int SIZE = 100000;

	private static Comparator<Integer> comparator;

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

	protected abstract AddressableHeap<Integer, String> createHeap(Comparator<Integer> comparator, int capacity);

	protected abstract AddressableHeap<Integer, String> createHeap(int capacity);

	@Test
	public void test() {
		AddressableHeap<Integer, String> h = createHeap(SIZE);

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
		AddressableHeap<Integer, String> h = createHeap(SIZE);

		for (int i = 0; i < SIZE; i++) {
			h.insert(SIZE - i);
			assertEquals(Integer.valueOf(SIZE - i), h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}
	}

	@Test
	public void testComparator() {
		AddressableHeap<Integer, String> h = createHeap(comparator, SIZE);
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
	public void testOnly4() {
		AddressableHeap<Integer, String> h = createHeap(4);

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
		AddressableHeap<Integer, String> h = createHeap(comparator, 4);

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
		AddressableHeap<Integer, String> h = createHeap(SIZE);

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
	public void testSortRandomSeed2() {
		AddressableHeap<Integer, String> h = createHeap(SIZE);

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

	@Test(expected = IllegalStateException.class)
	public void testMaxSize() {
		AddressableHeap<Integer, String> h = createHeap(4);
		h.insert(1);
		h.insert(2);
		h.insert(3);
		h.insert(4);
		h.insert(5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize() {
		AddressableHeap<Integer, String> h = createHeap(-4);
		h.insert(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize1() {
		AddressableHeap<Integer, String> h = createHeap(-1);
		h.insert(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize2() {
		AddressableHeap<Integer, String> h = createHeap(Integer.MAX_VALUE - 8);
		h.insert(1);
	}

	@Test
	public void testClear() {
		AddressableHeap<Integer, String> h = createHeap(15);

		for (int i = 0; i < 15; i++) {
			h.insert(i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {
		AddressableHeap<Integer, String> h = createHeap(15);

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

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		h = (AddressableHeap<Integer, String>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Integer.valueOf(i), h.findMin().getKey());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

}
