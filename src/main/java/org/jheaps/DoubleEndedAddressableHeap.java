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
 * A double-ended heap whose elements can be addressed using handles.
 *
 * An insert operation returns a {@link AddressableHeap.Handle} which can later
 * be used in order to manipulate the element, such as decreasing its key,
 * increasing its key, or deleting it. Storing the handle externally is the
 * responsibility of the user.
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public interface DoubleEndedAddressableHeap<K, V> extends AddressableHeap<K, V> {

    /**
     * Insert a new element into the heap.
     *
     * @param key
     *            the element's key
     * @param value
     *            the element's value
     * 
     * @return a handle for the newly added element
     */
    Handle<K, V> insert(K key, V value);

    /**
     * Insert a new element into the heap with a null value.
     *
     * @param key
     *            the element's key
     * @return a handle for the newly added element
     */
    Handle<K, V> insert(K key);

    /**
     * Find an element with the minimum key.
     *
     * @return a handle to an element with minimum key
     */
    Handle<K, V> findMin();

    /**
     * Delete and return an element with the minimum key. If multiple such
     * elements exists, only one of them will be deleted. After the element is
     * deleted the handle is invalidated and only method {@link Handle#getKey()}
     * and {@link Handle#getValue()} can be used.
     * 
     * @return a handle to the deleted element with minimum key
     */
    Handle<K, V> deleteMin();

    /**
     * Find an element with the maximum key.
     *
     * @return an element with the maximum key
     * @throws NoSuchElementException
     *             if the heap is empty
     */
    Handle<K, V> findMax();

    /**
     * Delete and return an element with the maximum key. If multiple such
     * elements exists, only one of them will be deleted.
     *
     * @return the deleted element with the maximum key
     * @throws NoSuchElementException
     *             if the heap is empty
     */
    Handle<K, V> deleteMax();

    /**
     * A double-ended heap element handle. Allows someone to address an element
     * already in a heap and perform additional operations.
     *
     * @param <K>
     *            the type of keys maintained by this heap
     * @param <V>
     *            the type of values maintained by this heap
     */
    interface Handle<K, V> extends AddressableHeap.Handle<K, V> {

        /**
         * Increase the key of the element.
         *
         * @param newKey
         *            the new key
         * @throws IllegalArgumentException
         *             if the new key is smaller than the old key according to
         *             the comparator used when constructing the heap or the
         *             natural ordering of the elements if no comparator was
         *             used
         */
        void increaseKey(K newKey);

    }

}
