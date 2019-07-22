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
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.annotations.LinearTime;

/**
 * An array based binary addressable heap. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * The implementation uses an array in order to store the elements and
 * automatically maintains the size of the array much like a
 * {@link java.util.Vector} does, providing amortized O(log(n)) time cost for
 * the {@code insert} and {@code deleteMin} operations. Operation
 * {@code findMin}, is a worst-case O(1) operation. Operations {@code delete}
 * and {@code decreaseKey} take worst-case O(log(n)) time. The bounds are
 * worst-case if the user initializes the heap with a capacity larger or equal
 * to the total number of elements that are going to be inserted into the heap.
 * 
 * <p>
 * Constructing such a heap from an array of elements can be performed using the
 * method {@link #heapify(Object[], Object[])} or
 * {@link #heapify(Object[], Object[], Comparator)} in linear time.
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
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public class BinaryArrayAddressableHeap<K, V> extends AbstractArrayAddressableHeap<K, V> implements Serializable {

    private final static long serialVersionUID = 1;

    /**
     * Default initial capacity of the binary heap.
     */
    public static final int DEFAULT_HEAP_CAPACITY = 16;

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
     * The initial capacity of the heap is {@link #DEFAULT_HEAP_CAPACITY} and
     * adjusts automatically based on the sequence of insertions and deletions.
     */
    public BinaryArrayAddressableHeap() {
        this(null, DEFAULT_HEAP_CAPACITY);
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
    public BinaryArrayAddressableHeap(int capacity) {
        this(null, capacity);
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
     * The initial capacity of the heap is {@link #DEFAULT_HEAP_CAPACITY} and
     * adjusts automatically based on the sequence of insertions and deletions.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     */
    public BinaryArrayAddressableHeap(Comparator<? super K> comparator) {
        this(comparator, DEFAULT_HEAP_CAPACITY);
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
    public BinaryArrayAddressableHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
    }

    /**
     * Create a heap from an array of elements. The elements of the array are
     * not destroyed. The method has linear time complexity.
     *
     * @param <K>
     *            the type of keys maintained by the heap
     * @param <V>
     *            the type of values maintained by the heap
     * @param keys
     *            an array of keys
     * @param values
     *            an array of values
     * @return a binary heap
     * @throws IllegalArgumentException
     *             in case the array is null
     */
    @LinearTime
    public static <K, V> BinaryArrayAddressableHeap<K, V> heapify(K[] keys, V[] values) {
        if (keys == null) {
            throw new IllegalArgumentException("Key array cannot be null");
        }
        if (values != null && keys.length != values.length) {
            throw new IllegalArgumentException("Values array must have the same length as the keys array");
        }
        if (keys.length == 0) {
            return new BinaryArrayAddressableHeap<K, V>();
        }

        BinaryArrayAddressableHeap<K, V> h = new BinaryArrayAddressableHeap<K, V>(keys.length);

        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            V value = (values == null) ? null : values[i];
            AbstractArrayAddressableHeap<K, V>.ArrayHandle ah = h.new ArrayHandle(key, value);
            ah.index = i + 1;
            h.array[i + 1] = ah;
        }
        h.size = keys.length;

        for (int i = keys.length / 2; i > 0; i--) {
            h.fixdown(i);
        }

        return h;
    }

    /**
     * Create a heap from an array of elements. The elements of the array are
     * not destroyed. The method has linear time complexity.
     *
     * @param <K>
     *            the type of keys maintained by the heap
     * @param <V>
     *            the type of values maintained by the heap
     * @param keys
     *            an array of keys
     * @param values
     *            an array of values
     * @param comparator
     *            the comparator to use
     * @return a binary heap
     * @throws IllegalArgumentException
     *             in case the array is null
     */
    @LinearTime
    public static <K, V> BinaryArrayAddressableHeap<K, V> heapify(K[] keys, V[] values,
            Comparator<? super K> comparator) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys array cannot be null");
        }
        if (values != null && keys.length != values.length) {
            throw new IllegalArgumentException("Values array must have the same length as the keys array");
        }
        if (keys.length == 0) {
            return new BinaryArrayAddressableHeap<K, V>(comparator);
        }

        BinaryArrayAddressableHeap<K, V> h = new BinaryArrayAddressableHeap<K, V>(comparator, keys.length);

        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            V value = (values == null) ? null : values[i];
            AbstractArrayAddressableHeap<K, V>.ArrayHandle ah = h.new ArrayHandle(key, value);
            ah.index = i + 1;
            h.array[i + 1] = ah;
        }
        h.size = keys.length;

        for (int i = keys.length / 2; i > 0; i--) {
            h.fixdownWithComparator(i);
        }

        return h;
    }

    /**
     * Get an iterator for all handles currently in the heap.
     * 
     * This method is especially useful when building a heap using the heapify
     * method. Unspecified behavior will occur if the heap is modified while
     * using this iterator.
     * 
     * @return an iterator which will return all handles of the heap
     */
    public Iterator<AddressableHeap.Handle<K, V>> handlesIterator() {
        return new Iterator<AddressableHeap.Handle<K, V>>() {
            private int pos = 1;

            @Override
            public boolean hasNext() {
                return pos <= size;
            }

            @Override
            public AddressableHeap.Handle<K, V> next() {
                if (pos > size) { 
                    throw new NoSuchElementException();
                }
                return array[pos++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
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
        ArrayHandle[] newArray = (ArrayHandle[]) Array.newInstance(ArrayHandle.class, capacity + 1);
        System.arraycopy(array, 1, newArray, 1, size);
        array = newArray;
    }

    @Override
    protected void forceFixup(int k) {
        ArrayHandle h = array[k];
        while (k > 1) {
            array[k] = array[k / 2];
            array[k].index = k;
            k /= 2;
        }
        array[k] = h;
        h.index = k;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void fixup(int k) {
        ArrayHandle h = array[k];
        while (k > 1 && ((Comparable<? super K>) array[k / 2].getKey()).compareTo(h.getKey()) > 0) {
            array[k] = array[k / 2];
            array[k].index = k;
            k /= 2;
        }
        array[k] = h;
        h.index = k;
    }

    @Override
    protected void fixupWithComparator(int k) {
        ArrayHandle h = array[k];
        while (k > 1 && comparator.compare(array[k / 2].getKey(), h.getKey()) > 0) {
            array[k] = array[k / 2];
            array[k].index = k;
            k /= 2;
        }
        array[k] = h;
        h.index = k;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void fixdown(int k) {
        ArrayHandle h = array[k];
        while (2 * k <= size) {
            int j = 2 * k;
            if (j < size && ((Comparable<? super K>) array[j].getKey()).compareTo(array[j + 1].getKey()) > 0) {
                j++;
            }
            if (((Comparable<? super K>) h.getKey()).compareTo(array[j].getKey()) <= 0) {
                break;
            }
            array[k] = array[j];
            array[k].index = k;
            k = j;
        }
        array[k] = h;
        h.index = k;
    }

    @Override
    protected void fixdownWithComparator(int k) {
        ArrayHandle h = array[k];
        while (2 * k <= size) {
            int j = 2 * k;
            if (j < size && comparator.compare(array[j].getKey(), array[j + 1].getKey()) > 0) {
                j++;
            }
            if (comparator.compare(h.getKey(), array[j].getKey()) <= 0) {
                break;
            }
            array[k] = array[j];
            array[k].index = k;
            k = j;
        }
        array[k] = h;
        h.index = k;
    }

}
