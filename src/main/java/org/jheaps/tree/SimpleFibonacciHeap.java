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
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableAddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Simple Fibonacci heaps. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * This variant of the Fibonacci heaps is described in detail in the following
 * <a href="https://arxiv.org/abs/1407.5750">paper</a>:
 * <ul>
 * <li>Haim Kaplan, Robert E. Tarjan, Uri Zwick, Fibonacci Heaps Revisited,
 * arXiv 1407.5750, 2014.</li>
 * </ul>
 *
 * <p>
 * This implementation provides amortized O(1) time for operations that do not
 * involve deleting an element such as {@code insert}, and {@code decreaseKey}.
 * Operation {@code findMin} is worst-case O(1). Operations {@code deleteMin}
 * and {@code delete} are amortized O(log(n)). The operation {@code meld} is
 * also amortized O(1).
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
 * Note that the ordering maintained by this heap, like any heap, and whether or
 * not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but this heap performs all
 * key comparisons using its {@code compareTo} (or {@code compare}) method, so
 * two keys that are deemed equal by this method are, from the standpoint of the
 * Fibonacci heap, equal. The behavior of a heap <em>is</em> well-defined even
 * if its ordering is inconsistent with {@code equals}; it just fails to obey
 * the general contract of the {@code AddressableHeap} interface.
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
 */
