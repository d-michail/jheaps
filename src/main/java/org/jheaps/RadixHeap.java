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
import java.util.ArrayList;
import java.util.List;

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
public class RadixHeap<V> extends AbstractRadixHeap<Long, V> {

	private final static long serialVersionUID = 1;

	/**
	 * Maximum number of buckets to handle the full range of values supported by
	 * a long number from zero to 2<sup>63</sup>-1.
	 */
	private final static int MAX_BUCKETS = 66;

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
	public RadixHeap(long minKey, long maxKey) {
		super();
		if (minKey < 0) {
			throw new IllegalArgumentException("Minimum key must be non-negative");
		}
		this.minKey = minKey;
		if (maxKey < minKey) {
			throw new IllegalArgumentException("Maximum key cannot be less than the minimum");
		}
		this.maxKey = maxKey;

		// compute number of buckets
		int numBuckets;
		if (minKey == 0 && maxKey == Long.MAX_VALUE) {
			numBuckets = MAX_BUCKETS;
		} else {
			numBuckets = 3 + (int) Math.floor(Math.log(maxKey - minKey + 1) / Math.log(2));
		}

		// construct representation
		this.buckets = (List<Entry<Long, V>>[]) Array.newInstance(List.class, numBuckets);
		for (int i = 0; i < this.buckets.length; i++) {
			buckets[i] = new ArrayList<Entry<Long, V>>();
		}
		this.size = 0;
		this.currentMin = null;
		this.currentMinBucket = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int compare(Long o1, Long o2) {
		if (o1 < o2) {
			return -1;
		} else if (o1 > o2) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int msd(Long a, Long b) {
		/*
		 * Value equal
		 */
		if (a.longValue() == b.longValue()) {
			return -1;
		}
		/*
		 * This is a fast way to compute floor(log_2(a xor b)).
		 */
		return Math.getExponent(a ^ b);
	}

}
