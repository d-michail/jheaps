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

import java.util.Comparator;

public class FixedSizeBinaryArrayAddressableHeapTest extends AbstractStaticAddressableHeapTest {

	protected AddressableHeap<Integer, String> createHeap(Comparator<Integer> comparator, int capacity) {
		return new FixedSizeBinaryArrayAddressableHeap<Integer, String>(comparator, capacity);
	}

	protected AddressableHeap<Integer, String> createHeap(int capacity) {
		return new FixedSizeBinaryArrayAddressableHeap<Integer, String>(capacity);
	}

}
