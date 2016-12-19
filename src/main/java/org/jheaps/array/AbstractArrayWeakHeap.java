package org.jheaps.array;

import java.io.Serializable;
import java.util.Comparator;

import org.jheaps.Heap;
import org.jheaps.annotations.ConstantTime;

/**
 * An abstract weak heap with an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
abstract class AbstractArrayWeakHeap<K> implements Heap<K>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The maximum heap capacity.
     */
    protected static final int MAX_HEAP_CAPACITY = Integer.MAX_VALUE - 8 - 1;

    /**
     * The minimum heap capacity.
     */
    protected static final int MIN_HEAP_CAPACITY = 0;

    /**
     * Limit for the heap capacity when down-sizing.
     */
    protected static final int DOWNSIZING_MIN_HEAP_CAPACITY = 16;

    /**
     * The comparator used to maintain order in this heap, or null if it uses
     * the natural ordering of its keys.
     *
     * @serial
     */
    protected final Comparator<? super K> comparator;

    /**
     * The array used for representing the heap.
     */
    protected K[] array;

    /**
     * Number of elements in the heap.
     */
    protected int size;

    /**
     * Minimum capacity due to initially requested capacity.
     */
    protected final int minCapacity;

    /**
     * Constructor
     * 
     * @param comparator
     *            the comparator to use
     * @param capacity
     *            the requested capacity
     */
    public AbstractArrayWeakHeap(Comparator<? super K> comparator, int capacity) {
        checkCapacity(capacity);
        this.size = 0;
        this.comparator = comparator;
        this.minCapacity = Math.max(capacity, DOWNSIZING_MIN_HEAP_CAPACITY);
        initCapacity(minCapacity);
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
        size = 0;
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
    protected final void checkCapacity(int capacity) {
        if (capacity < MIN_HEAP_CAPACITY) {
            throw new IllegalArgumentException("Heap capacity must be >= " + MIN_HEAP_CAPACITY);
        }
        if (capacity > MAX_HEAP_CAPACITY) {
            throw new IllegalArgumentException("Heap capacity too large");
        }
    }

    /**
     * Initialize array representation.
     * 
     * @param capacity
     *            the capacity
     */
    protected abstract void initCapacity(int capacity);

    /**
     * Make sure the array representation can hold a certain number of elements.
     * 
     * @param capacity
     *            the capacity
     */
    protected abstract void ensureCapacity(int capacity);

    /**
     * Upwards fix starting from a particular element
     * 
     * @param k
     *            the index of the starting element
     */
    protected abstract void fixup(int k);

    /**
     * Upwards fix starting from a particular element. Performs comparisons
     * using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    protected abstract void fixupWithComparator(int k);

    /**
     * Downwards fix starting from a particular element
     * 
     * @param k
     *            the index of the starting element
     */
    protected abstract void fixdown(int k);

    /**
     * Downwards fix starting from a particular element. Performs comparisons
     * using the comparator.
     * 
     * @param k
     *            the index of the starting element
     */
    protected abstract void fixdownWithComparator(int k);

}
