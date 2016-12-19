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
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.Constants;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Abstract implementation of a heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
abstract class AbstractArrayAddressableHeap<K, V> implements AddressableHeap<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Denotes that a handle is not in the array
     */
    protected static final int NO_INDEX = -1;

    /**
     * The maximum heap capacity.
     */
    protected static final int MAX_HEAP_CAPACITY = Integer.MAX_VALUE - 8 - 1;

    /**
     * The minimum heap capacity.
     */
    protected static final int MIN_HEAP_CAPACITY = 0;

    /**
     * Limit for the heap capacity when down-sizing.
     */
    protected static final int DOWNSIZING_MIN_HEAP_CAPACITY = 16;

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
    protected ArrayHandle[] array;

    /**
     * Number of elements in the heap.
     */
    protected int size;

    /**
     * Minimum capacity due to initially requested capacity.
     */
    protected final int minCapacity;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public AbstractArrayAddressableHeap(Comparator<? super K> comparator, int capacity) {
        checkCapacity(capacity);
        this.size = 0;
        this.comparator = comparator;
        this.minCapacity = Math.max(capacity, DOWNSIZING_MIN_HEAP_CAPACITY);
        this.array = (ArrayHandle[]) Array.newInstance(ArrayHandle.class, minCapacity + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public Handle<K, V> findMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
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
    public Handle<K, V> insert(K key) {
        return insert(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime(amortized = true)
    public Handle<K, V> insert(K key, V value) {
        if (Constants.NOT_BENCHMARK) {
            if (key == null) {
                throw new NullPointerException("Null keys not permitted");
            }
            // make sure there is space
            if (size == array.length - 1) {
                if (array.length == 1) {
                    ensureCapacity(1);
                } else {
                    ensureCapacity(2 * (array.length - 1));
                }
            }
        }

        ArrayHandle p = new ArrayHandle(key, value);
        size++;
        array[size] = p;
        p.index = size;

        if (comparator == null) {
            fixup(size);
        } else {
            fixupWithComparator(size);
        }

        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime(amortized = true)
    public Handle<K, V> deleteMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }

        ArrayHandle result = array[1];
        result.index = NO_INDEX;
        if (size == 1) {
            array[1] = null;
            size = 0;
        } else {
            array[1] = array[size--];
            if (comparator == null) {
                fixdown(1);
            } else {
                fixdownWithComparator(1);
            }
        }

        if (Constants.NOT_BENCHMARK) {
            if (2 * minCapacity < array.length - 1 && 4 * size < array.length - 1) {
                ensureCapacity((array.length - 1) / 2);
            }
        }
        return result;
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

    protected abstract void forceFixup(int k);

    protected abstract void fixup(int k);

    protected abstract void fixupWithComparator(int k);

    protected abstract void fixdown(int k);

    protected abstract void fixdownWithComparator(int k);

    // handle
    protected class ArrayHandle implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        K key;
        V value;
        int index;

        ArrayHandle(K key, V value) {
            this.key = key;
            this.value = value;
            this.index = NO_INDEX;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public void setValue(V value) {
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        @LogarithmicTime
        public void decreaseKey(K newKey) {
            if (index == NO_INDEX) {
                throw new IllegalArgumentException("Invalid handle!");
            }

            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) newKey).compareTo(key);
            } else {
                c = comparator.compare(newKey, key);
            }
            if (c > 0) {
                throw new IllegalArgumentException("Keys can only be decreased!");
            }

            key = newKey;
            if (c == 0 || index == 1) {
                return;
            }

            if (comparator == null) {
                fixup(index);
            } else {
                fixupWithComparator(index);
            }
        }

        @Override
        public void delete() {
            if (index == NO_INDEX) {
                throw new IllegalArgumentException("Invalid handle!");
            }

            if (index == 1) {
                deleteMin();
                return;
            }

            forceFixup(index);
            deleteMin();
        }

    }

}
