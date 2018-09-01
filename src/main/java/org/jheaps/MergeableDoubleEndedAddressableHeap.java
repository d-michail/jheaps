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

/**
 * A double-ended addressable heap that allows melding with another double-ended
 * addressable heap.
 *
 * <p>
 * The second heap becomes empty and unusable after the meld operation, meaning
 * than further insertions are not possible and will throw an
 * {@link IllegalStateException}.
 *
 * <p>
 * A {@link ClassCastException} will be thrown if the two heaps are not of the
 * same type. Moreover, the two heaps need to use the same comparators. If only
 * one of them uses a custom comparator or both use custom comparators but are
 * not the same by <em>equals</em>, an {@code IllegalArgumentException} is
 * thrown.
 * 
 * <p>
 * Note that all running time bounds on mergeable heaps are valid assuming that
 * the user does not perform cascading melds on heaps such as:
 * 
 * <pre>
 * d.meld(e);
 * c.meld(d);
 * b.meld(c);
 * a.meld(b);
 * </pre>
 * 
 * The above scenario, although efficiently supported by using union-find with
 * path compression, invalidates the claimed bounds.
 *
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 * 
 * @author Dimitrios Michail
 */
public interface MergeableDoubleEndedAddressableHeap<K, V> extends DoubleEndedAddressableHeap<K, V> {

    /**
     * Meld a heap into the current heap.
     *
     * After the operation the {@code other} heap will be empty and will not
     * permit further insertions.
     *
     * @param other
     *            a merge-able heap
     * @throws ClassCastException
     *             if {@code other} is not compatible with this heap
     * @throws IllegalArgumentException
     *             if {@code other} does not have a compatible comparator
     */
    void meld(MergeableDoubleEndedAddressableHeap<K, V> other);

}
