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
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableAddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Pairing heaps. The heap is sorted according to the {@linkplain Comparable
 * natural ordering} of its keys, or by a {@link Comparator} provided at heap
 * creation time, depending on which constructor is used.
 *
 * <p>
 * This implementation provides amortized O(log(n)) time cost for the
 * {@code insert}, {@code deleteMin}, and {@code decreaseKey} operations.
 * Operation {@code findMin}, is a worst-case O(1) operation. The algorithms are
 * based on the <a href="http://dx.doi.org/10.1007/BF01840439">pairing heap
 * paper</a>. Pairing heaps are very efficient in practice, especially in
 * applications requiring the {@code decreaseKey} operation. The operation
 * {@code meld} is amortized O(log(n)).
 * 
 * <p>
 * All the above bounds, however, assume that the user does not perform
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
 * Note that the ordering maintained by a pairing heap, like any heap, and
 * whether or not an explicit comparator is provided, must be <em>consistent
 * with {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but a pairing heap performs
 * all key comparisons using its {@code compareTo} (or {@code compare}) method,
 * so two keys that are deemed equal by this method are, from the standpoint of
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
 *
 * @see RankPairingHeap
 * @see CostlessMeldPairingHeap
 * @see FibonacciHeap
 */
public class PairingHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * The root of the pairing heap
     */
    private Node<K, V> root;

    /**
     * Size of the pairing heap
     */
    private long size;

    /**
     * Used to reference the current heap or some other pairing heap in case of
     * melding, so that handles remain valid even after a meld, without having
     * to iterate over them.
     * 
     * In order to avoid maintaining a full-fledged union-find data structure,
     * we disallow a heap to be used in melding more than once. We use however,
     * path-compression in case of cascading melds, that it, a handle moves from
     * one heap to another and then another.
     */
    private PairingHeap<K, V> other;

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
     */
    @ConstantTime
    public PairingHeap() {
        this(null);
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
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     */
    @ConstantTime
    public PairingHeap(Comparator<? super K> comparator) {
        this.root = null;
        this.comparator = comparator;
        this.size = 0;
        this.other = this;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalStateException
     *             if the heap has already been used in the right hand side of a
     *             meld
     */
    @Override
    @LogarithmicTime(amortized = true)
    public AddressableHeap.Handle<K, V> insert(K key, V value) {
        if (other != this) {
            throw new IllegalStateException("A heap cannot be used after a meld");
        }
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }
        Node<K, V> n = new Node<K, V>(this, key, value);
        if (comparator == null) {
            root = link(root, n);
        } else {
            root = linkWithComparator(root, n);
        }
        size++;
        return n;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalStateException
     *             if the heap has already been used in the right hand side of a
     *             meld
     */
    @Override
    @LogarithmicTime(amortized = true)
    public AddressableHeap.Handle<K, V> insert(K key) {
        return insert(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime(amortized = false)
    public AddressableHeap.Handle<K, V> findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime(amortized = true)
    public AddressableHeap.Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        // assert root.o_s == null && root.y_s == null;

        Handle<K, V> oldRoot = root;

        // cut all children, combine them and overwrite old root
        root = combine(cutChildren(root));

        // decrease size
        size--;

        return oldRoot;
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
    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime(amortized = false)
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime(amortized = true)
    public void meld(MergeableAddressableHeap<K, V> other) {
        PairingHeap<K, V> h = (PairingHeap<K, V>) other;

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

        // perform the meld
        size += h.size;
        if (comparator == null) {
            root = link(root, h.root);
        } else {
            root = linkWithComparator(root, h.root);
        }

        // clear other
        h.size = 0;
        h.root = null;

        // take ownership
        h.other = this;
    }

    // --------------------------------------------------------------------
    static class Node<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        /*
         * We maintain explicitly the belonging heap, instead of using an inner
         * class due to possible cascading melding.
         */
        PairingHeap<K, V> heap;

        K key;
        V value;
        Node<K, V> o_c; // older child
        Node<K, V> y_s; // younger sibling
        Node<K, V> o_s; // older sibling or parent

        Node(PairingHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
            this.o_c = null;
            this.y_s = null;
            this.o_s = null;
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
        @LogarithmicTime(amortized = true)
        public void decreaseKey(K newKey) {
            getOwner().decreaseKey(this, newKey);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @LogarithmicTime(amortized = true)
        public void delete() {
            getOwner().delete(this);
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        PairingHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                PairingHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                PairingHeap<K, V> cur = heap;
                while (cur.other != root) {
                    PairingHeap<K, V> next = cur.other;
                    cur.other = root;
                    cur = next;
                }
                heap = root;
            }
            return heap;
        }
    }

    /**
     * Decrease the key of a node.
     * 
     * @param n
     *            the node
     * @param newKey
     *            the new key
     */
    @SuppressWarnings("unchecked")
    private void decreaseKey(Node<K, V> n, K newKey) {
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
        if (c == 0 || root == n) {
            return;
        }

        if (n.o_s == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        // unlink from parent
        if (n.y_s != null) {
            n.y_s.o_s = n.o_s;
        }
        if (n.o_s.o_c == n) { // I am the oldest :(
            n.o_s.o_c = n.y_s;
        } else { // I have an older sibling!
            n.o_s.y_s = n.y_s;
        }
        n.y_s = null;
        n.o_s = null;

        // merge with root
        if (comparator == null) {
            root = link(root, n);
        } else {
            root = linkWithComparator(root, n);
        }
    }

    /*
     * Delete a node
     */
    private void delete(Node<K, V> n) {
        if (root == n) {
            deleteMin();
            n.o_c = null;
            n.y_s = null;
            n.o_s = null;
            return;
        }

        if (n.o_s == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        // unlink from parent
        if (n.y_s != null) {
            n.y_s.o_s = n.o_s;
        }
        if (n.o_s.o_c == n) { // I am the oldest :(
            n.o_s.o_c = n.y_s;
        } else { // I have an older sibling!
            n.o_s.y_s = n.y_s;
        }
        n.y_s = null;
        n.o_s = null;

        // perform delete-min at tree rooted at this
        Node<K, V> t = combine(cutChildren(n));

        // and merge with other cut tree
        if (comparator == null) {
            root = link(root, t);
        } else {
            root = linkWithComparator(root, t);
        }

        size--;
    }

    /*
     * Two pass pair and compute root.
     */
    private Node<K, V> combine(Node<K, V> l) {
        if (l == null) {
            return null;
        }

        assert l.o_s == null;

        // left-right pass
        Node<K, V> pairs = null;
        Node<K, V> it = l, p_it;
        if (comparator == null) { // no comparator
            while (it != null) {
                p_it = it;
                it = it.y_s;

                if (it == null) {
                    // append last node to pair list
                    p_it.y_s = pairs;
                    p_it.o_s = null;
                    pairs = p_it;
                } else {
                    Node<K, V> n_it = it.y_s;

                    // disconnect both
                    p_it.y_s = null;
                    p_it.o_s = null;
                    it.y_s = null;
                    it.o_s = null;

                    // link trees
                    p_it = link(p_it, it);

                    // append to pair list
                    p_it.y_s = pairs;
                    pairs = p_it;

                    // advance
                    it = n_it;
                }
            }
        } else {
            while (it != null) {
                p_it = it;
                it = it.y_s;

                if (it == null) {
                    // append last node to pair list
                    p_it.y_s = pairs;
                    p_it.o_s = null;
                    pairs = p_it;
                } else {
                    Node<K, V> n_it = it.y_s;

                    // disconnect both
                    p_it.y_s = null;
                    p_it.o_s = null;
                    it.y_s = null;
                    it.o_s = null;

                    // link trees
                    p_it = linkWithComparator(p_it, it);

                    // append to pair list
                    p_it.y_s = pairs;
                    pairs = p_it;

                    // advance
                    it = n_it;
                }
            }
        }

        // second pass (reverse order - due to add first)
        it = pairs;
        Node<K, V> f = null;
        if (comparator == null) {
            while (it != null) {
                Node<K, V> nextIt = it.y_s;
                it.y_s = null;
                f = link(f, it);
                it = nextIt;
            }
        } else {
            while (it != null) {
                Node<K, V> nextIt = it.y_s;
                it.y_s = null;
                f = linkWithComparator(f, it);
                it = nextIt;
            }
        }

        return f;
    }

    /**
     * Cut the children of a node and return the list.
     * 
     * @param n
     *            the node
     * @return the first node in the children list
     */
    private Node<K, V> cutChildren(Node<K, V> n) {
        Node<K, V> child = n.o_c;
        n.o_c = null;
        if (child != null) {
            child.o_s = null;
        }
        return child;
    }

    @SuppressWarnings("unchecked")
    private Node<K, V> link(Node<K, V> f, Node<K, V> s) {
        if (s == null) {
            return f;
        } else if (f == null) {
            return s;
        } else if (((Comparable<? super K>) f.key).compareTo(s.key) <= 0) {
            s.y_s = f.o_c;
            s.o_s = f;
            if (f.o_c != null) {
                f.o_c.o_s = s;
            }
            f.o_c = s;
            return f;
        } else {
            return link(s, f);
        }
    }

    private Node<K, V> linkWithComparator(Node<K, V> f, Node<K, V> s) {
        if (s == null) {
            return f;
        } else if (f == null) {
            return s;
        } else if (comparator.compare(f.key, s.key) <= 0) {
            s.y_s = f.o_c;
            s.o_s = f;
            if (f.o_c != null) {
                f.o_c.o_s = s;
            }
            f.o_c = s;
            return f;
        } else {
            return linkWithComparator(s, f);
        }
    }

}
