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
     * Denotes that a list node does not belong to a bucket list.
     */
    private static final int NO_BUCKET = -1;

    /**
     * The buckets as lists.
     */
    protected Node[] buckets;

    /**
     * Number of elements
     */
    protected long size;

    /**
     * The current minimum value
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
     *             if the key is less than the current minimum
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
     *             if the key is less than the current minimum
     */
    @Override
    @ConstantTime
    public Handle<K, V> insert(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys not permitted");
        }
        if (compare(key, minKey) < 0) {
            throw new IllegalArgumentException("Key is less than the minimum allowed key");
        }
        if (compare(key, maxKey) > 0) {
            throw new IllegalArgumentException("Key is more than the maximum allowed key");
        }

        Node p = new Node(key, value);
        if (size == 0) {
            currentMin = p;
        } else {
            if (compare(key, currentMin.getKey()) < 0) {
                throw new IllegalArgumentException("Invalid key. Monotone heap.");
            }
            int b = computeBucket(key, currentMin.getKey());
            p.bucket = b;
            if (buckets[b] == null) {
                buckets[b] = p;
            } else {
                buckets[b].prev = p;
                p.next = buckets[b];
                buckets[b] = p;
            }
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
        
        Node result = currentMin;
        
        // special case
        if (size == 1) {
            currentMin = null;
            size--;
            return result;
        }
        
        // find first non-empty bucket
        int first = -1;
        for (int i = 0; i < this.buckets.length; i++) {
            if (buckets[i] != null) {
                first = i;
                break;
            }
        }
        assert first >= 0;
        
        // new minimum was on the first bucket
        if (first == 0) {
            currentMin = buckets[first];
            buckets[first] = currentMin.next;
            if (buckets[first] != null) { 
                buckets[first].prev = null;
            }
            currentMin.next = null;
            currentMin.prev = null;
            currentMin.bucket = NO_BUCKET;
            size--;
            return result;
        }

        // find new minimum
        currentMin = null;
        Node val = buckets[first];
        while(val != null) { 
            if (currentMin == null || compare(val.key, currentMin.key) < 0) { 
                currentMin = val;
            }
            val = val.next;
        }
        
        // redistribute all elements
        val = buckets[first];
        while(val != null) {
            buckets[first] = val.next;
            if (buckets[first] != null) { 
                buckets[first].prev = null;
            }
            val.next = null;
            val.prev = null;
            val.bucket = NO_BUCKET;
            
            if (val != currentMin) { 
                int b = computeBucket(val.key, currentMin.key);
                val.next = buckets[b];
                if (buckets[b] != null) { 
                    buckets[b].prev = val;
                }
                buckets[b] = val;
                val.bucket = b;
            }
            val = buckets[first];
        }
        
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
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
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
            this.bucket = NO_BUCKET;
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
            if (compare(newKey, currentMin.getKey()) < 0) {
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
            if (bucket == NO_BUCKET) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            
            // find new bucket
            int newBucket = computeBucket(key, currentMin.getKey());
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
            if (this == currentMin) {
                deleteMin();
                return;
            }
            
            if (size == 0 || bucket == NO_BUCKET) {
                throw new IllegalArgumentException("Invalid handle!");
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
            bucket = NO_BUCKET;
            size--;
        }
    }
}
