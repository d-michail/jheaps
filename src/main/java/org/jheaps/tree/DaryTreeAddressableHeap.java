package org.jheaps.tree;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * An explicit d-ary tree addressable heap. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 *
 * <p>
 * The worst-case cost of {@code insert}, {@code deleteMin}, {@code delete} and
 * {@code decreaceKey} operations is O(d log_d(n)) and the cost of {@code findMin}
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
public class DaryTreeAddressableHeap<K, V> implements AddressableHeap<K, V>, Serializable {

    private static final long serialVersionUID = 1;

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
     * Branching factor. Always a power of two.
     */
    private final int d;

    /**
     * Base 2 logarithm of branching factor.
     */
    private final int log2d;

    /**
     * Auxiliary for swapping children.
     */
    private Node[] aux;

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
     * 
     * @param d
     *            the branching factor. Should be a power of 2.
     */
    public DaryTreeAddressableHeap(int d) {
        this(d, null);
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
     * @param d
     *            the branching factor. Should be a power of 2.
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     */
    @SuppressWarnings("unchecked")
    public DaryTreeAddressableHeap(int d, Comparator<? super K> comparator) {
        this.comparator = comparator;
        this.size = 0;
        this.root = null;
        if (d < 2 || ((d & (d - 1)) != 0)) {
            throw new IllegalArgumentException("Branching factor d should be a power of 2.");
        }
        this.d = d;
        this.log2d = log2(d);
        this.aux = (DaryTreeAddressableHeap<K, V>.Node[]) Array.newInstance(Node.class, d);
    }

    @Override
    @LogarithmicTime
    public Handle<K, V> insert(K key, V value) {
        if (key == null) {
            throw new NullPointerException("Null keys not permitted");
        }
        Node n = new Node(key, value, d);

        if (size == 0) {
            root = n;
            size = 1;
            return n;
        }

        Node p = findNode(size);
        for (int i = 0; i < d; i++) {
            if (p.children[i] == null) {
                p.children[i] = n;
                break;
            }
        }
        n.parent = p;
        size++;

        fixup(n);

        return n;
    }

    @Override
    @LogarithmicTime
    public Handle<K, V> insert(K key) {
        return insert(key, null);
    }

    @Override
    @ConstantTime
    public Handle<K, V> findMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return root;
    }

    @Override
    @LogarithmicTime
    public Handle<K, V> deleteMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        Node oldRoot = root;

        if (size == 1) {
            root = null;
            size = 0;
        } else {
            root.delete();
        }

