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
 * Skew heaps. The heap is sorted according to the {@linkplain Comparable
 * natural ordering} of its keys, or by a {@link Comparator} provided at heap
 * creation time, depending on which constructor is used.
 *
 * <p>
 * Operations {@code insert}, {@code deleteMin}, and {@code delete} take
 * amortized O(log(n)). Operation {@code findMin} is worst-case O(1). Note that
 * a skew-heap does not efficiently support the operation {@code decreaseKey}
 * which is amortized &#937;(log(n)).
 * 
 * <p>
 * Note that the ordering maintained by this heap, like any heap, and whether or
 * not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the {@code Heap}
 * interface. (See {@code Comparable} or {@code Comparator} for a precise
 * definition of <em>consistent with equals</em>.) This is so because the
 * {@code Heap} interface is defined in terms of the {@code equals} operation,
 * but this heap performs all key comparisons using its {@code compareTo} (or
 * {@code compare}) method, so two keys that are deemed equal by this method
 * are, from the standpoint of this heap, equal. The behavior of a heap
 * <em>is</em> well-defined even if its ordering is inconsistent with
 * {@code equals}; it just fails to obey the general contract of the
 * {@code Heap} interface.
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
public class SkewHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    protected final Comparator<? super K> comparator;

    /**
     * Size of the heap
     */
    protected long size;

    /**
     * Root node of the heap
     */
    protected Node<K, V> root;

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
    protected SkewHeap<K, V> other;

    /**
     * Constructs a new, empty heap, using the natural ordering of its keys.
     *
     * <p>
     * All keys inserted into the heap must implement the {@link Comparable}
     * interface. Furthermore, all such keys must be <em>mutually
     * comparable</em>: {@code k1.compareTo(k2)} must not throw a
     * {@code ClassCastException} for any keys {@code k1} and {@code k2} in the
     * heap. If the user attempts to put a key into the heap that violates this
     * constraint (for example, the user attempts to put a string key into a
     * heap whose keys are integers), the {@code insert(Object key)} call will
     * throw a {@code ClassCastException}.
     */
    public SkewHeap() {
        this(null);
    }

    /**
     * Constructs a new, empty heap, ordered according to the given comparator.
     *
     * <p>
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
    public SkewHeap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        this.size = 0;
        this.root = null;
        this.other = this;
    }

    /**
     * {@inheritDoc}
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
    @LogarithmicTime(amortized = true)
    @SuppressWarnings("unchecked")
    public AddressableHeap.Handle<K, V> insert(K key, V value) {
        if (other != this) {
            throw new IllegalStateException("A heap cannot be used after a meld");
        }
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }
        Node<K, V> n = createNode(key, value);

        // easy special cases
        if (size == 0) {
            root = n;
            size = 1;
            return n;
        } else if (size == 1) {
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) key).compareTo(root.key);
            } else {
                c = comparator.compare(key, root.key);
            }
            if (c <= 0) {
                n.o_c = root;
                root.y_s = n;
                root = n;
            } else {
                root.o_c = n;
                n.y_s = root;
            }
            size = 2;
            return n;
        }

        if (comparator == null) {
            root = union(root, n);
        } else {
            root = unionWithComparator(root, n);
        }
        size++;

        return n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
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
    public Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        Node<K, V> oldRoot = root;

        // easy special cases
        if (size == 1) {
            root = null;
            size = 0;
            return oldRoot;
        } else if (size == 2) {
            root = root.o_c;
            root.o_c = null;
            root.y_s = null;
            size = 1;
            oldRoot.o_c = null;
            return oldRoot;
        }

        root = unlinkAndUnionChildren(root);
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
    @ConstantTime
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public void meld(MergeableAddressableHeap<K, V> other) {
        SkewHeap<K, V> h = (SkewHeap<K, V>) other;

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
            root = union(root, h.root);
        } else {
            root = unionWithComparator(root, h.root);
        }

        // clear other
        h.size = 0;
        h.root = null;

        // take ownership
        h.other = this;
    }

    // ~-----------------------------------------------------------------------------
    static class Node<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        /*
         * We maintain explicitly the belonging heap, instead of using an inner
         * class due to possible cascading melding.
         */
        SkewHeap<K, V> heap;

        K key;
        V value;
        Node<K, V> o_c; // older child
        Node<K, V> y_s; // younger sibling or parent

        Node(SkewHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
            this.o_c = null;
            this.y_s = null;
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
            getOwner().decreaseKey(this, newKey);
        }

        @Override
        public void delete() {
            getOwner().delete(this);
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        SkewHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                SkewHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                SkewHeap<K, V> cur = heap;
                while (cur.other != root) {
                    SkewHeap<K, V> next = cur.other;
                    cur.other = root;
                    cur = next;
                }
                heap = root;
            }
            return heap;
        }
    }

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
        if (c == 0 || root == n) {
            n.key = newKey;
            return;
        }

        /*
         * Delete and reinsert
         */
        delete(n);
        n.key = newKey;
        if (comparator == null) {
            root = union(root, n);
        } else {
            root = unionWithComparator(root, n);
        }
        size++;
    }

    /**
     * Create a new node.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the newly created node
     */
    protected Node<K, V> createNode(K key, V value) {
        return new Node<K, V>(this, key, value);
    }

    /**
     * Delete a node from the heap.
     * 
     * @param n
     *            the node
     */
    protected void delete(Node<K, V> n) {
        if (n == root) {
            deleteMin();
            return;
        }

        if (n.y_s == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        // disconnect and union children of node
        Node<K, V> childTree = unlinkAndUnionChildren(n);

        // find parent
        Node<K, V> p = getParent(n);

        // link children tree in place of node
        if (childTree == null) {
            // no children, just unlink from parent
            if (p.o_c == n) {
                if (n.y_s == p) {
                    p.o_c = null;
                } else {
                    p.o_c = n.y_s;
                }
            } else {
                p.o_c.y_s = p;
            }
        } else {
            // link children tree to parent
            if (p.o_c == n) {
                childTree.y_s = n.y_s;
                p.o_c = childTree;
            } else {
                p.o_c.y_s = childTree;
                childTree.y_s = p;
            }
        }

        size--;
        n.o_c = null;
        n.y_s = null;
    }

    /**
     * Unlink the two children of a node and union them forming a new tree.
     * 
     * @param n
     *            the node
     * @return the tree which is formed by the two children subtrees of the node
     */
    protected Node<K, V> unlinkAndUnionChildren(Node<K, V> n) {
        // disconnect children
        Node<K, V> child1 = n.o_c;
        if (child1 == null) {
            return null;
        }
        n.o_c = null;

        Node<K, V> child2 = child1.y_s;
        if (child2 == n) {
            child2 = null;
        } else {
            child2.y_s = null;
        }
        child1.y_s = null;

        if (comparator == null) {
            return union(child1, child2);
        } else {
            return unionWithComparator(child1, child2);
        }
    }

    /**
     * Get the parent node of a given node.
     * 
     * @param n
     *            the node
     * @return the parent of a node
     */
    protected Node<K, V> getParent(Node<K, V> n) {
        if (n.y_s == null) {
            return null;
        }
        Node<K, V> c = n.y_s;
        if (c.o_c == n) {
            return c;
        }
        Node<K, V> p1 = c.y_s;
        if (p1 != null && p1.o_c == n) {
            return p1;
        }
        return c;
    }

    /**
     * Unlink the right child of a node.
     * 
     * @param n
     *            the node
     * @return the right child after unlinking
     */
    protected Node<K, V> unlinkRightChild(Node<K, V> n) {
        Node<K, V> left = n.o_c;
        if (left == null || left.y_s == n) {
            return null;
        }
        Node<K, V> right = left.y_s;
        left.y_s = n;
        right.y_s = null;
        return right;
    }

    /**
     * Top-down union of two skew heaps.
     * 
     * @param root1
     *            the root of the first heap
     * @param root2
     *            the root of the right heap
     * @return the new root of the merged heap
     */
    @SuppressWarnings("unchecked")
    protected Node<K, V> union(Node<K, V> root1, Node<K, V> root2) {
        if (root1 == null) {
            return root2;
        } else if (root2 == null) {
            return root1;
        }

        Node<K, V> newRoot;
        Node<K, V> cur;

        // find initial
        int c = ((Comparable<? super K>) root1.key).compareTo(root2.key);
        if (c <= 0) {
            newRoot = root1;
            root1 = unlinkRightChild(root1);
        } else {
            newRoot = root2;
            root2 = unlinkRightChild(root2);
        }
        cur = newRoot;

        // merge
        while (root1 != null && root2 != null) {
            c = ((Comparable<? super K>) root1.key).compareTo(root2.key);
            if (c <= 0) {
                // link as left child of cur
                if (cur.o_c == null) {
                    root1.y_s = cur;
                } else {
                    root1.y_s = cur.o_c;
                }
                cur.o_c = root1;
                cur = root1;
                root1 = unlinkRightChild(root1);
            } else {
                // link as left child of cur
                if (cur.o_c == null) {
                    root2.y_s = cur;
                } else {
                    root2.y_s = cur.o_c;
                }
                cur.o_c = root2;
                cur = root2;
                root2 = unlinkRightChild(root2);
            }
        }

        while (root1 != null) {
            // link as left child of cur
            if (cur.o_c == null) {
                root1.y_s = cur;
            } else {
                root1.y_s = cur.o_c;
            }
            cur.o_c = root1;
            cur = root1;
            root1 = unlinkRightChild(root1);
        }

        while (root2 != null) {
            // link as left child of cur
            if (cur.o_c == null) {
                root2.y_s = cur;
            } else {
                root2.y_s = cur.o_c;
            }
            cur.o_c = root2;
            cur = root2;
            root2 = unlinkRightChild(root2);
        }

        return newRoot;
    }

    /**
     * Top-down union of two skew heaps with comparator.
     * 
     * @param root1
     *            the root of the first heap
     * @param root2
     *            the root of the right heap
     * @return the new root of the merged heap
     */
    protected Node<K, V> unionWithComparator(Node<K, V> root1, Node<K, V> root2) {
        if (root1 == null) {
            return root2;
        } else if (root2 == null) {
            return root1;
        }

        Node<K, V> newRoot;
        Node<K, V> cur;

        // find initial
        int c = comparator.compare(root1.key, root2.key);
        if (c <= 0) {
            newRoot = root1;
            root1 = unlinkRightChild(root1);
        } else {
            newRoot = root2;
            root2 = unlinkRightChild(root2);
        }
        cur = newRoot;

        // merge
        while (root1 != null && root2 != null) {
            c = comparator.compare(root1.key, root2.key);
            if (c <= 0) {
                // link as left child of cur
                if (cur.o_c == null) {
                    root1.y_s = cur;
                } else {
                    root1.y_s = cur.o_c;
                }
                cur.o_c = root1;
                cur = root1;
                root1 = unlinkRightChild(root1);
            } else {
                // link as left child of cur
                if (cur.o_c == null) {
                    root2.y_s = cur;
                } else {
                    root2.y_s = cur.o_c;
                }
                cur.o_c = root2;
                cur = root2;
                root2 = unlinkRightChild(root2);
            }
        }

        while (root1 != null) {
            // link as left child of cur
            if (cur.o_c == null) {
                root1.y_s = cur;
            } else {
                root1.y_s = cur.o_c;
            }
            cur.o_c = root1;
            cur = root1;
            root1 = unlinkRightChild(root1);
        }

        while (root2 != null) {
            // link as left child of cur
            if (cur.o_c == null) {
                root2.y_s = cur;
            } else {
                root2.y_s = cur.o_c;
            }
            cur.o_c = root2;
            cur = root2;
            root2 = unlinkRightChild(root2);
        }

        return newRoot;
    }

}
