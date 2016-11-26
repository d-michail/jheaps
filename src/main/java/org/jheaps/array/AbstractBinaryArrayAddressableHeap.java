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

/**
 * Abstract implementation of a binary heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 */
abstract class AbstractBinaryArrayAddressableHeap<K, V> extends AbstractArrayAddressableHeap<K, V>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public AbstractBinaryArrayAddressableHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
    }

    protected void forceFixup(int k) {
        // assert k >= 1 && k <= size;

        ArrayHandle h = array[k];
        while (k > 1) {
            array[k] = array[k / 2];
            array[k].index = k;
            k /= 2;
        }
        array[k] = h;
        h.index = k;
    }

    @SuppressWarnings("unchecked")
    protected void fixup(int k) {
        // assert k >= 1 && k <= size;

        ArrayHandle h = array[k];
        while (k > 1 && ((Comparable<? super K>) array[k / 2].getKey()).compareTo(h.getKey()) > 0) {
            array[k] = array[k / 2];
            array[k].index = k;
            k /= 2;
        }
        array[k] = h;
        h.index = k;
    }

    protected void fixupWithComparator(int k) {
        // assert k >= 1 && k <= size;

        ArrayHandle h = array[k];
        while (k > 1 && comparator.compare(array[k / 2].getKey(), h.getKey()) > 0) {
            array[k] = array[k / 2];
            array[k].index = k;
            k /= 2;
        }
        array[k] = h;
        h.index = k;
    }

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
