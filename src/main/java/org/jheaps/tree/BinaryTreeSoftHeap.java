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
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.NoSuchElementException;

import org.jheaps.Heap;
import org.jheaps.MergeableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.VisibleForTesting;

/**
 * A binary tree soft heap. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 * 
 * <p>
 * If n elements are inserted into a soft heap, then up to &#949;n of the
 * elements still contained in the heap, for a given error parameter &#949;, may
 * be corrupted, i.e., have their keys artificially increased. In exchange for
 * allowing these corruptions, each soft heap operation is performed in O(log
 * 1/&#949;) amortized time. Note that n here is the number of elements inserted
 * into the heaps, not the current number of elements in the heap which may be
 * considerably smaller. Moreover the user has no control on which elements may
 * be corrupted.
 * 
 * <p>
 * This variant of the soft heap is due to Kaplan and Zwick, described in detail
 * in the following
 * <a href="http://dx.doi.org/10.1137/1.9781611973068.53">paper</a>:
 * <ul>
 * <li>Haim Kaplan and Uri Zwick, A simpler implementation and analysis of
 * Chazelle's Soft Heaps, In Proceedings of the 20th Annual ACM-SIAM Symposium
 * on Discrete Algorithms (SODA 2009), 477--485, 2009.</li>
 * </ul>
 *
 * <p>
 * Note that the ordering maintained by a soft heap, like any heap, and whether
 * or not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the {@code Heap}
 * interface. (See {@code Comparable} or {@code Comparator} for a precise
 * definition of <em>consistent with equals</em>.) This is so because the
 * {@code Heap} interface is defined in terms of the {@code equals} operation,
 * but a pairing heap performs all key comparisons using its {@code compareTo}
 * (or {@code compare}) method, so two keys that are deemed equal by this method
 * are, from the standpoint of the heap, equal. The behavior of a heap
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
 *
 * @author Dimitrios Michail
 */
public class BinaryTreeSoftHeap<K> implements Heap<K>, MergeableHeap<K>, Serializable {

    private static final long serialVersionUID = 1;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * Already computed values for target sizes.
     */
    private static final long[] TARGET_SIZE = { 1, 2, 3, 5, 8, 12, 18, 27, 41, 62, 93, 140, 210, 315, 473, 710, 1065,
            1598, 2397, 3596, 5394, 8091, 12137, 18206, 27309, 40964, 61446, 92169, 138254, 207381, 311072, 466608,
            699912, 1049868, 1574802, 2362203, 3543305, 5314958, 7972437, 11958656, 17937984, 26906976, 40360464,
            60540696, 90811044, 136216566, 204324849, 306487274, 459730911, 689596367, 1034394551, 1551591827,
            2327387741L, 3491081612L, 5236622418L, 7854933627L, 11782400441L, 17673600662L, 26510400993L, 39765601490L,
            59648402235L, 89472603353L, 134208905030L };

    /**
     * Tree nodes with less or equal than this rank will have no corrupted keys.
     */
    private final int rankLimit;

    /**
     * The root list, in non-decreasing rank order.
     */
    @VisibleForTesting
    final RootList<K> rootList;

