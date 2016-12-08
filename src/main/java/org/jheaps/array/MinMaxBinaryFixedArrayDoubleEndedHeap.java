package org.jheaps.array;

import java.util.Comparator;

import org.jheaps.annotations.LinearTime;

/**
 * An array based binary MinMax heap with a maximum number of elements. The heap
 * is sorted according to the {@linkplain Comparable natural ordering} of its
 * keys, or by a {@link Comparator} provided at heap creation time, depending on
 * which constructor is used.
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
 * The implementation uses a fixed size array in order to store the elements,
 * providing worst case O(log(n)) time for the {@code insert},
 * {@code deleteMin}, and {@code deleteMax} operations. Operations
 * {@code findMin} and {@code findMax} are worst-case O(1).
 * {@link MinMaxBinaryArrayDoubleEndedHeap} provides a more dynamic
 * implementation in the expense of amortized complexity bounds.
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
public class MinMaxBinaryFixedArrayDoubleEndedHeap<K> extends AbstractBinaryArrayDoubleEndedHeap<K> {

    private static final long serialVersionUID = -3409705010080904056L;

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
     * The heap has a fixed maximum capacity. If the user attempts to insert
     * more elements than the maximum capacity, a {@code IllegalStateException}
     * will be thrown.
     *
     * @param capacity
     *            the capacity
     */
    public MinMaxBinaryFixedArrayDoubleEndedHeap(int capacity) {
        super(null, capacity);
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
     * The heap has a fixed maximum capacity. If the user attempts to insert
     * more elements than the maximum capacity, a {@code IllegalStateException}
     * will be thrown.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     * @param capacity
     *            the capacity
     */
    public MinMaxBinaryFixedArrayDoubleEndedHeap(Comparator<? super K> comparator, int capacity) {
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
    public static <K> MinMaxBinaryFixedArrayDoubleEndedHeap<K> heapify(K[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            return new MinMaxBinaryFixedArrayDoubleEndedHeap<K>(0);
        }

        MinMaxBinaryFixedArrayDoubleEndedHeap<K> h = new MinMaxBinaryFixedArrayDoubleEndedHeap<K>(array.length);

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
    public static <K> MinMaxBinaryFixedArrayDoubleEndedHeap<K> heapify(K[] array, Comparator<? super K> comparator) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        if (array.length == 0) {
            return new MinMaxBinaryFixedArrayDoubleEndedHeap<K>(comparator, 0);
        }

        MinMaxBinaryFixedArrayDoubleEndedHeap<K> h = new MinMaxBinaryFixedArrayDoubleEndedHeap<K>(comparator,
                array.length);

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
    protected void ensureCapacity(int capacity) {
        checkCapacity(capacity);
        if (capacity >= array.length) {
            throw new IllegalStateException("Data structure has no extra space");
        }
    }

}
