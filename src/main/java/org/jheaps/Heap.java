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

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A heap.
 *
 * @param <K>
 *            the type of keys maintained by this heap
 *
 * @author Dimitrios Michail
 */
public interface Heap<K> {

    /**
     * Returns the comparator used to order the keys in this heap, or
     * {@code null} if this heap uses the {@linkplain Comparable natural
     * ordering} of its keys.
     *
     * @return the comparator used to order the keys in this heap, or
     *         {@code null} if this heap uses the natural ordering of its keys
     */
    Comparator<? super K> comparator();

    /**
     * Insert a key into the heap.
     *
     * @param key
     *            the key to insert
     */
    void insert(K key);

    /**
     * Find an element with the minimum key.
     *
     * @return an element with the minimum key
     * @throws NoSuchElementException
     *             if the heap is empty
     */
    K findMin();

    /**
     * Delete and return an element with the minimum key. If multiple such
     * elements exists, only one of them will be deleted.
     *
     * @return the deleted element with the minimum key
     * @throws NoSuchElementException
     *             if the heap is empty
     */
    K deleteMin();

    /**
     * Returns {@code true} if this heap is empty.
     *
     * @return {@code true} if this heap is empty, {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Returns the number of elements in this heap.
     *
     * @return the number of elements in this heap
     */
    long size();

    /**
     * Clear all the elements of this heap.
     */
    void clear();

}
