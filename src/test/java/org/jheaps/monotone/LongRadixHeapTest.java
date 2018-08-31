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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.Heap;
import org.jheaps.monotone.LongRadixHeap;
import org.junit.Test;

public class LongRadixHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void test() {
		Heap<Long> h = new LongRadixHeap(0, SIZE);

		for (long i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Long.valueOf(0), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin().longValue(), Long.valueOf(SIZE - i - 1).longValue());
			h.deleteMin();
		}
	}

	@Test
	public void testVerySmall() {
		Heap<Long> h = new LongRadixHeap(29, 36);

		h.insert(29L);
		h.insert(30L);
		h.insert(31L);
		h.insert(30L);
		h.insert(33L);
		h.insert(36L);
		h.insert(35L);

		assertEquals(h.size(), 7);
		assertEquals(h.findMin().longValue(), 29L);
		assertEquals(h.size(), 7);
		assertEquals(h.deleteMin().longValue(), 29L);
		assertEquals(h.size(), 6);
		assertEquals(h.findMin().longValue(), 30L);
		assertEquals(h.deleteMin().longValue(), 30L);
		assertEquals(h.findMin().longValue(), 30L);
		assertEquals(h.deleteMin().longValue(), 30L);
		assertEquals(h.findMin().longValue(), 31L);
		assertEquals(h.deleteMin().longValue(), 31L);
		assertEquals(h.findMin().longValue(), 33L);
		assertEquals(h.deleteMin().longValue(), 33L);
		assertEquals(h.findMin().longValue(), 35L);
		assertEquals(h.deleteMin().longValue(), 35L);
		assertEquals(h.findMin().longValue(), 36L);
		assertEquals(h.deleteMin().longValue(), 36L);
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void testSortRandomSeed1() {
		Heap<Long> h = new LongRadixHeap(0, SIZE + 1);

		Random generator = new Random(1);

		long[] a = new long[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (long) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
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
	public void testSort2RandomSeed1() {
		Heap<Long> h = new LongRadixHeap(0, SIZE + 1);

		Random generator = new Random(1);

		long[] a = new long[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (long) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
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
		Heap<Long> h = new LongRadixHeap(0, SIZE + 1);

		Random generator = new Random(2);

		long[] a = new long[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (long) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
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
	public void testSort2RandomSeed2() {
		Heap<Long> h = new LongRadixHeap(0, SIZE + 1);

		Random generator = new Random(2);

		long[] a = new long[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = (long) (SIZE * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE; i++) {
			h.insert(a[i]);
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
	public void testSameMinMax() {
		Heap<Long> h = new LongRadixHeap(100, 100);

		for (int i = 0; i < 15; i++) {
			h.insert(100L);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(100L, h.deleteMin().longValue());
		}
		assertEquals(0, h.size());
	}
	
    @Test
    public void testMultipleDeleteMin() {
        final long step = 7;
        final long min = 0L;
        final long max = 100000L;
        
        Heap<Long> h = new LongRadixHeap(min, max);
        
        h.insert(min);
        long cur = min;
        while(cur < max) { 
            assertEquals(cur, h.findMin(), 1e-9);
            if (cur + step >= max) {
                break;
            }
            long newCur = cur + step;
            h.insert(newCur);
            assertEquals(cur, h.findMin(), 1e-9);
            assertEquals(cur, h.deleteMin(), 1e-9);
            cur = newCur;
        }
    }

	@Test
	public void testMaxDifference() {
		Heap<Long> h = new LongRadixHeap(0, Long.MAX_VALUE);

		h.insert(0L);
		h.insert(Long.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0L, h.deleteMin().longValue());
		assertEquals(Long.MAX_VALUE, h.deleteMin().longValue());
		assertEquals(0, h.size());
	}

	@Test
	public void testClear() {
		Heap<Long> h = new LongRadixHeap(0, 15);

		for (long i = 0; i < 15; i++) {
			h.insert(i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@Test
	public void testComparator() {
		Heap<Long> h = new LongRadixHeap(0, 15);
		assertNull(h.comparator());
	}

	@Test
	public void testMonotoneOkOnLastDeleted() {
		Heap<Long> h = new LongRadixHeap(0, 1000);
		h.insert(100L);
		assertEquals(100L, h.findMin().longValue());
		h.insert(99L);
		assertEquals(99L, h.findMin().longValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
    public void testMonotoneNotOkOnLastDeleted() {
        Heap<Long> h = new LongRadixHeap(0, 1000);
        h.insert(100L);
        h.deleteMin();
        h.insert(99L);
    }

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new LongRadixHeap(-1, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new LongRadixHeap(100, 99);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert() {
		new LongRadixHeap(0, 15).insert(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert1() {
		new LongRadixHeap(10, 15).insert(9L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert2() {
		new LongRadixHeap(10, 15).insert(16L);
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadDeleteMin() {
		new LongRadixHeap(10, 15).deleteMin();
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadFindMin() {
		new LongRadixHeap(10, 15).findMin();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSerializable() throws IOException, ClassNotFoundException {
		Heap<Long> h = new LongRadixHeap(0, 15);

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
