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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

public class HeapifyTest {

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

	@Test
	public void testHeapifySort() {
		Random generator = new Random(1);

		Integer[] a = new Integer[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = generator.nextInt();
		}

		@SuppressWarnings("unchecked")
		Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, 10);

		h[0] = BinaryArrayHeap.heapify(a);
		h[1] = FixedSizeBinaryArrayHeap.heapify(a);
		h[2] = DaryArrayHeap.heapify(2, a);
		h[3] = DaryArrayHeap.heapify(3, a);
		h[4] = DaryArrayHeap.heapify(4, a);
		h[5] = DaryArrayHeap.heapify(5, a);
		h[6] = FixedSizeDaryArrayHeap.heapify(2, a);
		h[7] = FixedSizeDaryArrayHeap.heapify(3, a);
		h[8] = FixedSizeDaryArrayHeap.heapify(4, a);
		h[9] = FixedSizeDaryArrayHeap.heapify(5, a);

		int elements = SIZE;
		Integer prev = null, cur;
		while (elements > 0) {
			cur = h[0].findMin();
			for (int i = 1; i < 10; i++) {
				assertEquals(cur.intValue(), h[i].findMin().intValue());
			}
			for (int i = 0; i < 10; i++) {
				h[i].deleteMin();
			}
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
			elements--;
		}
	}

	@Test
	public void testHeapifySortWithComparator() {
		Random generator = new Random(1);

		Integer[] a = new Integer[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = generator.nextInt();
		}

		@SuppressWarnings("unchecked")
		Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, 10);

		h[0] = BinaryArrayHeap.heapify(a, comparator);
		h[1] = FixedSizeBinaryArrayHeap.heapify(a, comparator);
		h[2] = DaryArrayHeap.heapify(2, a, comparator);
		h[3] = DaryArrayHeap.heapify(3, a, comparator);
		h[4] = DaryArrayHeap.heapify(4, a, comparator);
		h[5] = DaryArrayHeap.heapify(5, a, comparator);
		h[6] = FixedSizeDaryArrayHeap.heapify(2, a, comparator);
		h[7] = FixedSizeDaryArrayHeap.heapify(3, a, comparator);
		h[8] = FixedSizeDaryArrayHeap.heapify(4, a, comparator);
		h[9] = FixedSizeDaryArrayHeap.heapify(5, a, comparator);

		int elements = SIZE;
		Integer prev = null, cur;
		while (elements > 0) {
			cur = h[0].findMin();
			for (int i = 1; i < 10; i++) {
				assertEquals(cur.intValue(), h[i].findMin().intValue());
			}
			for (int i = 0; i < 10; i++) {
				h[i].deleteMin();
			}
			if (prev != null) {
				assertTrue(comparator.compare(prev, cur) <= 0);
			}
			prev = cur;
			elements--;
		}
	}

	@Test
	public void testHeapifyZeroLengthArray() {
		Integer[] a = new Integer[0];

		@SuppressWarnings("unchecked")
		Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, 10);

		h[0] = BinaryArrayHeap.heapify(a);
		h[1] = DaryArrayHeap.heapify(2, a);
		h[2] = DaryArrayHeap.heapify(3, a);
		h[3] = DaryArrayHeap.heapify(4, a);
		h[4] = DaryArrayHeap.heapify(5, a);
		h[5] = FixedSizeBinaryArrayHeap.heapify(a);
		h[6] = FixedSizeDaryArrayHeap.heapify(2, a);
		h[7] = FixedSizeDaryArrayHeap.heapify(3, a);
		h[8] = FixedSizeDaryArrayHeap.heapify(4, a);
		h[9] = FixedSizeDaryArrayHeap.heapify(5, a);

		for (int i = 0; i < 10; i++) {
			assertTrue(h[i].isEmpty());
			try {
				h[i].insert(1);
				if (i >= 5) {
					fail("No!");
				}
			} catch (IllegalStateException e) {
				if (i < 5) {
					fail("No!");
				}
			}
		}
	}

	@Test
	public void testHeapifyZeroLengthArrayComparator() {
		Integer[] a = new Integer[0];

		@SuppressWarnings("unchecked")
		Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, 10);

		h[0] = BinaryArrayHeap.heapify(a, comparator);
		h[1] = DaryArrayHeap.heapify(2, a, comparator);
		h[2] = DaryArrayHeap.heapify(3, a, comparator);
		h[3] = DaryArrayHeap.heapify(4, a, comparator);
		h[4] = DaryArrayHeap.heapify(5, a, comparator);
		h[5] = FixedSizeBinaryArrayHeap.heapify(a, comparator);
		h[6] = FixedSizeDaryArrayHeap.heapify(2, a, comparator);
		h[7] = FixedSizeDaryArrayHeap.heapify(3, a, comparator);
		h[8] = FixedSizeDaryArrayHeap.heapify(4, a, comparator);
		h[9] = FixedSizeDaryArrayHeap.heapify(5, a, comparator);

		for (int i = 0; i < 10; i++) {
			assertTrue(h[i].isEmpty());
			try {
				h[i].insert(1);
				if (i >= 5) {
					fail("No!");
				}
			} catch (IllegalStateException e) {
				if (i < 5) {
					fail("No!");
				}
			}
		}
	}

	@Test
	public void testHeapifyZeroLengthArray1() {
		Integer[] a = new Integer[0];

		AddressableHeap<Integer, String> h = BinaryArrayAddressableHeap.heapify(a, null);

		assertTrue(h.isEmpty());
		assertEquals(1, h.insert(1).getKey().intValue());
	}

	@Test
	public void testHeapifyZeroLengthArray2() {
		Integer[] a = new Integer[0];

		Heap<Integer> h = BinaryArrayHeap.heapify(a, null);
		h.insert(1);
		h.insert(2);
		h.insert(3);
		h.insert(4);

		assertEquals(4, h.size());
	}

	@Test
	public void testHeapifyZeroLengthArrayComparator1() {
		Integer[] a = new Integer[0];

		AddressableHeap<Integer, String> h = BinaryArrayAddressableHeap.heapify(a, null, comparator);

		assertTrue(h.isEmpty());
		assertEquals(1, h.insert(1).getKey().intValue());
	}

	@Test
	public void testHeapifyBadParameters() {
		Integer[] a = new Integer[0];
		try {
			BinaryArrayHeap.heapify(null);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			BinaryArrayHeap.heapify(null, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			DaryArrayHeap.heapify(1, a);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			DaryArrayHeap.heapify(1, a, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			DaryArrayHeap.heapify(2, null);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			DaryArrayHeap.heapify(2, null, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			FixedSizeBinaryArrayHeap.heapify(null);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			FixedSizeBinaryArrayHeap.heapify(null, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			FixedSizeDaryArrayHeap.heapify(1, a);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			FixedSizeDaryArrayHeap.heapify(1, a, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			FixedSizeDaryArrayHeap.heapify(2, null);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			FixedSizeDaryArrayHeap.heapify(2, null, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			BinaryArrayAddressableHeap.heapify(null, null);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			BinaryArrayAddressableHeap.heapify(null, null, comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			BinaryArrayAddressableHeap.heapify(new Integer[2], new Integer[3]);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

		try {
			BinaryArrayAddressableHeap.heapify(new Integer[2], new Integer[3], comparator);
			fail("No!");
		} catch (IllegalArgumentException e) {
		}

	}

	@Test
	public void testBinaryArrayAddressableHeapifySort() {
		Random generator = new Random(1);

		Integer[] a = new Integer[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = generator.nextInt();
		}

		AddressableHeap<Integer, String> h = BinaryArrayAddressableHeap.heapify(a, null);

		int elements = SIZE;
		Integer prev = null, cur;
		while (elements > 0) {
			cur = h.findMin().getKey();
			assertEquals(cur, h.findMin().getKey());
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
			elements--;
		}
	}

	@Test
	public void testBinaryArrayAddressableHeapifySortWithValues() {
		Random generator = new Random(1);

		Integer[] a = new Integer[SIZE];
		String[] b = new String[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = generator.nextInt();
			b[i] = a[i].toString();
		}

		AddressableHeap<Integer, String> h = BinaryArrayAddressableHeap.heapify(a, b);

		int elements = SIZE;
		Integer prev = null, cur;
		while (elements > 0) {
			cur = h.findMin().getKey();
			assertEquals(cur, h.findMin().getKey());
			assertEquals(h.findMin().getValue(), h.findMin().getKey().toString());
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
			elements--;
		}
	}

	@Test
	public void testBinaryArrayAddressableHeapifySortComparator() {
		Random generator = new Random(1);

		Integer[] a = new Integer[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = generator.nextInt();
		}

		AddressableHeap<Integer, String> h = BinaryArrayAddressableHeap.heapify(a, null, comparator);

		int elements = SIZE;
		Integer prev = null, cur;
		while (elements > 0) {
			cur = h.findMin().getKey();
			assertEquals(cur, h.findMin().getKey());
			h.deleteMin();
			if (prev != null) {
				assertTrue(comparator.compare(prev, cur) <= 0);
			}
			prev = cur;
			elements--;
		}
	}

	@Test
	public void testBinaryArrayAddressableHeapifySortComparatorWithValues() {
		Random generator = new Random(1);

		Integer[] a = new Integer[SIZE];
		String[] b = new String[SIZE];
		for (int i = 0; i < SIZE; i++) {
			a[i] = generator.nextInt();
			b[i] = a[i].toString();
		}

		AddressableHeap<Integer, String> h = BinaryArrayAddressableHeap.heapify(a, b, comparator);

		int elements = SIZE;
		Integer prev = null, cur;
		while (elements > 0) {
			cur = h.findMin().getKey();
			assertEquals(cur, h.findMin().getKey());
			assertEquals(h.findMin().getValue(), h.findMin().getKey().toString());
			h.deleteMin();
			if (prev != null) {
				assertTrue(comparator.compare(prev, cur) <= 0);
			}
			prev = cur;
			elements--;
		}
	}

}
