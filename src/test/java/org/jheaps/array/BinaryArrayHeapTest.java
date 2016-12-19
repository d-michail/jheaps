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

import java.util.Comparator;

import org.jheaps.Heap;
import org.jheaps.tree.AbstractComparatorLongHeapTest;
import org.junit.Test;

public class BinaryArrayHeapTest extends AbstractComparatorLongHeapTest {

	protected Heap<Long> createHeap() {
		return new BinaryArrayHeap<Long>();
	}

	protected Heap<Long> createHeap(int capacity) {
		return new BinaryArrayHeap<Long>(capacity);
	}

	@Override
	protected Heap<Long> createHeap(Comparator<Long> comparator) {
		return new BinaryArrayHeap<Long>(comparator);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize() {
		Heap<Long> h = createHeap(-4);
		h.insert(1L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize1() {
		Heap<Long> h = createHeap(-1);
		h.insert(1L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSize2() {
		Heap<Long> h = createHeap(Integer.MAX_VALUE - 8);
		h.insert(1L);
	}

}
