/*
 * (C) Copyright 2014-2018, by Dimitrios Michail
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
package org.jheaps.monotone;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jheaps.Heap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Base abstract implementation of a radix heap.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the key type
 */
abstract class AbstractRadixHeap<K> implements Heap<K>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The buckets as lists. We use array-lists instead of linked-lists, to be
     * cache friendly.
     */
    protected List<K>[] buckets;

    /**
     * Number of elements
     */
    protected long size;

    /**
     * The current minimum value
     */
    protected K currentMin;

    /**
     * Minimum key allowed
     */
    protected K minKey;

    /**
     * Maximum key allowed
     */
    protected K maxKey;

    /**
     * Constructor
     */
    AbstractRadixHeap() {
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
        return currentMin;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if the key is null
     * @throws IllegalArgumentException
     *             if the key is less than the minimum allowed key
     * @throws IllegalArgumentException
     *             if the key is more than the maximum allowed key
     * @throws IllegalArgumentException
     *             if the key is less than the current minimum
     */
    @Override
    @ConstantTime
    public void insert(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys not permitted");
        }
        if (compare(key, minKey) < 0) {
            throw new IllegalArgumentException("Key is less than the minimum allowed key");
        }
        if (compare(key, maxKey) > 0) {
            throw new IllegalArgumentException("Key is more than the maximum allowed key");
        }

        if (size == 0) {
            currentMin = key;
        } else {
            if (compare(key, currentMin) < 0) {
                throw new IllegalArgumentException("Invalid key. Monotone heap.");
            }
            int b = computeBucket(key, currentMin);
            buckets[b].add(key);
        }
        size++;
    }

    /**
     * {@inheritDoc}
     * 
     * The cost of this operation is amortized O(logC) assuming the heap
     * contains keys in the range [0, C] or equivalently [a, a+C].
     */
    @Override
    @LogarithmicTime(amortized = true)
    public K deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        K result = currentMin;
        
        // special case
        if (size == 1) {
            currentMin = null;
            size--;
            return result;
        }
        
        // find first non-empty bucket
        int first = -1;
        for (int i = 0; i < this.buckets.length; i++) {
            if (buckets[i].size() > 0) {
                first = i;
                break;
            }
        }
        assert first >= 0;
        
        // new minimum is on the first bucket
        if (first == 0) {
            currentMin = buckets[first].remove(buckets[first].size()-1);
            size--;
            return result;
        }
        
        // find new minimum and its position (beware of cached values)
        currentMin = null;
        int minPos = -1;
        int pos = 0;
        for (K val : buckets[first]) {
            if (currentMin == null || compare(val, currentMin) < 0) { 
                currentMin = val;
                minPos = pos;
            }
            ++pos;
        }
        assert currentMin != null && minPos >= 0;
        
        // redistribute all elements
        pos = 0;
        for (K val : buckets[first]) {
            if (pos != minPos) { 
                int b = computeBucket(val, currentMin);
                assert b < first;
                buckets[b].add(val);     
            }
            ++pos;
        }
        buckets[first].clear();
        
        size--;
        return result;
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
    public void clear() {
        for (List<K> bucket : buckets) {
            bucket.clear();
        }
        size = 0;
        currentMin = null;
    }

    /**
     * Always returns {@code null} since this heap uses the
     * {@linkplain Comparable natural ordering} of its keys.
     * 
     * @return {@code null} since this heap uses the natural ordering of its
     *         keys
     */
    @Override
    public Comparator<? super K> comparator() {
        return null;
    }

    /**
     * Compares its two arguments for order. Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     *
     * @param o1
     *            the first object to be compared.
     * @param o2
     *            the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    protected abstract int compare(K o1, K o2);

    /**
     * Compute the bucket of a key based on a minimum key.
     * 
     * @param key
     *            the key
     * @param minKey
     *            the minimum key
     * @return the bucket where the key should go
     */
    protected int computeBucket(K key, K minKey) {
        return 1 + Math.min(msd(key, minKey), buckets.length - 2);
    }

    /**
     * Compute the most significant digit which is different in the binary
     * representation of two values, or -1 if numbers are equal.
     * 
     * @param a
     *            the first value
     * @param b
     *            the second value
     * @return the most significant digit which is different or -1 if numbers
     *         are equal
     */
    protected abstract int msd(K a, K b);

}
