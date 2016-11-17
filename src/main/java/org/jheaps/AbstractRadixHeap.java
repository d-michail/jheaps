package org.jheaps;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jheaps.annotations.ConstantTime;

/**
 * Base abstract implementation of a radix heap.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
abstract class AbstractRadixHeap<K, V> implements MapHeap<K, V>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The buckets as lists. We use array-lists instead of linked-lists, to be
	 * cache friendly.
	 */
	protected List<Entry<K, V>>[] buckets;

	/**
	 * Number of elements
	 */
	protected long size;

	/**
	 * The current minimum value
	 */
	protected Entry<K, V> currentMin;

	/**
	 * The bucket of the current minimum value
	 */
	protected int currentMinBucket;

	/**
	 * Minimum key allowed
	 */
	protected K minKey;

	/**
	 * Maximum key allowed
	 */
	protected K maxKey;

	/**
	 * Constructor
	 */
	AbstractRadixHeap() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public Entry<K, V> findMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return currentMin;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException
	 *             if the key is null
	 * @throws IllegalArgumentException
	 *             if the key is less than the minimum allowed key
	 * @throws IllegalArgumentException
	 *             if the key is more than the maximum allowed key
	 * @throws IllegalArgumentException
	 *             if the key is less than the current minimum
	 */
	@Override
	@ConstantTime
	public void insert(K key, V value) {
		if (key == null) {
			throw new IllegalArgumentException("Null keys not permitted");
		}
		if (compare(key, minKey) < 0) {
			throw new IllegalArgumentException("Key is less than the minimum allowed key");
		}
		if (compare(key, maxKey) > 0) {
			throw new IllegalArgumentException("Key is more than the maximum allowed key");
		}

		Entry<K, V> p = new DefaultMapHeapEntryImpl<K, V>(key, value);
		if (size == 0) {
			buckets[0].add(p);
			currentMin = p;
			currentMinBucket = 0;
		} else {
			if (compare(key, currentMin.getKey()) < 0) {
				throw new IllegalArgumentException("Invalid key. Monotone heap.");
			}
			buckets[computeBucket(key, currentMin.getKey())].add(p);
		}
		size++;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The cost of this operation is amortized O(logC) assuming the heap
	 * contains keys in the range [0, C] or equivalently [a, a+C].
	 */
	@Override
	@ConstantTime(amortized = true)
	public Entry<K, V> deleteMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		List<Entry<K, V>> b = buckets[currentMinBucket];
		int bSize = b.size();

		Entry<K, V> result = currentMin;
		if (currentMinBucket == 0 || bSize == 1) {
			b.remove(bSize - 1);
			updateMin(currentMinBucket);
		} else {
			/*
			 * Find minimum and second minimum. Be careful with cached Long
			 * values.
			 */
			int minPos = -1;
			int pos = 0;
			Entry<K, V> min = null;
			Entry<K, V> secondMin = null;
			for (Entry<K, V> val : b) {
				// track position of current minimum
				if (currentMin == val) {
					minPos = pos;
				}
				// track minimum and second minimum values
				if (min == null || compare(val.getKey(), min.getKey()) < 0) {
					secondMin = min;
					min = val;
				} else if (secondMin == null || compare(val.getKey(), secondMin.getKey()) < 0) {
					secondMin = val;
				}
				pos++;
			}

			/*
			 * Redistribute all but minimum using second minimum. On purpose
			 * with position of minimum since Longs are often cached.
			 */
			pos = 0;
			int minNewBucket = currentMinBucket;
			for (Entry<K, V> val : b) {
				if (pos != minPos) {
					int newBucket = computeBucket(val.getKey(), secondMin.getKey());
					if (newBucket == currentMinBucket) {
						throw new IllegalStateException("bug! Please contact the developers");
					}
					if (newBucket < minNewBucket) {
						minNewBucket = newBucket;
					}
					buckets[newBucket].add(val);
				}
				pos++;
			}

			// empty bucket
			b.clear();

			// find current minimum
			updateMin(minNewBucket);
		}

		size--;
		return result;
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
		for (int i = 0; i < buckets.length; i++) {
			buckets[i].clear();
		}
		size = 0;
		currentMin = null;
		currentMinBucket = 0;
	}

	/**
	 * Always returns {@code null} since this heap uses the
	 * {@linkplain Comparable natural ordering} of its keys.
	 * 
	 * @return {@code null} since this heap uses the natural ordering of its
	 *         keys
	 */
	@Override
	public Comparator<? super K> comparator() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Heap<K> asHeap() {
		return new MapHeapAsHeapAdapter<K, V>(this);
	}

	/**
	 * Compares its two arguments for order. Returns a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 *
	 * @param o1
	 *            the first object to be compared.
	 * @param o2
	 *            the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	protected abstract int compare(K o1, K o2);

	/**
	 * Compute the bucket of a key based on a minimum key.
	 * 
	 * @param key
	 *            the key
	 * @param minKey
	 *            the minimum key
	 * @return the bucket where the key should go
	 */
	protected int computeBucket(K key, K minKey) {
		return 1 + Math.min(msd(key, minKey), buckets.length - 2);
	}

	/**
	 * Compute the most significant digit which is different in the binary
	 * representation of two values, or -1 if numbers are equal.
	 * 
	 * @param a
	 *            the first value
	 * @param b
	 *            the second value
	 * @return the most significant digit which is different or -1 if numbers
	 *         are equal
	 */
	protected abstract int msd(K a, K b);

	/**
	 * Find the current minimum starting searching from a specified bucket.
	 */
	protected void updateMin(int startBucket) {
		if (size > 0) {
			for (int i = startBucket; i < this.buckets.length; i++) {
				if (buckets[i].size() > 0) {
					if (i == 0) {
						currentMin = buckets[i].get(buckets[i].size() - 1);
					} else {
						Entry<K, V> min = null;
						for (Entry<K, V> val : buckets[i]) {
							if (min == null || compare(val.getKey(), min.getKey()) < 0) {
								min = val;
							}
						}
						currentMin = min;
					}
					currentMinBucket = i;
					return;
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.buckets.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('[');
			int pos = 0;
			for (Entry<K, V> val : buckets[i]) {
				if (pos > 0) {
					sb.append(',');
				}
				sb.append(val);
				pos++;
			}
			sb.append(']');
		}
		return sb.toString();
	}

}