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

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableAddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.VisibleForTesting;

/**
 * A binary tree soft addressable heap. The heap is sorted according to the
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
 * Note that the operation {@code decreaseKey()} always throws an
 * {@link UnsupportedOperationException} as a soft heap does not support such an
 * operation.
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
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public class BinaryTreeSoftAddressableHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

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
    final RootList<K, V> rootList;

    /**
     * Size of the heap.
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
    private BinaryTreeSoftAddressableHeap<K, V> other;

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
    public BinaryTreeSoftAddressableHeap(double errorRate) {
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
    public BinaryTreeSoftAddressableHeap(double errorRate, Comparator<? super K> comparator) {
        if (Double.compare(errorRate, 0d) <= 0) {
            throw new IllegalArgumentException("Error rate must be positive");
        }
        if (Double.compare(errorRate, 1d) >= 0) {
            throw new IllegalArgumentException("Error rate must be less than one");
        }
        this.rankLimit = (int) Math.ceil(Math.log(1d / errorRate) / Math.log(2)) + 5;
        this.rootList = new RootList<K, V>();
        this.comparator = comparator;
        this.size = 0;
        this.other = this;
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
    public void meld(MergeableAddressableHeap<K, V> other) {
        BinaryTreeSoftAddressableHeap<K, V> h = (BinaryTreeSoftAddressableHeap<K, V>) other;

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

        if (h.other != h) {
            throw new IllegalStateException("A heap cannot be used after a meld.");
        }

        // perform the meld
        mergeInto(h.rootList.head, h.rootList.tail);
        size += h.size;

        // clear other
        h.size = 0;
        h.rootList.head = null;
        h.rootList.tail = null;

        // take ownership
        h.other = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Handle<K, V> insert(K key, V value) {
        if (other != this) {
            throw new IllegalStateException("A heap cannot be used after a meld");
        }
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }
        /*
         * Create a single element heap
         */
        SoftHandle<K, V> n = new SoftHandle<K, V>(this, key, value);
        TreeNode<K, V> treeNode = new TreeNode<K, V>(n);
        RootListNode<K, V> rootListNode = new RootListNode<K, V>(treeNode);

        /*
         * Merge new list into old list
         */
        mergeInto(rootListNode, rootListNode);

        size++;
        return n;
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
    public SoftHandle<K, V> findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return rootList.head.suffixMin.root.cHead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        // find tree with minimum
        RootListNode<K, V> minRootListNode = rootList.head.suffixMin;
        TreeNode<K, V> root = minRootListNode.root;

        // remove from list
        SoftHandle<K, V> result = root.cHead;
        if (result.next != null) {
            result.next.prev = null;
            result.next.tree = root;
        }
        root.cHead = result.next;
        root.cSize--;

        // replenish keys if needed
        if (root.cHead == null || root.cSize <= targetSize(root.rank) / 2) {
            if (root.left != null || root.right != null) {
                // get keys from children
                sift(root);
                updateSuffixMin(minRootListNode);
            } else if (root.cHead == null) {
                // no children and empty list, just remove the tree
                RootListNode<K, V> minRootPrevListNode = minRootListNode.prev;
                delete(minRootListNode);
                updateSuffixMin(minRootPrevListNode);
            }
        }

        result.next = null;
        result.prev = null;
        result.tree = null;
        size--;
        return result;
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class RootList<K, V> implements Serializable {

        private static final long serialVersionUID = 1;

        RootListNode<K, V> head;
        RootListNode<K, V> tail;

        RootList() {
            this.head = null;
            this.tail = null;
        }
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class RootListNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1;

        RootListNode<K, V> next;
        RootListNode<K, V> prev;
        RootListNode<K, V> suffixMin;
        TreeNode<K, V> root;

        RootListNode(TreeNode<K, V> tree) {
            this.root = tree;
            tree.parent = this;
            this.suffixMin = this;
            this.next = null;
            this.prev = null;
        }
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class TreeNode<K, V> implements Serializable {

        private static final long serialVersionUID = 1;

        // rank
        int rank;
        // parent
        Object parent;
        // left child
        TreeNode<K, V> left;
        // right child
        TreeNode<K, V> right;
        // corrupted list head
        SoftHandle<K, V> cHead;
        // corrupted list tail
        SoftHandle<K, V> cTail;
        /*
         * Corrupted list size. This may be larger than the actual size as it
         * contains also a count of ghost elements (deleted by using directly
         * the handle). Checking whether the corrupted list is empty should be
         * performed using cHead.
         */
        long cSize;
        // corrupted key
        K cKey;

        TreeNode() {
            this(null);
        }

        TreeNode(SoftHandle<K, V> n) {
            this.rank = 0;
            this.parent = null;
            this.left = null;
            this.right = null;
            this.cHead = n;
            this.cTail = n;
            if (n != null) {
                this.cSize = 1;
                this.cKey = n.key;
                n.tree = this;
            } else {
                this.cSize = 0;
                this.cKey = null;
            }
        }
    }

    // --------------------------------------------------------------------
    @VisibleForTesting
    static class SoftHandle<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

        private static final long serialVersionUID = 1;

        /*
         * We maintain explicitly the belonging heap, instead of using an inner
         * class due to possible cascading melding.
         */
        BinaryTreeSoftAddressableHeap<K, V> heap;

        K key;
        V value;
        SoftHandle<K, V> next;
        SoftHandle<K, V> prev;

        /*
         * We maintain the invariant that the first node of a list must contain
         * the tree that it belongs. Due to appending lists, other nodes may
         * point to the wrong tree.
         */
        TreeNode<K, V> tree;

        SoftHandle(BinaryTreeSoftAddressableHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
            this.next = null;
            this.prev = null;
            this.tree = null;
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
         * 
         * @throws UnsupportedOperationException
         *             always, as this operation is not supported in soft heaps
         */
        @Override
        public void decreaseKey(K newKey) {
            throw new UnsupportedOperationException("Not supported in a soft heap");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete() {
            getOwner().delete(this);
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        BinaryTreeSoftAddressableHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                BinaryTreeSoftAddressableHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                BinaryTreeSoftAddressableHeap<K, V> cur = heap;
                while (cur.other != root) {
                    BinaryTreeSoftAddressableHeap<K, V> nextOne = cur.other;
                    cur.other = root;
                    cur = nextOne;
                }
                heap = root;
            }
            return heap;
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
    private void sift(TreeNode<K, V> x) {
        Deque<TreeNode<K, V>> stack = new ArrayDeque<TreeNode<K, V>>();
        stack.push(x);

        while (!stack.isEmpty()) {
            x = stack.peek();
            TreeNode<K, V> xLeft = x.left;
            TreeNode<K, V> xRight = x.right;

            // if leaf or list has enough elements, skip
            if (xLeft == null && xRight == null || x.cHead != null && x.cSize >= targetSize(x.rank)) {
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
            if (x.cHead != null) {
                x.cHead.prev = xLeft.cTail;
            }
            x.cHead = xLeft.cHead;
            if (x.cTail == null) {
                x.cTail = xLeft.cTail;
            }
            x.cHead.tree = x;
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
    private TreeNode<K, V> combine(TreeNode<K, V> x, TreeNode<K, V> y) {
        TreeNode<K, V> z = new TreeNode<K, V>();
        z.left = x;
        x.parent = z;
        z.right = y;
        y.parent = z;
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
    private void updateSuffixMin(RootListNode<K, V> t) {
        if (comparator == null) {
            while (t != null) {
                if (t.next == null) {
                    t.suffixMin = t;
                } else {
                    RootListNode<K, V> nextSuffixMin = t.next.suffixMin;
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
                    RootListNode<K, V> nextSuffixMin = t.next.suffixMin;
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
    private void mergeInto(RootListNode<K, V> head, RootListNode<K, V> tail) {
        // if root list empty, just copy
        if (rootList.head == null) {
            rootList.head = head;
            rootList.tail = tail;
            return;
        }

        // initialize
        RootListNode<K, V> resultHead;
        RootListNode<K, V> resultTail;
        RootListNode<K, V> resultTailPrev = null;
        RootListNode<K, V> cur1 = rootList.head;
        RootListNode<K, V> cur2 = head;

        // add first node
        if (cur1.root.rank <= cur2.root.rank) {
            resultHead = cur1;
            resultTail = cur1;
            RootListNode<K, V> cur1next = cur1.next;
            cur1.next = null;
            cur1 = cur1next;
            if (cur1next != null) {
                cur1next.prev = null;
            }
        } else {
            resultHead = cur2;
            resultTail = cur2;
            RootListNode<K, V> cur2next = cur2.next;
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
                    resultTail.root.parent = resultTail;
                    // remove cur1
                    RootListNode<K, V> cur1next = cur1.next;
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
                    resultTail.root.parent = resultTail;
                    // remove cur2
                    RootListNode<K, V> cur2next = cur2.next;
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
        RootListNode<K, V> updateSuffixFix = resultTail;

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

    /**
     * Delete a node from the root list.
     * 
     * @param n
     *            the node
     */
    private void delete(RootListNode<K, V> n) {
        RootListNode<K, V> nPrev = n.prev;

        if (nPrev != null) {
            nPrev.next = n.next;
        } else {
            rootList.head = n.next;
        }

        if (n.next != null) {
            n.next.prev = nPrev;
        } else {
            rootList.tail = nPrev;
        }

        n.prev = null;
        n.next = null;
    }

    /**
     * Delete an element.
     * 
     * @param n
     *            the element to delete
     */
    @SuppressWarnings("unchecked")
    private void delete(SoftHandle<K, V> n) {
        if (n.tree == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        /*
         * Delete from belonging list. Care must be taken as the tree reference
         * is valid only if the node is the first in the list.
         */
        TreeNode<K, V> tree = n.tree;

        if (tree.cHead != n) {
            /*
             * Not first in list. Each case, remove and leave as ghost element.
             */
            if (n.next != null) {
                n.next.prev = n.prev;
            }
            n.prev.next = n.next;
        } else {
            /*
             * First in list
             */
            SoftHandle<K, V> nNext = n.next;
            tree.cHead = nNext;
            if (nNext != null) {
                /*
                 * More elements exists, remove and leave as ghost element.
                 * Update new first element to point to correct tree.
                 */
                nNext.prev = null;
                nNext.tree = tree;
            } else {
                /*
                 * No more elements, sift.
                 */
                sift(tree);

                /*
                 * If still no elements, remove tree.
                 */
                if (tree.cHead == null) {
                    if (tree.parent instanceof TreeNode) {
                        TreeNode<K, V> p = (TreeNode<K, V>) tree.parent;
                        if (p.left == tree) {
                            p.left = null;
                        } else {
                            p.right = null;
                        }
                    } else {
                        delete((RootListNode<K, V>) tree.parent);
                    }
                }

            }
        }

        n.tree = null;
        n.prev = null;
        n.next = null;

        size--;
    }

}
