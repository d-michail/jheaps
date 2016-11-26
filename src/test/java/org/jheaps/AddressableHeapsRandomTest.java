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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jheaps.AddressableHeap.Handle;
import org.jheaps.array.BinaryArrayAddressableHeap;
import org.junit.Test;

public class AddressableHeapsRandomTest {

	private static final int SIZE = 250000;

	@Test
	public void test() {
		test(new Random());
	}

	@Test
	public void testSeed13() {
		test(new Random(13));
	}

	@Test
	public void testSeed37() {
		test(new Random(37));
	}

	@Test
	public void testRandomDeletesSeed37() {
		testRandomDeletes(37);
	}

	@Test
	public void testRandomDelete() {
		testRandomDeletes(new Random().nextLong());
	}

	private void test(Random rng) {

		final int classes = 5;

		@SuppressWarnings("unchecked")
		AddressableHeap<Integer, Void>[] h = (AddressableHeap<Integer, Void>[]) Array.newInstance(AddressableHeap.class,
				classes);
		h[0] = new PairingHeap<Integer, Void>();
		h[1] = new BinaryTreeAddressableHeap<Integer, Void>();
		h[2] = new FibonacciHeap<Integer, Void>();
		h[3] = new BinaryArrayAddressableHeap<Integer, Void>();
		h[4] = new CostlessMeldPairingHeap<Integer, Void>();

		@SuppressWarnings("unchecked")
		List<Handle<Integer, Void>>[] s = (List<Handle<Integer, Void>>[]) Array.newInstance(List.class, classes);
		s[0] = new ArrayList<Handle<Integer, Void>>();
		s[1] = new ArrayList<Handle<Integer, Void>>();
		s[2] = new ArrayList<Handle<Integer, Void>>();
		s[3] = new ArrayList<Handle<Integer, Void>>();
		s[4] = new ArrayList<Handle<Integer, Void>>();

		for (int i = 0; i < SIZE; i++) {
			Integer k = rng.nextInt();
			for (int j = 0; j < classes; j++) {
				s[j].add(h[j].insert(k, null));
			}
			for (int j = 1; j < classes; j++) {
				assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
			}
		}

		for (int i = 0; i < 10; i++) {
			Iterator<Handle<Integer, Void>> it0 = s[0].iterator();
			Iterator<Handle<Integer, Void>> it1 = s[1].iterator();
			Iterator<Handle<Integer, Void>> it2 = s[2].iterator();
			Iterator<Handle<Integer, Void>> it3 = s[3].iterator();
			while (it0.hasNext() && it1.hasNext() && it2.hasNext() && it3.hasNext()) {
				Handle<Integer, Void> h0 = it0.next();
				Handle<Integer, Void> h1 = it1.next();
				Handle<Integer, Void> h2 = it2.next();
				Handle<Integer, Void> h3 = it3.next();
				int newKey = h0.getKey() / 2;
				if (newKey < h0.getKey()) {
					h0.decreaseKey(newKey);
					h1.decreaseKey(newKey);
					h2.decreaseKey(newKey);
					h3.decreaseKey(newKey);
				}
				assertEquals(h[0].findMin().getKey().intValue(), h[1].findMin().getKey().intValue());
				assertEquals(h[0].findMin().getKey().intValue(), h[2].findMin().getKey().intValue());
				assertEquals(h[0].findMin().getKey().intValue(), h[3].findMin().getKey().intValue());
			}
		}

		while (!h[0].isEmpty()) {
			assertEquals(h[0].findMin().getKey().intValue(), h[1].findMin().getKey().intValue());
			assertEquals(h[0].findMin().getKey().intValue(), h[2].findMin().getKey().intValue());
			assertEquals(h[0].findMin().getKey().intValue(), h[3].findMin().getKey().intValue());
			h[0].deleteMin();
			h[1].deleteMin();
			h[2].deleteMin();
			h[3].deleteMin();
		}

	}

	private void testRandomDeletes(long seed) {

		final int classes = 5;

		@SuppressWarnings("unchecked")
		AddressableHeap<Integer, Void>[] h = (AddressableHeap<Integer, Void>[]) Array.newInstance(AddressableHeap.class,
				classes);
		h[0] = new PairingHeap<Integer, Void>();
		h[1] = new BinaryTreeAddressableHeap<Integer, Void>();
		h[2] = new FibonacciHeap<Integer, Void>();
		h[3] = new BinaryArrayAddressableHeap<Integer, Void>();
		h[4] = new CostlessMeldPairingHeap<Integer, Void>();

		@SuppressWarnings("unchecked")
		List<Handle<Integer, Void>>[] s = (List<Handle<Integer, Void>>[]) Array.newInstance(List.class, classes);
		s[0] = new ArrayList<Handle<Integer, Void>>();
		s[1] = new ArrayList<Handle<Integer, Void>>();
		s[2] = new ArrayList<Handle<Integer, Void>>();
		s[3] = new ArrayList<Handle<Integer, Void>>();
		s[4] = new ArrayList<Handle<Integer, Void>>();

		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < classes; j++) {
				s[j].add(h[j].insert(i, null));
			}
			for (int j = 1; j < classes; j++) {
				assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
			}
		}

		for (int j = 0; j < classes; j++) {
			Collections.shuffle(s[j], new Random(seed));
		}

		for (int i = 0; i < SIZE; i++) {
			for (int j = 1; j < classes; j++) {
				assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
			}
			for (int j = 0; j < classes; j++) {
				s[j].get(i).delete();
			}
		}

		for (int j = 0; j < classes; j++) {
			assertTrue(h[j].isEmpty());
		}

	}

}
