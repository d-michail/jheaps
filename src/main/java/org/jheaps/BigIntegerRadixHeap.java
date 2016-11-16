/*
 * (C) Copyright 2014-2016, by Dimitrios Michail
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
package org.jheaps;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An implicit radix heap implementation of the {@link MapHeap} interface. The
 * heap stores long keys sorted according to the {@linkplain Comparable natural
 * ordering} of its keys. A radix heap is a monotone heap, especially designed
 * for algorithms (such as Dijkstra) which scan elements in order of
 * nondecreasing keys.
 *
 * <p>
 * Implicit implementations of a heap use arrays in order to store the elements.
 * Operations {@code insert} and {@code findMin} are worst-case constant time.
 * The cost of operation {@code deleteMin} is amortized O(logC) assuming the
 * radix-heap contains keys in the range {@literal [0, C]} or equivalently
 * {@literal [a,a+C]}.
 * 
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a heap concurrently, and at least one of the threads
 * modifies the heap structurally, it <em>must</em> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or more
 * elements or changing the key of some element.) This is typically accomplished
 * by synchronizing on some object that naturally encapsulates the heap.
 *
 * @author Dimitrios Michail
 * 
 * @see MapHeap
 * @see Serializable
 */
public class BigIntegerRadixHeap<V> implements MapHeap<BigInteger, V>, Serializable {

	private final static long serialVersionUID = 1;

	/**
	 * The buckets as lists. We use array-lists instead of linked-lists, to be
	 * cache friendly.
	 */
	private List<Entry<BigInteger, V>>[] buckets;

	/**
	 * Number of elements
	 */
	private long size;

	/**
	 * Minimum key allowed
	 */
	private BigInteger minKey;

	/**
	 * Maximum key allowed
	 */
	private BigInteger maxKey;

	/**
	 * The current minimum value
	 */
	private Entry<BigInteger, V> currentMin;

	/**
	 * The bucket of the current minimum value
	 */
	private int currentMinBucket;

	/**
	 * Constructs a new heap which can store values between a minimum and a
	 * maximum key value (inclusive).
	 * 
	 * It is important to use the smallest key range as the heap uses O(logC)
	 * where C=maxKey-minKey+1 buckets to store elements. Moreover, the
	 * operation {@code deleteMin} requires amortized O(logC) time.
	 * 
	 * @param minKey
	 *            the non-negative minimum key that this heap supports
	 *            (inclusive)
	 * @param maxKey
	 *            the maximum key that this heap supports (inclusive)
	 * @throws IllegalArgumentException
	 *             if the minimum key is negative
	 * @throws IllegalArgumentException
	 *             if the maximum key is less than the minimum key
	 */
	@SuppressWarnings("unchecked")
	public BigIntegerRadixHeap(BigInteger minKey, BigInteger maxKey) {
		if (minKey == null) {
			throw new IllegalArgumentException("Minimum key cannot be null");
		}
		if (minKey.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentException("Minimum key must be non-negative");
		}
		this.minKey = minKey;
		if (maxKey == null) {
			throw new IllegalArgumentException("Maximum key cannot be null");
		}
		if (maxKey.compareTo(minKey) < 0) {
			throw new IllegalArgumentException("Maximum key cannot be less than the minimum");
		}
		this.maxKey = maxKey;

		// compute number of buckets
		int numBuckets = 2 + maxKey.subtract(minKey).add(BigInteger.ONE).bitLength();

		// construct representation
		this.buckets = (List<Entry<BigInteger, V>>[]) Array.newInstance(List.class, numBuckets);
		for (int i = 0; i < this.buckets.length; i++) {
			buckets[i] = new ArrayList<Entry<BigInteger, V>>();
		}
		this.size = 0;
		this.currentMin = null;
		this.currentMinBucket = 0;
	}

	/**
	 * Always returns {@code null} since this heap uses the
	 * {@linkplain Comparable natural ordering} of its keys.
	 * 
	 * @return {@code null} since this heap uses the natural ordering of its
	 *         keys
	 */
	@Override
	public Comparator<? super BigInteger> comparator() {
		return null;
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
	public void insert(BigInteger key, V value) {
		if (key == null) {
			throw new IllegalArgumentException("Null keys not permitted");
		}
		if (key.compareTo(minKey) < 0) {
			throw new IllegalArgumentException("Key is less than the minimum allowed key");
		}
		if (key.compareTo(maxKey) > 0) {
			throw new IllegalArgumentException("Key is more than the maximum allowed key");
		}

		Entry<BigInteger, V> p = new DefaultMapHeapEntryImpl<BigInteger, V>(key, value);
		if (size == 0) {
			buckets[0].add(p);
			currentMin = p;
			currentMinBucket = 0;
		} else {
			if (key.compareTo(currentMin.getKey()) < 0) {
				throw new IllegalArgumentException("Invalid key. Monotone heap.");
			}
			// here key >= min
			int b = 1 + Math.min(msd(currentMin.getKey(), key), buckets.length - 2);
			buckets[b].add(p);
		}
		size++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public Entry<BigInteger, V> findMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return currentMin;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The cost of this operation is amortized O(logC) assuming the heap
	 * contains keys in the range [0, C] or equivalently [a, a+C].
	 */
	@Override
	@ConstantTime(amortized = true)
	public Entry<BigInteger, V> deleteMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		List<Entry<BigInteger, V>> b = buckets[currentMinBucket];
		int bSize = b.size();

		Entry<BigInteger, V> result = currentMin;
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
			Entry<BigInteger, V> min = null;
			Entry<BigInteger, V> secondMin = null;
			for (Entry<BigInteger, V> val : b) {
				// track position of current minimum
				if (currentMin == val) {
					minPos = pos;
				}
				// track minimum and second minimum values
				if (min == null || val.getKey().compareTo(min.getKey()) < 0) {
					secondMin = min;
					min = val;
				} else if (secondMin == null || val.getKey().compareTo(secondMin.getKey()) < 0) {
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
			for (Entry<BigInteger, V> val : b) {
				if (pos != minPos) {
					int newBucket = 1 + Math.min(msd(secondMin.getKey(), val.getKey()), buckets.length - 2);
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
	 * {@inheritDoc}
	 */
	@Override
	public Heap<BigInteger> asHeap() {
		return new MapHeapAsHeapAdapter<BigInteger, V>(this);
	}

	/**
	 * Find the current minimum starting searching from a specified bucket.
	 */
	private void updateMin(int startBucket) {
		if (size > 0) {
			for (int i = startBucket; i < this.buckets.length; i++) {
				if (buckets[i].size() > 0) {
					if (i == 0) {
						currentMin = buckets[i].get(buckets[i].size() - 1);
					} else {
						Entry<BigInteger, V> min = null;
						for (Entry<BigInteger, V> val : buckets[i]) {
							if (min == null || val.getKey().compareTo(min.getKey()) < 0) {
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
	private int msd(BigInteger a, BigInteger b) {
		if (a.equals(b)) {
			return -1;
		}
		/*
		 * return floor(log_2(a xor b)).
		 */
		return a.xor(b).bitLength() - 1;
	}

}
