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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jheaps.AddressableHeap.Handle;
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

	private void test(Random rng) {
		@SuppressWarnings("unchecked")
		AddressableHeap<Integer>[] h = (AddressableHeap<Integer>[]) Array.newInstance(AddressableHeap.class, 3);
		h[0] = new PairingHeap<Integer>();
		h[1] = new ExplicitBinaryHeap<Integer>();
		h[2] = new FibonacciHeap<Integer>();

		@SuppressWarnings("unchecked")
		List<Handle<Integer>>[] s = (List<Handle<Integer>>[]) Array.newInstance(List.class, 3);
		s[0] = new ArrayList<Handle<Integer>>();
		s[1] = new ArrayList<Handle<Integer>>();
		s[2] = new ArrayList<Handle<Integer>>();

		for (int i = 0; i < SIZE; i++) {
			Integer k = rng.nextInt();
			s[0].add(h[0].insert(k));
			s[1].add(h[1].insert(k));
			s[2].add(h[2].insert(k));
			assertEquals(h[0].findMin().getKey().intValue(), h[1].findMin().getKey().intValue());
			assertEquals(h[0].findMin().getKey().intValue(), h[2].findMin().getKey().intValue());
		}

		for (int i = 0; i < 10; i++) {
			Iterator<Handle<Integer>> it0 = s[0].iterator();
			Iterator<Handle<Integer>> it1 = s[1].iterator();
			Iterator<Handle<Integer>> it2 = s[2].iterator();
			while (it0.hasNext() && it1.hasNext() && it2.hasNext()) {
				Handle<Integer> h0 = it0.next();
				Handle<Integer> h1 = it1.next();
				Handle<Integer> h2 = it2.next();
				int newKey = h0.getKey() / 2;
				if (newKey < h0.getKey()) {
					h0.decreaseKey(newKey);
					h1.decreaseKey(newKey);
					h2.decreaseKey(newKey);
				}
				assertEquals(h[0].findMin().getKey().intValue(), h[1].findMin().getKey().intValue());
				assertEquals(h[0].findMin().getKey().intValue(), h[2].findMin().getKey().intValue());
			}
		}

		while (!h[0].isEmpty()) {
			assertEquals(h[0].findMin().getKey().intValue(), h[1].findMin().getKey().intValue());
			assertEquals(h[0].findMin().getKey().intValue(), h[2].findMin().getKey().intValue());
			h[0].deleteMin();
			h[1].deleteMin();
			h[2].deleteMin();
		}

	}

}
