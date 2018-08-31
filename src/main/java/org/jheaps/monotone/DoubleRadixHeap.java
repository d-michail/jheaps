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
package org.jheaps.monotone;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A radix heap for double keys. The heap stores double keys sorted according to
 * the {@linkplain Comparable natural ordering} of its keys. A radix heap is a
 * monotone heap, especially designed for algorithms (such as Dijkstra) which
 * scan elements in order of nondecreasing keys.
 *
 * <p>
 * Note that this implementation uses the fact that the IEEE floating-point
 * standard has the property that for any valid floating-point numbers a and b,
 * {@literal a<=b} if and only if {@literal bits(a)<= bits(b)}, where
 * {@literal bits(x)} denotes the re-interpretation of x as an unsigned integer
 * (long in our case).
 *
 * <p>
 * This implementation uses arrays in order to store the elements. Operations
 * {@code insert} and {@code findMin} are worst-case constant time. The cost of
 * operation {@code deleteMin} is amortized O(logC) assuming the radix-heap
 * contains keys in the range {@literal [0, C]} or equivalently
 * {@literal [a,a+C]}. Note, however, that C here depends on the distance of the
 * minimum and maximum value when they are translated into unsigned longs.
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
 */
public class DoubleRadixHeap extends AbstractRadixHeap<Double> {

    private final static long serialVersionUID = 1;

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
    public DoubleRadixHeap(double minKey, double maxKey) {
        super();
        if (!Double.isFinite(minKey) || minKey < 0.0) {
            throw new IllegalArgumentException("Minimum key must be finite and non-negative");
        }
        this.minKey = minKey;
        this.lastDeletedKey = minKey;        
        if (!Double.isFinite(maxKey) || maxKey < minKey) {
            throw new IllegalArgumentException("Maximum key must be finite and not less than the minimum");
        }
        this.maxKey = maxKey;

        // compute number of buckets
        BigInteger minKeyAsBigInt = UnsignedUtils.unsignedLongToBigInt(Double.doubleToLongBits(minKey));
        BigInteger maxKeyAsBigInt = UnsignedUtils.unsignedLongToBigInt(Double.doubleToLongBits(maxKey));
        BigInteger diff = maxKeyAsBigInt.subtract(minKeyAsBigInt);
        int numBuckets = 2 + 1 + diff.bitLength();

        // construct representation
        this.buckets = (List<Double>[]) Array.newInstance(List.class, numBuckets);
        for (int i = 0; i < this.buckets.length; i++) {
            buckets[i] = new ArrayList<Double>();
        }
        this.size = 0;
        this.currentMin = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int compare(Double o1, Double o2) {
        /*
         * Convert to IEEE and compare as unsigned
         */
        long x = Double.doubleToLongBits(o1) ^ Long.MIN_VALUE;
        long y = Double.doubleToLongBits(o2) ^ Long.MIN_VALUE;

        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int msd(Double a, Double b) {
        /*
         * For this to work, arithmetic must be unsigned
         */
        long ux = Double.doubleToLongBits(a);
        long uy = Double.doubleToLongBits(b);
        if (ux == uy) {
            return -1;
        }
        double d = UnsignedUtils.unsignedLongToDouble(ux ^ uy);
        return Math.getExponent(d);
    }

}
