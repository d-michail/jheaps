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

import java.util.NoSuchElementException;

/**
 * A double-ended heap with values.
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public interface DoubleEndedValueHeap<K, V> extends DoubleEndedHeap<K> {

    /**
     * Insert an element into the heap.
     *
     * @param key
     *            the key to insert
     * @param value
     *            the value to insert
     */
    void insert(K key, V value);

    /**
     * Find the value of an element with the minimum key.
     *
     * @return the value of an element with the minimum key
     * @throws NoSuchElementException
     *             if the heap is empty
     */
    V findMinValue();

    /**
     * Find the value of an element with the maximum key.
     *
     * @return the value of an element with the maximum key
     * @throws NoSuchElementException
     *             if the heap is empty
     */
    V findMaxValue();

}
