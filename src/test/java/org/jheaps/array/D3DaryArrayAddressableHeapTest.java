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
package org.jheaps.array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.array.DaryArrayAddressableHeap;
import org.jheaps.tree.AbstractAddressableHeapTest;
import org.junit.Test;

public class D3DaryArrayAddressableHeapTest extends AbstractAddressableHeapTest {

	@Override
	protected AddressableHeap<Integer, Void> createHeap() {
		return new DaryArrayAddressableHeap<Integer, Void>(3);
	}

	@Override
	protected AddressableHeap<Integer, Void> createHeap(Comparator<Integer> comparator) {
		return new DaryArrayAddressableHeap<Integer, Void>(3, comparator);
	}

	@Override
	protected AddressableHeap<Integer, String> createHeapWithStringValues() {
		return new DaryArrayAddressableHeap<Integer, String>(3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongD() {
		new DaryArrayAddressableHeap<Integer, Void>(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegal1() {
		new DaryArrayAddressableHeap<Integer, String>(3, -5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegal2() {
		new DaryArrayAddressableHeap<Integer, String>(3, Integer.MAX_VALUE);
	}
	
	@Test
	public void testHandlesIterator() {
		Integer[] a = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) {
        	a[i] = SIZE-i;
        }
		
        DaryArrayAddressableHeap<Integer, Void> heap = DaryArrayAddressableHeap.heapify(3, a, null);
		Iterator<Handle<Integer, Void>> it = heap.handlesIterator();

		Set<Integer> found = new HashSet<Integer>();
		while(it.hasNext()) {
			Handle<Integer, Void> handle = it.next();
			assertTrue(found.add(handle.getKey()));
		}
		assertEquals(SIZE, found.size());
	}

}
