package org.jheaps.monotone;

import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.annotations.ConstantTime;

/**
 * Base abstract implementation of an addressable radix heap.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 * 
 */
abstract class AbstractRadixAddressableHeap<K, V> implements AddressableHeap<K, V>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Denotes that a list node does not belong to a bucket list.
	 */
	private static final int NO_BUCKET = -1;

	/**
	 * The buckets as lists.
	 */
	protected Node[] buckets;

	/**
	 * Number of elements
	 */
	protected long size;

	/**
	 * The current minimum value
	 */
	protected Node currentMin;

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
	AbstractRadixAddressableHeap() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public Handle<K, V> findMin() {
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
	public Handle<K, V> insert(K key) {
		return insert(key, null);
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
	public Handle<K, V> insert(K key, V value) {
		if (key == null) {
			throw new IllegalArgumentException("Null keys not permitted");
		}
		if (compare(key, minKey) < 0) {
			throw new IllegalArgumentException("Key is less than the minimum allowed key");
		}
		if (compare(key, maxKey) > 0) {
			throw new IllegalArgumentException("Key is more than the maximum allowed key");
		}

		Node p = new Node(key, value);
		if (size == 0) {
			p.bucket = 0;
			buckets[0] = p;
			currentMin = p;
			currentMinBucket = 0;
		} else {
			if (compare(key, currentMin.getKey()) < 0) {
				throw new IllegalArgumentException("Invalid key. Monotone heap.");
			}
			int b = computeBucket(key, currentMin.getKey());
			p.bucket = b;
			if (buckets[b] == null) {
				buckets[b] = p;
			} else {
				buckets[b].prev = p;
				p.next = buckets[b];
				buckets[b] = p;
			}
		}
		size++;
		return p;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The cost of this operation is amortized O(logC) assuming the heap
	 * contains keys in the range [0, C] or equivalently [a, a+C].
	 */
	@Override
	@ConstantTime(amortized = true)
	public Handle<K, V> deleteMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		Node b = buckets[currentMinBucket];
		Node result = currentMin;

		if (currentMinBucket == 0 || b.next == null) {
			if (b.next == null) {
				buckets[currentMinBucket] = null;
			} else {
				b.next.prev = null;
				buckets[currentMinBucket] = b.next;
			}
			b.next = null;
			b.bucket = NO_BUCKET;
			currentMin = null;
			size--;
			updateMin(currentMinBucket);
		} else {
			/*
			 * Find minimum and second minimum.
			 */
			Node min = null;
			Node secondMin = null;
			Node val = b;
			while (val != null) {
				// track minimum and second minimum values
				if (min == null || compare(val.getKey(), min.getKey()) < 0) {
					secondMin = min;
					min = val;
				} else if (secondMin == null || compare(val.getKey(), secondMin.getKey()) < 0) {
					secondMin = val;
				}
				val = val.next;
			}

			/*
			 * Redistribute all but minimum using second minimum.
			 */
			int minNewBucket = currentMinBucket;
			val = b;
			while (val != null) {
				Node nextVal = val.next;
				if (val != min) {
					int newBucket = computeBucket(val.getKey(), secondMin.getKey());
					if (newBucket == currentMinBucket) {
						throw new IllegalStateException("bug! Please contact the developers");
					}
					if (newBucket < minNewBucket) {
						minNewBucket = newBucket;
					}

					// add as first in bucket
					if (buckets[newBucket] == null) {
						buckets[newBucket] = val;
						val.next = null;
					} else {
						buckets[newBucket].prev = val;
						val.next = buckets[newBucket];
						buckets[newBucket] = val;
					}
					val.prev = null;
					val.bucket = newBucket;
				} else {
					val.bucket = NO_BUCKET;
					val.next = null;
					val.prev = null;
				}
				val = nextVal;
			}

			// empty bucket
			buckets[currentMinBucket] = null;

			// find current minimum
			currentMin = null;
			size--;
			updateMin(minNewBucket);
		}

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
			buckets[i] = null;
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
				if (buckets[i] != null) {
					if (i == 0) {
						currentMin = buckets[i];
					} else {
						Node min = null;
						Node val = buckets[i];
						while (val != null) {
							if (min == null || compare(val.getKey(), min.getKey()) < 0) {
								min = val;
							}
							val = val.next;
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
			Node n = buckets[i];
			while (n != null) {
				if (pos > 0) {
					sb.append(',');
				}
				sb.append(n.getKey());
				n = n.next;
				pos++;
			}
			sb.append(']');
		}
		return sb.toString();
	}

	// list node
	protected class Node implements Handle<K, V>, Serializable {

		private static final long serialVersionUID = 1L;

		K key;
		V value;
		Node next;
		Node prev;
		int bucket;

		public Node(K key, V value) {
			this.key = key;
			this.value = value;
			this.next = null;
			this.prev = null;
			this.bucket = NO_BUCKET;
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
		public void decreaseKey(K newKey) {
			if (bucket == NO_BUCKET || size == 0) {
				throw new IllegalArgumentException("Invalid handle!");
			}
			if (compare(newKey, currentMin.getKey()) < 0) {
				throw new IllegalArgumentException("Invalid key. Monotone heap.");
			}
			int c = compare(newKey, key);
			if (c > 0) {
				throw new IllegalArgumentException("Keys can only be decreased!");
			}
			key = newKey;
			if (c == 0) {
				return;
			}

			// find new bucket
			int newBucket = computeBucket(key, currentMin.getKey());
			if (newBucket == bucket) {
				return;
			}

			// remove from list
			Node head = buckets[bucket];
			if (next != null) {
				next.prev = prev;
			}
			if (prev != null) {
				prev.next = next;
			}
			if (head == this) {
				prev = null;
				buckets[bucket] = next;
			}

			// add to new list
			head = buckets[newBucket];
			if (buckets[newBucket] == null) {
				buckets[newBucket] = this;
				this.next = null;
			} else {
				buckets[newBucket].prev = this;
				this.next = buckets[newBucket];
				buckets[newBucket] = this;
			}
			this.prev = null;
			this.bucket = newBucket;
		}

		@Override
		public void delete() {
			if (bucket == NO_BUCKET || size == 0) {
				throw new IllegalArgumentException("Invalid handle!");
			}
			if (this == currentMin) {
				deleteMin();
				return;
			}

			// remove from list
			Node head = buckets[bucket];
			if (next != null) {
				next.prev = prev;
			}
			if (prev != null) {
				prev.next = next;
			}
			if (head == this) {
				buckets[bucket] = next;
			}
			prev = null;
			next = null;
			bucket = NO_BUCKET;
			size--;
		}
	}
}