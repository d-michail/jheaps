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
import org.junit.Test;

public class DoubleRadixAddressableHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void testVerySmall() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(15.0, 50.5);

		h.insert(15.3);
		h.insert(50.4);
		h.insert(20.999999);
		h.insert(50.5);
		h.insert(30.3);
		h.insert(25.2);
		h.insert(17.7777);

		assertEquals(7, h.size());
		assertEquals(15.3, h.findMin().getKey(), 1e-9);
		assertEquals(7, h.size());
		assertEquals(15.3, h.deleteMin().getKey(), 1e-9);
		assertEquals(6, h.size());
		assertEquals(17.7777, h.findMin().getKey(), 1e-9);
		assertEquals(17.7777, h.deleteMin().getKey(), 1e-9);
		assertEquals(20.999999, h.findMin().getKey(), 1e-9);
		assertEquals(20.999999, h.deleteMin().getKey(), 1e-9);
		assertEquals(25.2, h.findMin().getKey(), 1e-9);
		assertEquals(25.2, h.deleteMin().getKey(), 1e-9);
		assertEquals(30.3, h.findMin().getKey(), 1e-9);
		assertEquals(30.3, h.deleteMin().getKey(), 1e-9);
		assertEquals(50.4, h.findMin().getKey(), 1e-9);
		assertEquals(50.4, h.deleteMin().getKey(), 1e-9);
		assertEquals(50.5, h.findMin().getKey(), 1e-9);
		assertEquals(50.5, h.deleteMin().getKey(), 1e-9);
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void test() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, SIZE);

		for (long i = 0; i < SIZE; i++) {
			h.insert((double) i);
			assertEquals(0d, h.findMin().getKey(), 1e-9);
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals((double) (SIZE - i - 1), h.findMin().getKey(), 1e-9);
			h.deleteMin();
		}
	}

	@Test
	public void testSortRandomSeed1() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 1.0);

		Random generator = new Random(1);

		h.insert(0.0d);
		for (int i = 1; i < SIZE; i++) {
			double d = generator.nextDouble();
			h.insert(d);
		}

		Double prev = null, cur;
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
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 1.0);

		Random generator = new Random(2);

		h.insert(0.0d);
		for (int i = 1; i < SIZE; i++) {
			double d = generator.nextDouble();
			h.insert(d);
		}

		Double prev = null, cur;
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
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(1.0, 1.0);

		for (int i = 0; i < 15; i++) {
			h.insert(1.0);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(1.0, h.deleteMin().getKey(), 1e-9);
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testMaxDifference() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, Double.MAX_VALUE);

		h.insert(0.0);
		h.insert(Double.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(Double.MAX_VALUE, h.deleteMin().getKey(), 1e-9);
		assertEquals(0, h.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 15.0);

		AddressableHeap.Handle<Double, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert((double) i);
		}

		array[5].delete();
		assertEquals(0d, h.findMin().getKey(), 1e-9);
		array[7].delete();
		assertEquals(0d, h.findMin().getKey(), 1e-9);
		array[0].delete();
		assertEquals(1d, h.findMin().getKey(), 1e-9);
		array[2].delete();
		assertEquals(1d, h.findMin().getKey(), 1e-9);
		array[1].delete();
		assertEquals(3d, h.findMin().getKey(), 1e-9);
		array[3].delete();
		assertEquals(4d, h.findMin().getKey(), 1e-9);
		array[9].delete();
		assertEquals(4d, h.findMin().getKey(), 1e-9);
		array[4].delete();
		assertEquals(6d, h.findMin().getKey(), 1e-9);
		array[8].delete();
		assertEquals(6d, h.findMin().getKey(), 1e-9);
		array[11].delete();
		assertEquals(6d, h.findMin().getKey(), 1e-9);
		array[6].delete();
		assertEquals(10d, h.findMin().getKey(), 1e-9);
		array[12].delete();
		assertEquals(10d, h.findMin().getKey(), 1e-9);
		array[10].delete();
		assertEquals(13d, h.findMin().getKey(), 1e-9);
		array[13].delete();
		assertEquals(14d, h.findMin().getKey(), 1e-9);
		array[14].delete();
		assertTrue(h.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete1() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 10.0);

		AddressableHeap.Handle<Double, Void> array[];
		array = new AddressableHeap.Handle[8];
		for (int i = 0; i < 8; i++) {
			array[i] = h.insert((double) i);
		}

		array[5].delete();
		assertEquals(0d, h.findMin().getKey(), 1e-9);
		array[7].delete();
		assertEquals(0d, h.findMin().getKey(), 1e-9);
		array[0].delete();
		assertEquals(1d, h.findMin().getKey(), 1e-9);
		array[2].delete();
		assertEquals(1d, h.findMin().getKey(), 1e-9);
		array[1].delete();
		assertEquals(3d, h.findMin().getKey(), 1e-9);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDelete() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, (double) SIZE);

		AddressableHeap.Handle<Double, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert((double) i);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			array[i].delete();
			if (i > 0) {
				assertEquals(0d, h.findMin().getKey(), 1e-9);
			}
		}
		assertTrue(h.isEmpty());
	}

	@Test
	public void testClear() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 15.0);

		for (int i = 0; i < 15; i++) {
			h.insert((double) i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTwice() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 15.0);

		AddressableHeap.Handle<Double, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert((double) i);
		}

		array[5].delete();
		assertEquals(0d, h.findMin().getKey(), 1e-9);
		array[7].delete();
		assertEquals(0d, h.findMin().getKey(), 1e-9);
		array[0].delete();
		assertEquals(1d, h.findMin().getKey(), 1e-9);
		array[2].delete();
		assertEquals(1d, h.findMin().getKey(), 1e-9);
		array[1].delete();
		assertEquals(3d, h.findMin().getKey(), 1e-9);
		array[3].delete();
		assertEquals(4d, h.findMin().getKey(), 1e-9);
		array[9].delete();
		assertEquals(4d, h.findMin().getKey(), 1e-9);
		array[4].delete();
		assertEquals(6d, h.findMin().getKey(), 1e-9);

		// again
		array[2].delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 100.0);
		AddressableHeap.Handle<Double, Void> e1 = h.insert(50.0);
		h.insert(100.0);
		h.deleteMin();
		e1.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice1() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(99.0, 200.0);

		for (int i = 100; i < 200; i++) {
			h.insert((double) i);
		}

		h.deleteMin().delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDecreaseKey() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(100.0, 200.0);

		for (int i = 100; i < 200; i++) {
			h.insert((double) i);
		}
		h.deleteMin().decreaseKey(0.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 200.0);

		AddressableHeap.Handle<Double, Void> array[];
		array = new AddressableHeap.Handle[15];

		h.insert(0.0); // monotone

		for (int i = 0; i < 15; i++) {
			array[i] = h.insert((double) i + 100.0);
		}

		array[5].decreaseKey(5.0);
		array[1].decreaseKey(50.0);
		array[10].decreaseKey(3.0);
		array[0].decreaseKey(1.0);
		array[5].delete();
		array[2].delete();

		assertEquals(0.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(1.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(3.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(50.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(103.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(104.0, h.deleteMin().getKey(), 1e-9);

		array[14].decreaseKey(107.5);
		array[13].decreaseKey(108.5);

		assertEquals(106.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(107.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(107.5, h.deleteMin().getKey(), 1e-9);
		assertEquals(108.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(108.5, h.deleteMin().getKey(), 1e-9);
		assertEquals(109.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(111.0, h.deleteMin().getKey(), 1e-9);
		assertEquals(112.0, h.deleteMin().getKey(), 1e-9);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {
		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 200.0);

		AddressableHeap.Handle<Double, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert((double) i + 100.0);
		}

		assertEquals(Double.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(5.0);
		assertEquals(Double.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(102.0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {

		AddressableHeap<Double, Void> h = new DoubleRadixAddressableHeap<Void>(0.0, 15.0);

		for (int i = 0; i < 15; i++) {
			h.insert((double) i);
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
		h = (AddressableHeap<Double, Void>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals((double) i, h.findMin().getKey(), 1e-9);
			h.deleteMin();
		}
		assertTrue(h.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new DoubleRadixAddressableHeap<Void>(-1, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new DoubleRadixAddressableHeap<Void>(10, 9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction2() {
		new DoubleRadixAddressableHeap<Void>(Double.NEGATIVE_INFINITY, 9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction3() {
		new DoubleRadixAddressableHeap<Void>(0d, Double.POSITIVE_INFINITY);
	}

}
