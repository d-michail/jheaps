package org.jheaps;

import java.io.Serializable;
import java.util.Comparator;

import org.jheaps.annotations.ConstantTime;

/**
 * An abstract weak heap with an array representation.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 */
public abstract class AbstractArrayWeakHeap<K> implements Heap<K>, Serializable {

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
	 * The comparator used to maintain order in this heap, or null if it uses
	 * the natural ordering of its keys.
	 *
	 * @serial
	 */
	protected Comparator<? super K> comparator;

	/**
	 * The array used for representing the heap.
	 */
	protected K[] array;

	/**
	 * Number of elements in the heap.
	 */
	protected int size;

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
		initCapacity(capacity);
		this.size = 0;
		this.comparator = comparator;
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

	protected final void checkCapacity(int capacity) {
		if (capacity < MIN_HEAP_CAPACITY) {
			throw new IllegalArgumentException("Heap capacity must be >= " + MIN_HEAP_CAPACITY);
		}
		if (capacity > MAX_HEAP_CAPACITY) {
			throw new IllegalArgumentException("Heap capacity too large");
		}
	}

	protected abstract void initCapacity(int capacity);

	protected abstract void ensureCapacity(int capacity);

	protected abstract void fixup(int k);

	protected abstract void fixupWithComparator(int k);

	protected abstract void fixdown(int k);

	protected abstract void fixdownWithComparator(int k);

}