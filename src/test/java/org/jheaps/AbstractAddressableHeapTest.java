/*
 * (C) Copyright 2014-2016, by Dimitrios Michail.
 *
 * Java Heaps Library
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
import java.util.Random;

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

	protected abstract AddressableHeap<Integer> createHeap();

	protected abstract AddressableHeap<Integer> createHeap(Comparator<Integer> comparator);

	@Test
	public void test() {

		AddressableHeap<Integer> h = createHeap();

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

		AddressableHeap<Integer> h = createHeap();

		for (int i = 0; i < SIZE; i++) {
			h.insert(SIZE - i);
			assertEquals(Integer.valueOf(SIZE - i), h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

	}

	@Test
	public void testComparator() {

		AddressableHeap<Integer> h = createHeap(comparator);
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

		AddressableHeap<Integer> h = createHeap();

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
		AddressableHeap<Integer> h = createHeap(comparator);

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

		AddressableHeap<Integer> h = createHeap();

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

		AddressableHeap<Integer> h = createHeap();

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

	@SuppressWarnings("unchecked")
	@Test
	public void testDelete() {

		AddressableHeap<Integer> h = createHeap();

		AddressableHeap.Handle<Integer> array[];
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
	public void testClear() {

		AddressableHeap<Integer> h = createHeap();

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

		AddressableHeap<Integer> h = createHeap();

		AddressableHeap.Handle<Integer> array[];
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
		array[2].delete();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDecreaseKey() {

		AddressableHeap<Integer> h = createHeap();

		AddressableHeap.Handle<Integer> array[];
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

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {

		AddressableHeap<Integer> h = createHeap();

		AddressableHeap.Handle<Integer> array[];
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

		AddressableHeap<Integer> h = createHeap();

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
		h = (AddressableHeap<Integer>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Integer.valueOf(i), h.findMin().getKey());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

}
