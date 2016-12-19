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

import java.util.NoSuchElementException;

import org.jheaps.ValueHeap;
import org.jheaps.tree.AbstractIntegerHeapTest;
import org.junit.Test;

public class BinaryArrayIntegerValueHeapTest extends AbstractIntegerHeapTest {

	@Override
	protected ValueHeap<Integer, String> createHeap() {
		return new BinaryArrayIntegerValueHeap<String>(1);
	}

	@Override
	protected ValueHeap<Integer, String> createHeap(int capacity) {
		return new BinaryArrayIntegerValueHeap<String>(capacity);
	}

	@Test(expected = NoSuchElementException.class)
	public void testBadFindMinValue() {
		ValueHeap<Integer, String> h = createHeap(1);
		h.findMinValue();
	}

}
