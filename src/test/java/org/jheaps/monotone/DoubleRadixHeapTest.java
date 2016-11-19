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

import java.util.Random;

import org.jheaps.Heap;
import org.jheaps.monotone.DoubleRadixHeap;
import org.junit.Test;

public class DoubleRadixHeapTest {

	private static final int SIZE = 100000;

	@Test
	public void testVerySmall() {
		Heap<Double> h = new DoubleRadixHeap(15.0, 50.5);

		h.insert(15.3);
		h.insert(50.4);
		h.insert(20.999999);
		h.insert(50.5);
		h.insert(30.3);
		h.insert(25.2);
		h.insert(17.7777);

		assertEquals(7, h.size());
		assertEquals(15.3, h.findMin(), 1e-9);
		assertEquals(7, h.size());
		assertEquals(15.3, h.deleteMin(), 1e-9);
		assertEquals(6, h.size());
		assertEquals(17.7777, h.findMin(), 1e-9);
		assertEquals(17.7777, h.deleteMin(), 1e-9);
		assertEquals(20.999999, h.findMin(), 1e-9);
		assertEquals(20.999999, h.deleteMin(), 1e-9);
		assertEquals(25.2, h.findMin(), 1e-9);
		assertEquals(25.2, h.deleteMin(), 1e-9);
		assertEquals(30.3, h.findMin(), 1e-9);
		assertEquals(30.3, h.deleteMin(), 1e-9);
		assertEquals(50.4, h.findMin(), 1e-9);
		assertEquals(50.4, h.deleteMin(), 1e-9);
		assertEquals(50.5, h.findMin(), 1e-9);
		assertEquals(50.5, h.deleteMin(), 1e-9);
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void test() {
		Heap<Double> h = new DoubleRadixHeap(0.0, SIZE);

		for (long i = 0; i < SIZE; i++) {
			h.insert(Double.valueOf(i));
			assertEquals(Double.valueOf(0), h.findMin(), 1e-9);
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE - 1; i >= 0; i--) {
			assertEquals(Double.valueOf(SIZE - i - 1), h.findMin(), 1e-9);
			h.deleteMin();
		}
	}

	@Test
	public void testSortRandomSeed1() {
		Heap<Double> h = new DoubleRadixHeap(0.0, 1.0);

		Random generator = new Random(1);

		h.insert(0.0d);
		for (int i = 1; i < SIZE; i++) {
			double d = generator.nextDouble();
			h.insert(d);
		}

		Double prev = null, cur;
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
		Heap<Double> h = new DoubleRadixHeap(0.0, 1.0);

		Random generator = new Random(2);

		h.insert(0.0d);
		for (int i = 1; i < SIZE; i++) {
			double d = generator.nextDouble();
			h.insert(d);
		}

		Double prev = null, cur;
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
		Heap<Double> h = new DoubleRadixHeap(1.0, 1.0);

		for (int i = 0; i < 15; i++) {
			h.insert(1.0);
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(1.0, h.deleteMin(), 1e-9);
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testMaxDifference() {
		Heap<Double> h = new DoubleRadixHeap(0.0, Double.MAX_VALUE);

		h.insert(0.0);
		h.insert(Double.MAX_VALUE);

		assertEquals(2, h.size());
		assertEquals(0.0, h.deleteMin(), 1e-9);
		assertEquals(Double.MAX_VALUE, h.deleteMin(), 1e-9);
		assertEquals(0, h.size());
	}

}
