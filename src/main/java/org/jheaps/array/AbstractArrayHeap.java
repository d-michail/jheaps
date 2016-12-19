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

import java.util.Comparator;
import java.util.NoSuchElementException;

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
abstract class AbstractArrayHeap<K> extends AbstractArrayWeakHeap<K> {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new heap
     * 
     * @param comparator
     *            the comparator to use
     * @param capacity
     *            the initial capacity
     */
    public AbstractArrayHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
    }

    /**
     * Initialize the array representation
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void initCapacity(int capacity) {
        this.array = (K[]) new Object[capacity + 1];
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
        return array[1];
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
            if (size == array.length - 1) {
                if (array.length == 1) {
                    ensureCapacity(1);
                } else {
                    ensureCapacity(2 * (array.length - 1));
                }
            }
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
    public K deleteMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }

        K result = array[1];
        if (size == 1) {
            array[1] = null;
            size = 0;
        } else {
            array[1] = array[size];
            array[size] = null;
            size--;
            if (comparator == null) {
                fixdown(1);
            } else {
                fixdownWithComparator(1);
            }
        }

        if (Constants.NOT_BENCHMARK) {
            // free unused space
            int currentCapacity = array.length - 1;
            if (2 * minCapacity <= currentCapacity && 4 * size < currentCapacity) {
                ensureCapacity(currentCapacity / 2);
            }
        }

        return result;
    }

}
