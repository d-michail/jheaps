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

/**
 * An addressable heap factory.
 * 
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by the heap
 * @param <V>
 *            the type of values maintained by the heap
 */
public interface AddressableHeapFactory<K, V> {

    /**
     * Get a new heap.
     * 
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     * 
     * @return a new heap
     */
    AddressableHeap<K, V> get(Comparator<? super K> comparator);

}
