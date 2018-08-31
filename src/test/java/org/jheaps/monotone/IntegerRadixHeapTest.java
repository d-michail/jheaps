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
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.Heap;
import org.jheaps.monotone.IntegerRadixHeap;
import org.junit.Test;

public class IntegerRadixHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void test() {
		Heap<Integer> h = new IntegerRadixHeap(0, SIZE);

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
		Heap<Integer> h = new IntegerRadixHeap(29, 36);

		h.insert(29);
		h.insert(30);
		h.insert(31);
		h.insert(30);
		h.insert(33);
		h.insert(36);
		h.insert(35);

		assertEquals(h.size(), 7);
		assertEquals(h.findMin().intValue(), 29L);
		assertEquals(h.size(), 7);
		assertEquals(h.deleteMin().intValue(), 29L);
		assertEquals(h.size(), 6);
		assertEquals(h.findMin().intValue(), 30L);
		assertEquals(h.deleteMin().intValue(), 30L);
		assertEquals(h.findMin().intValue(), 30L);
		assertEquals(h.deleteMin().intValue(), 30L);
		assertEquals(h.findMin().intValue(), 31L);
		assertEquals(h.deleteMin().intValue(), 31L);
		assertEquals(h.findMin().intValue(), 33L);
		assertEquals(h.deleteMin().intValue(), 33L);
		assertEquals(h.findMin().intValue(), 35L);
		assertEquals(h.deleteMin().intValue(), 35L);
		assertEquals(h.findMin().intValue(), 36L);
		assertEquals(h.deleteMin().intValue(), 36L);
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void testSortRandomSeed1() {
		Heap<Integer> h = new IntegerRadixHeap(0, SIZE + 1);

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
		Heap<Integer> h = new IntegerRadixHeap(0, SIZE + 1);

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
		Heap<Integer> h = new IntegerRadixHeap(0, SIZE + 1);

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
		Heap<Integer> h = new IntegerRadixHeap(0, SIZE + 1);

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
	public void testSameMinMax() {
		Heap<Integer> h = new IntegerRadixHeap(100, 100);

		for (int i = 0; i < 15; i++) {
			h.insert(100);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(100L, h.deleteMin().intValue());
		}
		assertEquals(0, h.size());
	}
	
    @Test
    public void testMultipleDeleteMin() {
        final int step = 7;
        final int min = 0;
        final int max = 100000;
        
        Heap<Integer> h = new IntegerRadixHeap(min, max);
        
        h.insert(min);
        int cur = min;
        while(cur < max) { 
            assertEquals(cur, h.findMin(), 1e-9);
            if (cur + step >= max) {
                break;
            }
            int newCur = cur + step;
            h.insert(newCur);
            assertEquals(cur, h.findMin(), 1e-9);
            assertEquals(cur, h.deleteMin(), 1e-9);
            cur = newCur;
        }
    }

	@Test
	public void testMaxDifference() {
		Heap<Integer> h = new IntegerRadixHeap(0, Integer.MAX_VALUE);

		h.insert(0);
		h.insert(Integer.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0, h.deleteMin().intValue());
		assertEquals(Integer.MAX_VALUE, h.deleteMin().intValue());
		assertEquals(0, h.size());
	}

	@Test
	public void testClear() {
		Heap<Integer> h = new IntegerRadixHeap(0, 15);

		for (int i = 0; i < 15; i++) {
			h.insert(i);
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@Test
    public void testMonotoneOkOnLastDeleted() {
        Heap<Integer> h = new IntegerRadixHeap(0, 1000);
        h.insert(100);
        assertEquals(100, h.findMin().intValue());
        h.insert(99);
        assertEquals(99, h.findMin().intValue());
    }
	
	@Test(expected = IllegalArgumentException.class)
	public void testMonotoneNotOkOnLastDeleted() {
		Heap<Integer> h = new IntegerRadixHeap(0, 1000);
		h.insert(100);
		assertEquals(100, h.findMin().intValue());
		h.deleteMin();
		h.insert(99);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new IntegerRadixHeap(-1, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new IntegerRadixHeap(100, 99);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSerializable() throws IOException, ClassNotFoundException {
		Heap<Integer> h = new IntegerRadixHeap(0, 15);

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
		h = (Heap<Integer>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Integer.valueOf(i), h.findMin());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert() {
		new IntegerRadixHeap(0, 15).insert(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert1() {
		new IntegerRadixHeap(10, 15).insert(9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadInsert2() {
		new IntegerRadixHeap(10, 15).insert(16);
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadDeleteMin() {
		new IntegerRadixHeap(10, 15).deleteMin();
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadFindMin() {
		new IntegerRadixHeap(10, 15).findMin();
	}

}
