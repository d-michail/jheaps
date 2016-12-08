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

import java.util.BitSet;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * An abstract implementation of a binary weak heap.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
abstract class AbstractBinaryArrayWeakHeap<K> extends AbstractArrayWeakHeap<K> {

    private static final long serialVersionUID = 1L;

    /**
     * Reverse bits
     */
    protected BitSet reverse;

    /**
     * {@inheritDoc}
     */
    public AbstractBinaryArrayWeakHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
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
        return array[0];
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
        if (size == array.length) {
            if (size == 0) {
                ensureCapacity(1);
            } else {
                ensureCapacity(2 * array.length);
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
        if (size == 0) {
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

        if (DOWNSIZING_MIN_HEAP_CAPACITY < array.length && 4 * size < array.length) {
            ensureCapacity(array.length / 2);
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initCapacity(int capacity) {
        this.array = (K[]) new Object[capacity];
        this.reverse = new BitSet(capacity);
    }

    protected int dancestor(int j) {
        while ((j % 2 == 1) == reverse.get(j / 2)) {
            j = j / 2;
        }
        return j / 2;
    }

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
