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
 * Rank-Pairing heaps. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * This is the type-1 rank-pairing heap described in detail in the following
 * <a href="https://doi.org/10.1137/100785351">paper</a>:
 * <ul>
 * <li>B Haeupler, S Sen, RE Tarjan. Rank-Pairing Heaps. SIAM Journal of
 * Computing, 40(6): 1463--1485, 2011.</li>
 * </ul>
 *
 * <p>
 * This implementation provides amortized O(1) time for operations that do not
 * involve deleting an element such as {@code insert}, and {@code decreaseKey}.
 * Operations {@code deleteMin} and {@code delete} are amortized O(log(n)). The
 * operation {@code meld} is also amortized O(1).
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
 * two keys that are deemed equal by this method are, from the standpoint of
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
public class RankPairingHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

    private final static long serialVersionUID = 1;

    /**
     * Size of bucket array. Based on maximum rank.
     */
    private final static int AUX_BUCKET_ARRAY_SIZE = 65;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    /**
     * The last node in the root list
     */
    private Node<K, V> minRoot;

    /**
     * Size of the pairing heap
     */
    private long size;

    /**
     * Auxiliary array for consolidation.
     */
    private Node<K, V>[] aux;

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
    private RankPairingHeap<K, V> other;

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
    public RankPairingHeap() {
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
    public RankPairingHeap(Comparator<? super K> comparator) {
        this.minRoot = null;
        this.comparator = comparator;
        this.size = 0;
        this.aux = (Node<K, V>[]) Array.newInstance(Node.class, AUX_BUCKET_ARRAY_SIZE);
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
        if (minRoot == null) {
            n.r = n;
            minRoot = n;
        } else {
            n.r = minRoot.r;
            minRoot.r = n;
            if (less(n, minRoot)) {
                minRoot = n;
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
    @ConstantTime(amortized = false)
    public AddressableHeap.Handle<K, V> findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return minRoot;
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

        /*
         * Sever spine of left child of minimum
         */
        Node<K, V> oldMinRoot = minRoot;
        Node<K, V> spine = null;
        if (minRoot.l != null) {
            spine = severSpine(minRoot.l);
            minRoot.l = null;
        }

        /*
         * One pass spine
         */
        int maxRank = -1;
        Node<K, V> output = null;
        while (spine != null) {
            // remove second from list
            Node<K, V> cur;
            if (spine.r == spine) {
                cur = spine;
                spine = null;
            } else {
                cur = spine.r;
                spine.r = cur.r;
            }
            cur.r = null;

            int rank = cur.rank;
            Node<K, V> auxEntry = aux[rank];
            if (auxEntry == null) {
                aux[rank] = cur;
                if (rank > maxRank) {
                    maxRank = rank;
                }
            } else {
                aux[rank] = null;
                cur = link(cur, auxEntry);
                // add to output list
                if (output == null) {
                    cur.r = cur;
                    output = cur;
                } else {
                    cur.r = output.r;
                    output.r = cur;
                    if (less(cur, output)) {
                        // keep track of new minimum
                        output = cur;
                    }
                }
            }
        }

        /*
         * One pass old half-trees, careful to skip old minimum which is still
         * in the root list.
         */
        while (minRoot != null) {
            // remove second from list
            Node<K, V> cur;
            if (minRoot.r == minRoot) {
                cur = minRoot;
                minRoot = null;
            } else {
                cur = minRoot.r;
                minRoot.r = cur.r;
            }
            cur.r = null;

            if (cur == oldMinRoot) {
                // skip the old minimum
                continue;
            }

            int rank = cur.rank;
            Node<K, V> auxEntry = aux[rank];
            if (auxEntry == null) {
                aux[rank] = cur;
                if (rank > maxRank) {
                    maxRank = rank;
                }
            } else {
                aux[rank] = null;
                cur = link(cur, auxEntry);
                // add to output list
                if (output == null) {
                    cur.r = cur;
                    output = cur;
                } else {
                    cur.r = output.r;
                    output.r = cur;
                    if (less(cur, output)) {
                        // keep track of new minimum
                        output = cur;
                    }
                }
            }
        }

        /*
         * Process remaining in buckets
         */
        for (int i = 0; i <= maxRank; i++) {
            Node<K, V> cur = aux[i];
            if (cur != null) {
                aux[i] = null;
                // add to output list
                if (output == null) {
                    cur.r = cur;
                    output = cur;
                } else {
                    cur.r = output.r;
                    output.r = cur;
                    if (less(cur, output)) {
                        // keep track of new minimum
                        output = cur;
                    }
                }
            }
        }

        minRoot = output;
        size--;
        return oldMinRoot;
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
        minRoot = null;
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public void meld(MergeableAddressableHeap<K, V> other) {
        RankPairingHeap<K, V> h = (RankPairingHeap<K, V>) other;

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
        if (minRoot == null) {
            minRoot = h.minRoot;
        } else if (h.minRoot != null) {
            Node<K, V> afterMinRoot = minRoot.r;
            Node<K, V> hAfterMinRoot = h.minRoot.r;

            minRoot.r = hAfterMinRoot;
            h.minRoot.r = afterMinRoot;
            if (less(h.minRoot, minRoot)) {
                minRoot = h.minRoot;
            }
        }
        size += h.size;

        // clear other
        h.size = 0;
        h.minRoot = null;

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
        RankPairingHeap<K, V> heap;

        K key;
        V value;
        Node<K, V> p; // parent
        Node<K, V> l; // left child
        Node<K, V> r; // right child or next root
        int rank;

        Node(RankPairingHeap<K, V> heap, K key, V value) {
            this.heap = heap;
            this.key = key;
            this.value = value;
            this.p = null;
            this.l = null;
            this.r = null;
            this.rank = 0;
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
            getOwner().decreaseKey(this, newKey);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @LogarithmicTime(amortized = true)
        public void delete() {
            if (p == null && r == null) {
                throw new IllegalArgumentException("Invalid handle!");
            }
            RankPairingHeap<K, V> h = getOwner();
            h.forceDecreaseKeyToMinimum(this);
            h.deleteMin();
        }

        /*
         * Get the owner heap of the handle. This is union-find with
         * path-compression between heaps.
         */
        RankPairingHeap<K, V> getOwner() {
            if (heap.other != heap) {
                // find root
                RankPairingHeap<K, V> root = heap;
                while (root != root.other) {
                    root = root.other;
                }
                // path-compression
                RankPairingHeap<K, V> cur = heap;
                while (cur.other != root) {
                    RankPairingHeap<K, V> next = cur.other;
                    cur.other = root;
                    cur = next;
                }
                heap = root;
            }
            return heap;
        }
    }

    /*
     * Decrease the key of a node to the minimum. Helper function for performing
     * a delete operation. Does not change the node's actual key, but behaves as
     * the key is the minimum key in the heap.
     */
    private void forceDecreaseKeyToMinimum(Node<K, V> n) {
        // already at root list
        if (n.p == null) {
            minRoot = n;
            return;
        }

        // perform the cut
        Node<K, V> u = n.p;
        cut(n);
        if (minRoot == null) {
            n.r = n;
        } else {
            n.r = minRoot.r;
            minRoot.r = n;
        }
        minRoot = n;

        // restore type-1 ranks
        n.rank = (n.l == null) ? 0 : n.l.rank + 1;
        restoreType1Ranks(u);
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
        if (c == 0) {
            return;
        }

        if (n.p == null && n.r == null) {
            throw new IllegalArgumentException("Invalid handle!");
        }

        // already at root list, just update minimum if needed
        if (n.p == null) {
            if (less(n, minRoot)) {
                minRoot = n;
            }
            return;
        }

        // perform the cut
        Node<K, V> u = n.p;
        cut(n);
        if (minRoot == null) {
            n.r = n;
            minRoot = n;
        } else {
            n.r = minRoot.r;
            minRoot.r = n;
            if (less(n, minRoot)) {
                minRoot = n;
            }
        }

        n.rank = (n.l == null) ? 0 : n.l.rank + 1;
        restoreType1Ranks(u);
    }

    /*
     * Return the minimum node
     */
    @SuppressWarnings("unchecked")
    private boolean less(Node<K, V> x, Node<K, V> y) {
        if (comparator == null) {
            return (((Comparable<? super K>) x.key).compareTo(y.key) < 0);
        } else {
            return comparator.compare(x.key, y.key) < 0;
        }
    }

    /*
     * Iterate over all right children of x and create a list of half-tree
     * roots. We only need to nullify the parent pointers. At the same time give
     * each new root a rank that is one greater than that of its left child.
     */
    private Node<K, V> severSpine(Node<K, V> x) {
        Node<K, V> cur = x;
        while (cur.r != null) {
            cur.p = null;
            if (cur.l == null) {
                cur.rank = 0;
            } else {
                cur.rank = cur.l.rank + 1;
            }
            cur = cur.r;
        }
        cur.p = null;
        if (cur.l == null) {
            cur.rank = 0;
        } else {
            cur.rank = cur.l.rank + 1;
        }
        cur.r = x;
        return x;
    }

    /*
     * Link two half-trees. Assumes that x and y are the roots.
     */
    @SuppressWarnings("unchecked")
    private Node<K, V> link(Node<K, V> x, Node<K, V> y) {
        assert x.rank == y.rank;
        int c;
        if (comparator == null) {
            c = ((Comparable<? super K>) x.key).compareTo(y.key);
        } else {
            c = comparator.compare(x.key, y.key);
        }

        if (c <= 0) {
            y.r = x.l;
            if (x.l != null) {
                x.l.p = y;
            }
            x.l = y;
            y.p = x;
            x.rank += 1;
            return x;
        } else {
            x.r = y.l;
            if (y.l != null) {
                y.l.p = x;
            }
            y.l = x;
            x.p = y;
            y.rank += 1;
            return y;
        }
    }

    /*
     * Cut x and its left child from its parent. Leave the right child of x in
     * place of x.
     */
    private void cut(Node<K, V> x) {
        Node<K, V> u = x.p;
        assert u != null;
        Node<K, V> y = x.r;
        if (u.l == x) {
            u.l = y;
        } else {
            u.r = y;
        }
        if (y != null) {
            y.p = u;
        }
        x.p = null;
        x.r = x;
    }

    /*
     * Restore the type-1 ranks after a cut.
     * 
     * @param The old parent of the node which was cut.
     */
    private void restoreType1Ranks(Node<K, V> u) {
        while (u != null) {
            int leftRank = (u.l == null) ? -1 : u.l.rank;
            if (u.p == null) {
                u.rank = leftRank + 1;
                break;
            }
            int rightRank = (u.r == null) ? -1 : u.r.rank;
            int k = (leftRank == rightRank) ? leftRank + 1 : Math.max(leftRank, rightRank);
            if (k >= u.rank) {
                break;
            }
            u.rank = k;
            u = u.p;
        }
    }

}
