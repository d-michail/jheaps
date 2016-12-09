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

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Leftist heaps. The heap is sorted according to the {@linkplain Comparable
 * natural ordering} of its keys, or by a {@link Comparator} provided at heap
 * creation time, depending on which constructor is used.
 *
 * <p>
 * Operations {@code insert}, {@code deleteMin}, {@code decreaseKey}, and
 * {@code delete} take worst-case O(log(n)). Operation {@code findMin} is
 * worst-case O(1).
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
public class LeftistHeap<K, V> extends SkewHeap<K, V> {

    private static final long serialVersionUID = -5948402731186806608L;

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
    public LeftistHeap() {
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
    public LeftistHeap(Comparator<? super K> comparator) {
        super(comparator);
    }

    // ~-----------------------------------------------------------------------
    static class LeftistNode<K, V> extends Node<K, V> {

        private static final long serialVersionUID = 1L;

        int npl; // null path length

        LeftistNode(LeftistHeap<K, V> heap, K key, V value) {
            super(heap, key, value);
            this.npl = 0;
        }
    }

    /**
     * Factory method for new node creation
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the newly created node
     */
    protected Node<K, V> createNode(K key, V value) {
        return new LeftistNode<K, V>(this, key, value);
    }

    /**
     * Swap the children of a node.
     * 
     * @param n
     *            the node
     */
    protected void swapChildren(Node<K, V> n) {
        Node<K, V> left = n.o_c;
        if (left != null) {
            Node<K, V> right = left.y_s;
            if (right != n) {
                n.o_c = right;
                right.y_s = left;
                left.y_s = n;
            }
        }
    }

    /**
     * Top-down union two skew heaps
     * 
     * @param root1
     *            the root of the first heap
     * @param root2
     *            the root of the right heap
     * @return the new root of the merged heap
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Node<K, V> union(Node<K, V> root1, Node<K, V> root2) {
        if (root1 == null) {
            return root2;
        } else if (root2 == null) {
            return root1;
        }

        Node<K, V> newRoot;
        Deque<LeftistNode<K, V>> path = new LinkedList<LeftistNode<K, V>>();

        // find initial
        int c = ((Comparable<? super K>) root1.key).compareTo(root2.key);
        if (c <= 0) {
            newRoot = root1;
            root1 = unlinkRightChild(root1);
        } else {
            newRoot = root2;
            root2 = unlinkRightChild(root2);
        }
        Node<K, V> cur = newRoot;
        path.push((LeftistNode<K, V>) cur);

        // merge
        while (root1 != null && root2 != null) {
            c = ((Comparable<? super K>) root1.key).compareTo(root2.key);
            if (c <= 0) {
                // link as right child of cur
                if (cur.o_c == null) {
                    cur.o_c = root1;
                } else {
                    cur.o_c.y_s = root1;
                }
                root1.y_s = cur;
                cur = root1;
                path.push((LeftistNode<K, V>) cur);
                root1 = unlinkRightChild(root1);
            } else {
                // link as right child of cur
                if (cur.o_c == null) {
                    cur.o_c = root2;
                } else {
                    cur.o_c.y_s = root2;
                }
                root2.y_s = cur;
                cur = root2;
                path.push((LeftistNode<K, V>) cur);
                root2 = unlinkRightChild(root2);
            }
        }

        if (root1 != null) {
            // link as right child of cur
            if (cur.o_c == null) {
                cur.o_c = root1;
            } else {
                cur.o_c.y_s = root1;
            }
            root1.y_s = cur;
        }

        if (root2 != null) {
            // link as right child of cur
            if (cur.o_c == null) {
                cur.o_c = root2;
            } else {
                cur.o_c.y_s = root2;
            }
            root2.y_s = cur;
        }

        /*
         * Traverse path upwards, update null path length and swap if needed.
         */
        while (!path.isEmpty()) {
            LeftistNode<K, V> n = path.pop();

            if (n.o_c != null) {
                // at least on child
                LeftistNode<K, V> nLeft = (LeftistNode<K, V>) n.o_c;
                int nplLeft = nLeft.npl;
                int nplRight = -1;
                if (nLeft.y_s != n) {
                    // two children
                    LeftistNode<K, V> nRight = (LeftistNode<K, V>) nLeft.y_s;
                    nplRight = nRight.npl;
                }
                n.npl = 1 + Math.min(nplLeft, nplRight);

                if (nplLeft < nplRight) {
                    // swap
                    swapChildren(n);
                }

            } else {
                // no children
                n.npl = 0;
            }

        }

        return newRoot;
    }

    /**
     * Top-down union of two leftist heaps with comparator.
     * 
     * @param root1
     *            the root of the first heap
     * @param root2
     *            the root of the right heap
     * @return the new root of the merged heap
     */
    @Override
    protected Node<K, V> unionWithComparator(Node<K, V> root1, Node<K, V> root2) {
        if (root1 == null) {
            return root2;
        } else if (root2 == null) {
            return root1;
        }

        Node<K, V> newRoot;
        Deque<LeftistNode<K, V>> path = new LinkedList<LeftistNode<K, V>>();

        // find initial
        int c = comparator.compare(root1.key, root2.key);
        if (c <= 0) {
            newRoot = root1;
            root1 = unlinkRightChild(root1);
        } else {
            newRoot = root2;
            root2 = unlinkRightChild(root2);
        }
        Node<K, V> cur = newRoot;
        path.push((LeftistNode<K, V>) cur);

        // merge
        while (root1 != null && root2 != null) {
            c = comparator.compare(root1.key, root2.key);
            if (c <= 0) {
                // link as right child of cur
                if (cur.o_c == null) {
                    cur.o_c = root1;
                } else {
                    cur.o_c.y_s = root1;
                }
                root1.y_s = cur;
                cur = root1;
                path.push((LeftistNode<K, V>) cur);
                root1 = unlinkRightChild(root1);
            } else {
                // link as right child of cur
                if (cur.o_c == null) {
                    cur.o_c = root2;
                } else {
                    cur.o_c.y_s = root2;
                }
                root2.y_s = cur;
                cur = root2;
                path.push((LeftistNode<K, V>) cur);
                root2 = unlinkRightChild(root2);
            }
        }

        if (root1 != null) {
            // link as right child of cur
            if (cur.o_c == null) {
                cur.o_c = root1;
            } else {
                cur.o_c.y_s = root1;
            }
            root1.y_s = cur;
        }

        if (root2 != null) {
            // link as right child of cur
            if (cur.o_c == null) {
                cur.o_c = root2;
            } else {
                cur.o_c.y_s = root2;
            }
            root2.y_s = cur;
        }

        /*
         * Traverse path upwards, update null path length and swap if needed.
         */
        while (!path.isEmpty()) {
            LeftistNode<K, V> n = path.pop();

            if (n.o_c != null) {
                // at least on child
                LeftistNode<K, V> nLeft = (LeftistNode<K, V>) n.o_c;
                int nplLeft = nLeft.npl;
                int nplRight = -1;
                if (nLeft.y_s != n) {
                    // two children
                    LeftistNode<K, V> nRight = (LeftistNode<K, V>) nLeft.y_s;
                    nplRight = nRight.npl;
                }
                n.npl = 1 + Math.min(nplLeft, nplRight);

                if (nplLeft < nplRight) {
                    // swap
                    swapChildren(n);
                }

            } else {
                // no children
                n.npl = 0;
            }

        }

        return newRoot;
    }

}
