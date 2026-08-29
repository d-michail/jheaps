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
import org.jheaps.annotations.LogarithmicTime;
import org.jheaps.annotations.VisibleForTesting;

/**
 * Strict Fibonacci heaps. The heap is sorted according to the {@linkplain Comparable
 * natural ordering} of its keys, or by a {@link Comparator} provided at heap creation
 * time, depending on which constructor is used.
 *
 * <p>
 * A strict Fibonacci heap is a pointer-based heap described in detail in the following
 * <a href="https://doi.org/10.1145/3707692">paper</a>:
 * <ul>
 * <li>Gerth St&oslash;lting Brodal, George Lagogiannis, and Robert E. Tarjan, Strict
 * Fibonacci Heaps, ACM Transactions on Algorithms 21(2), Article 15, 2025.</li>
 * </ul>
 * Ordinary Fibonacci heaps achieve their bounds only in the amortized sense; a single
 * {@code decreaseKey} or {@code meld} may still be expensive. A strict Fibonacci heap
 * matches those same bounds in the <em>worst case</em>, on a pointer machine, using
 * linear space. Every node is either <em>active</em> (owned by a live heap) or
 * <em>passive</em> (its subtree structure has been "forgotten" by a cheap meld); active
 * nodes are further either <em>free</em> or <em>fixed</em>, each fixed node carrying a
 * small integer <em>loss</em>. A heap keeps its active nodes partitioned into a
 * constant number of groups (the "fix-list"), which lets four O(1) local
 * transformations restore, after every operation, the invariants that keep the maximum
 * rank, degree and total loss of the heap logarithmic in its size. Melding two heaps
 * simply marks every active node of the smaller heap passive in O(1) time (a single
 * flag flip shared by all its nodes), discarding the smaller heap's bookkeeping instead
 * of merging it, which is the key simplification over earlier worst-case constant-time
 * meldable heaps.
 *
 * <p>
 * This implementation provides worst-case O(1) time cost for the operations
 * {@code insert}, {@code findMin}, {@code meld} and {@code decreaseKey}, and worst-case
 * O(log(n)) time cost for the operations {@code deleteMin} and {@code delete}.
 *
 * <p>
 * All the above bounds, however, assume that the user does not perform cascading melds
 * on heaps such as:
 *
 * <pre>
 * d.meld(e);
 * c.meld(d);
 * b.meld(c);
 * a.meld(b);
 * </pre>
 *
 * The above scenario, although efficiently supported by using union-find with path
 * compression, invalidates the claimed bounds. More precisely, {@code insert},
 * {@code findMin}, {@code deleteMin} and {@code meld}, when invoked directly on a live
 * heap reference, always remain worst-case as stated above, no matter how many melds
 * that heap has previously been the receiver of. Only {@link AddressableHeap.Handle#decreaseKey(Object)}
 * and {@link AddressableHeap.Handle#delete()}, when invoked on a handle that was
 * absorbed into another heap through one or more melds in which its own heap was the
 * smaller side, pay an additional amortized (not worst-case) union-find lookup in order
 * to locate the handle's current heap.
 *
 * <p>
 * Note that the ordering maintained by a strict Fibonacci heap, like any heap, and
 * whether or not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the {@code AddressableHeap}
 * interface. (See {@code Comparable} or {@code Comparator} for a precise definition of
 * <em>consistent with equals</em>.) This is so because the {@code AddressableHeap}
 * interface is defined in terms of the {@code equals} operation, but a strict Fibonacci
 * heap performs all key comparisons using its {@code compareTo} (or {@code compare})
 * method, so two keys that are deemed equal by this method are, from the standpoint of
 * this heap, equal. The behavior of a heap <em>is</em> well-defined even if its ordering
 * is inconsistent with {@code equals}; it just fails to obey the general contract of the
 * {@code AddressableHeap} interface.
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If multiple
 * threads access a heap concurrently, and at least one of the threads modifies the heap
 * structurally, it <em>must</em> be synchronized externally. (A structural modification
 * is any operation that adds or deletes one or more elements or changing the key of
 * some element.) This is typically accomplished by synchronizing on some object that
 * naturally encapsulates the heap.
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 *
 * @see FibonacciHeap
 * @see SimpleFibonacciHeap
 */
