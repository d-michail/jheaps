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
package org.jheaps.tree;

import java.util.Comparator;

import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeapFactory;

/**
 * Reflected double ended heaps based on pairing heaps. The heap is sorted
 * according to the {@linkplain Comparable natural ordering} of its keys, or by
 * a {@link Comparator} provided at heap creation time, depending on which
 * constructor is used.
 * 
 * <p>
 * This class implements a general technique which uses two {@link PairingHeap}s
 * to implement a double ended heap, described in detail in the following
 * <a href="http://dx.doi.org/10.1016/S0020-0190(02)00501-X">paper</a>:
 * <ul>
 * <li>C. Makris, A. Tsakalidis, and K. Tsichlas. Reflected min-max heaps.
 * Information Processing Letters, 86(4), 209--214, 2003.</li>
 * </ul>
 * 
 * <p>
 * This implementation provides amortized O(log(n)) time cost for the
 * {@code insert}, {@code deleteMin}, {@code deleteMax}, {@code decreaseKey},
 * {@code increaseKey}, and {@code delete} operations. {@code findMin} and
 * {@code findMax} are worst-case O(1) operations. The operation {@code meld} is
 * amortized O(log(n)).
 * 
 * <p>
 * All the above bounds, however, assume that the user does not perform
 * cascading melds on heaps such as:
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
 * <p>
 * Note that the ordering maintained by a this heap, like any heap, and whether
 * or not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but this heap performs all
 * key comparisons using its {@code compareTo} (or {@code compare}) method, so
 * two keys that are deemed equal by this method are, from the standpoint of
 * this heap, equal. The behavior of a heap <em>is</em> well-defined even if its
 * ordering is inconsistent with {@code equals}; it just fails to obey the
 * general contract of the {@code AddressableHeap} interface.
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a heap concurrently, and at least one of the threads
 * modifies the heap structurally, it <em>must</em> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or more
 * elements or changing the key of some element.) This is typically accomplished
 * by synchronizing on some object that naturally encapsulates the heap.
 * 
 * @param <K>
 *            the type of keys maintained by this heap
 * @param <V>
 *            the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 */
public class ReflectedPairingHeap<K, V> extends ReflectedHeap<K, V> {

    private static final long serialVersionUID = 651281438828109106L;

    /**
     * Constructs a new, empty heap, using the natural ordering of its keys. All
     * keys inserted into the heap must implement the {@link Comparable}
     * interface. Furthermore, all such keys must be <em>mutually
     * comparable</em>: {@code k1.compareTo(k2)} must not throw a
     * {@code ClassCastException} for any keys {@code k1} and {@code k2} in the
     * heap. If the user attempts to put a key into the heap that violates this
     * constraint (for example, the user attempts to put a string key into a
     * heap whose keys are integers), the {@code insert(Object key)} call will
     * throw a {@code ClassCastException}.
     */
    public ReflectedPairingHeap() {
        this(null);
    }

    /**
     * Constructs a new, empty heap, ordered according to the given comparator.
     * All keys inserted into the heap must be <em>mutually comparable</em> by
     * the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys {@code k1}
     * and {@code k2} in the heap. If the user attempts to put a key into the
     * heap that violates this constraint, the {@code insert(Object key)} call
     * will throw a {@code ClassCastException}.
     *
     * @param comparator
     *            the comparator that will be used to order this heap. If
     *            {@code null}, the {@linkplain Comparable natural ordering} of
     *            the keys will be used.
     */
    public ReflectedPairingHeap(Comparator<? super K> comparator) {
        super(new Factory<K, V>(), comparator);
    }

    // underlying heap factory
    private static class Factory<K, V> implements AddressableHeapFactory<K, V> {

        @Override
        public AddressableHeap<K, V> get(Comparator<? super K> comparator) {
            return new PairingHeap<K, V>(comparator);
        }

    }

}
