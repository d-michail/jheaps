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

import java.io.IOException;
import java.util.Comparator;

import org.junit.Test;

public class PairingHeapTest extends AbstractAddressableHeapTest {

	@Test
	public void testMeld() throws IOException, ClassNotFoundException {

		PairingHeap<Integer> h1 = new PairingHeap<Integer>();
		PairingHeap<Integer> h2 = new PairingHeap<Integer>();

		for (int i = 0; i < SIZE; i++) {
			if (i % 2 == 0) {
				h1.insert(i);
			} else {
				h2.insert(i);
			}
		}

		h1.meld(h2);

		assertTrue(h2.isEmpty());
		assertEquals(0, h2.size());

		for (int i = 0; i < SIZE; i++) {
			assertEquals(Integer.valueOf(i), h1.findMin().getKey());
			h1.deleteMin();
		}
		assertTrue(h1.isEmpty());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testMeldWrong() throws IOException, ClassNotFoundException {

		PairingHeap<Integer> h1 = new PairingHeap<Integer>();
		PairingHeap<Integer> h2 = new PairingHeap<Integer>(comparator);

		for (int i = 0; i < SIZE; i++) {
			if (i % 2 == 0) {
				h1.insert(i);
			} else {
				h2.insert(i);
			}
		}

		h1.meld(h2);

		assertTrue(h2.isEmpty());
		assertEquals(0, h2.size());

		for (int i = 0; i < SIZE; i++) {
			assertEquals(Integer.valueOf(i), h1.findMin().getKey());
			h1.deleteMin();
		}
		assertTrue(h1.isEmpty());

	}

	@Override
	protected AddressableHeap<Integer> createHeap() {
		return new PairingHeap<Integer>();
	}

	@Override
	protected AddressableHeap<Integer> createHeap(Comparator<Integer> comparator) {
		return new PairingHeap<Integer>(comparator);
	}
}
