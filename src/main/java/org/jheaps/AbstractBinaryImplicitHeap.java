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
 * Abstract implementation of a binary heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
abstract class AbstractBinaryImplicitHeap<K> extends AbstractImplicitHeap<K> implements Serializable {

	private static final long serialVersionUID = 1L;

	AbstractBinaryImplicitHeap() {
		super();
	}

	public AbstractBinaryImplicitHeap(Comparator<? super K> comparator, int capacity) {
		super(comparator, capacity);
	}

	@SuppressWarnings("unchecked")
	protected void fixup(int k) {
		assert k >= 1 && k <= size;

		while (k > 1 && ((Comparable<? super K>) array[k / 2]).compareTo(array[k]) > 0) {
			K tmp = array[k];
			array[k] = array[k / 2];
			array[k / 2] = tmp;
			k /= 2;
		}
	}

	protected void fixupWithComparator(int k) {
		assert k >= 1 && k <= size;

		while (k > 1 && comparator.compare(array[k / 2], array[k]) > 0) {
			K tmp = array[k];
			array[k] = array[k / 2];
			array[k / 2] = tmp;
			k /= 2;
		}
	}

	@SuppressWarnings("unchecked")
	protected void fixdown(int k) {
		while (2 * k <= size) {
			int j = 2 * k;
			if (j < size && ((Comparable<? super K>) array[j]).compareTo(array[j + 1]) > 0) {
				j++;
			}
			if (((Comparable<? super K>) array[k]).compareTo(array[j]) <= 0) {
				break;
			}

			K tmp = array[k];
			array[k] = array[j];
			array[j] = tmp;
			k = j;
		}
	}

	protected void fixdownWithComparator(int k) {
		while (2 * k <= size) {
			int j = 2 * k;
			if (j < size && comparator.compare(array[j], array[j + 1]) > 0) {
				j++;
			}
			if (comparator.compare(array[k], array[j]) <= 0) {
				break;
			}

			K tmp = array[k];
			array[k] = array[j];
			array[j] = tmp;
			k = j;
		}
	}

}
