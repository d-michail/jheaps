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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Abstract implementation of a d-ary heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the key type
 */
abstract class AbstractDaryImplicitHeap<K> extends AbstractImplicitHeap<K> implements Serializable {

	private static final long serialVersionUID = 1L;

	protected int d;

	AbstractDaryImplicitHeap() {
		super();
	}

	public AbstractDaryImplicitHeap(int d, Comparator<? super K> comparator, int capacity) {
		super(comparator, capacity);
		if (d < 2) {
			throw new IllegalArgumentException("D-ary heaps must have at least 2 children per node");
		}
		this.d = d;
	}

	@SuppressWarnings("unchecked")
	protected void fixup(int k) {
		// assert k >= 1 && k <= size;

		K key = array[k];
		while (k > 1) {
			int p = (k - 2) / d + 1;
			if (((Comparable<? super K>) array[p]).compareTo(key) <= 0) {
				break;
			}
			array[k] = array[p];
			k = p;
		}
		array[k] = key;
	}

	protected void fixupWithComparator(int k) {
		// assert k >= 1 && k <= size;

		K key = array[k];
		while (k > 1) {
			int p = (k - 2) / d + 1;
			if (comparator.compare(array[p], key) <= 0) {
				break;
			}
			array[k] = array[p];
			k = p;
		}
		array[k] = key;
	}

	@SuppressWarnings("unchecked")
	protected void fixdown(int k) {
		int c;
		K key = array[k];
		while ((c = d * (k - 1) + 2) <= size) {
			int maxc = c;
			for (int i = 1; i < d; i++) {
				if (c + i <= size && ((Comparable<? super K>) array[maxc]).compareTo(array[c + i]) > 0) {
					maxc = c + i;
				}
			}
			if (((Comparable<? super K>) key).compareTo(array[maxc]) <= 0) {
				break;
			}
			array[k] = array[maxc];
			k = maxc;
		}
		array[k] = key;
	}

	protected void fixdownWithComparator(int k) {
		int c;
		K key = array[k];
		while ((c = d * (k - 1) + 2) <= size) {
			int maxc = c;
			for (int i = 1; i < d; i++) {
				if (c + i <= size && comparator.compare(array[maxc], array[c + i]) > 0) {
					maxc = c + i;
				}
			}
			if (comparator.compare(key, array[maxc]) <= 0) {
				break;
			}
			array[k] = array[maxc];
			k = maxc;
		}
		array[k] = key;
	}

}
