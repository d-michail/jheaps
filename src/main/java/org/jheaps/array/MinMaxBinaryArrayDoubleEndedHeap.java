package org.jheaps.array;

import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.Constants;
import org.jheaps.DoubleEndedHeap;
import org.jheaps.annotations.LinearTime;
import org.jheaps.annotations.VisibleForTesting;

/**
 * An array based binary MinMax heap. The heap is sorted according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 * 
 * <p>
 * For details about the implementation see the following
 * <a href="http://doi.acm.org/10.1145/6617.6621">paper</a>:
 * <ul>
 * <li>M. D. Atkinson, J.-R. Sack, N. Santoro, and T. Strothotte. Min-max Heaps
 * and Generalized Priority Queues. Commun. ACM, 29(10), 996--1000, 1986.</li>
 * </ul>
 * 
 * <p>
 * The implementation uses an array in order to store the elements and
 * automatically maintains the size of the array much like a
 * {@link java.util.Vector} does, providing amortized O(log(n)) time cost for
 * the {@code insert}, {@code deleteMin}, and {@code deleteMax} operations.
 * Operations {@code findMin} and {@code findMax} are worst-case O(1). The
 * bounds are worst-case if the user initializes the heap with a capacity larger
 * or equal to the total number of elements that are going to be inserted into
 * the heap.
 * 
 * <p>
 * Constructing such a heap from an array of elements can be performed using the
 * method {@link #heapify(Object[])} or {@link #heapify(Object[], Comparator)}
 * in linear time.
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
 *
 * @author Dimitrios Michail
 */