        return oldRoot;
    }

    @Override
    @ConstantTime
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @ConstantTime
    public long size() {
        return size;
    }

    @Override
    @ConstantTime
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    // handle
    private class Node implements AddressableHeap.Handle<K, V>, Serializable {

        private final static long serialVersionUID = 1;

        K key;
        V value;
        Node parent;
        Node[] children;

        @SuppressWarnings("unchecked")
        Node(K key, V value, int d) {
            this.key = key;
            this.value = value;
            this.parent = null;
            this.children = (DaryTreeAddressableHeap<K, V>.Node[]) Array.newInstance(Node.class, d);
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
            if (parent == null && root != this) { 
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
            if (parent == null && root != this) { 
                throw new IllegalArgumentException("Invalid handle!");
            }
            
            if (size == 0) {
                throw new NoSuchElementException();
            }

            // swap with last node
            Node last = findNode(size - 1);
            swap(this, last);

            // remove from parent
            if (this.parent != null) {
                for (int i = 0; i < d; i++) {
                    if (this.parent.children[i] == this) {
                        this.parent.children[i] = null;
                    }
                }
                this.parent = null;
            }

            size--;
            if (size == 0) {
                root = null;
            } else if (this != last) {
                fixdown(last);
            }
        }
    }

    /**
     * Start at the root and traverse the tree in order to find a particular
     * node based on its numbering on a level-order traversal of the tree. Uses
     * the bit representation to keep the cost log_d(n).
     * 
     * @param node
     *            the node number assuming that the root node is number zero
     */
    private Node findNode(long node) {
        if (node == 0)
            return root;

        long mask = (long)d - 1;
        long location = (node - 1);
        int log = log2(node - 1) / log2d;

        Node cur = root;
        for (int i = log; i >= 0; i--) {
            int s = i * log2d;
            int path = (int) ((location & (mask << s)) >>> s);
            Node next = cur.children[path];
            if (next == null) {
                break;
            }
            cur = next;
        }
        return cur;
    }

    /**
     * Calculate the floor of the binary logarithm of n.
     *
     * @param n
     *            the input number
     * @return the binary logarithm
     */
    private int log2(long n) {
        // returns 0 for n=0
        long log = 0;
        if ((n & 0xffffffff00000000L) != 0) {
            n >>>= 32;
            log = 32;
        }
        if ((n & 0xffff0000) != 0) {
            n >>>= 16;
            log += 16;
        }
        if (n >= 256) {
            n >>>= 8;
            log += 8;
        }
        if (n >= 16) {
            n >>>= 4;
            log += 4;
        }
        if (n >= 4) {
            n >>>= 2;
            log += 2;
        }
        return (int) (log + (n >>> 1));
    }

    @SuppressWarnings("unchecked")
    private void fixup(Node n) {
        if (comparator == null) {
            Node p = n.parent;
            while (p != null) {
                if (((Comparable<? super K>) n.key).compareTo(p.key) >= 0) {
                    break;
                }
                Node pp = p.parent;
                swap(n, p);
                p = pp;
            }
        } else {
            Node p = n.parent;
            while (p != null) {
                if (comparator.compare(n.key, p.key) >= 0) {
                    break;
                }
                Node pp = p.parent;
                swap(n, p);
                p = pp;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fixdown(Node n) {
        if (comparator == null) {
            while (n.children[0] != null) {
                int min = 0;
                Node child = n.children[min];

                for (int i = 1; i < d; i++) {
                    Node candidate = n.children[i];
                    if (candidate != null && ((Comparable<? super K>) candidate.key).compareTo(child.key) < 0) {
                        min = i;
                        child = candidate;
                    }
                }

                if (((Comparable<? super K>) n.key).compareTo(child.key) <= 0) {
                    break;
                }
                swap(child, n);
            }
        } else {
            while (n.children[0] != null) {
                int min = 0;
                Node child = n.children[min];

                for (int i = 1; i < d; i++) {
                    Node candidate = n.children[i];
                    if (candidate != null && comparator.compare(candidate.key, child.key) < 0) {
                        min = i;
                        child = candidate;
                    }
                }

                if (comparator.compare(n.key, child.key) <= 0) {
                    break;
                }
                swap(child, n);
            }
        }
    }

    /**
     * Swap two nodes
     * 
     * @param a
     *            first node
     * @param b
     *            second node
     */
    private void swap(Node a, Node b) {
        if (a == null || b == null || a == b) {
            return;
        }

        if (a.parent == b) {
            Node tmp = a;
            a = b;
            b = tmp;
        }

        Node pa = a.parent;
        if (b.parent == a) {
            // a is the parent
            int whichChild = -1;
            for (int i = 0; i < d; i++) {
                aux[i] = b.children[i];
                if (b == a.children[i]) {
                    b.children[i] = a;
                    a.parent = b;
                } else {
                    b.children[i] = a.children[i];
                    if (b.children[i] != null) {
                        b.children[i].parent = b;
                    }
                }
                if (pa != null && pa.children[i] == a) { 
                    whichChild = i;
                }
            }
            
            b.parent = pa;
            if (pa != null) { 
                pa.children[whichChild] = b;
            }
            
            for (int i = 0; i < d; i++) {
                a.children[i] = aux[i];
                if (a.children[i] != null) {
                    a.children[i].parent = a;
                }
                aux[i] = null;
            }
        } else {
            // no parent child relationship
            Node pb = b.parent;
            for (int i = 0; i < d; i++) {
                aux[i] = b.children[i];
                b.children[i] = a.children[i];
                if (b.children[i] != null) {
                    b.children[i].parent = b;
                }
            }
            for (int i = 0; i < d; i++) {
                a.children[i] = aux[i];
                if (a.children[i] != null) {
                    a.children[i].parent = a;
                }
                aux[i] = null;
            }
            int aIsChild = -1;
            if (pa != null) {
                for (int i = 0; i < d; i++) {
                    if (pa.children[i] == a) {
                        aIsChild = i;
                    }
                }
            } else {
                b.parent = null;
            }
            int bIsChild = -1;
            if (pb != null) {
                for (int i = 0; i < d; i++) {
                    if (pb.children[i] == b) {
                        bIsChild = i;
                    }
                }
            } else {
                a.parent = null;
            }
            if (aIsChild>=0) { 
                pa.children[aIsChild] = b;
                b.parent = pa;
            }
            if (bIsChild>=0) { 
                pb.children[bIsChild] = a;
                a.parent = pb;
            }
        }

        // switch root
        if (root == a) {
            root = b;
        } else if (root == b) {
            root = a;
        }
    }

}
