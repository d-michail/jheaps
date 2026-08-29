/*
 * (C) Copyright 2014-2026, by Dimitrios Michail
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
import org.jheaps.annotations.LogLogTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Pure pairing heaps. The heap is sorted according to the {@linkplain Comparable
 * natural ordering} of its keys, or by a {@link Comparator} provided at heap
 * creation time, depending on which constructor is used.
 *
 * <p>
 * The pure pairing heap is a simplification of the {@link PairingHeap} due to
 * Tarjan and Xu, described in detail in the following
 * <a href="https://arxiv.org/abs/2607.23118">paper</a>:
 * <ul>
 * <li>Robert E. Tarjan and Xiaoyang Xu, Pure Pairing Heaps, arXiv:2607.23118,
 * 2026.</li>
 * </ul>
 * A standard pairing heap performs a {@code deleteMin} using a pairing pass
 * followed by an assembly pass which links together all the winners of the
 * pairing pass into a single tree. The pure pairing heap instead eliminates
 * the assembly pass entirely: during its single pairing pass it keeps track of
 * the winner of minimum key, and once the pass is complete, it simply makes
 * that winner the new root and re-parents every other surviving winner as one
 * of its children. Despite being simpler than the standard pairing heap, the
 * pure pairing heap achieves the same amortized bounds as the more complicated
 * multipass pairing heap.
 *
 * <p>
 * This implementation provides amortized O(log(n)) time cost for the
 * {@code deleteMin} operation, amortized O(loglog(n)) time cost for the
 * {@code decreaseKey} operation, and amortized O(1) time cost for the
 * {@code insert} and {@code meld} operations. Operation {@code findMin}, is a
 * worst-case O(1) operation.
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
 * Note that the ordering maintained by a pure pairing heap, like any heap, and
 * whether or not an explicit comparator is provided, must be <em>consistent
 * with {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but a pure pairing heap
 * performs all key comparisons using its {@code compareTo} (or
 * {@code compare}) method, so two keys that are deemed equal by this method
 * are, from the standpoint of this heap, equal. The behavior of a heap
 * <em>is</em> well-defined even if its ordering is inconsistent with
 * {@code equals}; it just fails to obey the general contract of the
 * {@code AddressableHeap} interface.
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
 * @see PairingHeap
 * @see RankPairingHeap
 * @see CostlessMeldPairingHeap
 * @see FibonacciHeap
 */
public class PurePairingHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

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
    private PurePairingHeap<K, V> other;

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
    public PurePairingHeap() {
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
    public PurePairingHeap(Comparator<? super K> comparator) {
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
    @ConstantTime(amortized = true)
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
    @ConstantTime(amortized = true)
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
    @ConstantTime(amortized = true)
    public void meld(MergeableAddressableHeap<K, V> other) {
        PurePairingHeap<K, V> h = (PurePairingHeap<K, V>) other;

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
        PurePairingHeap<K, V> heap;

        K key;
        V value;
        Node<K, V> o_c; // older child
        Node<K, V> y_s; // younger sibling
        Node<K, V> o_s; // older sibling or parent

        Node(PurePairingHeap<K, V> heap, K key, V value) {
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
        @LogLogTime(amortized = true)
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
        PurePairingHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                PurePairingHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                PurePairingHeap<K, V> cur = heap;
                while (cur.other != root) {
                    PurePairingHeap<K, V> next = cur.other;
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
     * Perform a delete-min on a list of roots using a single pairing pass and
     * no assembly pass: the pairing pass links roots pairwise, left to right,
     * while keeping track of the winner of minimum key; that winner becomes
     * the new root, and every other surviving winner is spliced in, as a
     * group, to the left of its pre-existing children.
     */
    private Node<K, V> combine(Node<K, V> l) {
        if (l == null) {
            return null;
        }

        assert l.o_s == null;

        // single pairing pass, building the list of winners (doubly-linked via
        // y_s/o_s, in original left-to-right order) while tracking the winner
        // of minimum key
        Node<K, V> headWinner = null, tailWinner = null, minWinner = null;
        Node<K, V> it = l;
        if (comparator == null) {
            while (it != null) {
                Node<K, V> a = it;
                it = it.y_s;

                Node<K, V> winner;
                if (it == null) {
                    winner = a;
                    winner.y_s = null;
                    winner.o_s = null;
                } else {
                    Node<K, V> b = it;
                    it = it.y_s;

                    // disconnect both
                    a.y_s = null;
                    a.o_s = null;
                    b.y_s = null;
                    b.o_s = null;

                    // link trees
                    winner = link(a, b);
                }

                // append winner to the winners list
                if (headWinner == null) {
                    headWinner = winner;
                } else {
                    tailWinner.y_s = winner;
                    winner.o_s = tailWinner;
                }
                tailWinner = winner;

                if (minWinner == null || ((Comparable<? super K>) winner.key).compareTo(minWinner.key) < 0) {
                    minWinner = winner;
                }
            }
        } else {
            while (it != null) {
                Node<K, V> a = it;
                it = it.y_s;

                Node<K, V> winner;
                if (it == null) {
                    winner = a;
                    winner.y_s = null;
                    winner.o_s = null;
                } else {
                    Node<K, V> b = it;
                    it = it.y_s;

                    // disconnect both
                    a.y_s = null;
                    a.o_s = null;
                    b.y_s = null;
                    b.o_s = null;

                    // link trees
                    winner = linkWithComparator(a, b);
                }

                // append winner to the winners list
                if (headWinner == null) {
                    headWinner = winner;
                } else {
                    tailWinner.y_s = winner;
                    winner.o_s = tailWinner;
                }
                tailWinner = winner;

                if (minWinner == null || comparator.compare(winner.key, minWinner.key) < 0) {
                    minWinner = winner;
                }
            }
        }

        // remove minWinner from the winners list
        Node<K, V> prev = minWinner.o_s;
        Node<K, V> next = minWinner.y_s;
        if (prev != null) {
            prev.y_s = next;
        } else {
            headWinner = next;
        }
        if (next != null) {
            next.o_s = prev;
        } else {
            tailWinner = prev;
        }

        // splice the remaining winners to the left of minWinner's existing
        // children; this is the assembly phase of a pure pairing heap
        if (headWinner != null) {
            Node<K, V> minOldChild = minWinner.o_c;
            tailWinner.y_s = minOldChild;
            if (minOldChild != null) {
                minOldChild.o_s = tailWinner;
            }
            minWinner.o_c = headWinner;
            headWinner.o_s = minWinner;
        }
        minWinner.y_s = null;
        minWinner.o_s = null;

        return minWinner;
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
