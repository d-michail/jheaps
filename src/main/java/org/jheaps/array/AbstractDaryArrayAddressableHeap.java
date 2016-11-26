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
 * Abstract implementation of a d-ary heap using an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 */
abstract class AbstractDaryArrayAddressableHeap<K, V> extends AbstractArrayAddressableHeap<K, V>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int d;

    public AbstractDaryArrayAddressableHeap(int d, Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
        if (d < 2) {
            throw new IllegalArgumentException("D-ary heaps must have at least 2 children per node");
        }
        this.d = d;
    }

    protected void forceFixup(int k) {
        // assert k >= 1 && k <= size;

        ArrayHandle h = array[k];
        while (k > 1) {
            int p = (k - 2) / d + 1;
            array[k] = array[p];
            array[k].index = k;
            k = p;
        }
        array[k] = h;
        h.index = k;
    }

    @SuppressWarnings("unchecked")
    protected void fixup(int k) {
        // assert k >= 1 && k <= size;

        ArrayHandle h = array[k];
        while (k > 1) {
            int p = (k - 2) / d + 1;
            if (((Comparable<? super K>) array[p].getKey()).compareTo(h.getKey()) <= 0) {
                break;
            }
            array[k] = array[p];
            array[k].index = k;
            k = p;
        }
        array[k] = h;
        h.index = k;
    }

    protected void fixupWithComparator(int k) {
        // assert k >= 1 && k <= size;

        ArrayHandle h = array[k];
        while (k > 1) {
            int p = (k - 2) / d + 1;
            if (comparator.compare(array[p].getKey(), h.getKey()) <= 0) {
                break;
            }
            array[k] = array[p];
            array[k].index = k;
            k = p;
        }
        array[k] = h;
        h.index = k;
    }

    @SuppressWarnings("unchecked")
    protected void fixdown(int k) {
        int c;
        ArrayHandle h = array[k];
        while ((c = d * (k - 1) + 2) <= size) {
            int maxc = c;
            for (int i = 1; i < d && c + i <= size; i++) {
                if (((Comparable<? super K>) array[maxc].getKey()).compareTo(array[c + i].getKey()) > 0) {
                    maxc = c + i;
                }
            }
            if (((Comparable<? super K>) h.getKey()).compareTo(array[maxc].getKey()) <= 0) {
                break;
            }
            array[k] = array[maxc];
            array[k].index = k;
            k = maxc;
        }
        array[k] = h;
        h.index = k;
    }

    protected void fixdownWithComparator(int k) {
        int c;
        ArrayHandle h = array[k];
        while ((c = d * (k - 1) + 2) <= size) {
            int maxc = c;
            for (int i = 1; i < d && c + i <= size; i++) {
                if (comparator.compare(array[maxc].getKey(), array[c + i].getKey()) > 0) {
                    maxc = c + i;
                }
            }
            if (comparator.compare(h.getKey(), array[maxc].getKey()) <= 0) {
                break;
            }
            array[k] = array[maxc];
            array[k].index = k;
            k = maxc;
        }
        array[k] = h;
        h.index = k;
    }
}
