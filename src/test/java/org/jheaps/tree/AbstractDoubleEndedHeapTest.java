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

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.DoubleEndedHeap;
import org.junit.Test;

public abstract class AbstractDoubleEndedHeapTest extends AbstractComparatorLongHeapTest {

	@Override
	protected abstract DoubleEndedHeap<Long> createHeap();

	@Override
	protected abstract DoubleEndedHeap<Long> createHeap(int capacity);

	@Override
	protected abstract DoubleEndedHeap<Long> createHeap(Comparator<Long> comparator);

	@Test
	public void testInsertDeleteMax() {
		DoubleEndedHeap<Long> h = createHeap();

		for (long i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Long.valueOf(0), h.findMin());
			assertEquals(Long.valueOf(i), h.findMax());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin(), Long.valueOf(0));
			assertEquals(h.findMax(), Long.valueOf(i));
			h.deleteMax();
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadDeleteMax() {
		DoubleEndedHeap<Long> h = createHeap();
		h.deleteMax();
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadFindMax() {
		DoubleEndedHeap<Long> h = createHeap();
		h.findMax();
	}

	@Test
	public void testSortMaxRandom() {
		testSort(SIZE, new Random(1));
		testSortReverse(SIZE, new Random(1));
		testSort(SIZE, new Random(2));
		testSortReverse(SIZE, new Random(2));
		testSort(SIZE, new Random(3));
		testSortReverse(SIZE, new Random(3));
		testSort(SIZE, new Random(4));
		testSortReverse(SIZE, new Random(4));
		testSort(SIZE, new Random(5));
		testSortReverse(SIZE, new Random(5));
		testSort(SIZE, new Random(6));
		testSortReverse(SIZE, new Random(6));
	}

	@Test
	public void testSmallInstanceSortMaxRandom() {
		for (long seed = 13; seed < 1000; seed++) {
			testSort(SIZE / 100, new Random(seed));
			testSortReverse(SIZE / 100, new Random(seed));
		}
	}

	@Test
	public void testFindMaxDeleteMaxSameObject() {
		DoubleEndedHeap<Long> h = createHeap();

		Random generator = new Random(1);

		for (int i = 0; i < SIZE; i++) {
			h.insert(generator.nextLong());
		}

		while (!h.isEmpty()) {
			assertEquals(h.findMax(), h.deleteMax());
		}
	}

	@Test
	public void testWithComparatorMax() {
		DoubleEndedHeap<Long> h = createHeap(comparator);
		long i;

		for (i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Long.valueOf(i), h.findMin());
			assertEquals(Long.valueOf(0), h.findMax());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin(), Long.valueOf(i));
			assertEquals(h.findMax(), Long.valueOf(0));
			h.deleteMin();
		}

	}

	@Test
	public void test3Elements() {
		DoubleEndedHeap<Long> h = createHeap();

		assertTrue(h.isEmpty());

		h.insert(780L);
		h.insert(800L);
		h.insert(900L);

		assertEquals(h.size(), 3);

		assertEquals(Long.valueOf(780), h.findMin());
		assertEquals(Long.valueOf(900), h.findMax());
	}

	@Test
	public void test3Elements1() {
		DoubleEndedHeap<Long> h = createHeap();

		assertTrue(h.isEmpty());

		h.insert(900L);
		h.insert(800L);
		h.insert(780L);

		assertEquals(h.size(), 3);

		assertEquals(Long.valueOf(780), h.findMin());
		assertEquals(Long.valueOf(900), h.findMax());
		assertEquals(Long.valueOf(900), h.deleteMax());
	}

	@Test
	public void test3ElementsComparator() {
		DoubleEndedHeap<Long> h = createHeap(comparator);

		assertTrue(h.isEmpty());

		h.insert(900L);
		h.insert(800L);
		h.insert(780L);

		assertEquals(h.size(), 3);

		assertEquals(Long.valueOf(780), h.findMax());
		assertEquals(Long.valueOf(900), h.findMin());
	}

	@Test
	public void test3ElementsComparator1() {
		DoubleEndedHeap<Long> h = createHeap(comparator);

		assertTrue(h.isEmpty());

		h.insert(900L);
		h.insert(800L);
		h.insert(780L);
		h.insert(850L);

		assertEquals(h.size(), 4);

		assertEquals(Long.valueOf(780), h.findMax());
		assertEquals(Long.valueOf(900), h.findMin());
		assertEquals(Long.valueOf(780), h.deleteMax());
	}

	@Test
	public void testOnly4ReverseMax() {
		DoubleEndedHeap<Long> h = createHeap(comparator);

		assertTrue(h.isEmpty());

		h.insert(780L);
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(780), h.findMin());
		assertEquals(Long.valueOf(780), h.findMax());

		h.insert(-389L);
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(780), h.findMin());
		assertEquals(Long.valueOf(-389), h.findMax());

		h.insert(306L);
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(780), h.findMin());
		assertEquals(Long.valueOf(-389), h.findMax());

		h.insert(579L);
		assertEquals(h.size(), 4);
		assertEquals(Long.valueOf(780), h.findMin());
		assertEquals(Long.valueOf(-389), h.findMax());

		h.deleteMin();
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(579), h.findMin());
		assertEquals(Long.valueOf(-389), h.findMax());

		h.deleteMax();
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(579), h.findMin());
		assertEquals(Long.valueOf(306), h.findMax());

		h.deleteMin();
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(306), h.findMin());

		h.deleteMax();
		assertEquals(h.size(), 0);

		assertTrue(h.isEmpty());
	}

	private void testSort(int size, Random rng) {
		DoubleEndedHeap<Long> h = createHeap();

		for (int i = 0; i < size; i++) {
			h.insert(rng.nextLong());
		}

		Long prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMax();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) >= 0);
			}
			prev = cur;
		}
	}

	private void testSortReverse(int size, Random rng) {
		DoubleEndedHeap<Long> h = createHeap(comparator);

		for (int i = 0; i < size; i++) {
			h.insert(rng.nextLong());
		}

		Long prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.deleteMax();
			if (prev != null) {
				assertTrue(comparator.compare(prev, cur) >= 0);
			}
			prev = cur;
		}
	}

}
