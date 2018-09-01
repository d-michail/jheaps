/*
 * (C) Copyright 2014-2018, by Dimitrios Michail
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.AddressableHeap;
import org.junit.Test;

public class LongRadixAddressableHeapTest {

	private static final int SIZE = 100000;

	@Test
    public void testBug2() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0L, 100L);
        h.insert(0L);
        assertEquals(0L, h.findMin().getKey().longValue());
        assertEquals(0L, h.deleteMin().getKey().longValue());
        h.insert(15L);
        assertEquals(15L, h.findMin().getKey().longValue());
    }
	
	@Test
	public void testVerySmall() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(15, 100);

		h.insert(15L);
		h.insert(50L);
		h.insert(21L);
		h.insert(51L);
		h.insert(30L);
		h.insert(25L);
		h.insert(18L);

		assertEquals(7, h.size());
		assertEquals(15L, h.findMin().getKey().longValue());
		assertEquals(7, h.size());
		assertEquals(15L, h.deleteMin().getKey().longValue());
		assertEquals(6, h.size());
		assertEquals(18L, h.findMin().getKey().longValue());
		assertEquals(18L, h.deleteMin().getKey().longValue());
		assertEquals(21L, h.findMin().getKey().longValue());
		assertEquals(21L, h.deleteMin().getKey().longValue());
		assertEquals(25L, h.findMin().getKey().longValue());
		assertEquals(25L, h.deleteMin().getKey().longValue());
		assertEquals(30L, h.findMin().getKey().longValue());
		assertEquals(30L, h.deleteMin().getKey().longValue());
		assertEquals(50L, h.findMin().getKey().longValue());
		assertEquals(50L, h.deleteMin().getKey().longValue());
		assertEquals(51L, h.findMin().getKey().longValue());
		assertEquals(51L, h.deleteMin().getKey().longValue());
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void test() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, SIZE);

		for (long i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(0L, h.findMin().getKey(), 1e-9);
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals((long) (SIZE - i - 1), h.findMin().getKey(), 1e-9);
			h.deleteMin();
		}
	}

	@Test
	public void testSortRandomSeed1() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, Long.MAX_VALUE);

		Random generator = new Random(1);

		h.insert(0L);
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

		h.insert(0L);
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
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(1L, 1L);

		for (int i = 0; i < 15; i++) {
			h.insert(1L);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(1L, h.deleteMin().getKey().longValue());
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testMaxDifference() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0L, Long.MAX_VALUE);

		h.insert(0L);
		h.insert(Long.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0L, h.deleteMin().getKey().longValue());
		assertEquals(Long.MAX_VALUE, h.deleteMin().getKey().longValue());
		assertEquals(0, h.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0L, 15L);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert((long) i);
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
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0L, 10L);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[8];
		for (int i = 0; i < 8; i++) {
			array[i] = h.insert((long) i);
		}

		array[5].delete();
		assertEquals(0L, h.findMin().getKey(), 1e-9);
		array[7].delete();
		assertEquals(0L, h.findMin().getKey(), 1e-9);
		array[0].delete();
		assertEquals(1L, h.findMin().getKey(), 1e-9);
		array[2].delete();
		assertEquals(1L, h.findMin().getKey(), 1e-9);
		array[1].delete();
		assertEquals(3L, h.findMin().getKey(), 1e-9);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDelete() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, (long) SIZE);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[SIZE];
		for (int i = 0; i < SIZE; i++) {
			array[i] = h.insert((long) i);
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
			h.insert((long) i);
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
			array[i] = h.insert((long) i);
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

	@Test
	public void testDeleteMinUpdate() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, Long.MAX_VALUE);
		h.insert(0L);
		h.insert(0L);
		h.insert(Long.MAX_VALUE);
		h.insert(Long.MAX_VALUE);
		h.insert(Long.MAX_VALUE);
		h.insert(Long.MAX_VALUE);
		h.deleteMin();
		h.deleteMin();
		assertEquals(Long.MAX_VALUE, h.findMin().getKey().longValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		AddressableHeap.Handle<Long, Void> e1 = h.insert(50L);
		h.insert(100L);
		h.deleteMin();
		e1.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice1() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(99, 200);

		for (int i = 100; i < 200; i++) {
			h.insert((long) i);
		}

		h.deleteMin().delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDecreaseKey() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(100, 200);

		for (int i = 100; i < 200; i++) {
			h.insert((long) i);
		}
		h.deleteMin().decreaseKey(0L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		h.insert(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert1() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		h.insert(200L);
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadDeleteMin() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		h.deleteMin();
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadFindMin() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		h.findMin();
	}

	@Test
	public void testComparator() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 100);
		assertNull(h.comparator());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {
		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 200);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];

		h.insert(0L); // monotone

		for (int i = 0; i < 15; i++) {
			array[i] = h.insert((long) i + 100);
		}

		array[5].decreaseKey(5L);
		array[1].decreaseKey(50L);
		array[10].decreaseKey(3L);
		array[0].decreaseKey(1L);
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

		array[14].decreaseKey(111L);
		array[13].decreaseKey(109L);

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
			array[i] = h.insert((long) i + 100);
		}

		assertEquals(Long.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(5L);
		assertEquals(Long.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(102L);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {

		AddressableHeap<Long, Void> h = new LongRadixAddressableHeap<Void>(0, 15);

		for (int i = 0; i < 15; i++) {
			h.insert((long) i);
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
		assertEquals("hello", h.insert(0L, "hello").getValue());
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
