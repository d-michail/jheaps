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
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LinearTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * An array based binary weak heap using bulk insertion. The heap is sorted
 * according to the {@linkplain Comparable natural ordering} of its keys, or by
 * a {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * The implementation uses an array in order to store the elements and
 * automatically maintains the size of the array much like a
 * {@link java.util.Vector} does, providing amortized O(1) time cost for the
 * {@code insert} and amortized O(log(n)) for the {@code deleteMin} operation.
 * Operation {@code findMin}, is a worst-case O(1) operation.
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
public class BinaryArrayBulkInsertWeakHeap<K> extends BinaryArrayWeakHeap<K> implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * Insertion buffer capacity for integer size since we are using Java arrays
     * to store elements.
     */
    protected static final int INSERTION_BUFFER_CAPACITY = 32 + 2;

    /**
     * The insertion buffer
     */
    protected K[] insertionBuffer;

    /**
     * Number of elements in the insertion buffer
     */
    protected int insertionBufferSize;

    /**
     * Position of minimum in the insertion buffer
     */
    protected int insertionBufferMinPos;

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
     * {@link BinaryArrayBulkInsertWeakHeap#DEFAULT_HEAP_CAPACITY} and adjusts
     * automatically based on the sequence of insertions and deletions.
     */
    public BinaryArrayBulkInsertWeakHeap() {
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
     * automatically based on the sequence of insertions and deletions.
     *
     * @param capacity
     *            the initial heap capacity
     */
    public BinaryArrayBulkInsertWeakHeap(int capacity) {
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
     * The initial capacity of the heap is
     * {@link BinaryArrayBulkInsertWeakHeap#DEFAULT_HEAP_CAPACITY} and adjusts
     * automatically based on the sequence of insertions and deletions.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     */
    public BinaryArrayBulkInsertWeakHeap(Comparator<? super K> comparator) {
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
     * automatically based on the sequence of insertions and deletions.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     * @param capacity
     *            the initial heap capacity
     */
    @SuppressWarnings("unchecked")
    public BinaryArrayBulkInsertWeakHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
        this.insertionBuffer = (K[]) new Object[INSERTION_BUFFER_CAPACITY];
        this.insertionBufferSize = 0;
        this.insertionBufferMinPos = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public boolean isEmpty() {
        return size + insertionBufferSize == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public long size() {
        return (long)size + insertionBufferSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public void clear() {
        size = 0;
        insertionBufferSize = 0;
        insertionBufferMinPos = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    @SuppressWarnings("unchecked")
    public K findMin() {
        if (size + insertionBufferSize == 0) {
            throw new NoSuchElementException();
        }

        if (insertionBufferSize == 0) {
            return array[0];
        } else if (size == 0) {
            return insertionBuffer[insertionBufferMinPos];
        } else {
            K insertionBufferMin = insertionBuffer[insertionBufferMinPos];
            if (comparator == null) {
                if (((Comparable<? super K>) array[0]).compareTo(insertionBufferMin) <= 0) {
                    return array[0];
                } else {
                    return insertionBufferMin;
                }
            } else {
                if (comparator.compare(array[0], insertionBufferMin) <= 0) {
                    return array[0];
                } else {
                    return insertionBufferMin;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @ConstantTime(amortized = true)
    public void insert(K key) {
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }

        // add in buffer
        insertionBuffer[insertionBufferSize++] = key;

        if (isBulkInsertionBufferFull()) {
            if (size + insertionBufferSize > array.length) {
                // first try to double size
                if (array.length == 0) {
                    ensureCapacity(1);
                } else {
                    ensureCapacity(2 * array.length);
                }
                // if not enough, set to requested size
                ensureCapacity(size + insertionBufferSize);
            }
            if (comparator == null) {
                bulkInsert();
            } else {
                bulkInsertWithComparator();
            }
        } else if (insertionBufferSize > 1) {
            // update minimum
            K insertionBufferMin = insertionBuffer[insertionBufferMinPos];
            if (comparator == null) {
                if (((Comparable<? super K>) key).compareTo(insertionBufferMin) < 0) {
                    insertionBufferMinPos = insertionBufferSize - 1;
                }
            } else {
                if (comparator.compare(key, insertionBufferMin) < 0) {
                    insertionBufferMinPos = insertionBufferSize - 1;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @LogarithmicTime(amortized = true)
    public K deleteMin() {
        if (size + insertionBufferSize == 0) {
            throw new NoSuchElementException();
        }

        // where is the minimum
        boolean deleteFromInsertionBuffer = false;
        if (size == 0) {
            deleteFromInsertionBuffer = true;
        } else if (insertionBufferSize > 0) {
            K arrayMin = array[0];
            K insertionBufferMin = insertionBuffer[insertionBufferMinPos];
            if (comparator == null) {
                if (((Comparable<? super K>) insertionBufferMin).compareTo(arrayMin) < 0) {
                    deleteFromInsertionBuffer = true;
                }
            } else {
                if (comparator.compare(insertionBufferMin, arrayMin) < 0) {
                    deleteFromInsertionBuffer = true;
                }
            }
        }

        K result;
        if (deleteFromInsertionBuffer) {
            result = insertionBuffer[insertionBufferMinPos];
            insertionBuffer[insertionBufferMinPos] = insertionBuffer[insertionBufferSize - 1];
            insertionBuffer[insertionBufferSize - 1] = null;
            insertionBufferSize--;
            insertionBufferMinPos = 0;
            if (comparator == null) {
                for (int i = 1; i < insertionBufferSize; i++) {
                    if (((Comparable<? super K>) insertionBuffer[i])
                            .compareTo(insertionBuffer[insertionBufferMinPos]) < 0) {
                        insertionBufferMinPos = i;
                    }
                }
            } else {
                for (int i = 1; i < insertionBufferSize; i++) {
                    if (comparator.compare(insertionBuffer[i], insertionBuffer[insertionBufferMinPos]) < 0) {
                        insertionBufferMinPos = i;
                    }
                }
            }
        } else {
            result = array[0];
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
            if (minCapacity <= array.length && 4 * size < array.length) {
                ensureCapacity(array.length / 2);
            }
        }

        return result;
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
    public static <K> BinaryArrayBulkInsertWeakHeap<K> heapify(K[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            return new BinaryArrayBulkInsertWeakHeap<K>();
        }

        BinaryArrayBulkInsertWeakHeap<K> h = new BinaryArrayBulkInsertWeakHeap<K>(array.length);

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
    public static <K> BinaryArrayBulkInsertWeakHeap<K> heapify(K[] array, Comparator<? super K> comparator) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            return new BinaryArrayBulkInsertWeakHeap<K>(comparator);
        }

        BinaryArrayBulkInsertWeakHeap<K> h = new BinaryArrayBulkInsertWeakHeap<K>(comparator, array.length);

        System.arraycopy(array, 0, h.array, 0, array.length);
        h.size = array.length;

        for (int j = h.size - 1; j > 0; j--) {
            h.joinWithComparator(h.dancestor(j), j);
        }

        return h;
    }

    /**
     * Check if the bulk insertion buffer is full.
     * 
     * @return true if the bulk insertion buffer is full, false otherwise
     */
    protected boolean isBulkInsertionBufferFull() {
        if (insertionBufferSize >= insertionBuffer.length) {
            return true;
        }
        double sizeAsDouble = (double)size + insertionBufferSize;
        return Math.getExponent(sizeAsDouble) + 3 >= insertionBuffer.length;
    }

    /**
     * Bulk insert from insertion buffer into the weak heap.
     */
    protected void bulkInsert() {
        if (insertionBufferSize == 0) {
            return;
        }
        int right = size + insertionBufferSize - 2;
        int left = Math.max(size, right / 2);
        while (insertionBufferSize > 0) {
            --insertionBufferSize;
            array[size] = insertionBuffer[insertionBufferSize];
            insertionBuffer[insertionBufferSize] = null;
            reverse.clear(size);
            ++size;
        }
        while (right > left + 1) {
            left = left / 2;
            right = right / 2;
            for (int j = left; j <= right; j++) {
                fixdown(j);
            }
        }
        if (left != 0) {
            int i = dancestor(left);
            fixdown(i);
            fixup(i);
        }
        if (right != 0) {
            int i = dancestor(right);
            fixdown(i);
            fixup(i);
        }
        insertionBufferMinPos = 0;
    }

    /**
     * Bulk insert from insertion buffer into the weak heap.
     */
    protected void bulkInsertWithComparator() {
        if (insertionBufferSize == 0) {
            return;
        }
        int right = size + insertionBufferSize - 2;
        int left = Math.max(size, right / 2);
        while (insertionBufferSize > 0) {
            --insertionBufferSize;
            array[size] = insertionBuffer[insertionBufferSize];
            insertionBuffer[insertionBufferSize] = null;
            reverse.clear(size);
            ++size;
        }
        while (right > left + 1) {
            left = left / 2;
            right = right / 2;
            for (int j = left; j <= right; j++) {
                fixdownWithComparator(j);
            }
        }
        if (left != 0) {
            int i = dancestor(left);
            fixdownWithComparator(i);
            fixupWithComparator(i);
        }
        if (right != 0) {
            int i = dancestor(right);
            fixdownWithComparator(i);
            fixupWithComparator(i);
        }
        insertionBufferMinPos = 0;
    }

}
