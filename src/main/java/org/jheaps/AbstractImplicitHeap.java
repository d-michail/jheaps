/*
 * (C) Copyright 2014-2016, by Dimitrios Michail.
 *
 * Java Heaps Library
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
import java.util.NoSuchElementException;

/**
 * Abstract implementation of a heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the key type
 */
abstract class AbstractImplicitHeap<K> implements Heap<K>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The maximum heap capacity.
	 */
	protected static final int MAX_HEAP_CAPACITY = Integer.MAX_VALUE - 8 - 1;

	/**
	 * The minimum heap capacity.
	 */
	protected static final int MIN_HEAP_CAPACITY = 1;

	/**
	 * The comparator used to maintain order in this heap, or null if it uses
	 * the natural ordering of its keys.
	 *
	 * @serial
	 */
	protected Comparator<? super K> comparator;

	/**
	 * The array use for representing the tree.
	 */
	protected K[] array;

	/**
	 * Number of elements in the heap.
	 */
	protected int size;

	AbstractImplicitHeap() {
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AbstractImplicitHeap(Comparator<? super K> comparator, int capacity) {
		checkCapacity(capacity);
		this.array = (K[]) new Object[capacity + 1];
		this.size = 0;
		this.comparator = comparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public K findMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return array[1];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public long size() {
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comparator<? super K> comparator() {
		return comparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public void clear() {
		size = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@LogarithmicTime(amortized = true)
	public void insert(K key) {
		if (key == null) {
			throw new NullPointerException("Null keys not permitted");
		}

		// make sure there is space
		if (size == array.length - 1) {
			ensureCapacity(2 * array.length - 1);
		}

		array[++size] = key;

		if (comparator == null) {
			fixup(size);
		} else {
			fixupWithComparator(size);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@LogarithmicTime(amortized = true)
	public void deleteMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		array[1] = array[size--];

		if (comparator == null) {
			fixdown(1);
		} else {
			fixdownWithComparator(1);
		}

		if (2 * MIN_HEAP_CAPACITY < array.length - 1 && 4 * size < array.length - 1) {
			ensureCapacity((array.length - 1) / 2 + 1);
		}
	}

	protected final void checkCapacity(int capacity) {
		if (capacity < MIN_HEAP_CAPACITY) {
			throw new IllegalArgumentException("Heap capacity must be >= " + MIN_HEAP_CAPACITY);
		}
		if (capacity > MAX_HEAP_CAPACITY) {
			throw new IllegalArgumentException("Heap capacity too large");
		}
	}

	protected abstract void ensureCapacity(int capacity);

	protected abstract void fixup(int k);

	protected abstract void fixupWithComparator(int k);

	protected abstract void fixdown(int k);

	protected abstract void fixdownWithComparator(int k);

}
