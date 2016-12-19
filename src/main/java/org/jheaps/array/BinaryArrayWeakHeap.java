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

import java.io.Serializable;
import java.util.BitSet;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.Constants;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LinearTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * An array based binary weak heap. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * The implementation uses an array in order to store the elements and
 * automatically maintains the size of the array much like a
 * {@link java.util.Vector} does, providing amortized O(log(n)) time cost for
 * the {@code insert} and {@code deleteMin} operations. Operation
 * {@code findMin}, is a worst-case O(1) operation. The bounds are worst-case if
 * the user initializes the heap with a capacity larger or equal to the total
 * number of elements that are going to be inserted into the heap.
 * 
 * <p>
 * Constructing such a heap from an array of elements can be performed using the
 * method {@link #heapify(Object[])} or {@link #heapify(Object[], Comparator)}
 * in linear time.
 *
 * <p>
 * Note that the ordering maintained by a binary heap, like any heap, and
 * whether or not an explicit comparator is provided, must be <em>consistent
 * with {@code equals}</em> if this heap is to correctly implement the
 * {@code Heap} interface. (See {@code Comparable} or {@code Comparator} for a
 * precise definition of <em>consistent with equals</em>.) This is so because
 * the {@code Heap} interface is defined in terms of the {@code equals}
 * operation, but a binary heap performs all key comparisons using its
 * {@code compareTo} (or {@code compare}) method, so two keys that are deemed
 * equal by this method are, from the standpoint of the binary heap, equal. The
 * behavior of a heap <em>is</em> well-defined even if its ordering is
 * inconsistent with {@code equals}; it just fails to obey the general contract
 * of the {@code Heap} interface.
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a heap concurrently, and at least one of the threads
 * modifies the heap structurally, it <em>must</em> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or more
 * elements or changing the key of some element.) This is typically accomplished
 * by synchronizing on some object that naturally encapsulates the heap.
 *
 * @param <K>
 *            the type of keys maintained by this heap
 *
 * @author Dimitrios Michail
 */
public class BinaryArrayWeakHeap<K> extends AbstractArrayWeakHeap<K> implements Serializable {

    private static final long serialVersionUID = 7721391024028836146L;

    /**
     * Default initial capacity of the binary heap.
     */
    public static final int DEFAULT_HEAP_CAPACITY = 16;

    /**
     * Reverse bits
     */
    protected BitSet reverse;

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
     * automatically based on the sequence of insertions and deletions. The
     * capacity will never become smaller than the initial requested capacity.
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
     * automatically based on the sequence of insertions and deletions. The
     * capacity will never become smaller than the initial requested capacity.
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
     * @return a heap
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
     * @return a heap
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
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public K findMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }
        return array[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime(amortized = true)
    public void insert(K key) {
        if (Constants.NOT_BENCHMARK) {
            if (key == null) {
                throw new NullPointerException("Null keys not permitted");
            }
            // make sure there is space
            if (size == array.length) {
                if (size == 0) {
                    ensureCapacity(1);
                } else {
                    ensureCapacity(2 * array.length);
                }
            }
        }

        array[size] = key;
        reverse.clear(size);

        if (size % 2 == 0) {
            reverse.clear(size / 2);
        }

        if (comparator == null) {
            fixup(size);
        } else {
            fixupWithComparator(size);
        }

        ++size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime(amortized = true)
    public K deleteMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }

        K result = array[0];

        size--;
        array[0] = array[size];
        array[size] = null;

        if (size > 1) {
            if (comparator == null) {
                fixdown(0);
            } else {
                fixdownWithComparator(0);
            }
        }

        if (Constants.NOT_BENCHMARK) {
            if (2 * minCapacity <= array.length && 4 * size < array.length) {
                ensureCapacity(array.length / 2);
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initCapacity(int capacity) {
        this.array = (K[]) new Object[capacity];
        this.reverse = new BitSet(capacity);
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

    /**
     * Return the distinguished ancestor of an element.
     * 
     * @param j
     *            the element
     * @return the distinguished ancestor of the element
     */
    protected int dancestor(int j) {
        while ((j % 2 == 1) == reverse.get(j / 2)) {
            j = j / 2;
        }
        return j / 2;
    }

    /**
     * Join two weak heaps into one.
     * 
     * @param i
     *            root of the first weak heap
     * @param j
     *            root of the second weak heap
     * @return true if already a weak heap, false if a flip was needed
     */
    @SuppressWarnings("unchecked")
    protected boolean join(int i, int j) {
        if (((Comparable<? super K>) array[j]).compareTo(array[i]) < 0) {
            K tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
            reverse.flip(j);
            return false;
        }
        return true;
    }

    /**
     * Join two weak heaps into one.
     * 
     * @param i
     *            root of the first weak heap
     * @param j
     *            root of the second weak heap
     * @return true if already a weak heap, false if a flip was needed
     */
    protected boolean joinWithComparator(int i, int j) {
        if (comparator.compare(array[j], array[i]) < 0) {
            K tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
            reverse.flip(j);
            return false;
        }
        return true;
    }

    @Override
    protected void fixup(int j) {
        int i;
        while (j > 0) {
            i = dancestor(j);
            if (join(i, j)) {
                break;
            }
            j = i;
        }
    }

    @Override
    protected void fixupWithComparator(int j) {
        int i;
        while (j > 0) {
            i = dancestor(j);
            if (joinWithComparator(i, j)) {
                break;
            }
            j = i;
        }
    }

    @Override
    protected void fixdown(int j) {
        int k = 2 * j + (reverse.get(j) ? 0 : 1);
        int c;
        while ((c = 2 * k + (reverse.get(k) ? 1 : 0)) < size) {
            k = c;
        }
        while (k != j) {
            join(j, k);
            k = k / 2;
        }
    }

    @Override
    protected void fixdownWithComparator(int j) {
        int k = 2 * j + (reverse.get(j) ? 0 : 1);
        int c;
        while ((c = 2 * k + (reverse.get(k) ? 1 : 0)) < size) {
            k = c;
        }
        while (k != j) {
            joinWithComparator(j, k);
            k = k / 2;
        }
    }

}
