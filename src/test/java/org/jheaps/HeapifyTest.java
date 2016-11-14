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

		h[0] = BinaryHeap.heapify(a);
		h[1] = FixedSizeBinaryHeap.heapify(a);
		h[2] = DaryHeap.heapify(2, a);
		h[3] = DaryHeap.heapify(3, a);
		h[4] = DaryHeap.heapify(4, a);
		h[5] = DaryHeap.heapify(5, a);
		h[6] = FixedSizeDaryHeap.heapify(2, a);
		h[7] = FixedSizeDaryHeap.heapify(3, a);
		h[8] = FixedSizeDaryHeap.heapify(4, a);
		h[9] = FixedSizeDaryHeap.heapify(5, a);

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

		h[0] = BinaryHeap.heapify(a, comparator);
		h[1] = FixedSizeBinaryHeap.heapify(a, comparator);
		h[2] = DaryHeap.heapify(2, a, comparator);
		h[3] = DaryHeap.heapify(3, a, comparator);
		h[4] = DaryHeap.heapify(4, a, comparator);
		h[5] = DaryHeap.heapify(5, a, comparator);
		h[6] = FixedSizeDaryHeap.heapify(2, a, comparator);
		h[7] = FixedSizeDaryHeap.heapify(3, a, comparator);
		h[8] = FixedSizeDaryHeap.heapify(4, a, comparator);
		h[9] = FixedSizeDaryHeap.heapify(5, a, comparator);

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

}
