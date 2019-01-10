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
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Base abstract implementation of an addressable radix heap.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 * 
 */
abstract class AbstractRadixAddressableHeap<K, V> implements AddressableHeap<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Denotes that a key does not belong to a bucket
     */
    protected static final int EMPTY = -1;

    /**
     * The buckets as lists.
     */
    protected Node[] buckets;

    /**
     * Number of elements
     */
    protected long size;

    /**
     * Last deleted key. This value is used to distribute elements in the
     * buckets. Should be initialized with the {@link #minKey} value.
     */
    protected K lastDeletedKey;

    /**
     * The current minimum value (cached)
     */
    protected Node currentMin;

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
    AbstractRadixAddressableHeap() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public Handle<K, V> findMin() {
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
     *             if the key is less than the last deleted key (or the minimum
     *             key allowed if no key has been deleted)
     */
    @Override
    @ConstantTime
    public Handle<K, V> insert(K key) {
        return insert(key, null);
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
     *             if the key is less than the last deleted key (or the minimum
     *             key allowed if no key has been deleted)
     */
    @Override
    @ConstantTime
    public Handle<K, V> insert(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys not permitted");
        }
        if (compare(key, maxKey) > 0) {
            throw new IllegalArgumentException("Key is more than the maximum allowed key");
        }
        if (compare(key, lastDeletedKey) < 0) {
            throw new IllegalArgumentException("Invalid key. Monotone heap.");
        }

        // add to bucket
        Node p = new Node(key, value);
        int b = computeBucket(key, lastDeletedKey);
        p.bucket = b;
        if (buckets[b] == null) {
            buckets[b] = p;
        } else {
            buckets[b].prev = p;
            p.next = buckets[b];
            buckets[b] = p;
        }

        // update current minimum cache
        if (currentMin == null || compare(key, currentMin.key) < 0) {
            currentMin = p;
        }

        size++;
        return p;
    }

    /**
     * {@inheritDoc}
     * 
     * The cost of this operation is amortized O(logC) assuming the heap
     * contains keys in the range [0, C] or equivalently [a, a+C].
     */
    @Override
    @LogarithmicTime(amortized = true)
    public Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        // updated last deleted key
        Node result = currentMin;
        lastDeletedKey = currentMin.key;

        if (currentMin.bucket == 0) {
            Node head = buckets[currentMin.bucket];
            if (currentMin.next != null) {
                currentMin.next.prev = currentMin.prev;
            }
            if (currentMin.prev != null) {
                currentMin.prev.next = currentMin.next;
            }
            if (head == currentMin) {
                currentMin.prev = null;
                buckets[currentMin.bucket] = currentMin.next;
            }
            currentMin.next = null;
            currentMin.prev = null;
            currentMin.bucket = EMPTY;

            // update minimum cache
            currentMin = buckets[0];
            if (--size > 0) {
                findAndCacheMinimum(0);
            }
        } else {
            // redistribute all elements based on new lastDeletedKey
            Node newMin = null;

            int currentMinBucket = currentMin.bucket;
            Node val = buckets[currentMinBucket];
            while (val != null) {
                // remove first from list
                buckets[currentMinBucket] = val.next;
                if (buckets[currentMinBucket] != null) {
                    buckets[currentMinBucket].prev = null;
                }
                val.next = null;
                val.prev = null;
                val.bucket = EMPTY;

                // redistribute
                if (val != currentMin) {
                    int b = computeBucket(val.key, lastDeletedKey);
                    assert b < currentMinBucket;
                    val.next = buckets[b];
                    if (buckets[b] != null) {
                        buckets[b].prev = val;
                    }
                    buckets[b] = val;
                    val.bucket = b;

                    if (newMin == null || compare(val.key, newMin.key) < 0) {
                        newMin = val;
                    }

                }
                val = buckets[currentMinBucket];
            }

            // update minimum cache
            currentMin = newMin;
            if (--size > 0) {
                findAndCacheMinimum(currentMinBucket + 1);
            }
        }

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
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }
        size = 0;
        lastDeletedKey = minKey;
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

    /**
     * List Node
     */
    protected class Node implements Handle<K, V>, Serializable {

        private static final long serialVersionUID = 1L;

        K key;
        V value;
        Node next;
        Node prev;
        int bucket;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
            this.prev = null;
            this.bucket = EMPTY;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public void decreaseKey(K newKey) {
            if (size == 0) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            if (bucket == EMPTY) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            if (compare(newKey, lastDeletedKey) < 0) {
                throw new IllegalArgumentException("Invalid key. Monotone heap.");
            }

            int c = compare(newKey, key);
            if (c > 0) {
                throw new IllegalArgumentException("Keys can only be decreased!");
            }

            key = newKey;
            if (c == 0) {
                return;
            }

            // update minimum cache
            if (this == currentMin || compare(key, currentMin.key) < 0) {
                currentMin = this;
            }

            // find new bucket
            int newBucket = computeBucket(key, lastDeletedKey);
            if (newBucket == bucket) {
                return;
            }

            // remove from list
            Node head = buckets[bucket];
            if (next != null) {
                next.prev = prev;
            }
            if (prev != null) {
                prev.next = next;
            }
            if (head == this) {
                prev = null;
                buckets[bucket] = next;
            }

            // add to new list
            if (buckets[newBucket] == null) {
                buckets[newBucket] = this;
                this.next = null;
            } else {
                buckets[newBucket].prev = this;
                this.next = buckets[newBucket];
                buckets[newBucket] = this;
            }
            this.prev = null;
            this.bucket = newBucket;
        }

        @Override
        public void delete() {
            if (size == 0 || bucket == EMPTY) {
                throw new IllegalArgumentException("Invalid handle!");
            }

            if (this == currentMin) {
                deleteMin();
                return;
            }

            // remove from list
            Node head = buckets[bucket];
            if (next != null) {
                next.prev = prev;
            }
            if (prev != null) {
                prev.next = next;
            }
            if (head == this) {
                buckets[bucket] = next;
            }
            prev = null;
            next = null;
            bucket = EMPTY;
            size--;
        }
    }

    /**
     * Helper method for finding and caching the minimum. Assumes that the heap
     * contains at least one element.
     * 
     * @param firstBucket
     *            start looking for elements from this bucket
     */
    private void findAndCacheMinimum(int firstBucket) {
        if (currentMin == null) {
            // find first non-empty bucket
            int currentMinBucket = EMPTY;
            for (int i = firstBucket; i < this.buckets.length; i++) {
                if (buckets[i] != null) {
                    currentMinBucket = i;
                    break;
                }
            }
            // find new minimum and cache it
            if (currentMinBucket >= 0) {
                Node val = buckets[currentMinBucket];
                while (val != null) {
                    if (currentMin == null || compare(val.key, currentMin.key) < 0) {
                        currentMin = val;
                    }
                    val = val.next;
                }
            }
        }
    }

}
