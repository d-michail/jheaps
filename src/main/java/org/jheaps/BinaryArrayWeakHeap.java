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
import java.util.BitSet;
import java.util.Comparator;

import org.jheaps.annotations.LinearTime;

/**
 * TODO
 *
 * @param <K>
 *            the type of keys maintained by this heap
 *
 * @author Dimitrios Michail
 * @see Heap
 * @see Comparable
 * @see Comparator
 * @see Serializable
 */
public class BinaryArrayWeakHeap<K> extends AbstractBinaryArrayWeakHeap<K> implements Serializable {

	private final static long serialVersionUID = 1;

	/**
	 * Default initial capacity of the binary heap.
	 */
	public static final int DEFAULT_HEAP_CAPACITY = 128;

	/**
	 * Constructs a new, empty heap, using the natural ordering of its keys.
	 *
	 * <p>
	 * All keys inserted into the heap must implement the {@link Comparable}
	 * interface. Furthermore, all such keys must be <em>mutually
	 * comparable</em>: {@code k1.compareTo(k2)} must not throw a
	 * {@code ClassCastException} for any keys {@code k1} and {@code k2} in the
	 * heap. If the user attempts to put a key into the heap that violates this
	 * constraint (for example, the user attempts to put a string key into a
	 * heap whose keys are integers), the {@code insert(Object key)} call will
	 * throw a {@code ClassCastException}.
	 *
	 * <p>
	 * The initial capacity of the heap is
	 * {@link BinaryArrayWeakHeap#DEFAULT_HEAP_CAPACITY} and adjusts
	 * automatically based on the sequence of insertions and deletions.
	 */
	public BinaryArrayWeakHeap() {
		super(null, DEFAULT_HEAP_CAPACITY);
	}

	/**
	 * Constructs a new, empty heap, with a provided initial capacity using the
	 * natural ordering of its keys.
	 *
	 * <p>
	 * All keys inserted into the heap must implement the {@link Comparable}
	 * interface. Furthermore, all such keys must be <em>mutually
	 * comparable</em>: {@code k1.compareTo(k2)} must not throw a
	 * {@code ClassCastException} for any keys {@code k1} and {@code k2} in the
	 * heap. If the user attempts to put a key into the heap that violates this
	 * constraint (for example, the user attempts to put a string key into a
	 * heap whose keys are integers), the {@code insert(Object key)} call will
	 * throw a {@code ClassCastException}.
	 *
	 * <p>
	 * The initial capacity of the heap is provided by the user and is adjusted
	 * automatically based on the sequence of insertions and deletions.
	 *
	 * @param capacity
	 *            the initial heap capacity
	 */
	public BinaryArrayWeakHeap(int capacity) {
		super(null, capacity);
	}

	/**
	 * Constructs a new, empty heap, ordered according to the given comparator.
	 *
	 * <p>
	 * All keys inserted into the heap must be <em>mutually comparable</em> by
	 * the given comparator: {@code comparator.compare(k1,
	 * k2)} must not throw a {@code ClassCastException} for any keys {@code k1}
	 * and {@code k2} in the heap. If the user attempts to put a key into the
	 * heap that violates this constraint, the {@code insert(Object key)} call
	 * will throw a {@code ClassCastException}.
	 *
	 * <p>
	 * The initial capacity of the heap is
	 * {@link BinaryArrayWeakHeap#DEFAULT_HEAP_CAPACITY} and adjusts
	 * automatically based on the sequence of insertions and deletions.
	 *
	 * @param comparator
	 *            the comparator that will be used to order this heap. If
	 *            {@code null}, the {@linkplain Comparable natural ordering} of
	 *            the keys will be used.
	 */
	public BinaryArrayWeakHeap(Comparator<? super K> comparator) {
		super(comparator, DEFAULT_HEAP_CAPACITY);
	}

	/**
	 * Constructs a new, empty heap, with a provided initial capacity ordered
	 * according to the given comparator.
	 *
	 * <p>
	 * All keys inserted into the heap must be <em>mutually comparable</em> by
	 * the given comparator: {@code comparator.compare(k1,
	 * k2)} must not throw a {@code ClassCastException} for any keys {@code k1}
	 * and {@code k2} in the heap. If the user attempts to put a key into the
	 * heap that violates this constraint, the {@code insert(Object key)} call
	 * will throw a {@code ClassCastException}.
	 *
	 * <p>
	 * The initial capacity of the heap is provided by the user and is adjusted
	 * automatically based on the sequence of insertions and deletions.
	 *
	 * @param comparator
	 *            the comparator that will be used to order this heap. If
	 *            {@code null}, the {@linkplain Comparable natural ordering} of
	 *            the keys will be used.
	 * @param capacity
	 *            the initial heap capacity
	 */
	public BinaryArrayWeakHeap(Comparator<? super K> comparator, int capacity) {
		super(comparator, capacity);
	}

	/**
	 * Create a heap from an array of elements. The elements of the array are
	 * not destroyed. The method has linear time complexity.
	 *
	 * @param <K>
	 *            the type of keys maintained by the heap
	 * @param array
	 *            an array of elements
	 * @return a binary heap
	 * @throws IllegalArgumentException
	 *             in case the array is null
	 */
	@LinearTime
	public static <K> BinaryArrayWeakHeap<K> heapify(K[] array) {
		if (array == null) {
			throw new IllegalArgumentException("Array cannot be null");
		}
		if (array.length == 0) {
			return new BinaryArrayWeakHeap<K>();
		}

		BinaryArrayWeakHeap<K> h = new BinaryArrayWeakHeap<K>(array.length);

		System.arraycopy(array, 0, h.array, 0, array.length);
		h.size = array.length;

		for (int j = h.size - 1; j > 0; j--) {
			h.join(h.dancestor(j), j);
		}

		return h;
	}

	/**
	 * Create a heap from an array of elements. The elements of the array are
	 * not destroyed. The method has linear time complexity.
	 *
	 * @param <K>
	 *            the type of keys maintained by the heap
	 * @param array
	 *            an array of elements
	 * @param comparator
	 *            the comparator to use
	 * @return a binary heap
	 * @throws IllegalArgumentException
	 *             in case the array is null
	 */
	@LinearTime
	public static <K> BinaryArrayWeakHeap<K> heapify(K[] array, Comparator<? super K> comparator) {
		if (array == null) {
			throw new IllegalArgumentException("Array cannot be null");
		}
		if (array.length == 0) {
			return new BinaryArrayWeakHeap<K>(comparator);
		}

		BinaryArrayWeakHeap<K> h = new BinaryArrayWeakHeap<K>(comparator, array.length);

		System.arraycopy(array, 0, h.array, 0, array.length);
		h.size = array.length;

		for (int j = h.size - 1; j > 0; j--) {
			h.joinWithComparator(h.dancestor(j), j);
		}

		return h;
	}

	/**
	 * Ensure that the array representation has the necessary capacity.
	 * 
	 * @param capacity
	 *            the requested capacity
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void ensureCapacity(int capacity) {
		checkCapacity(capacity);
		K[] newArray = (K[]) new Object[capacity];
		System.arraycopy(array, 0, newArray, 0, size);
		array = newArray;
		BitSet newBitSet = new BitSet(capacity);
		newBitSet.or(reverse);
		reverse = newBitSet;
	}

}