    /**
     * Size of the heap.
     */
    private long size;

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
     * @param errorRate
     *            the error rate
     * @throws IllegalArgumentException
     *             if the error rate is less or equal to zero
     * @throws IllegalArgumentException
     *             if the error rate is greater or equal to one
     */
    public BinaryTreeSoftHeap(double errorRate) {
        this(errorRate, null);
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
     * @param errorRate
     *            the error rate
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     * @throws IllegalArgumentException
     *             if the error rate is less or equal to zero
     * @throws IllegalArgumentException
     *             if the error rate is greater or equal to one
     */
    public BinaryTreeSoftHeap(double errorRate, Comparator<? super K> comparator) {
        if (Double.compare(errorRate, 0d) <= 0) {
            throw new IllegalArgumentException("Error rate must be positive");
        }
        if (Double.compare(errorRate, 1d) >= 0) {
            throw new IllegalArgumentException("Error rate must be less than one");
        }
        this.rankLimit = (int) Math.ceil(Math.log(1d / errorRate) / Math.log(2d)) + 5;
        this.rootList = new RootList<K>();
        this.comparator = comparator;
        this.size = 0;
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
        rootList.head = null;
        rootList.tail = null;
        size = 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if {@code other} has a different error rate
     */
    @Override
    public void meld(MergeableHeap<K> other) {
        BinaryTreeSoftHeap<K> h = (BinaryTreeSoftHeap<K>) other;

        // check same comparator
        if (comparator != null) {
            if (h.comparator == null || !h.comparator.equals(comparator)) {
                throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
            }
        } else if (h.comparator != null) {
            throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
        }

        if (rankLimit != h.rankLimit) {
            throw new IllegalArgumentException("Cannot meld heaps with different error rates!");
        }

        // perform the meld
        mergeInto(h.rootList.head, h.rootList.tail);
        size += h.size;

        // clear other
        h.size = 0;
        h.rootList.head = null;
        h.rootList.tail = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(K key) {
        /*
         * Create a single element heap
         */
        SoftHandle<K> n = new SoftHandle<K>(key);
        TreeNode<K> treeNode = new TreeNode<K>(n);
        RootListNode<K> rootListNode = new RootListNode<K>(treeNode);

        /*
         * Merge new list into old list
         */
        mergeInto(rootListNode, rootListNode);

        size++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return rootList.head.suffixMin.root.cHead.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        // find tree with minimum
        RootListNode<K> minRootListNode = rootList.head.suffixMin;
        TreeNode<K> root = minRootListNode.root;

        // remove from list
        SoftHandle<K> result = root.cHead;
        root.cHead = result.next;
        root.cSize--;

        // replenish keys if needed
        if (root.cSize <= targetSize(root.rank) / 2) {
            if (root.left != null || root.right != null) {
                // get keys from children
                sift(root);
                updateSuffixMin(minRootListNode);
            } else if (root.cSize == 0) {
                // no children and empty list, just remove the tree
                RootListNode<K> minRootPrevListNode = minRootListNode.prev;

                if (minRootPrevListNode != null) {
                    minRootPrevListNode.next = minRootListNode.next;
                } else {
                    rootList.head = minRootListNode.next;
                }
                if (minRootListNode.next != null) {
                    minRootListNode.next.prev = minRootPrevListNode;
                } else {
                    rootList.tail = minRootPrevListNode;
                }
                minRootListNode.prev = null;
                minRootListNode.next = null;

                updateSuffixMin(minRootPrevListNode);
            }
        }

        result.next = null;
        size--;
        return result.key;
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class RootList<K> implements Serializable {

        private static final long serialVersionUID = 1;

        RootListNode<K> head;
        RootListNode<K> tail;

        RootList() {
            this.head = null;
            this.tail = null;
        }
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class RootListNode<K> implements Serializable {

        private static final long serialVersionUID = 1;

        RootListNode<K> next;
        RootListNode<K> prev;
        RootListNode<K> suffixMin;
        TreeNode<K> root;

        RootListNode(TreeNode<K> tree) {
            this.root = tree;
            this.suffixMin = this;
            this.next = null;
            this.prev = null;
        }
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class TreeNode<K> implements Serializable {

        private static final long serialVersionUID = 1;

        int rank;
        // left child
        TreeNode<K> left;
        // right child
        TreeNode<K> right;
        // corrupted list head
        SoftHandle<K> cHead;
        // corrupted list tail
        SoftHandle<K> cTail;
        // corrupted list size
        long cSize;
        // corrupted key
        K cKey;

        TreeNode() {
            this(null);
        }

        TreeNode(SoftHandle<K> n) {
            this.rank = 0;
            this.left = null;
            this.right = null;
            this.cHead = n;
            this.cTail = n;
            if (n != null) {
                this.cSize = 1;
                this.cKey = n.key;
            } else {
                this.cSize = 0;
                this.cKey = null;
            }
        }
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class SoftHandle<K> implements Serializable {

        private static final long serialVersionUID = 1;

        K key;
        SoftHandle<K> next;

        SoftHandle(K key) {
            this.key = key;
            this.next = null;
        }
    }

    /**
     * Compute the target size for a particular rank.
     * 
     * @param rank
     *            the rank
     * @return the target size
     */
    private long targetSize(int rank) {
        return rank <= rankLimit ? 1 : TARGET_SIZE[rank - rankLimit];
    }

    /**
     * Sift elements from children nodes until the current node has enough
     * elements in its list.
     * 
     * @param x
     *            the node
     */
    @SuppressWarnings("unchecked")
    private void sift(TreeNode<K> x) {
        Deque<TreeNode<K>> stack = new ArrayDeque<TreeNode<K>>();
        stack.push(x);

        while (!stack.isEmpty()) {
            x = stack.peek();
            TreeNode<K> xLeft = x.left;
            TreeNode<K> xRight = x.right;

            // if leaf or list has enough elements, skip
            if (xLeft == null && xRight == null || x.cSize >= targetSize(x.rank)) {
                stack.pop();
                continue;
            }

            // swap if needed
            if (xLeft == null || xRight != null
                    && ((comparator == null && ((Comparable<? super K>) xLeft.cKey).compareTo(xRight.cKey) > 0)
                            || (comparator != null && comparator.compare(xLeft.cKey, xRight.cKey) > 0))) {
                x.left = xRight;
                x.right = xLeft;
                xLeft = x.left;
            }

            // grab non-empty list from left child
            xLeft.cTail.next = x.cHead;
            x.cHead = xLeft.cHead;
            if (x.cTail == null) {
                x.cTail = xLeft.cTail;
            }
            x.cSize += xLeft.cSize;

            // set new corrupted key
            x.cKey = xLeft.cKey;

            // clear left child list
            xLeft.cKey = null;
            xLeft.cHead = null;
            xLeft.cTail = null;
            xLeft.cSize = 0;

            // recursively to left child if not a leaf
            if (xLeft.left != null || xLeft.right != null) {
                stack.push(xLeft);
            } else {
                x.left = null;
            }
        }
    }

    /**
     * Combine two trees into a new tree.
     * 
     * @param x
     *            the first tree
     * @param y
     *            the second tree
     * @return the combined tree
     */
    private TreeNode<K> combine(TreeNode<K> x, TreeNode<K> y) {
        TreeNode<K> z = new TreeNode<K>();
        z.left = x;
        z.right = y;
        z.rank = x.rank + 1;
        sift(z);
        return z;
    }

    /**
     * Update all suffix minimum pointers for a node and all its predecessors in
     * the root list.
     * 
     * @param t
     *            the node
     */
    @SuppressWarnings("unchecked")
    private void updateSuffixMin(RootListNode<K> t) {
        if (comparator == null) {
            while (t != null) {
                if (t.next == null) {
                    t.suffixMin = t;
                } else {
                    RootListNode<K> nextSuffixMin = t.next.suffixMin;
                    if (((Comparable<? super K>) t.root.cKey).compareTo(nextSuffixMin.root.cKey) <= 0) {
                        t.suffixMin = t;
                    } else {
                        t.suffixMin = nextSuffixMin;
                    }
                }
                t = t.prev;
            }
        } else {
            while (t != null) {
                if (t.next == null) {
                    t.suffixMin = t;
                } else {
                    RootListNode<K> nextSuffixMin = t.next.suffixMin;
                    if (comparator.compare(t.root.cKey, nextSuffixMin.root.cKey) <= 0) {
                        t.suffixMin = t;
                    } else {
                        t.suffixMin = nextSuffixMin;
                    }
                }
                t = t.prev;
            }
        }
    }

    /**
     * Merge a list into the root list. Assumes that the two lists are sorted in
     * non-decreasing order of rank.
     * 
     * @param head
     *            the list head
     * @param tail
     *            the list tail
     */
    @SuppressWarnings("squid:S2259")
    private void mergeInto(RootListNode<K> head, RootListNode<K> tail) {
        // if root list empty, just copy
        if (rootList.head == null) {
            rootList.head = head;
            rootList.tail = tail;
            return;
        }

        // initialize
        RootListNode<K> resultHead;
        RootListNode<K> resultTail;
        RootListNode<K> resultTailPrev = null;
        RootListNode<K> cur1 = rootList.head;
        RootListNode<K> cur2 = head;

        // add first node
        if (cur1.root.rank <= cur2.root.rank) {
            resultHead = cur1;
            resultTail = cur1;
            RootListNode<K> cur1next = cur1.next;
            cur1.next = null;
            cur1 = cur1next;
            if (cur1next != null) {
                cur1next.prev = null;
            }
        } else {
            resultHead = cur2;
            resultTail = cur2;
            RootListNode<K> cur2next = cur2.next;
            cur2.next = null;
            cur2 = cur2next;
            if (cur2next != null) {
                cur2next.prev = null;
            }
        }

        // merge
        int rank1; 
        int rank2;
        while (true) {
            int resultRank = resultTail.root.rank;

            // read rank1
            if (cur1 != null) {
                rank1 = cur1.root.rank;
            } else {
                if (cur2 != null && cur2.root.rank <= resultRank) {
                    rank1 = Integer.MAX_VALUE;
                } else {
                    break;
                }
            }

            // read rank2
            if (cur2 != null) {
                rank2 = cur2.root.rank;
            } else {
                if (cur1 != null && cur1.root.rank <= resultRank) {
                    rank2 = Integer.MAX_VALUE;
                } else {
                    break;
                }
            }

            if (rank1 <= rank2) {
                switch (Integer.compare(rank1, resultRank)) {
                case 0:
                    // combine into result
                    resultTail.root = combine(cur1.root, resultTail.root);
                    // remove cur1
                    RootListNode<K> cur1next = cur1.next;
                    cur1.next = null;
                    if (cur1next != null) {
                        cur1next.prev = null;
                    }
                    cur1 = cur1next;
                    break;
                case -1:
                    // can happen if three same ranks
                    cur1next = cur1.next;
                    // add before tail into result
                    cur1.next = resultTail;
                    resultTail.prev = cur1;
                    cur1.prev = resultTailPrev;
                    if (resultTailPrev != null) {
                        resultTailPrev.next = cur1;
                    } else {
                        resultHead = cur1;
                    }
                    resultTailPrev = cur1;
                    // advance cur1
                    if (cur1next != null) {
                        cur1next.prev = null;
                    }
                    cur1 = cur1next;
                    break;
                case 1:
                    // append into result
                    resultTail.next = cur1;
                    cur1.prev = resultTail;
                    resultTailPrev = resultTail;
                    resultTail = cur1;
                    // remove cur1
                    cur1 = cur1.next;
                    resultTail.next = null;
                    if (cur1 != null) {
                        cur1.prev = null;
                    }
                    break;
                default:
                    break;
                }
            } else {
                // symmetric case rank2 < rank1
                switch (Integer.compare(rank2, resultRank)) {
                case 0:
                    // combine into result
                    resultTail.root = combine(cur2.root, resultTail.root);
                    // remove cur2
                    RootListNode<K> cur2next = cur2.next;
                    cur2.next = null;
                    if (cur2next != null) {
                        cur2next.prev = null;
                    }
                    cur2 = cur2next;
                    break;
                case -1:
                    // can happen if three same ranks
                    cur2next = cur2.next;
                    // add before tail into result
                    cur2.next = resultTail;
                    resultTail.prev = cur2;
                    cur2.prev = resultTailPrev;
                    if (resultTailPrev != null) {
                        resultTailPrev.next = cur2;
                    } else {
                        resultHead = cur2;
                    }
                    resultTailPrev = cur2;
                    // advance cur2
                    if (cur2next != null) {
                        cur2next.prev = null;
                    }
                    cur2 = cur2next;
                    break;
                case 1:
                    // append into result
                    resultTail.next = cur2;
                    cur2.prev = resultTail;
                    resultTailPrev = resultTail;
                    resultTail = cur2;
                    // remove cur2
                    cur2 = cur2.next;
                    resultTail.next = null;
                    if (cur2 != null) {
                        cur2.prev = null;
                    }
                    break;
                default: 
                    break;
                }

            }
        }

        // record up to which point a suffix minimum update is needed
        RootListNode<K> updateSuffixFix = resultTail;

        // here rank of cur1 is more than result rank
        if (cur1 != null) {
            cur1.prev = resultTail;
            resultTail.next = cur1;
            resultTail = rootList.tail;
        }

        // here rank of cur2 is more than result rank
        if (cur2 != null) {
            cur2.prev = resultTail;
            resultTail.next = cur2;
            resultTail = tail;
        }

        // update suffix minimum
        updateSuffixMin(updateSuffixFix);

        // store final list
        rootList.head = resultHead;
        rootList.tail = resultTail;
    }

}
