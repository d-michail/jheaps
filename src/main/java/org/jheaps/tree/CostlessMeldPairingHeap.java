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
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableAddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogLogTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * The costless meld variant of the pairing heaps. The heap is sorted according
 * to the {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * This implementation provides amortized O(1) time for {@code findMin} and
 * {@code insert}, amortized O(log(n)) for {@code deleteMin} and {@code delete}
 * and amortized O(loglog(n)) for the {@code decreaseKey} operation. The
 * operation {@code meld} takes amortized zero time.
 * 
 * <p>
 * This variant of the pairing heap is due to Amr Elmasry, described in detail
 * in the following
 * <a href="http://dx.doi.org/10.1007/978-3-642-15781-3_16">paper</a>:
 * <ul>
 * <li>Amr Elmasry, Pairing Heaps with Costless Meld, In Proceedings of the 18th
 * Annual European Symposium on Algorithms (ESA 2010), 183--193, 2010.</li>
 * </ul>
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
 * the pairing heap, equal. The behavior of a heap <em>is</em> well-defined even
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
 * @see PairingHeap
 * @see FibonacciHeap
 */
public class CostlessMeldPairingHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * Maximum length of decrease pool for long type.
     */
    private final static int DEFAULT_DECREASE_POOL_SIZE = 64 + 1;

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
     * The decrease pool
     */
    private Node<K, V>[] decreasePool;

    /**
     * How many elements are valid in the decrease pool
     */
    private byte decreasePoolSize;

    /**
     * Index of node with minimum key in the decrease pool. Not existent if
     * {@literal decreasePoolMin >= decreasePoolSize}.
     */
    private byte decreasePoolMinPos;

    /**
     * Comparator for nodes in the decrease pool. Initialized lazily and used
     * when sorting entries in the decrease pool.
     */
    private transient Comparator<Node<K, V>> decreasePoolComparator;

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
    private CostlessMeldPairingHeap<K, V> other;

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
    public CostlessMeldPairingHeap() {
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
    public CostlessMeldPairingHeap(Comparator<? super K> comparator) {
        this.decreasePool = (Node<K, V>[]) Array.newInstance(Node.class, DEFAULT_DECREASE_POOL_SIZE);
        this.decreasePoolSize = 0;
        this.decreasePoolMinPos = 0;

        this.comparator = comparator;
        this.decreasePoolComparator = null;
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
    @SuppressWarnings("unchecked")
    @ConstantTime
    public AddressableHeap.Handle<K, V> findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        } else if (decreasePoolMinPos >= decreasePoolSize) {
            return root;
        } else {
            Node<K, V> poolMin = decreasePool[decreasePoolMinPos];
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) root.key).compareTo(poolMin.key);
            } else {
                c = comparator.compare(root.key, poolMin.key);
            }
            if (c <= 0) {
                return root;
            } else {
                return poolMin;
            }
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
    @SuppressWarnings("unchecked")
    @ConstantTime
    public void clear() {
        root = null;
        size = 0;
        decreasePool = (Node[]) Array.newInstance(Node.class, DEFAULT_DECREASE_POOL_SIZE);
        decreasePoolSize = 0;
        decreasePoolMinPos = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @LogarithmicTime(amortized = true)
    public AddressableHeap.Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        Node<K, V> min;
        if (decreasePoolMinPos >= decreasePoolSize) {
            // decrease pool empty
            min = root;

            // cut all children, and combine them
            root = combine(cutChildren(root));
        } else {
            Node<K, V> poolMin = decreasePool[decreasePoolMinPos];
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) root.key).compareTo(poolMin.key);
            } else {
                c = comparator.compare(root.key, poolMin.key);
            }

            if (c <= 0) {
                // root is smaller
                min = root;

                // cut children, combine
                Node<K, V> childrenTree = combine(cutChildren(root));
                root = null;

                /*
                 * Append to decrease pool without updating minimum as we are
                 * going to consolidate anyway
                 */
                if (childrenTree != null) {
                    addPool(childrenTree, false);
                }
                consolidate();
            } else {
                // minimum in pool is smaller
                min = poolMin;

                // cut children, combine
                Node<K, V> childrenTree = combine(cutChildren(poolMin));

                if (childrenTree != null) {
                    // add to location of previous minimum and consolidate
                    decreasePool[decreasePoolMinPos] = childrenTree;
                    childrenTree.poolIndex = decreasePoolMinPos;
                } else {
                    decreasePool[decreasePoolMinPos] = decreasePool[decreasePoolSize - 1];
                    decreasePool[decreasePoolMinPos].poolIndex = decreasePoolMinPos;
                    decreasePool[decreasePoolSize - 1] = null;
                    decreasePoolSize--;
                }
                poolMin.poolIndex = Node.NO_INDEX;

                consolidate();
            }
        }

        size--;
        return min;
    }

    /**
     * {@inheritDoc}
     * 
     * This operation takes amortized zero cost.
     */
    @Override
    @ConstantTime(amortized = true)
    public void meld(MergeableAddressableHeap<K, V> other) {
        CostlessMeldPairingHeap<K, V> h = (CostlessMeldPairingHeap<K, V>) other;

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
        if (size < h.size) {
            consolidate();

            if (comparator == null) {
                root = link(h.root, root);
            } else {
                root = linkWithComparator(h.root, root);
            }
            decreasePoolSize = h.decreasePoolSize;
            h.decreasePoolSize = 0;

            decreasePoolMinPos = h.decreasePoolMinPos;
            h.decreasePoolMinPos = 0;

            Node<K, V>[] tmp = decreasePool;
            decreasePool = h.decreasePool;
            h.decreasePool = tmp;
        } else {
            h.consolidate();

            if (comparator == null) {
                root = link(h.root, root);
            } else {
                root = linkWithComparator(h.root, root);
            }
        }

        size += h.size;
        h.root = null;
        h.size = 0;

        // take ownership
        h.other = this;
    }

    // node
    static class Node<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        static final byte NO_INDEX = -1;

        /*
         * We maintain explicitly the belonging heap, instead of using an inner
         * class due to possible cascading melding.
         */
        CostlessMeldPairingHeap<K, V> heap;

        K key;
        V value;
        Node<K, V> o_c; // older child
        Node<K, V> y_s; // younger sibling
        Node<K, V> o_s; // older sibling or parent
        byte poolIndex; // position in decrease pool

        Node(CostlessMeldPairingHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
            this.o_c = null;
            this.y_s = null;
            this.o_s = null;
            this.poolIndex = NO_INDEX;
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
        public void delete() {
            getOwner().delete(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @LogLogTime(amortized = true)
        public void decreaseKey(K newKey) {
            getOwner().decreaseKey(this, newKey);
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        CostlessMeldPairingHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                CostlessMeldPairingHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                CostlessMeldPairingHeap<K, V> cur = heap;
                while (cur.other != root) {
                    CostlessMeldPairingHeap<K, V> next = cur.other;
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
            // root or no change in key
            return;
        } else if (n.o_s == null && n.poolIndex == Node.NO_INDEX) {
            // no root, no parent and no pool index
            throw new IllegalArgumentException("Invalid handle!");
        } else if (n.o_s == null) {
            // no parent and not root, so inside pool
            Node<K, V> poolMin = decreasePool[decreasePoolMinPos];
            if (comparator == null) {
                c = ((Comparable<? super K>) newKey).compareTo(poolMin.key);
            } else {
                c = comparator.compare(newKey, poolMin.key);
            }
            if (c < 0) {
                decreasePoolMinPos = n.poolIndex;
            }
            return;
        } else {
            // node has a parent
            Node<K, V> oldestChild = cutOldestChild(n);
            if (oldestChild != null) {
                linkInPlace(oldestChild, n);
            } else {
                cutFromParent(n);
            }

            // append node (minus oldest child) to decrease pool
            addPool(n, true);

            // if decrease pool has >= ceil(logn) trees, consolidate
            double sizeAsDouble = size;
            if (decreasePoolSize >= Math.getExponent(sizeAsDouble) + 1) {
                consolidate();
            }
        }
    }

    /**
     * Delete a node.
     * 
     * @param n
     *            the node
     */
    private void delete(Node<K, V> n) {
        if (n != root && n.o_s == null && n.poolIndex == Node.NO_INDEX) {
            // no root, no parent and no pool index
            throw new IllegalArgumentException("Invalid handle!");
        }

        // node has a parent
        if (n.o_s != null) {
            // cut oldest child
            Node<K, V> oldestChild = cutOldestChild(n);
            if (oldestChild != null) {
                linkInPlace(oldestChild, n);
            } else {
                cutFromParent(n);
            }
        }

        // node has no parent
        // cut children
        Node<K, V> childrenTree = combine(cutChildren(n));
        boolean checkConsolidate = false;
        if (childrenTree != null) {
            checkConsolidate = true;
            addPool(childrenTree, true);
        }

        size--;
        if (n == root) {
            root = null;
            consolidate();
            checkConsolidate = false;
        } else if (n.poolIndex != Node.NO_INDEX) {
            byte curIndex = n.poolIndex;
            decreasePool[curIndex] = decreasePool[decreasePoolSize - 1];
            decreasePool[curIndex].poolIndex = curIndex;
            decreasePool[decreasePoolSize - 1] = null;
            decreasePoolSize--;
            n.poolIndex = Node.NO_INDEX;
            if (curIndex == decreasePoolMinPos) {
                // in decrease pool, and also the minimum
                consolidate();
                checkConsolidate = false;
            } else {
                // in decrease pool, and not the minimum
                if (decreasePoolMinPos == decreasePoolSize) {
                    decreasePoolMinPos = curIndex;
                }
                checkConsolidate = true;
            }
        }

        // if decrease pool has >= ceil(logn) trees, consolidate
        if (checkConsolidate) {
            double sizeAsDouble = size;
            if (decreasePoolSize >= Math.getExponent(sizeAsDouble) + 1) {
                consolidate();
            }
        }
    }

    /*
     * Consolidate. Combine the trees of the decrease pool in one tree by
     * sorting the values of the roots of these trees, and linking the trees in
     * this order such that their roots form a path of nodes in the combined
     * tree (make every root the leftmost child of the root with the next
     * smaller value). Join this combined tree with the main tree.
     */
    private void consolidate() {
        if (decreasePoolSize == 0) {
            return;
        }

        // lazily initialize comparator
        if (decreasePoolComparator == null) {
            if (comparator == null) {
                decreasePoolComparator = new Comparator<Node<K, V>>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public int compare(Node<K, V> o1, Node<K, V> o2) {
                        return ((Comparable<? super K>) o1.key).compareTo(o2.key);
                    }
                };
            } else {
                decreasePoolComparator = new Comparator<Node<K, V>>() {
                    @Override
                    public int compare(Node<K, V> o1, Node<K, V> o2) {
                        return CostlessMeldPairingHeap.this.comparator.compare(o1.key, o2.key);
                    }
                };
            }
        }

        // sort
        Arrays.sort(decreasePool, 0, decreasePoolSize, decreasePoolComparator);

        int i = decreasePoolSize - 1;
        Node<K, V> s = decreasePool[i];
        s.poolIndex = Node.NO_INDEX;
        while (i > 0) {
            Node<K, V> f = decreasePool[i - 1];
            f.poolIndex = Node.NO_INDEX;
            decreasePool[i] = null;

            // link (no comparison, due to sort)
            s.y_s = f.o_c;
            s.o_s = f;
            if (f.o_c != null) {
                f.o_c.o_s = s;
            }
            f.o_c = s;

            // advance
            s = f;
            i--;
        }

        // empty decrease pool
        decreasePool[0] = null;
        decreasePoolSize = 0;
        decreasePoolMinPos = 0;

        // merge tree with root
        if (comparator == null) {
            root = link(root, s);
        } else {
            root = linkWithComparator(root, s);
        }
    }

    /**
     * Append to decrease pool.
     */
    @SuppressWarnings("unchecked")
    private void addPool(Node<K, V> n, boolean updateMinimum) {
        decreasePool[decreasePoolSize] = n;
        n.poolIndex = decreasePoolSize;
        decreasePoolSize++;

        if (updateMinimum && decreasePoolSize > 1) {
            Node<K, V> poolMin = decreasePool[decreasePoolMinPos];
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) n.key).compareTo(poolMin.key);
            } else {
                c = comparator.compare(n.key, poolMin.key);
            }
            if (c < 0) {
                decreasePoolMinPos = n.poolIndex;
            }
        }
    }

    /**
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

    /**
     * Cut the oldest child of a node.
     * 
     * @param n
     *            the node
     * @return the oldest child of a node or null
     */
    private Node<K, V> cutOldestChild(Node<K, V> n) {
        Node<K, V> oldestChild = n.o_c;
        if (oldestChild != null) {
            if (oldestChild.y_s != null) {
                oldestChild.y_s.o_s = n;
            }
            n.o_c = oldestChild.y_s;
            oldestChild.y_s = null;
            oldestChild.o_s = null;
        }
        return oldestChild;
    }

    /**
     * Cut a node from its parent.
     * 
     * @param n
     *            the node
     */
    private void cutFromParent(Node<K, V> n) {
        if (n.o_s != null) {
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
        }
    }

    /**
     * Put an orphan node into the position of another node. The other node
     * becomes an orphan.
     * 
     * @param orphan
     *            the orphan node
     * @param n
     *            the node which will become an orphan
     */
    private void linkInPlace(Node<K, V> orphan, Node<K, V> n) {
        // link orphan at node's position
        orphan.y_s = n.y_s;
        if (n.y_s != null) {
            n.y_s.o_s = orphan;
        }
        orphan.o_s = n.o_s;
        if (n.o_s != null) {
            if (n.o_s.o_c == n) { // node is the oldest :(
                n.o_s.o_c = orphan;
            } else { // node has an older sibling!
                n.o_s.y_s = orphan;
            }
        }
        n.o_s = null;
        n.y_s = null;
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