public class SimpleFibonacciHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * Size of consolidation auxiliary array. Computed for number of elements
     * equal to {@link Long#MAX_VALUE}.
     */
    private final static int AUX_CONSOLIDATE_ARRAY_SIZE = 91;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * The root
     */
    private Node<K, V> root;

    /**
     * Size of the heap
     */
    private long size;

    /**
     * Auxiliary array for consolidation
     */
    private Node<K, V>[] aux;

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
    protected SimpleFibonacciHeap<K, V> other;

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
    public SimpleFibonacciHeap() {
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
    @SuppressWarnings("unchecked")
    public SimpleFibonacciHeap(Comparator<? super K> comparator) {
        this.root = null;
        this.comparator = comparator;
        this.size = 0;
        this.aux = (Node<K, V>[]) Array.newInstance(Node.class, AUX_CONSOLIDATE_ARRAY_SIZE);
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
    @SuppressWarnings("unchecked")
    @ConstantTime(amortized = true)
    public AddressableHeap.Handle<K, V> insert(K key, V value) {
        if (other != this) {
            throw new IllegalStateException("A heap cannot be used after a meld");
        }
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }

        Node<K, V> n = new Node<K, V>(this, key, value);
        if (root == null) {
            root = n;
        } else {
            if (comparator == null) {
                if (((Comparable<? super K>) n.key).compareTo(root.key) < 0) {
                    root = link(root, n);
                } else {
                    link(n, root);
                }
            } else {
                if (comparator.compare(n.key, root.key) < 0) {
                    root = link(root, n);
                } else {
                    link(n, root);
                }
            }
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
    @ConstantTime(amortized = true)
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
        if (comparator == null) {
            return comparableDeleteMin();
        } else {
            return comparatorDeleteMin();
        }
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
    @ConstantTime
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @ConstantTime(amortized = true)
    public void meld(MergeableAddressableHeap<K, V> other) {
        SimpleFibonacciHeap<K, V> h = (SimpleFibonacciHeap<K, V>) other;

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

        // meld
        if (root == null) {
            root = h.root;
        } else if (h.root != null) {
            if (comparator == null) {
                if (((Comparable<? super K>) h.root.key).compareTo(root.key) < 0) {
                    root = link(root, h.root);
                } else {
                    link(h.root, root);
                }
            } else {
                if (comparator.compare(h.root.key, root.key) < 0) {
                    root = link(root, h.root);
                } else {
                    link(h.root, root);
                }
            }
        }
        size += h.size;

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
        SimpleFibonacciHeap<K, V> heap;

        K key;
        V value;
        Node<K, V> parent; // parent
        Node<K, V> child; // any child
        Node<K, V> next; // younger sibling
        Node<K, V> prev; // older sibling
        int rank; // node rank
        boolean mark; // marked or not

        Node(SimpleFibonacciHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
            this.parent = null;
            this.child = null;
            this.next = this;
            this.prev = this;
            this.rank = 0;
            this.mark = false;
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
        @ConstantTime(amortized = true)
        public void decreaseKey(K newKey) {
            SimpleFibonacciHeap<K, V> h = getOwner();
            if (h.comparator == null) {
                h.comparableDecreaseKey(this, newKey);
            } else {
                h.comparatorDecreaseKey(this, newKey);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @LogarithmicTime(amortized = true)
        public void delete() {
            if (this.next == null) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            SimpleFibonacciHeap<K, V> h = getOwner();
            h.forceDecreaseKeyToMinimum(this);
            h.deleteMin();
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        SimpleFibonacciHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                SimpleFibonacciHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                SimpleFibonacciHeap<K, V> cur = heap;
                while (cur.other != root) {
                    SimpleFibonacciHeap<K, V> next = cur.other;
                    cur.other = root;
                    cur = next;
                }
                heap = root;
            }
            return heap;
        }
    }

    /*
     * Decrease the key of a node.
     */
    @SuppressWarnings("unchecked")
    private void comparableDecreaseKey(Node<K, V> n, K newKey) {
        int c = ((Comparable<? super K>) newKey).compareTo(n.key);
        if (c > 0) {
            throw new IllegalArgumentException("Keys can only be decreased!");
        }
        n.key = newKey;
        if (c == 0) {
            return;
        }

        if (n.next == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        // if not root and heap order violation
        Node<K, V> y = n.parent;
        if (y != null && ((Comparable<? super K>) n.key).compareTo(y.key) < 0) {
            cut(n, y);
            root.mark = false;
            cascadingRankChange(y);
            if (((Comparable<? super K>) n.key).compareTo(root.key) < 0) {
                root = link(root, n);
            } else {
                link(n, root);
            }
        }
    }

    /*
     * Decrease the key of a node.
     */
    private void comparatorDecreaseKey(Node<K, V> n, K newKey) {
        int c = comparator.compare(newKey, n.key);
        if (c > 0) {
            throw new IllegalArgumentException("Keys can only be decreased!");
        }
        n.key = newKey;
        if (c == 0) {
            return;
        }

        if (n.next == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        // if not root and heap order violation
        Node<K, V> y = n.parent;
        if (y != null && comparator.compare(n.key, y.key) < 0) {
            cut(n, y);
            root.mark = false;
            cascadingRankChange(y);
            if (comparator.compare(n.key, root.key) < 0) {
                root = link(root, n);
            } else {
                link(n, root);
            }
        }
    }

    /*
     * Decrease the key of a node to the minimum. Helper function for performing
     * a delete operation. Does not change the node's actual key, but behaves as
     * the key is the minimum key in the heap.
     */
    private void forceDecreaseKeyToMinimum(Node<K, V> n) {
        Node<K, V> y = n.parent;
        if (y != null) {
            cut(n, y);
            root.mark = false;
            cascadingRankChange(y);
            root = link(root, n);
        }
    }

    @SuppressWarnings("unchecked")
    private AddressableHeap.Handle<K, V> comparableDeleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        Node<K, V> z = root;
        Node<K, V> x = root.child;

        // clear fields of previous root
        z.child = null;
        z.next = null;
        z.prev = null;

        // simple case, no children
        if (x == null) {
            root = null;
            size = 0;
            return z;
        }

        // iterate over all children of root
        int maxDegree = -1;
        while (x != null) {
            Node<K, V> nextX = (x.next == x) ? null : x.next;

            // clear parent
            x.parent = null;

            // remove from child list
            x.prev.next = x.next;
            x.next.prev = x.prev;
            x.next = x;
            x.prev = x;

            int d = x.rank;

            while (true) {
                Node<K, V> y = aux[d];
                if (y == null) {
                    break;
                }

                // make sure x's key is smaller
                if (((Comparable<? super K>) y.key).compareTo(x.key) < 0) {
                    Node<K, V> tmp = x;
                    x = y;
                    y = tmp;
                }

                // make y a child of x
                link(y, x);
                // make link fair by increasing rank
                x.rank++;

                aux[d] = null;
                d++;
            }

            // store result
            aux[d] = x;

            // keep track of max degree
            if (d > maxDegree) {
                maxDegree = d;
            }

            // advance
            x = nextX;
        }

        // recreate tree
        int i = 0;
        while (i <= maxDegree && aux[i] == null) {
            i++;
        }
        root = aux[i];
        aux[i] = null;
        i++;
        while (i <= maxDegree) {
            Node<K, V> n = aux[i];
            if (n != null) {
                if (((Comparable<? super K>) n.key).compareTo(root.key) < 0) {
                    root = link(root, n);
                } else {
                    link(n, root);
                }
                aux[i] = null;
            }
            i++;
        }

        // decrease size
        size--;

        return z;
    }

    private AddressableHeap.Handle<K, V> comparatorDeleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        Node<K, V> z = root;
        Node<K, V> x = root.child;

        // clear fields of previous root
        z.child = null;
        z.next = null;
        z.prev = null;

        // simple case, no children
        if (x == null) {
            root = null;
            size = 0;
            return z;
        }

        // iterate over all children of root
        int maxDegree = -1;
        while (x != null) {
            Node<K, V> nextX = (x.next == x) ? null : x.next;

            // clear parent
            x.parent = null;

            // remove from child list
            x.prev.next = x.next;
            x.next.prev = x.prev;
            x.next = x;
            x.prev = x;

            int d = x.rank;

            while (true) {
                Node<K, V> y = aux[d];
                if (y == null) {
                    break;
                }

                // make sure x's key is smaller
                if (comparator.compare(y.key, x.key) < 0) {
                    Node<K, V> tmp = x;
                    x = y;
                    y = tmp;
                }

                // make y a child of x
                link(y, x);
                // make link fair by increasing rank
                x.rank++;

                aux[d] = null;
                d++;
            }

            // store result
            aux[d] = x;

            // keep track of max degree
            if (d > maxDegree) {
                maxDegree = d;
            }

            // advance
            x = nextX;
        }

        // recreate tree
        int i = 0;
        while (i <= maxDegree && aux[i] == null) {
            i++;
        }
        root = aux[i];
        aux[i] = null;
        i++;
        while (i <= maxDegree) {
            Node<K, V> n = aux[i];
            if (n != null) {
                if (comparator.compare(n.key, root.key) < 0) {
                    root = link(root, n);
                } else {
                    link(n, root);
                }
                aux[i] = null;
            }
            i++;
        }

        // decrease size
        size--;

        return z;
    }

    /*
     * (unfair) Link y as a child of x.
     */
    private Node<K, V> link(Node<K, V> y, Node<K, V> x) {
        y.parent = x;
        Node<K, V> child = x.child;
        if (child == null) {
            x.child = y;
            y.next = y;
            y.prev = y;
        } else {
            y.prev = child;
            y.next = child.next;
            child.next = y;
            y.next.prev = y;
        }
        return x;
    }

    /*
     * Cut the link between x and its parent y.
     */
    private void cut(Node<K, V> x, Node<K, V> y) {
        // advance y child
        y.child = x.next;
        if (y.child == x) {
            y.child = null;
        }

        // remove x from child list of y
        x.prev.next = x.next;
        x.next.prev = x.prev;
        x.next = x;
        x.prev = x;
        x.parent = null;

        // clear mark
        x.mark = false;

    }

    /*
     * Cascading rank change. Assumes that the root is unmarked.
     */
    private void cascadingRankChange(Node<K, V> y) {
        while (y.mark == true) {
            y.mark = false;
            if (y.rank > 0) {
                --y.rank;
            }
            y = y.parent;
        }
        y.mark = true;
        if (y.rank > 0) {
            --y.rank;
        }
    }

}
