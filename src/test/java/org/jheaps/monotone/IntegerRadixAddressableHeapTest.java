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
import java.util.Random;

import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.junit.Test;

public class IntegerRadixAddressableHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void testVerySmall() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(15, 100);

		h.insert(15);
		h.insert(50);
		h.insert(21);
		h.insert(51);
		h.insert(30);
		h.insert(25);
		h.insert(18);

		assertEquals(7, h.size());
		assertEquals(15l, h.findMin().getKey().longValue());
		assertEquals(7, h.size());
		assertEquals(15l, h.deleteMin().getKey().longValue());
		assertEquals(6, h.size());
		assertEquals(18l, h.findMin().getKey().longValue());
		assertEquals(18l, h.deleteMin().getKey().longValue());
		assertEquals(21l, h.findMin().getKey().longValue());
		assertEquals(21l, h.deleteMin().getKey().longValue());
		assertEquals(25l, h.findMin().getKey().longValue());
		assertEquals(25l, h.deleteMin().getKey().longValue());
		assertEquals(30l, h.findMin().getKey().longValue());
		assertEquals(30l, h.deleteMin().getKey().longValue());
		assertEquals(50l, h.findMin().getKey().longValue());
		assertEquals(50l, h.deleteMin().getKey().longValue());
		assertEquals(51l, h.findMin().getKey().longValue());
		assertEquals(51l, h.deleteMin().getKey().longValue());
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void test() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, SIZE);

		for (int i = 0; i < SIZE; i++) {
			h.insert(Integer.valueOf(i));
			assertEquals(Integer.valueOf(0), h.findMin().getKey(), 1e-9);
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(Integer.valueOf(SIZE - i - 1), h.findMin().getKey(), 1e-9);
			h.deleteMin();
		}
	}

	@Test
	public void testSortRandomSeed1() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, Integer.MAX_VALUE);

		Random generator = new Random(1);

		h.insert(0);
		for (int i = 1; i < SIZE; i++) {
			int d = Math.abs(generator.nextInt()) + 1;
			h.insert(d);
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
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, Integer.MAX_VALUE);

		Random generator = new Random(2);

		h.insert(0);
		for (int i = 1; i < SIZE; i++) {
			int d = Math.abs(generator.nextInt()) + 1;
			h.insert(d);
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
	public void testSameMinMax() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(1, 1);

		for (int i = 0; i < 15; i++) {
			h.insert(1);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(1l, h.deleteMin().getKey().longValue());
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testMaxDifference() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, Integer.MAX_VALUE);

		h.insert(0);
		h.insert(Integer.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0l, h.deleteMin().getKey().longValue());
		assertEquals(Integer.MAX_VALUE, h.deleteMin().getKey().longValue());
		assertEquals(0, h.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 15);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Integer.valueOf(i));
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
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 10);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[8];
		for (int i = 0; i < 8; i++) {
			array[i] = h.insert(Integer.valueOf(i));
		}

		array[5].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey(), 1e-9);
		array[7].delete();
		assertEquals(Integer.valueOf(0), h.findMin().getKey(), 1e-9);
		array[0].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey(), 1e-9);
		array[2].delete();
		assertEquals(Integer.valueOf(1), h.findMin().getKey(), 1e-9);
		array[1].delete();
		assertEquals(Integer.valueOf(3), h.findMin().getKey(), 1e-9);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDelete() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, Integer.valueOf(SIZE));

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert(Integer.valueOf(i));
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
	public void testClear() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 15);

		for (int i = 0; i < 15; i++) {
			h.insert(Integer.valueOf(i));
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTwice() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 15);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Integer.valueOf(i));
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
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 100);
		AddressableHeap.Handle<Integer, Void> e1 = h.insert(50);
		h.insert(100);
		h.deleteMin();
		e1.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice1() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(99, 200);

		for (int i = 100; i < 200; i++) {
			h.insert(Integer.valueOf(i));
		}

		h.deleteMin().delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDecreaseKey() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(100, 200);

		for (int i = 100; i < 200; i++) {
			h.insert(Integer.valueOf(i));
		}
		h.deleteMin().decreaseKey(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteEmpty() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 200);
		Handle<Integer, Void> handle = h.insert(1);
		h.deleteMin();
		handle.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDecreaseKeyEmpty() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(100, 200);
		Handle<Integer, Void> handle = h.insert(150);
		h.deleteMin();
		handle.decreaseKey(120);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDecreaseKeyMore() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(100, 200);
		Handle<Integer, Void> handle = h.insert(150);
		handle.decreaseKey(160);
	}

	@Test
	public void testDecreaseKeySame() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(100, 200);
		Handle<Integer, Void> handle = h.insert(150);
		handle.decreaseKey(150);
		assertEquals(150, h.findMin().getKey().intValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 200);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];

		h.insert(0); // monotone

		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Integer.valueOf(i) + 100);
		}

		array[5].decreaseKey(5);
		array[1].decreaseKey(50);
		array[10].decreaseKey(3);
		array[0].decreaseKey(1);
		array[5].delete();
		array[2].delete();
		array[11].delete();
		array[9].delete();

		assertEquals(Integer.valueOf(0), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(1), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(3), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(50), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(103), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(104), h.deleteMin().getKey());

		array[14].decreaseKey(111);
		array[13].decreaseKey(109);

		assertEquals(Integer.valueOf(106), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(107), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(108), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(109), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(111), h.deleteMin().getKey());
		assertEquals(Integer.valueOf(112), h.deleteMin().getKey());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {
		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 200);

		AddressableHeap.Handle<Integer, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Integer.valueOf(i) + 100);
		}

		assertEquals(Integer.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(5);
		assertEquals(Integer.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(102);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {

		AddressableHeap<Integer, Void> h = new IntegerRadixAddressableHeap<Void>(0, 15);

		for (int i = 0; i < 15; i++) {
			h.insert(Integer.valueOf(i));
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

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new IntegerRadixAddressableHeap<Void>(-1, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new IntegerRadixAddressableHeap<Void>(10, 9);
	}

}
