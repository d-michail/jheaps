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
 * A heap that allows melding with another heap.
 *
 * The second heap is destroyed (becomes empty) after the meld operation. A
 * {@code ClassCastException} will be thrown if the two heaps are not of the
 * same type. Moreover, the two heaps need to use the same comparators. If only
 * one of them uses a custom comparator or both use custom comparators but are
 * not the same by <em>equals</em>, an {@code IllegalArgumentException} is
 * thrown.
 *
 * @author Dimitrios Michail
 * @param <K>
 *            the type of keys maintained by this heap
 */
public interface MergeableHeap<K> {

	/**
	 * Meld a heap into the current heap.
	 *
	 * After the operation the {@code other} heap will be empty.
	 *
	 * @param other
	 *            a merge-able heap
	 * @throws ClassCastException
	 *             if {@code other} is not compatible with this heap
	 * @throws IllegalArgumentException
	 *             if {@code other} does not have a compatible comparator.
	 */
	void meld(MergeableHeap<K> other);

}