public class MinMaxBinaryArrayDoubleEndedHeap<K> extends AbstractArrayHeap<K>
        implements DoubleEndedHeap<K>, Serializable {

    private static final long serialVersionUID = -8985374211686556917L;

    /**
     * Default initial capacity of the heap.
     */
    public static final int DEFAULT_HEAP_CAPACITY = 16;

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
     * <p>
     * The initial capacity of the heap is {@link #DEFAULT_HEAP_CAPACITY} and
     * adjusts automatically based on the sequence of insertions and deletions.
     */
    public MinMaxBinaryArrayDoubleEndedHeap() {
        super(null, DEFAULT_HEAP_CAPACITY);
    }

    /**
     * Constructs a new, empty heap, with a provided initial capacity using the
     * natural ordering of its keys.
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
     * <p>
     * The initial capacity of the heap is provided by the user and is adjusted
     * automatically based on the sequence of insertions and deletions. The
     * capacity will never become smaller than the initial requested capacity.
     *
     * @param capacity
     *            the initial heap capacity
     */
    public MinMaxBinaryArrayDoubleEndedHeap(int capacity) {
        super(null, capacity);
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
     * <p>
     * The initial capacity of the heap is {@link #DEFAULT_HEAP_CAPACITY} and
     * adjusts automatically based on the sequence of insertions and deletions.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     */
    public MinMaxBinaryArrayDoubleEndedHeap(Comparator<? super K> comparator) {
        super(comparator, DEFAULT_HEAP_CAPACITY);
    }

    /**
     * Constructs a new, empty heap, with a provided initial capacity ordered
     * according to the given comparator.
     *
     * <p>
     * All keys inserted into the heap must be <em>mutually comparable</em> by
     * the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys {@code k1}
     * and {@code k2} in the heap. If the user attempts to put a key into the
     * heap that violates this constraint, the {@code insert(Object key)} call
     * will throw a {@code ClassCastException}.
     *
     * <p>
     * The initial capacity of the heap is provided by the user and is adjusted
     * automatically based on the sequence of insertions and deletions.The
     * capacity will never become smaller than the initial requested capacity.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     * @param capacity
     *            the initial heap capacity
     */
    public MinMaxBinaryArrayDoubleEndedHeap(Comparator<? super K> comparator, int capacity) {
        super(comparator, capacity);
    }

    /**
     * Create a heap from an array of elements. The elements of the array are
     * not destroyed. The method has linear time complexity.
     *
     * @param <K>
     *            the type of keys maintained by the heap
     * @param array
     *            an array of elements
     * @return a heap
     * @throws IllegalArgumentException
     *             in case the array is null
     */
    @LinearTime
    public static <K> MinMaxBinaryArrayDoubleEndedHeap<K> heapify(K[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            return new MinMaxBinaryArrayDoubleEndedHeap<K>();
        }

        MinMaxBinaryArrayDoubleEndedHeap<K> h = new MinMaxBinaryArrayDoubleEndedHeap<K>(array.length);

        System.arraycopy(array, 0, h.array, 1, array.length);
        h.size = array.length;

        for (int i = array.length / 2; i > 0; i--) {
            h.fixdown(i);
        }

        return h;
    }

    /**
     * Create a heap from an array of elements. The elements of the array are
     * not destroyed. The method has linear time complexity.
     *
     * @param <K>
     *            the type of keys maintained by the heap
     * @param array
     *            an array of elements
     * @param comparator
     *            the comparator to use
     * @return a heap
     * @throws IllegalArgumentException
     *             in case the array is null
     */
    @LinearTime
    public static <K> MinMaxBinaryArrayDoubleEndedHeap<K> heapify(K[] array, Comparator<? super K> comparator) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            return new MinMaxBinaryArrayDoubleEndedHeap<K>(comparator);
        }

        MinMaxBinaryArrayDoubleEndedHeap<K> h = new MinMaxBinaryArrayDoubleEndedHeap<K>(comparator, array.length);

        System.arraycopy(array, 0, h.array, 1, array.length);
        h.size = array.length;

        for (int i = array.length / 2; i > 0; i--) {
            h.fixdownWithComparator(i);
        }

        return h;
    }

    /**
     * Ensure that the array representation has the necessary capacity.
     * 
     * @param capacity
     *            the requested capacity
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void ensureCapacity(int capacity) {
        checkCapacity(capacity);
        K[] newArray = (K[]) new Object[capacity + 1];
        System.arraycopy(array, 1, newArray, 1, size);
        array = newArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public K findMax() {
        switch (size) {
        case 0:
            throw new NoSuchElementException();
        case 1:
            return array[1];
        case 2:
            return array[2];
        default:
            if (comparator == null) {
                if (((Comparable<? super K>) array[3]).compareTo(array[2]) > 0) {
                    return array[3];
                } else {
                    return array[2];
                }
            } else {
                if (comparator.compare(array[3], array[2]) > 0) {
                    return array[3];
                } else {
                    return array[2];
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public K deleteMax() {
        K result;
        switch (size) {
        case 0:
            throw new NoSuchElementException();
        case 1:
            result = array[1];
            array[1] = null;
            size--;
            break;
        case 2:
            result = array[2];
            array[2] = null;
            size--;
            break;
        default:
            if (comparator == null) {
                if (((Comparable<? super K>) array[3]).compareTo(array[2]) > 0) {
                    result = array[3];
                    array[3] = array[size];
                    array[size] = null;
                    size--;
                    if (size >= 3) {
                        fixdownMax(3);
                    }
                } else {
                    result = array[2];
                    array[2] = array[size];
                    array[size] = null;
                    size--;
                    fixdownMax(2);
                }
            } else {
                if (comparator.compare(array[3], array[2]) > 0) {
                    result = array[3];
                    array[3] = array[size];
                    array[size] = null;
                    size--;
                    if (size >= 3) {
                        fixdownMaxWithComparator(3);
                    }
                } else {
                    result = array[2];
                    array[2] = array[size];
                    array[size] = null;
                    size--;
                    fixdownMaxWithComparator(2);
                }
            }
            break;
        }

        if (Constants.NOT_BENCHMARK) {
            if (2 * minCapacity < array.length - 1 && 4 * size < array.length - 1) {
                ensureCapacity((array.length - 1) / 2);
            }
        }

        return result;
    }

    /**
     * Upwards fix starting from a particular element
     * 
     * @param k
     *            the index of the starting element
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void fixup(int k) {
        if (onMinLevel(k)) {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && ((Comparable<? super K>) array[p]).compareTo(kValue) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMax(p);
            } else {
                fixupMin(k);
            }
        } else {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && ((Comparable<? super K>) kValue).compareTo(array[p]) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMin(p);
            } else {
                fixupMax(k);
            }
        }
    }

    /**
     * Upwards fix starting from a particular element
     * 
     * @param k
     *            the index of the starting element
     */
    protected void fixupWithComparator(int k) {
        if (onMinLevel(k)) {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && comparator.compare(array[p], kValue) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMaxWithComparator(p);
            } else {
                fixupMinWithComparator(k);
            }
        } else {
            int p = k / 2;
            K kValue = array[k];
            if (p > 0 && comparator.compare(kValue, array[p]) < 0) {
                array[k] = array[p];
                array[p] = kValue;
                fixupMinWithComparator(p);
            } else {
                fixupMaxWithComparator(k);
            }
        }
    }

    /**
     * Upwards fix starting from a particular element at a minimum level
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixupMin(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && ((Comparable<? super K>) array[gp]).compareTo(key) > 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Upwards fix starting from a particular element at a minimum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixupMinWithComparator(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && comparator.compare(array[gp], key) > 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Upwards fix starting from a particular element at a maximum level
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixupMax(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && ((Comparable<? super K>) array[gp]).compareTo(key) < 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Upwards fix starting from a particular element at a maximum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixupMaxWithComparator(int k) {
        K key = array[k];
        int gp = k / 4;
        while (gp > 0 && comparator.compare(array[gp], key) < 0) {
            array[k] = array[gp];
            k = gp;
            gp = k / 4;
        }
        array[k] = key;
    }

    /**
     * Downwards fix starting from a particular element.
     * 
     * @param k
     *            the index of the starting element
     */
    @Override
    protected void fixdown(int k) {
        if (onMinLevel(k)) {
            fixdownMin(k);
        } else {
            fixdownMax(k);
        }
    }

    /**
     * Downwards fix starting from a particular element. Performs comparisons
     * using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    @Override
    protected void fixdownWithComparator(int k) {
        if (onMinLevel(k)) {
            fixdownMinWithComparator(k);
        } else {
            fixdownMaxWithComparator(k);
        }
    }

    /**
     * Downwards fix starting from a particular element at a minimum level.
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixdownMin(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = minChildOrGrandchild(k);
            if (m > c + 1) { // grandchild
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) >= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (((Comparable<? super K>) array[m]).compareTo(array[m / 2]) > 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) < 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Downwards fix starting from a particular element at a minimum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixdownMinWithComparator(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = minChildOrGrandchildWithComparator(k);
            if (m > c + 1) { // grandchild
                if (comparator.compare(array[m], array[k]) >= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (comparator.compare(array[m], array[m / 2]) > 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (comparator.compare(array[m], array[k]) < 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Downwards fix starting from a particular element at a maximum level.
     * 
     * @param k
     *            the index of the starting element
     */
    @SuppressWarnings("unchecked")
    private void fixdownMax(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = maxChildOrGrandchild(k);
            if (m > c + 1) { // grandchild
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) <= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (((Comparable<? super K>) array[m]).compareTo(array[m / 2]) < 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (((Comparable<? super K>) array[m]).compareTo(array[k]) > 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Downwards fix starting from a particular element at a maximum level.
     * Performs comparisons using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    private void fixdownMaxWithComparator(int k) {
        int c = 2 * k;
        while (c <= size) {
            int m = maxChildOrGrandchildWithComparator(k);
            if (m > c + 1) { // grandchild
                if (comparator.compare(array[m], array[k]) <= 0) {
                    break;
                }
                K tmp = array[k];
                array[k] = array[m];
                array[m] = tmp;
                if (comparator.compare(array[m], array[m / 2]) < 0) {
                    tmp = array[m];
                    array[m] = array[m / 2];
                    array[m / 2] = tmp;
                }
                // go down
                k = m;
                c = 2 * k;
            } else { // child
                if (comparator.compare(array[m], array[k]) > 0) {
                    K tmp = array[k];
                    array[k] = array[m];
                    array[m] = tmp;
                }
                break;
            }
        }
    }

    /**
     * Return true if on a minimum level, false otherwise.
     * 
     * @param k
     *            the element
     * @return true if on a minimum level, false otherwise
     */
    @VisibleForTesting
    boolean onMinLevel(int k) {
        float kAsFloat = k;
        int exponent = Math.getExponent(kAsFloat);
        return exponent % 2 == 0;
    }

    /**
     * Given a node at a maximum level, find its child or grandchild with the
     * maximum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a maximum level
     * @return the child or grandchild with a maximum key, or undefined if there
     *         are no children
     */
    @SuppressWarnings("unchecked")
    private int maxChildOrGrandchild(int k) {
        int gc = 4 * k;
        int maxgc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            maxgc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            maxgc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        }

        // 0 grandchildren
        maxgc = 2 * k;
        gcValue = array[maxgc];
        if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) > 0) {
            maxgc = 2 * k + 1;
        }
        return maxgc;
    }

    /**
     * Given a node at a maximum level, find its child or grandchild with the
     * maximum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a maximum level
     * @return the child or grandchild with a maximum key, or undefined if there
     *         are no children
     */
    private int maxChildOrGrandchildWithComparator(int k) {
        int gc = 4 * k;
        int maxgc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            maxgc = gc;
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            maxgc = gc;
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) > 0) {
                maxgc = gc;
            }
            return maxgc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (comparator.compare(array[++gc], gcValue) > 0) {
                gcValue = array[gc];
                maxgc = gc;
            }
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            maxgc = gc;
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) > 0) {
                maxgc = 2 * k + 1;
            }
            return maxgc;
        }

        // 0 grandchildren
        maxgc = 2 * k;
        gcValue = array[maxgc];
        if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) > 0) {
            maxgc = 2 * k + 1;
        }
        return maxgc;
    }

    /**
     * Given a node at a minimum level, find its child or grandchild with the
     * minimum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a minimum level
     * @return the child or grandchild with a minimum key, or undefined if there
     *         are no children
     */
    @SuppressWarnings("unchecked")
    private int minChildOrGrandchild(int k) {
        int gc = 4 * k;
        int mingc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            mingc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            mingc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (((Comparable<? super K>) array[++gc]).compareTo(gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        }

        // 0 grandchildren
        mingc = 2 * k;
        gcValue = array[mingc];
        if (2 * k + 1 <= size && ((Comparable<? super K>) array[2 * k + 1]).compareTo(gcValue) < 0) {
            mingc = 2 * k + 1;
        }
        return mingc;
    }

    /**
     * Given a node at a minimum level, find its child or grandchild with the
     * minimum key. This method should not be called for a node which has no
     * children.
     * 
     * @param k
     *            a node at a minimum level
     * @return the child or grandchild with a minimum key, or undefined if there
     *         are no children
     */
    private int minChildOrGrandchildWithComparator(int k) {
        int gc = 4 * k;
        int mingc;
        K gcValue;

        // 4 grandchilden
        if (gc + 3 <= size) {
            gcValue = array[gc];
            mingc = gc;
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        }

        // less or equal to 3
        switch (size - gc) {
        case 2:
            // 3 grandchildren, two children
            gcValue = array[gc];
            mingc = gc;
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (comparator.compare(array[++gc], gcValue) < 0) {
                mingc = gc;
            }
            return mingc;
        case 1:
            // 2 grandchildren, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (comparator.compare(array[++gc], gcValue) < 0) {
                gcValue = array[gc];
                mingc = gc;
            }
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        case 0:
            // 1 grandchild, maybe two children
            gcValue = array[gc];
            mingc = gc;
            if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) < 0) {
                mingc = 2 * k + 1;
            }
            return mingc;
        }

        // 0 grandchildren
        mingc = 2 * k;
        gcValue = array[mingc];
        if (2 * k + 1 <= size && comparator.compare(array[2 * k + 1], gcValue) < 0) {
            mingc = 2 * k + 1;
        }
        return mingc;
    }

}
