package org.jheaps.array;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.Constants;
import org.jheaps.ValueHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * An optimized array-based binary heap with integer keys.
 *
 * <p>
 * This is a highly optimized implementation which uses (a) the Wegener
 * bottom-up heuristic and (b) sentinel values. The implementation uses an array
 * in order to store the elements, providing amortized O(log(n)) time for the
 * {@code insert} and {@code deleteMin} operations. Operation {@code findMin},
 * is a worst-case O(1) operation. All bounds are worst-case if the user
 * initializes the heap with a capacity larger or equal to the total number of
 * elements that are going to be inserted into the heap.
 * 
 * <p>
 * See the following papers for details about the optimizations:
 * <ul>
 * <li>Ingo Wegener. BOTTOM-UP-HEAPSORT, a new variant of HEAPSORT beating, on
 * an average, QUICKSORT (if n is not very small). Theoretical Computer Science,
 * 118(1), 81--98, 1993.</li>
 * <li>Peter Sanders. Fast Priority Queues for Cached Memory. Algorithms
 * Engineering and Experiments (ALENEX), 312--327, 1999.</li>
 * </ul>
 * 
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a heap concurrently, and at least one of the threads
 * modifies the heap structurally, it <em>must</em> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or more
 * elements or changing the key of some element.) This is typically accomplished
 * by synchronizing on some object that naturally encapsulates the heap.
 *
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public class BinaryArrayIntegerValueHeap<V> implements ValueHeap<Integer, V>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default initial capacity of the heap.
     */
    public static final int DEFAULT_HEAP_CAPACITY = 16;

    /**
     * Supremum
     */
    private static final int SUP_KEY = Integer.MAX_VALUE;

    /**
     * Infimum
     */
    private static final int INF_KEY = Integer.MIN_VALUE;

    /**
     * The maximum heap capacity.
     */
    private static final int MAX_HEAP_CAPACITY = Integer.MAX_VALUE - 8 - 1;

    /**
     * The minimum heap capacity.
     */
    private static final int MIN_HEAP_CAPACITY = 0;

    /**
     * The array used for representing the heap.
     */
    private Elem<V>[] array;

    /**
     * Number of elements in the heap.
     */
    private int size;

    /**
     * Minimum capacity due to initially requested capacity.
     */
    private int minCapacity;

    /**
     * Constructs a new, empty heap, using the natural ordering of its keys.
     *
     * <p>
     * The initial capacity of the heap is {@link #DEFAULT_HEAP_CAPACITY} and
     * adjusts automatically based on the sequence of insertions and deletions.
     */
    public BinaryArrayIntegerValueHeap() {
        this(DEFAULT_HEAP_CAPACITY);
    }

    /**
     * Constructs a new, empty heap, with a provided initial capacity using the
     * natural ordering of its keys.
     *
     * <p>
     * The initial capacity of the heap is provided by the user and is adjusted
     * automatically based on the sequence of insertions and deletions. The
     * capacity will never become smaller than the initial requested capacity.
     *
     * @param capacity
     *            the initial heap capacity
     */
    @SuppressWarnings("unchecked")
    public BinaryArrayIntegerValueHeap(int capacity) {
        checkCapacity(capacity);
        this.minCapacity = Math.max(capacity, DEFAULT_HEAP_CAPACITY);
        this.array = (Elem<V>[]) Array.newInstance(Elem.class, minCapacity + 2);
        this.array[0] = new Elem<V>(INF_KEY, null);
        for (int i = 1; i < minCapacity + 2; i++) {
            this.array[i] = new Elem<V>(SUP_KEY, null);
        }
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
    @ConstantTime
    public void clear() {
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<? super Integer> comparator() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public Integer findMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }
        return array[1].key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ConstantTime
    public V findMinValue() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }
        return array[1].value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime
    public void insert(Integer key, V value) {
        if (Constants.NOT_BENCHMARK) {
            if (key == null) {
                throw new NullPointerException("Null keys not permitted");
            }
            // make space if needed
            if (size == array.length - 2) {
                if (array.length == 2) {
                    ensureCapacity(1);
                } else {
                    ensureCapacity(2 * (array.length - 2));
                }
            }
        }

        ++size;
        int hole = size;
        int pred = hole >> 1;
        Elem<V> predElem = array[pred];

        while (predElem.key > key) {
            array[hole].key = predElem.key;
            array[hole].value = predElem.value;

            hole = pred;
            pred >>= 1;
            predElem = array[pred];
        }

        array[hole].key = key;
        array[hole].value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime
    public void insert(Integer key) {
        insert(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogarithmicTime
    public Integer deleteMin() {
        if (Constants.NOT_BENCHMARK && size == 0) {
            throw new NoSuchElementException();
        }

        Integer result = array[1].key;

        // first move up elements on a min-path
        int hole = 1;
        int succ = 2;
        int sz = size;
        while (succ < sz) {
            int key1 = array[succ].key;
            int key2 = array[succ + 1].key;
            if (key1 > key2) {
                succ++;
                array[hole].key = key2;
                array[hole].value = array[succ].value;
            } else {
                array[hole].key = key1;
                array[hole].value = array[succ].value;
            }
            hole = succ;
            succ <<= 1;
        }

        // bubble up rightmost element
        int bubble = array[sz].key;
        int pred = hole >> 1;
        while (array[pred].key > bubble) {
            array[hole].key = array[pred].key;
            array[hole].value = array[pred].value;
            hole = pred;
            pred >>= 1;
        }

        // finally move data to hole
        array[hole].key = bubble;
        array[hole].value = array[sz].value;

        array[size].key = SUP_KEY;
        array[size].value = null;
        size = sz - 1;

        if (Constants.NOT_BENCHMARK) {
            // free unused space
            int currentCapacity = array.length - 2;
            if (2 * minCapacity <= currentCapacity && 4 * size < currentCapacity) {
                ensureCapacity(currentCapacity / 2);
            }
        }

        return result;
    }

    /**
     * Ensure that the array representation has the necessary capacity.
     * 
     * @param capacity
     *            the requested capacity
     */
    @SuppressWarnings("unchecked")
    private void ensureCapacity(int capacity) {
        checkCapacity(capacity);
        Elem<V>[] newArray = (Elem<V>[]) Array.newInstance(Elem.class, capacity + 2);
        if (newArray.length >= array.length) {
            System.arraycopy(array, 0, newArray, 0, array.length);
            for (int i = array.length; i < newArray.length; i++) {
                newArray[i] = new Elem<V>(SUP_KEY, null);
            }
        } else {
            System.arraycopy(array, 0, newArray, 0, newArray.length);
        }
        array = newArray;
    }

    /**
     * Check that a capacity is valid.
     * 
     * @param capacity
     *            the capacity
     * 
     * @throws IllegalArgumentException
     *             if the capacity is negative or more than the maximum array
     *             size
     */
    private void checkCapacity(int capacity) {
        if (capacity < MIN_HEAP_CAPACITY) {
            throw new IllegalArgumentException("Heap capacity must be >= " + MIN_HEAP_CAPACITY);
        }
        if (capacity > MAX_HEAP_CAPACITY) {
            throw new IllegalArgumentException("Heap capacity too large");
        }
    }

    private static class Elem<V> implements Serializable {

        private static final long serialVersionUID = 1L;

        int key;
        V value;

        public Elem(Integer key, V value) {
            this.key = key;
            this.value = value;
        }
    }

}
