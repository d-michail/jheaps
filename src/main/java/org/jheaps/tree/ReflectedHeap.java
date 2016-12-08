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
package org.jheaps.tree;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeapFactory;
import org.jheaps.MergeableAddressableHeap;
import org.jheaps.MergeableDoubleEndedAddressableHeap;

/**
 * Reflected double ended heaps. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 * 
 * <p>
 * This class implements a general technique which uses two
 * {@link MergeableAddressableHeap}s to implement a double ended heap, described
 * in detail in the following
 * <a href="http://dx.doi.org/10.1016/S0020-0190(02)00501-X">paper</a>:
 * <ul>
 * <li>C. Makris, A. Tsakalidis, and K. Tsichlas. Reflected min-max heaps.
 * Information Processing Letters, 86(4), 209--214, 2003.</li>
 * </ul>
 * 
 * <p>
 * The running time bounds depend on the implementation of the underlying used
 * heap. All the above bounds, however, assume that the user does not perform
 * cascading melds on heaps such as:
 * 
 * <pre>
 * d.meld(e);
 * c.meld(d);
 * b.meld(c);
 * a.meld(b);
 * </pre>
 * 
 * The above scenario, although efficiently supported by using union-find with
 * path compression, invalidates the claimed bounds.
 *
 * <p>
 * Note that the ordering maintained by a this heap, like any heap, and whether
 * or not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but this heap performs all
 * key comparisons using its {@code compareTo} (or {@code compare}) method, so
 * two keys that are deemed equal by this method are, from the standpoint of
 * this heap, equal. The behavior of a heap <em>is</em> well-defined even if its
 * ordering is inconsistent with {@code equals}; it just fails to obey the
 * general contract of the {@code AddressableHeap} interface.
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a heap concurrently, and at least one of the threads
 * modifies the heap structurally, it <em>must</em> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or more
 * elements or changing the key of some element.) This is typically accomplished
 * by synchronizing on some object that naturally encapsulates the heap.
 * 
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public class ReflectedHeap<K, V> implements MergeableDoubleEndedAddressableHeap<K, V>, Serializable {

    private static final long serialVersionUID = -5428954082047233961L;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * A minimum heap
     */
    private final AddressableHeap<K, HandleMap<K, V>> minHeap;

    /**
     * A maximum heap
     */
    private final AddressableHeap<K, HandleMap<K, V>> maxHeap;

    /**
     * A free element in case the size is odd
     */
    private ReflectedHandle<K, V> free;

    /**
     * Size of the heap
     */
    private long size;

    /**
     * Used to reference the current heap or some other heap in case of melding,
     * so that handles remain valid even after a meld, without having to iterate
     * over them.
     * 
     * In order to avoid maintaining a full-fledged union-find data structure,
     * we disallow a heap to be used in melding more than once. We use however,
     * path-compression in case of cascading melds, that it, a handle moves from
     * one heap to another and then another.
     */
    private ReflectedHeap<K, V> other;

    /**
     * Constructs a new, empty heap, using the natural ordering of its keys. All
     * keys inserted into the heap must implement the {@link Comparable}
     * interface. Furthermore, all such keys must be <em>mutually
     * comparable</em>: {@code k1.compareTo(k2)} must not throw a
     * {@code ClassCastException} for any keys {@code k1} and {@code k2} in the
     * heap. If the user attempts to put a key into the heap that violates this
     * constraint (for example, the user attempts to put a string key into a
     * heap whose keys are integers), the {@code insert(Object key)} call will
     * throw a {@code ClassCastException}.
     * 
     * @param heapFactory
     *            a factory for the underlying heap implementation
     * @throws NullPointerException
     *             if the heap factory is null
     */
    public ReflectedHeap(AddressableHeapFactory<K, ?> heapFactory) {
        this(heapFactory, null);
    }

    /**
     * Constructs a new, empty heap, ordered according to the given comparator.
     * All keys inserted into the heap must be <em>mutually comparable</em> by
     * the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys {@code k1}
     * and {@code k2} in the heap. If the user attempts to put a key into the
     * heap that violates this constraint, the {@code insert(Object key)} call
     * will throw a {@code ClassCastException}.
     *
     * @param heapFactory
     *            a factory for the underlying heap implementation
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     * 
     * @throws NullPointerException
     *             if the heap factory is null
     */
    @SuppressWarnings("unchecked")
    public ReflectedHeap(AddressableHeapFactory<K, ?> heapFactory, Comparator<? super K> comparator) {
        if (heapFactory == null) {
            throw new NullPointerException("Underlying heap factory cannot be null");
        }
        this.comparator = comparator;
        this.minHeap = (AddressableHeap<K, HandleMap<K, V>>) heapFactory.get(comparator);
        this.maxHeap = (AddressableHeap<K, HandleMap<K, V>>) heapFactory.get(Collections.reverseOrder(comparator));
        this.free = null;
        this.size = 0;
        this.other = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        size = 0;
        free = null;
        minHeap.clear();
        maxHeap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Handle<K, V> insert(K key, V value) {
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        } else if (other != this) {
            throw new IllegalStateException("A heap cannot be used after a meld");
        } else if (size % 2 == 0) {
            free = new ReflectedHandle<K, V>(this, key, value);
            size++;
            return free;
        } else {
            ReflectedHandle<K, V> newHandle = new ReflectedHandle<K, V>(this, key, value);
            insertPair(newHandle, free);
            free = null;
            size++;
            return newHandle;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Handle<K, V> insert(K key) {
        return insert(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Handle<K, V> findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else if (size == 1) {
            return free;
        } else if (size % 2 == 0) {
            return minHeap.findMin().getValue().outer;
        } else {
            AddressableHeap.Handle<K, HandleMap<K, V>> minInnerHandle = minHeap.findMin();
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) minInnerHandle.getKey()).compareTo(free.key);
            } else {
                c = comparator.compare(minInnerHandle.getKey(), free.key);
            }
            if (c < 0) {
                return minInnerHandle.getValue().outer;
            } else {
                return free;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Handle<K, V> findMax() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else if (size == 1) {
            return free;
        } else if (size % 2 == 0) {
            return maxHeap.findMin().getValue().outer;
        } else {
            AddressableHeap.Handle<K, HandleMap<K, V>> maxInnerHandle = maxHeap.findMin();
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) maxInnerHandle.getKey()).compareTo(free.key);
            } else {
                c = comparator.compare(maxInnerHandle.getKey(), free.key);
            }
            if (c > 0) {
                return maxInnerHandle.getValue().outer;
            } else {
                return free;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else if (size == 1) {
            Handle<K, V> min = free;
            free = null;
            size--;
            return min;
        } else if (size % 2 == 0) {
            // find min
            AddressableHeap.Handle<K, HandleMap<K, V>> minInner = minHeap.deleteMin();
            ReflectedHandle<K, V> minOuter = minInner.getValue().outer;
            minOuter.inner = null;
            minOuter.minNotMax = false;

            // delete max and keep as free
            AddressableHeap.Handle<K, HandleMap<K, V>> maxInner = minInner.getValue().otherInner;
            ReflectedHandle<K, V> maxOuter = maxInner.getValue().outer;
            maxInner.delete();
            maxOuter.inner = null;
            maxOuter.minNotMax = false;

            free = maxOuter;
            size--;

            return minOuter;
        } else {
            // find min
            AddressableHeap.Handle<K, HandleMap<K, V>> minInner = minHeap.findMin();
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) minInner.getKey()).compareTo(free.key);
            } else {
                c = comparator.compare(minInner.getKey(), free.key);
            }
            if (c >= 0) {
                Handle<K, V> min = free;
                free = null;
                size--;
                return min;
            }

            // minInner is smaller
            minInner.delete();
            ReflectedHandle<K, V> minOuter = minInner.getValue().outer;
            minOuter.inner = null;
            minOuter.minNotMax = false;

            // delete max
            AddressableHeap.Handle<K, HandleMap<K, V>> maxInner = minInner.getValue().otherInner;
            ReflectedHandle<K, V> maxOuter = maxInner.getValue().outer;
            maxInner.delete();
            maxOuter.inner = null;
            maxOuter.minNotMax = false;

            // reinsert max with free
            insertPair(maxOuter, free);
            free = null;
            size--;

            return minOuter;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Handle<K, V> deleteMax() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else if (size == 1) {
            Handle<K, V> max = free;
            free = null;
            size--;
            return max;
        } else if (size % 2 == 0) {
            // find max
            AddressableHeap.Handle<K, HandleMap<K, V>> maxInner = maxHeap.deleteMin();
            ReflectedHandle<K, V> maxOuter = maxInner.getValue().outer;
            maxOuter.inner = null;
            maxOuter.minNotMax = false;

            // delete min and keep as free
            AddressableHeap.Handle<K, HandleMap<K, V>> minInner = maxInner.getValue().otherInner;
            ReflectedHandle<K, V> minOuter = minInner.getValue().outer;
            minInner.delete();
            minOuter.inner = null;
            minOuter.minNotMax = false;

            free = minOuter;
            size--;

            return maxOuter;
        } else {
            // find max
            AddressableHeap.Handle<K, HandleMap<K, V>> maxInner = maxHeap.findMin();
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) maxInner.getKey()).compareTo(free.key);
            } else {
                c = comparator.compare(maxInner.getKey(), free.key);
            }
            if (c < 0) {
                Handle<K, V> max = free;
                free = null;
                size--;
                return max;
            }

            // maxInner is larger
            maxInner.delete();
            ReflectedHandle<K, V> maxOuter = maxInner.getValue().outer;
            maxOuter.inner = null;
            maxOuter.minNotMax = false;

            // delete min
            AddressableHeap.Handle<K, HandleMap<K, V>> minInner = maxInner.getValue().otherInner;
            ReflectedHandle<K, V> minOuter = minInner.getValue().outer;
            minInner.delete();
            minOuter.inner = null;
            minOuter.minNotMax = false;

            // reinsert min with free
            insertPair(minOuter, free);
            free = null;
            size--;

            return maxOuter;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void meld(MergeableDoubleEndedAddressableHeap<K, V> other) {
        ReflectedHeap<K, V> h = (ReflectedHeap<K, V>) other;

        // check same comparator
        if (comparator != null) {
            if (h.comparator == null || !h.comparator.equals(comparator)) {
                throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
            }
        } else if (h.comparator != null) {
            throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
        }

        if (h.other != h) {
            throw new IllegalStateException("A heap cannot be used after a meld.");
        }

        if (!(minHeap instanceof MergeableAddressableHeap)) {
            throw new IllegalArgumentException("Underlying heaps are not meldable.");
        }

        // meld min heaps
        MergeableAddressableHeap<K, V> minAsMergeableHeap = (MergeableAddressableHeap<K, V>) minHeap;
        MergeableAddressableHeap<K, V> hMinAsMergeableHeap = (MergeableAddressableHeap<K, V>) h.minHeap;
        minAsMergeableHeap.meld(hMinAsMergeableHeap);

        // meld max heaps
        MergeableAddressableHeap<K, V> maxAsMergeableHeap = (MergeableAddressableHeap<K, V>) maxHeap;
        MergeableAddressableHeap<K, V> hMaxAsMergeableHeap = (MergeableAddressableHeap<K, V>) h.maxHeap;
        maxAsMergeableHeap.meld(hMaxAsMergeableHeap);

        // meld free
        if (free == null) {
            if (h.free != null) {
                free = h.free;
                h.free = null;
            }
        } else {
            if (h.free != null) {
                insertPair(free, h.free);
                h.free = null;
                free = null;
            }
        }

        // set new sizes
        size += h.size;
        h.size = 0;

        // take ownership
        h.other = this;
    }

    /**
     * Insert a pair of elements, one in the min heap and one in the max heap.
     * 
     * @param handle1
     *            a handle to the first element
     * @param handle2
     *            a handle to the second element
     */
    @SuppressWarnings("unchecked")
    private void insertPair(ReflectedHandle<K, V> handle1, ReflectedHandle<K, V> handle2) {
        int c;
        if (comparator == null) {
            c = ((Comparable<? super K>) handle1.key).compareTo(handle2.key);
        } else {
            c = comparator.compare(handle1.key, handle2.key);
        }

        AddressableHeap.Handle<K, HandleMap<K, V>> innerHandle1;
        AddressableHeap.Handle<K, HandleMap<K, V>> innerHandle2;

        if (c <= 0) {
            innerHandle1 = minHeap.insert(handle1.key);
            handle1.minNotMax = true;
            innerHandle2 = maxHeap.insert(handle2.key);
            handle2.minNotMax = false;
        } else {
            innerHandle1 = maxHeap.insert(handle1.key);
            handle1.minNotMax = false;
            innerHandle2 = minHeap.insert(handle2.key);
            handle2.minNotMax = true;
        }
        handle1.inner = innerHandle1;
        handle2.inner = innerHandle2;

        innerHandle1.setValue(new HandleMap<K, V>(handle1, innerHandle2));
        innerHandle2.setValue(new HandleMap<K, V>(handle2, innerHandle1));
    }

    /**
     * Delete an element
     * 
     * @param n
     *            a handle to the element
     */
    private void delete(ReflectedHandle<K, V> n) {
        if (n.inner == null && free != n) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        if (free == n) {
            free = null;
        } else {
            // delete from inner queue
            AddressableHeap.Handle<K, HandleMap<K, V>> nInner = n.inner;
            ReflectedHandle<K, V> nOuter = nInner.getValue().outer;
            nInner.delete();
            nOuter.inner = null;
            nOuter.minNotMax = false;

            // delete pair from inner queue
            AddressableHeap.Handle<K, HandleMap<K, V>> otherInner = nInner.getValue().otherInner;
            ReflectedHandle<K, V> otherOuter = otherInner.getValue().outer;
            otherInner.delete();
            otherOuter.inner = null;
            otherOuter.minNotMax = false;

            // reinsert either as free or as pair with free
            if (free == null) {
                free = otherOuter;
            } else {
                insertPair(otherOuter, free);
                free = null;
            }
        }
        size--;
    }

    /**
     * Decrease the key of an element.
     * 
     * @param n
     *            the element
     * @param newKey
     *            the new key
     */
    @SuppressWarnings("unchecked")
    private void decreaseKey(ReflectedHandle<K, V> n, K newKey) {
        if (n.inner == null && free != n) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        int c;
        if (comparator == null) {
            c = ((Comparable<? super K>) newKey).compareTo(n.key);
        } else {
            c = comparator.compare(newKey, n.key);
        }
        if (c > 0) {
            throw new IllegalArgumentException("Keys can only be decreased!");
        }
        n.key = newKey;
        if (c == 0 || free == n) {
            return;
        }

        // actual decrease
        AddressableHeap.Handle<K, HandleMap<K, V>> nInner = n.inner;
        if (n.minNotMax) {
            // we are in the min heap, easy case
            n.inner.decreaseKey(newKey);
        } else {
            // we are in the max heap, remove
            nInner.delete();
            ReflectedHandle<K, V> nOuter = nInner.getValue().outer;
            nOuter.inner = null;
            nOuter.minNotMax = false;

            // remove min
            AddressableHeap.Handle<K, HandleMap<K, V>> minInner = nInner.getValue().otherInner;
            ReflectedHandle<K, V> minOuter = minInner.getValue().outer;
            minInner.delete();
            minOuter.inner = null;
            minOuter.minNotMax = false;

            // update key
            nOuter.key = newKey;

            // reinsert both
            insertPair(nOuter, minOuter);
        }
    }

    /**
     * Increase the key of an element.
     * 
     * @param n
     *            the element
     * @param newKey
     *            the new key
     */
    @SuppressWarnings("unchecked")
    private void increaseKey(ReflectedHandle<K, V> n, K newKey) {
        if (n.inner == null && free != n) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        int c;
        if (comparator == null) {
            c = ((Comparable<? super K>) newKey).compareTo(n.key);
        } else {
            c = comparator.compare(newKey, n.key);
        }
        if (c < 0) {
            throw new IllegalArgumentException("Keys can only be increased!");
        }
        n.key = newKey;
        if (c == 0 || free == n) {
            return;
        }

        // actual increase
        AddressableHeap.Handle<K, HandleMap<K, V>> nInner = n.inner;
        if (!n.minNotMax) {
            // we are in the max heap, easy case
            n.inner.decreaseKey(newKey);
        } else {
            // we are in the min heap, remove
            nInner.delete();
            ReflectedHandle<K, V> nOuter = nInner.getValue().outer;
            nOuter.inner = null;
            nOuter.minNotMax = false;

            // remove max
            AddressableHeap.Handle<K, HandleMap<K, V>> maxInner = nInner.getValue().otherInner;
            ReflectedHandle<K, V> maxOuter = maxInner.getValue().outer;
            maxInner.delete();
            maxOuter.inner = null;
            maxOuter.minNotMax = false;

            // update key
            nOuter.key = newKey;

            // reinsert both
            insertPair(nOuter, maxOuter);
        }
    }

    // ~-------------------------------------------------------------------

    /*
     * This is the outer handle which we provide to the users.
     */
    private static class ReflectedHandle<K, V> implements Handle<K, V>, Serializable {

        private static final long serialVersionUID = 3179286196684064903L;

        /*
         * We maintain explicitly the belonging heap, instead of using an inner
         * class due to possible cascading melding.
         */
        ReflectedHeap<K, V> heap;

        K key;
        V value;

        /*
         * Whether the key is inside the minimum or the maximum heap (if not the
         * free element).
         */
        boolean minNotMax;
        /*
         * Handle inside one of the inner heaps, or null if free element.
         */
        AddressableHeap.Handle<K, HandleMap<K, V>> inner;

        public ReflectedHandle(ReflectedHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K getKey() {
            return key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(V value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void decreaseKey(K newKey) {
            getOwner().decreaseKey(this, newKey);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete() {
            getOwner().delete(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void increaseKey(K newKey) {
            getOwner().increaseKey(this, newKey);
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        ReflectedHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                ReflectedHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                ReflectedHeap<K, V> cur = heap;
                while (cur.other != root) {
                    ReflectedHeap<K, V> next = cur.other;
                    cur.other = root;
                    cur = next;
                }
                heap = root;
            }
            return heap;
        }

    }

    /*
     * Value kept in the inner heaps, in order to map (a) to the outer heap and
     * (b) to the pair inside the other inner heap.
     */
    private static class HandleMap<K, V> implements Serializable {

        private static final long serialVersionUID = 1L;

        ReflectedHandle<K, V> outer;
        AddressableHeap.Handle<K, HandleMap<K, V>> otherInner;

        public HandleMap(ReflectedHandle<K, V> outer, AddressableHeap.Handle<K, HandleMap<K, V>> otherInner) {
            this.outer = outer;
            this.otherInner = otherInner;
        }
    }

}
