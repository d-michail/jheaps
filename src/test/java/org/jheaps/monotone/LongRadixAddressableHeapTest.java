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

public class LongRadixAddressableHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void testVerySmall() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(15, 100);

		h.insert(15l);
		h.insert(50l);
		h.insert(21l);
		h.insert(51l);
		h.insert(30l);
		h.insert(25l);
		h.insert(18l);

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
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, SIZE);

		for (long i = 0; i < SIZE; i++) {
			h.insert(Long.valueOf(i));
			assertEquals(Long.valueOf(0), h.findMin().getKey(), 1e-9);
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(Long.valueOf(SIZE - i - 1), h.findMin().getKey(), 1e-9);
			h.deleteMin();
		}
	}

	@Test
	public void testSortRandomSeed1() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, Long.MAX_VALUE);

		Random generator = new Random(1);

		h.insert(0l);
		for (int i = 1; i < SIZE; i++) {
			long d = Math.abs(generator.nextLong()) + 1;
			h.insert(d);
		}

		Long prev = null, cur;
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
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, Long.MAX_VALUE);

		Random generator = new Random(2);

		h.insert(0l);
		for (int i = 1; i < SIZE; i++) {
			long d = Math.abs(generator.nextLong()) + 1;
			h.insert(d);
		}

		Long prev = null, cur;
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
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(1l, 1l);

		for (int i = 0; i < 15; i++) {
			h.insert(1l);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(1l, h.deleteMin().getKey().longValue());
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testMaxDifference() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0l, Long.MAX_VALUE);

		h.insert(0l);
		h.insert(Long.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0l, h.deleteMin().getKey().longValue());
		assertEquals(Long.MAX_VALUE, h.deleteMin().getKey().longValue());
		assertEquals(0, h.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0l, 15l);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Long.valueOf(i));
		}

		array[5].delete();
		assertEquals(Long.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(Long.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(Long.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(Long.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(Long.valueOf(3), h.findMin().getKey());
		array[3].delete();
		assertEquals(Long.valueOf(4), h.findMin().getKey());
		array[9].delete();
		assertEquals(Long.valueOf(4), h.findMin().getKey());
		array[4].delete();
		assertEquals(Long.valueOf(6), h.findMin().getKey());
		array[8].delete();
		assertEquals(Long.valueOf(6), h.findMin().getKey());
		array[11].delete();
		assertEquals(Long.valueOf(6), h.findMin().getKey());
		array[6].delete();
		assertEquals(Long.valueOf(10), h.findMin().getKey());
		array[12].delete();
		assertEquals(Long.valueOf(10), h.findMin().getKey());
		array[10].delete();
		assertEquals(Long.valueOf(13), h.findMin().getKey());
		array[13].delete();
		assertEquals(Long.valueOf(14), h.findMin().getKey());
		array[14].delete();
		assertTrue(h.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete1() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0l, 10l);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[8];
		for (int i = 0; i < 8; i++) {
			array[i] = h.insert(Long.valueOf(i));
		}

		array[5].delete();
		assertEquals(Long.valueOf(0), h.findMin().getKey(), 1e-9);
		array[7].delete();
		assertEquals(Long.valueOf(0), h.findMin().getKey(), 1e-9);
		array[0].delete();
		assertEquals(Long.valueOf(1), h.findMin().getKey(), 1e-9);
		array[2].delete();
		assertEquals(Long.valueOf(1), h.findMin().getKey(), 1e-9);
		array[1].delete();
		assertEquals(Long.valueOf(3), h.findMin().getKey(), 1e-9);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDelete() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, Long.valueOf(SIZE));

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert(Long.valueOf(i));
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			array[i].delete();
			if (i > 0) {
				assertEquals(Long.valueOf(0), h.findMin().getKey());
			}
		}
		assertTrue(h.isEmpty());
	}

	@Test
	public void testClear() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 15);

		for (int i = 0; i < 15; i++) {
			h.insert(Long.valueOf(i));
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTwice() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 15);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Long.valueOf(i));
		}

		array[5].delete();
		assertEquals(Long.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(Long.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(Long.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(Long.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(Long.valueOf(3), h.findMin().getKey());
		array[3].delete();
		assertEquals(Long.valueOf(4), h.findMin().getKey());
		array[9].delete();
		assertEquals(Long.valueOf(4), h.findMin().getKey());
		array[4].delete();
		assertEquals(Long.valueOf(6), h.findMin().getKey());

		// again
		array[2].delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		AddressableHeap.Handle<Long, Void> e1 = h.insert(50l);
		h.insert(100l);
		h.deleteMin();
		e1.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice1() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(99, 200);

		for (int i = 100; i < 200; i++) {
			h.insert(Long.valueOf(i));
		}

		h.deleteMin().delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDecreaseKey() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(100, 200);

		for (int i = 100; i < 200; i++) {
			h.insert(Long.valueOf(i));
		}
		h.deleteMin().decreaseKey(0l);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 200);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];

		h.insert(0l); // monotone

		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Long.valueOf(i) + 100);
		}

		array[5].decreaseKey(5l);
		array[1].decreaseKey(50l);
		array[10].decreaseKey(3l);
		array[0].decreaseKey(1l);
		array[5].delete();
		array[2].delete();
		array[11].delete();
		array[9].delete();

		assertEquals(Long.valueOf(0), h.deleteMin().getKey());
		assertEquals(Long.valueOf(1), h.deleteMin().getKey());
		assertEquals(Long.valueOf(3), h.deleteMin().getKey());
		assertEquals(Long.valueOf(50), h.deleteMin().getKey());
		assertEquals(Long.valueOf(103), h.deleteMin().getKey());
		assertEquals(Long.valueOf(104), h.deleteMin().getKey());

		array[14].decreaseKey(111l);
		array[13].decreaseKey(109l);

		assertEquals(Long.valueOf(106), h.deleteMin().getKey());
		assertEquals(Long.valueOf(107), h.deleteMin().getKey());
		assertEquals(Long.valueOf(108), h.deleteMin().getKey());
		assertEquals(Long.valueOf(109), h.deleteMin().getKey());
		assertEquals(Long.valueOf(111), h.deleteMin().getKey());
		assertEquals(Long.valueOf(112), h.deleteMin().getKey());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 200);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Long.valueOf(i) + 100);
		}

		assertEquals(Long.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(5l);
		assertEquals(Long.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(102l);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {

		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 15);

		for (int i = 0; i < 15; i++) {
			h.insert(Long.valueOf(i));
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
		h = (AddressableHeap<Long, Void>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Long.valueOf(i), h.findMin().getKey());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

	@Test
	public void testGetValue() {
		AddressableHeap<Long, String> h = new LongRadixAddressableHeap<String>(0, 0);
		assertEquals("hello", h.insert(0l, "hello").getValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new LongRadixAddressableHeap<Void>(-1, 10);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new LongRadixAddressableHeap<Void>(10, 9);
	}


}