public class StrictFibonacciHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * The comparator used to maintain order in this heap, or null if it uses the
     * natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * The current heap record backing this heap. A meld may cheaply repoint this field
     * to another, larger, heap record instead of physically merging bookkeeping; see
     * {@link #meld(MergeableAddressableHeap)}.
     */
    @VisibleForTesting
    HeapRecord<K, V> rec;

    /**
     * Constructs a new, empty heap, using the natural ordering of its keys. All keys
     * inserted into the heap must implement the {@link Comparable} interface.
     * Furthermore, all such keys must be <em>mutually comparable</em>:
     * {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
     * {@code k1} and {@code k2} in the heap. If the user attempts to put a key into the
     * heap that violates this constraint (for example, the user attempts to put a
     * string key into a heap whose keys are integers), the {@code insert(Object key)}
     * call will throw a {@code ClassCastException}.
     */
    @ConstantTime
    public StrictFibonacciHeap() {
        this(null);
    }

    /**
     * Constructs a new, empty heap, ordered according to the given comparator. All keys
     * inserted into the heap must be <em>mutually comparable</em> by the given
     * comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
     * {@code k2} in the heap. If the user attempts to put a key into the heap that
     * violates this constraint, the {@code insert(Object key)} call will throw a
     * {@code ClassCastException}.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If {@code null},
     *            the {@linkplain Comparable natural ordering} of the keys will be used.
     */
    @ConstantTime
    public StrictFibonacciHeap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        this.rec = new HeapRecord<K, V>(comparator);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException
     *             if the heap has already been used in the right hand side of a meld
     */
    @Override
    @ConstantTime
    public AddressableHeap.Handle<K, V> insert(K key, V value) {
        if (!rec.isActive()) {
            throw new IllegalStateException("A heap cannot be used after a meld");
        }
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }
        Node<K, V> n = new Node<K, V>(rec, key, value);
        if (rec.root == null) {
            rec.root = n;
        } else {
            rec.root = rec.link(rec.root, n);
            rec.applyFreeRootReductions(3, 2);
        }
        return n;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException
     *             if the heap has already been used in the right hand side of a meld
     */
    @Override
    @ConstantTime
    public AddressableHeap.Handle<K, V> insert(K key) {
        return insert(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public AddressableHeap.Handle<K, V> findMin() {
        if (rec.size == 0) {
            throw new NoSuchElementException();
        }
        return rec.root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime
    public AddressableHeap.Handle<K, V> deleteMin() {
        if (rec.size == 0) {
            throw new NoSuchElementException();
        }
        Node<K, V> min = rec.root;
        rec.deleteMin();
        return min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public boolean isEmpty() {
        return rec.size == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public long size() {
        return rec.size;
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
        rec = new HeapRecord<K, V>(comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    @SuppressWarnings("unchecked")
    public void meld(MergeableAddressableHeap<K, V> other) {
        StrictFibonacciHeap<K, V> o = (StrictFibonacciHeap<K, V>) other;

        // check same comparator
        if (comparator != null) {
            if (o.comparator == null || !o.comparator.equals(comparator)) {
                throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
            }
        } else if (o.comparator != null) {
            throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
        }

        if (!rec.isActive() || !o.rec.isActive()) {
            throw new IllegalStateException("A heap cannot be used after a meld.");
        }

        HeapRecord<K, V> small, large;
        if (rec.size < o.rec.size) {
            small = rec;
            large = o.rec;
        } else {
            small = o.rec;
            large = rec;
        }

        // the main novelty in the paper: forget the smaller heap's structure by simply
        // marking it passive in O(1) time
        small.other = large;
        large.size += small.size;
        if (small.size > 0) {
            Node<K, V> smallRoot = small.root;
            small.size = 0;
            small.root = null;
            small.rankList = null;
            small.retireFixList();

            // insert small's fix-list as passive at the front of large's fix-list
            Node<K, V> largeHead = large.fixListHead();
            Node<K, V> smallHead = smallRoot;
            Node<K, V> smallTail = smallRoot.fixPrev;
            smallHead.fixPrev = largeHead.fixPrev;
            smallTail.fixNext = largeHead;
            smallTail.fixNext.fixPrev = smallTail;
            smallHead.fixPrev.fixNext = smallHead;
            large.fixPassive = smallHead;

            // link roots and apply reductions
            large.root = large.link(large.root, smallRoot);
            large.applyFreeRootReductions(1, 1);
        }

        // the receiver always ends up owning the winning (surviving) heap record,
        // and the argument always ends up owning the losing (now empty, passive)
        // one, regardless of which side actually turned out larger
        rec = large;
        o.rec = small;
    }

    /**
     * The heap record backing an active (or, once melded away, permanently passive)
     * strict Fibonacci heap. Node and rank records reference this object directly
     * instead of the outer {@link StrictFibonacciHeap}, so that a meld can cheaply
     * repoint the outer wrapper at whichever heap record turns out to be the larger one,
     * without ever having to rewrite any existing node's or rank's back-pointer.
     */
    static class HeapRecord<K, V> implements Serializable {

        private final static long serialVersionUID = 1;

        // fix-list groups
        static final int PASSIVE = 0;
        static final int FREE_MULTIPLE = 1;
        static final int FREE_SINGLE = 2;
        static final int LOSS_ZERO = 3;
        static final int LOSS_ONE_MULTIPLE = 4;
        static final int LOSS_ONE_SINGLE = 5;
        static final int LOSS_TWO = 6;

        final Comparator<? super K> comparator;
        long size;
        Node<K, V> root;
        Rank<K, V> rankList;
        Node<K, V> fixPassive;
        Node<K, V> fixFreeMultiple;
        Node<K, V> fixFreeSingle;
        Node<K, V> fixLossZero;
        Node<K, V> fixLossOneMultiple;
        Node<K, V> fixLossOneSingle;
        Node<K, V> fixLossTwo;

        /*
         * Union-find pointer used both to mark this record permanently passive
         * (other != this) once it loses a meld, and to let a node handle that was
         * absorbed several melds ago find its current owner in amortized O(1) time via
         * path compression. A record is active if and only if other == this.
         */
        HeapRecord<K, V> other;

        HeapRecord(Comparator<? super K> comparator) {
            this.comparator = comparator;
            this.size = 0;
            this.root = null;
            this.rankList = null;
            this.fixPassive = null;
            this.fixFreeMultiple = null;
            this.fixFreeSingle = null;
            this.fixLossZero = null;
            this.fixLossOneMultiple = null;
            this.fixLossOneSingle = null;
            this.fixLossTwo = null;
            this.other = this;
        }

        boolean isActive() {
            return other == this;
        }

        HeapRecord<K, V> find() {
            if (other != this) {
                HeapRecord<K, V> r = this;
                while (r.other != r) {
                    r = r.other;
                }
                HeapRecord<K, V> cur = this;
                while (cur.other != r) {
                    HeapRecord<K, V> next = cur.other;
                    cur.other = r;
                    cur = next;
                }
                return r;
            }
            return this;
        }

        /*
         * Unified strict node ordering used everywhere the reference algorithm compares
         * two nodes. This deliberately does not duplicate a comparator/Comparable code
         * path pair as most other heaps in this library do: the fix-list bookkeeping
         * ported here is large and intricate enough that duplicating it entirely was
         * judged too risky, so a single helper is used at the cost of one extra
         * indirection per comparison.
         */
        @SuppressWarnings("unchecked")
        boolean less(Node<K, V> x, Node<K, V> y) {
            if (x.forcedMinimum) {
                return true;
            }
            if (y.forcedMinimum) {
                return false;
            }
            int c;
            if (comparator == null) {
                c = ((Comparable<? super K>) x.key).compareTo(y.key);
            } else {
                c = comparator.compare(x.key, y.key);
            }
            return c != 0 ? c < 0 : x.seq < y.seq;
        }

        Node<K, V> link(Node<K, V> x, Node<K, V> y) {
            if (less(x, y)) {
                x.addChild(y);
                return x;
            } else {
                y.addChild(x);
                return y;
            }
        }

        /*
         * Allocation-free equivalent of the reference's generic round-robin
         * apply_reductions, specialized to the only two reduction kinds it is ever
         * called with (free node and root degree reductions). Repeatedly attempts the
         * still-pending attempts of both kinds until either the budget is exhausted or
         * a full round makes no progress at all.
         */
        void applyFreeRootReductions(int freeAttempts, int rootAttempts) {
            while (freeAttempts > 0 || rootAttempts > 0) {
                int f = freeAttempts;
                int r = rootAttempts;
                freeAttempts = 0;
                rootAttempts = 0;
                boolean progress = false;
                for (int i = 0; i < f; i++) {
                    if (reduceFree()) {
                        progress = true;
                    } else {
                        freeAttempts++;
                    }
                }
                for (int i = 0; i < r; i++) {
                    if (reduceRoot()) {
                        progress = true;
                    } else {
                        rootAttempts++;
                    }
                }
                if (!progress) {
                    return;
                }
            }
        }

        void reducePassive() {
            for (int i = 0; i < 3; i++) {
                if (fixPassive == null) {
                    return;
                }
                fixPassive.passive2free(this);
            }
        }

        boolean reduceFree() {
            Node<K, V> x = fixFreeMultiple;
            if (x == null) {
                return false;
            }
            Node<K, V> y = x.fixNext;
            if (less(y, x)) {
                Node<K, V> t = x;
                x = y;
                y = t;
            }

            y.cut(this);
            y.free2fixed();
            x.addChild(y);
            Node<K, V> z = x.leftChild.left;
            if (z.passive()) {
                z.cut(this);
                root.addChild(z);
            }
            return true;
        }

        boolean reduceRoot() {
            if (root == null || root.leftChild == null) {
                return false;
            }
            Node<K, V> z = root.leftChild.left;
            Node<K, V> y = z.left;
            Node<K, V> x = y.left;
            if (z == y || z == x || !x.passive()) {
                return false;
            }

            x.cut(this);
            y.cut(this);
            z.cut(this);
            x.passive2free(this);
            y.passive2free(this);
            z.passive2free(this);

            Node<K, V> t;
            if (less(y, x)) {
                t = x;
                x = y;
                y = t;
            }
            if (less(z, y)) {
                t = y;
                y = z;
                z = t;
                if (less(y, x)) {
                    t = x;
                    x = y;
                    y = t;
                }
            }

            y.free2fixed();
            z.free2fixed();
            root.addChild(x);
            x.addChild(y);
            y.addChild(z);
            return true;
        }

        boolean reduceLoss() {
            return reduceLossOne() || reduceLossTwo();
        }

        boolean reduceLossOne() {
            Node<K, V> x = fixLossOneMultiple;
            if (x == null) {
                return false;
            }
            Node<K, V> y = x.fixNext;
            if (less(y, x)) {
                Node<K, V> t = x;
                x = y;
                y = t;
            }

            y.cut(this);
            x.fixListRemove(this);
            y.fixListRemove(this);
            x.loss--;
            y.loss--;
            x.fixListAdd(this);
            y.fixListAdd(this);
            x.addChild(y);
            Node<K, V> z = x.leftChild.left;
            if (z.passive()) {
                z.cut(this);
                root.addChild(z);
            }
            return true;
        }

        boolean reduceLossTwo() {
            Node<K, V> x = fixLossTwo;
            if (x == null) {
                return false;
            }
            x.fixed2free();
            return true;
        }

        Rank<K, V> rankZero() {
            Rank<K, V> r = rankList;
            if (r == null) {
                r = new Rank<K, V>(this, 0);
                rankList = r;
            }
            r.increaseReferenceCount();
            return r;
        }

        void deleteMin() {
            Node<K, V> oldRoot = root;
            if (size == 1) {
                root = null;
                oldRoot.retire(this);
                return;
            }

            Node<K, V> newRoot = oldRoot.leftChild;
            Node<K, V> cur = newRoot.right;
            while (cur != oldRoot.leftChild) {
                if (less(cur, newRoot)) {
                    newRoot = cur;
                }
                cur = cur.right;
            }
            if (newRoot.fixed()) {
                newRoot.fixed2free();
            }
            newRoot.cut(this);
            while (oldRoot.leftChild != null) {
                Node<K, V> child = oldRoot.leftChild;
                if (child.fixed()) {
                    child.fixed2free();
                }
                child.cut(this);
                link(newRoot, child);
            }
            root = newRoot;
            oldRoot.retire(this);

            reducePassive();
            while (reduceLoss()) {
                // repeat until no loss reduction is possible
            }
            while (reduceFree() || reduceRoot()) {
                // repeat until no free or root reduction can be applied
            }
        }

        /*
         * Structural half of a decrease-key, shared verbatim by both a genuine
         * decreaseKey and a delete (the latter via Node.forcedMinimum, see delete()
         * below). The node's key (or forcedMinimum flag) must already reflect its new,
         * smaller value by the time this is called.
         */
        void decreaseKeyStructural(Node<K, V> node) {
            Node<K, V> parent = node.parent;
            if (parent == null || less(parent, node)) {
                return;
            }
            if (node.fixed()) {
                node.fixed2free();
            }
            node.cut(this);
            root = link(node, root);
            reduceLoss();
            applyFreeRootReductions(6, 4);
        }

        /*
         * Delete a node by temporarily treating it as though its key were minus
         * infinity (without actually touching its real key, since K is generic and has
         * no synthesizable sentinel value), moving it to the root via the same
         * structural transformation used by decreaseKey, and then deleting the
         * (now-minimum) root. At most one node per heap ever has forcedMinimum set at a
         * time, since deleteMin() below fully retires exactly that node before
         * returning.
         */
        void delete(Node<K, V> node) {
            node.forcedMinimum = true;
            decreaseKeyStructural(node);
            deleteMin();
        }

        void retireFixList() {
            fixPassive = null;
            fixFreeMultiple = null;
            fixFreeSingle = null;
            fixLossZero = null;
            fixLossOneMultiple = null;
            fixLossOneSingle = null;
            fixLossTwo = null;
        }

        Node<K, V> fixListHead() {
            return fixListInsertionPoint(PASSIVE);
        }

        Node<K, V> fixListGroupHead(int group) {
            switch (group) {
            case PASSIVE:
                return fixPassive;
            case FREE_MULTIPLE:
                return fixFreeMultiple;
            case FREE_SINGLE:
                return fixFreeSingle;
            case LOSS_ZERO:
                return fixLossZero;
            case LOSS_ONE_MULTIPLE:
                return fixLossOneMultiple;
            case LOSS_ONE_SINGLE:
                return fixLossOneSingle;
            default:
                return fixLossTwo;
            }
        }

        Node<K, V> fixListInsertionPoint(int group) {
            for (int i = 0; i < 7; i++) {
                Node<K, V> point = fixListGroupHead((group + i) % 7);
                if (point != null) {
                    return point;
                }
            }
            return null;
        }

        void fixListInsert(int group, Node<K, V> node) {
            Node<K, V> succ = fixListInsertionPoint(group);
            node.fixListInsertBefore(succ);
            switch (group) {
            case FREE_MULTIPLE:
                fixFreeMultiple = node;
                break;
            case FREE_SINGLE:
                fixFreeSingle = node;
                break;
            case LOSS_ZERO:
                fixLossZero = node;
                break;
            case LOSS_ONE_MULTIPLE:
                fixLossOneMultiple = node;
                break;
            case LOSS_ONE_SINGLE:
                fixLossOneSingle = node;
                break;
            case LOSS_TWO:
                fixLossTwo = node;
                break;
            default:
                break;
            }
        }

    }

    // --------------------------------------------------------------------
    static class Node<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        /*
         * Global, monotonically increasing counter used only to break ties between
         * equal keys with a strict order, exactly as the reference implementation
         * breaks ties using node identity. Must be global rather than per-heap, since
         * nodes originally created in different heaps can end up compared to each
         * other after a meld.
         */
        private static long nextSeq = 0;

        K key;
        V value;

        // tree structure
        Node<K, V> parent;
        Node<K, V> leftChild;
        Node<K, V> left;
        Node<K, V> right;

        // active-node state
        boolean free;
        int loss;
        Rank<K, V> rank;

        // fix-list
        Node<K, V> fixPrev;
        Node<K, V> fixNext;

        final long seq;

        /*
         * Used by delete() to emulate decreasing this node's key to minus infinity
         * without needing a synthesizable sentinel K value; see HeapRecord.delete.
         */
        boolean forcedMinimum;

        Node(HeapRecord<K, V> heap, K key, V value) {
            heap.size++;
            this.key = key;
            this.value = value;
            this.parent = null;
            this.leftChild = null;
            this.right = this;
            this.left = this;
            this.free = true;
            this.loss = 0;
            this.rank = heap.rankZero();
            this.fixPrev = this;
            this.fixNext = this;
            this.seq = nextSeq++;
            this.forcedMinimum = false;
            fixListAdd(heap);
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
        @ConstantTime
        @SuppressWarnings("unchecked")
        public void decreaseKey(K newKey) {
            if (rank == null) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            HeapRecord<K, V> heap = rank.heap.find();

            int c;
            if (heap.comparator == null) {
                c = ((Comparable<? super K>) newKey).compareTo(this.key);
            } else {
                c = heap.comparator.compare(newKey, this.key);
            }
            if (c > 0) {
                throw new IllegalArgumentException("Keys can only be decreased!");
            }
            this.key = newKey;
            heap.decreaseKeyStructural(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @LogarithmicTime
        public void delete() {
            if (rank == null) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            rank.heap.find().delete(this);
        }

        void retire(HeapRecord<K, V> heap) {
            Rank<K, V> r = this.rank;
            fixListRemove(heap);
            heap.size--;
            this.rank = null;
            this.forcedMinimum = false;
            r.decreaseReferenceCount();
        }

        HeapRecord<K, V> heap() {
            return rank.heap;
        }

        boolean active() {
            return heap().isActive();
        }

        boolean passive() {
            return !active();
        }

        boolean free() {
            return active() && free;
        }

        boolean fixed() {
            return active() && !free;
        }

        void changeRank(Rank<K, V> newRank) {
            HeapRecord<K, V> heap = heap();
            newRank.increaseReferenceCount();
            fixListRemove(heap);
            rank.decreaseReferenceCount();
            rank = newRank;
            fixListAdd(heap);
        }

        void increaseRank() {
            changeRank(rank.next());
        }

        void decreaseRank() {
            changeRank(rank.prev());
        }

        void addChild(Node<K, V> child) {
            child.parent = this;
            if (this.leftChild == null) {
                this.leftChild = child;
            } else {
                child.right = this.leftChild;
                child.left = child.right.left;
                child.right.left = child;
                child.left.right = child;
                if (!child.passive()) {
                    this.leftChild = child;
                }
            }
            if (this.active() && child.fixed()) {
                this.increaseRank();
            }
        }

        void cut(HeapRecord<K, V> heap) {
            Node<K, V> parent = this.parent;
            Node<K, V> right = this.right;
            Node<K, V> left = this.left;

            this.parent = null;
            if (parent.leftChild == this) {
                if (right != this) {
                    parent.leftChild = right;
                } else {
                    parent.leftChild = null;
                }
            }
            if (right != this) {
                left.right = right;
                right.left = left;
                this.right = this;
                this.left = this;
            }
            if (this.fixed() && parent.active()) {
                parent.decreaseRank();
                if (parent.fixed()) {
                    parent.fixListRemove(heap);
                    parent.loss++;
                    parent.fixListAdd(heap);
                }
            }
        }

        void free2fixed() {
            HeapRecord<K, V> heap = heap();
            fixListRemove(heap);
            this.free = false;
            this.loss = 0;
            fixListAdd(heap);
        }

        void fixed2free() {
            Node<K, V> parent = this.parent;
            HeapRecord<K, V> heap = heap();
            fixListRemove(heap);
            this.free = true;
            this.loss = 0;
            fixListAdd(heap);
            parent.decreaseRank();
            if (parent.fixed()) {
                parent.fixListRemove(heap);
                parent.loss++;
                parent.fixListAdd(heap);
            }
        }

        void passive2free(HeapRecord<K, V> heap) {
            fixListRemove(heap);
            rank.decreaseReferenceCount();
            rank = heap.rankZero();
            this.free = true;
            this.loss = 0;
            Node<K, V> parent = this.parent;
            if (parent != null) {
                cut(heap);
                parent.addChild(this);
            }
            fixListAdd(heap);
        }

        void fixListUnlink() {
            fixPrev.fixNext = fixNext;
            fixNext.fixPrev = fixPrev;
            fixPrev = this;
            fixNext = this;
        }

        void fixListInsertBefore(Node<K, V> succ) {
            if (succ != null) {
                this.fixNext = succ;
                this.fixPrev = succ.fixPrev;
                this.fixNext.fixPrev = this;
                this.fixPrev.fixNext = this;
            }
        }

        void fixListInsertAfter(Node<K, V> prev) {
            this.fixPrev = prev;
            this.fixNext = prev.fixNext;
            this.fixNext.fixPrev = this;
            this.fixPrev.fixNext = this;
        }

        boolean fixListSameGroup(Node<K, V> other) {
            return (free() && other.free() && this.rank == other.rank)
                    || (fixed() && other.fixed() && this.rank == other.rank && this.loss == 1 && other.loss == 1);
        }

        void fixListGroup(Node<K, V> head, GroupInfo<K, V> out) {
            int count = 1;
            Node<K, V> first = this;
            Node<K, V> last = this;
            while (count < 3 && first != head && fixListSameGroup(first.fixPrev)) {
                first = first.fixPrev;
                count++;
            }
            while (count < 3 && last.fixNext != head && fixListSameGroup(last.fixNext)) {
                last = last.fixNext;
                count++;
            }
            out.count = count;
            out.first = first;
        }

        void fixListAdd(HeapRecord<K, V> heap) {
            Node<K, V> head = heap.fixListHead();
            if (free()) {
                Node<K, V> first = rank.free;
                if (first == null) {
                    rank.free = this;
                    heap.fixListInsert(HeapRecord.FREE_SINGLE, this);
                } else {
                    GroupInfo<K, V> info = new GroupInfo<K, V>();
                    first.fixListGroup(head, info);
                    if (info.count >= 2) {
                        fixListInsertAfter(first);
                    } else {
                        if (heap.fixFreeSingle == first) {
                            if (first.fixNext != head && first.fixNext.free()) {
                                heap.fixFreeSingle = first.fixNext;
                            } else {
                                heap.fixFreeSingle = null;
                            }
                        }
                        first.fixListUnlink();
                        heap.fixListInsert(HeapRecord.FREE_MULTIPLE, this);
                        heap.fixListInsert(HeapRecord.FREE_MULTIPLE, first);
                    }
                }
            } else {
                if (loss == 0) {
                    heap.fixListInsert(HeapRecord.LOSS_ZERO, this);
                } else if (loss >= 2) {
                    heap.fixListInsert(HeapRecord.LOSS_TWO, this);
                } else {
                    Node<K, V> first = rank.lossOne;
                    if (first == null) {
                        rank.lossOne = this;
                        heap.fixListInsert(HeapRecord.LOSS_ONE_SINGLE, this);
                    } else {
                        GroupInfo<K, V> info = new GroupInfo<K, V>();
                        first.fixListGroup(head, info);
                        if (info.count >= 2) {
                            fixListInsertAfter(first);
                        } else {
                            if (heap.fixLossOneSingle == first) {
                                if (first.fixNext != head && first.fixNext.fixed() && first.fixNext.loss == 1) {
                                    heap.fixLossOneSingle = first.fixNext;
                                } else {
                                    heap.fixLossOneSingle = null;
                                }
                            }
                            first.fixListUnlink();
                            heap.fixListInsert(HeapRecord.LOSS_ONE_MULTIPLE, this);
                            heap.fixListInsert(HeapRecord.LOSS_ONE_MULTIPLE, first);
                        }
                    }
                }
            }
        }

        void fixListRemove(HeapRecord<K, V> heap) {
            Node<K, V> succ = fixNext;
            Node<K, V> head = heap.fixListHead();

            if (active() && rank.free == this) {
                if (succ != head && succ.free() && succ.rank == rank) {
                    rank.free = succ;
                } else {
                    rank.free = null;
                }
            }
            if (active() && rank.lossOne == this) {
                if (succ != head && succ.fixed() && succ.loss == 1 && succ.rank == rank) {
                    rank.lossOne = succ;
                } else {
                    rank.lossOne = null;
                }
            }

            if (heap.size == 1) {
                heap.retireFixList();
                return;
            }

            if (passive()) {
                if (this == heap.fixPassive) {
                    if (succ != head && succ.passive()) {
                        heap.fixPassive = succ;
                    } else {
                        heap.fixPassive = null;
                    }
                }
            } else if (free()) {
                GroupInfo<K, V> info = new GroupInfo<K, V>();
                fixListGroup(head, info);
                int count = info.count;
                Node<K, V> first = info.first;
                if (count == 1) {
                    if (heap.fixFreeSingle == this) {
                        if (succ != head && succ.free()) {
                            heap.fixFreeSingle = succ;
                        } else {
                            heap.fixFreeSingle = null;
                        }
                    }
                } else if (count >= 3) {
                    if (heap.fixFreeMultiple == this) {
                        heap.fixFreeMultiple = succ;
                    }
                } else {
                    Node<K, V> other = (first != this) ? first : succ;
                    if (heap.fixFreeMultiple == first) {
                        Node<K, V> f = first.fixNext.fixNext;
                        if (f != head && f.free() && f.fixNext != head && f.fixNext.free() && f.rank == f.fixNext.rank) {
                            heap.fixFreeMultiple = f;
                        } else {
                            heap.fixFreeMultiple = null;
                        }
                    }
                    other.fixListUnlink();
                    heap.fixListInsert(HeapRecord.FREE_SINGLE, other);
                }
            } else {
                if (loss == 0) {
                    if (heap.fixLossZero == this) {
                        if (succ != head && succ.fixed() && succ.loss == 0) {
                            heap.fixLossZero = succ;
                        } else {
                            heap.fixLossZero = null;
                        }
                    }
                } else if (loss >= 2) {
                    if (heap.fixLossTwo == this) {
                        if (succ != head && succ.fixed() && succ.loss >= 2) {
                            heap.fixLossTwo = succ;
                        } else {
                            heap.fixLossTwo = null;
                        }
                    }
                } else {
                    GroupInfo<K, V> info = new GroupInfo<K, V>();
                    fixListGroup(head, info);
                    int count = info.count;
                    Node<K, V> first = info.first;
                    if (count == 1) {
                        if (heap.fixLossOneSingle == this) {
                            if (succ != head && succ.fixed() && succ.loss == 1) {
                                heap.fixLossOneSingle = succ;
                            } else {
                                heap.fixLossOneSingle = null;
                            }
                        }
                    } else if (count >= 3) {
                        if (heap.fixLossOneMultiple == this) {
                            heap.fixLossOneMultiple = succ;
                        }
                    } else {
                        Node<K, V> other = (first != this) ? first : succ;
                        if (heap.fixLossOneMultiple == first) {
                            Node<K, V> f = first.fixNext.fixNext;
                            if (f != head && f.fixed() && f.loss == 1 && f.fixNext != head && f.fixNext.fixed()
                                    && f.fixNext.loss == 1 && f.rank == f.fixNext.rank) {
                                heap.fixLossOneMultiple = f;
                            } else {
                                heap.fixLossOneMultiple = null;
                            }
                        }
                        other.fixListUnlink();
                        heap.fixListInsert(HeapRecord.LOSS_ONE_SINGLE, other);
                    }
                }
            }
            fixListUnlink();
        }

    }

    // --------------------------------------------------------------------
    static class Rank<K, V> implements Serializable {

        private final static long serialVersionUID = 1;

        int rank;
        Rank<K, V> prev;
        Rank<K, V> next;
        HeapRecord<K, V> heap;
        Node<K, V> free;
        Node<K, V> lossOne;
        int referenceCount;

        Rank(HeapRecord<K, V> heap, int rank) {
            this.next = null;
            this.prev = null;
            this.heap = heap;
            this.rank = rank;
            this.free = null;
            this.lossOne = null;
            this.referenceCount = 0;
        }

        void retire() {
            if (heap.rankList == this) {
                heap.rankList = this.next;
            }
            if (this.next != null) {
                this.next.prev = this.prev;
            }
            if (this.prev != null) {
                this.prev.next = this.next;
            }
            this.next = null;
            this.prev = null;
            this.heap = null;
            this.free = null;
            this.lossOne = null;
        }

        void increaseReferenceCount() {
            referenceCount++;
        }

        void decreaseReferenceCount() {
            referenceCount--;
            if (referenceCount == 0) {
                retire();
            }
        }

        Rank<K, V> next() {
            if (next == null || next.rank > rank + 1) {
                new Rank<K, V>(heap, rank + 1).insertAfter(this);
            }
            return next;
        }

        Rank<K, V> prev() {
            if (prev == null || prev.rank < rank - 1) {
                new Rank<K, V>(heap, rank - 1).insertAfter(prev);
            }
            return prev;
        }

        void insertAfter(Rank<K, V> previousRank) {
            this.prev = previousRank;
            this.next = previousRank.next;
            previousRank.next = this;
            if (this.next != null) {
                this.next.prev = this;
            }
        }

    }

    /*
     * Tiny mutable holder replacing the (count, first) tuple returned by the
     * reference's fix_list_group.
     */
    private static class GroupInfo<K, V> {
        int count;
        Node<K, V> first;
    }

}
