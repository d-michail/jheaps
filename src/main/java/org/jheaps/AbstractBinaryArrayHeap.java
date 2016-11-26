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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Abstract implementation of a binary heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
abstract class AbstractBinaryArrayHeap<K> extends AbstractArrayHeap<K> implements Serializable {

    private static final long serialVersionUID = 1L;

    public AbstractBinaryArrayHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
    }

    @SuppressWarnings("unchecked")
    protected void fixup(int k) {
        // assert k >= 1 && k <= size;

        K key = array[k];
        while (k > 1 && ((Comparable<? super K>) array[k / 2]).compareTo(key) > 0) {
            array[k] = array[k / 2];
            k /= 2;
        }
        array[k] = key;
    }

    protected void fixupWithComparator(int k) {
        // assert k >= 1 && k <= size;

        K key = array[k];
        while (k > 1 && comparator.compare(array[k / 2], key) > 0) {
            array[k] = array[k / 2];
            k /= 2;
        }
        array[k] = key;
    }

    @SuppressWarnings("unchecked")
    protected void fixdown(int k) {
        K key = array[k];
        while (2 * k <= size) {
            int j = 2 * k;
            if (j < size && ((Comparable<? super K>) array[j]).compareTo(array[j + 1]) > 0) {
                j++;
            }
            if (((Comparable<? super K>) key).compareTo(array[j]) <= 0) {
                break;
            }
            array[k] = array[j];
            k = j;
        }
        array[k] = key;
    }

    protected void fixdownWithComparator(int k) {
        K key = array[k];
        while (2 * k <= size) {
            int j = 2 * k;
            if (j < size && comparator.compare(array[j], array[j + 1]) > 0) {
                j++;
            }
            if (comparator.compare(key, array[j]) <= 0) {
                break;
            }
            array[k] = array[j];
            k = j;
        }
        array[k] = key;
    }

}
