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
import java.util.BitSet;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * An explicit binary tree addressable heap. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * The worst-case cost of {@code insert}, {@code deleteMin}, {@code delete} and
 * {@code decreaceKey} operations is O(log(n)) and the cost of {@code findMin}
 * is O(1).
 *
 * <p>
 * Note that the ordering maintained by a binary heap, like any heap, and
 * whether or not an explicit comparator is provided, must be <em>consistent
 * with {@code equals}</em> if this heap is to correctly implement the
 * {@code Heap} interface. (See {@code Comparable} or {@code Comparator} for a
 * precise definition of <em>consistent with equals</em>.) This is so because
 * the {@code Heap} interface is defined in terms of the {@code equals}
 * operation, but a binary heap performs all key comparisons using its
 * {@code compareTo} (or {@code compare}) method, so two keys that are deemed
 * equal by this method are, from the standpoint of the binary heap, equal. The
 * behavior of a heap <em>is</em> well-defined even if its ordering is
 * inconsistent with {@code equals}; it just fails to obey the general contract
 * of the {@code Heap} interface.
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
 * @see AddressableHeap
 * @see Comparable
 * @see Comparator
 */
public class BinaryTreeAddressableHeap<K, V> implements AddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * Size of the heap
     */
    private long size;

    /**
     * Root node of the heap
     */
    private Node root;

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
    public BinaryTreeAddressableHeap() {
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
    public BinaryTreeAddressableHeap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        this.size = 0;
        this.root = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime
    public AddressableHeap.Handle<K, V> insert(K key) {
        return insert(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime
    @SuppressWarnings("unchecked")
    public AddressableHeap.Handle<K, V> insert(K key, V value) {
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }
        Node n = new Node(key, value);

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
            if (c < 0) {
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

        // find parent of last node and hang
        Node p = findParentNode(size + 1);
        if (p.o_c == null) {
            p.o_c = n;
        } else {
            p.o_c.y_s = n;
        }
        n.y_s = p;

        // increase size
        size++;

        // fix priorities
        fixup(n);

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
    @LogarithmicTime
    public AddressableHeap.Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        Node oldRoot = root;

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

        // remove last node
        Node lastNodeParent = findParentNode(size);
        Node lastNode = lastNodeParent.o_c;
        if (lastNode.y_s != lastNodeParent) {
            Node tmp = lastNode;
            lastNode = tmp.y_s;
            tmp.y_s = lastNodeParent;
        } else {
            lastNodeParent.o_c = null;
        }
        lastNode.y_s = null;

        // decrease size
        size--;

        // place it as root
        // (assumes root.o_c exists)
        if (root.o_c.y_s == root) {
            root.o_c.y_s = lastNode;
        } else {
            root.o_c.y_s.y_s = lastNode;
        }
        lastNode.o_c = root.o_c;
        root = lastNode;

        // fix priorities
        fixdown(root);

        oldRoot.o_c = null;
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

    // handle
    private class Node implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        K key;
        V value;
        Node o_c; // older child
        Node y_s; // younger sibling or parent

        Node(K key, V value) {
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
        @LogarithmicTime
        @SuppressWarnings("unchecked")
        public void decreaseKey(K newKey) {
            if (this != root && y_s == null) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) newKey).compareTo(key);
            } else {
                c = comparator.compare(newKey, key);
            }

            if (c > 0) {
                throw new IllegalArgumentException("Keys can only be decreased!");
            }
            key = newKey;
            if (c == 0 || root == this) {
                return;
            }
            fixup(this);
        }

        @Override
        @LogarithmicTime
        public void delete() {
            if (this != root && y_s == null) {
                throw new IllegalArgumentException("Invalid handle!");
            }

            Node p = getParent(this);
            while (p != null) {
                Node pp = getParent(p);
                swap(this, p, pp);
                p = pp;
            }

            // remove root
            deleteMin();
            o_c = null;
            y_s = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void fixup(Node n) {
        if (comparator == null) {
            Node p = getParent(n);
            while (p != null) {
                if (((Comparable<? super K>) n.key).compareTo(p.key) >= 0) {
                    break;
                }
                Node pp = getParent(p);
                swap(n, p, pp);
                p = pp;
            }
        } else {
            Node p = getParent(n);
            while (p != null) {
                if (comparator.compare(n.key, p.key) >= 0) {
                    break;
                }
                Node pp = getParent(p);
                swap(n, p, pp);
                p = pp;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fixdown(Node n) {
        if (comparator == null) {
            Node p = getParent(n);
            while (n.o_c != null) {
                Node child = n.o_c;
                if (child.y_s != n && ((Comparable<? super K>) child.y_s.key).compareTo(child.key) < 0) {
                    child = child.y_s;
                }
                if (((Comparable<? super K>) n.key).compareTo(child.key) <= 0) {
                    break;
                }
                swap(child, n, p);
                p = child;
            }
        } else {
            Node p = getParent(n);
            while (n.o_c != null) {
                Node child = n.o_c;
                if (child.y_s != n && comparator.compare(child.y_s.key, child.key) < 0) {
                    child = child.y_s;
                }
                if (comparator.compare(n.key, child.key) <= 0) {
                    break;
                }
                swap(child, n, p);
                p = child;
            }
        }
    }

    /*
     * Get the parent node of a given node.
     */
    private Node getParent(Node n) {
        if (n.y_s == null) {
            return null;
        }
        Node c = n.y_s;
        if (c.o_c == n) {
            return c;
        }
        Node p1 = c.y_s;
        if (p1 != null && p1.o_c == n) {
            return p1;
        }
        return c;
    }

    /*
     * Start at the root and traverse the tree in order to find the parent node
     * of a particular node. Uses the bit representation to keep the cost
     * log(n).
     * 
     * @param node the node number assuming that the root node is number one
     */
    private Node findParentNode(long node) {
        // assert node > 0;

        // find bit representation of node
        long[] s = { node };
        BitSet bits = BitSet.valueOf(s);

        // traverse path to last node
        Node cur = root;
        for (int i = bits.length() - 2; i > 0; i--) {
            if (bits.get(i)) {
                cur = cur.o_c.y_s;
            } else {
                cur = cur.o_c;
            }
        }
        return cur;
    }

    /*
     * Swap a node with its parent which must be the root.
     */
    private void swap(Node n, Node root) {
        // assert this.root == root;

        Node nLeftChild = n.o_c;
        if (root.o_c == n) {
            if (n.y_s == root) {
                // n is left child and no right sibling
                n.o_c = root;
                root.y_s = n;
            } else {
                // n is left child and has right sibling
                root.y_s = n.y_s;
                root.y_s.y_s = n;
                n.o_c = root;
            }
        } else {
            // n is right child
            root.o_c.y_s = root;
            n.o_c = root.o_c;
            root.y_s = n;
        }
        n.y_s = null;

        // hang children
        root.o_c = nLeftChild;
        if (nLeftChild != null) {
            if (nLeftChild.y_s == n) {
                nLeftChild.y_s = root;
            } else {
                nLeftChild.y_s.y_s = root;
            }
        }
        this.root = n;
    }

    /*
     * Swap a node with its parent
     * 
     * @param n the node
     * 
     * @param p the parent node
     * 
     * @param pp the parent of the parent node, maybe null
     */
    private void swap(Node n, Node p, Node pp) {
        if (pp == null) {
            swap(n, p);
            return;
        }

        Node nLeftChild = n.o_c;
        if (pp.o_c == p) {
            // p left child of pp
            if (p.o_c == n) {
                if (n.y_s == p) {
                    // n left child of p and no sibling
                    pp.o_c = n;
                    n.y_s = p.y_s;
                    n.o_c = p;
                    p.y_s = n;
                } else {
                    // n left child or p and sibling
                    n.y_s.y_s = n;
                    Node tmp = n.y_s;
                    n.y_s = p.y_s;
                    p.y_s = tmp;
                    pp.o_c = n;
                    n.o_c = p;
                }
            } else {
                // n right child of p
                Node tmp = p.o_c;
                n.y_s = p.y_s;
                pp.o_c = n;
                n.o_c = tmp;
                tmp.y_s = p;
                p.y_s = n;
            }
        } else {
            // p right child of pp
            if (p.o_c == n) {
                if (n.y_s == p) {
                    // n left child of p and no sibling
                    n.y_s = pp;
                    pp.o_c.y_s = n;
                    n.o_c = p;
                    p.y_s = n;
                } else {
                    // n left child of p and sibling
                    pp.o_c.y_s = n;
                    p.y_s = n.y_s;
                    n.y_s = pp;
                    n.o_c = p;
                    p.y_s.y_s = n;
                }
            } else {
                // n right child of p
                pp.o_c.y_s = n;
                n.y_s = pp;
                n.o_c = p.o_c;
                n.o_c.y_s = p;
                p.y_s = n;
            }
        }

        // hang children
        p.o_c = nLeftChild;
        if (nLeftChild != null) {
            if (nLeftChild.y_s == n) {
                nLeftChild.y_s = p;
            } else {
                nLeftChild.y_s.y_s = p;
            }
        }
    }

}
