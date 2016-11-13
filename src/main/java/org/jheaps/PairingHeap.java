/*
 * (C) Copyright 2014-2016, by Dimitrios Michail.
 *
 * Java Heaps Library
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
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A pairing heap implementation of the {@link AddressableHeap} interface. The
 * heap is sorted according to the {@linkplain Comparable natural ordering} of
 * its keys, or by a {@link Comparator} provided at heap creation time,
 * depending on which constructor is used.
 *
 * <p>
 * This implementation provides amortized log(n) time cost for the
 * {@code insert}, {@code deleteMin}, and {@code decreaseKey} operations.
 * Operation {@code findMin}, is a worst-case O(1) operation. The algorithms are
 * based on the <a href="http://dx.doi.org/10.1007/BF01840439"> Pairing Heap
 * paper</a>. Pairing heaps are very efficient in practice, especially in
 * applications requiring the {@code decreaseKey} operation.
 *
 * <p>
 * Note that the ordering maintained by a pairing heap, like any heap, and
 * whether or not an explicit comparator is provided, must be <em>consistent
 * with {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but a pairing heap performs
 * all key comparisons using its {@code compareTo} (or {@code compare}) method,
 * so two keys that are deemed equal by this method are, from the standpoint of
 * the pairing heap, equal. The behavior of a heap <em>is</em> well-defined even
 * if its ordering is inconsistent with {@code equals}; it just fails to obey
 * the general contract of the {@code AddressableHeap} interface.
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
 *
 * @author Dimitrios Michail
 * 
 * @see AddressableHeap
 * @see MergeableHeap
 * @see Comparable
 * @see Comparator
 */
public class PairingHeap<K> implements AddressableHeap<K>, MergeableHeap<K>, Serializable {

	private final static long serialVersionUID = 1;

	/**
	 * The comparator used to maintain order in this heap, or null if it uses
	 * the natural ordering of its keys.
	 *
	 * @serial
	 */
	private final Comparator<? super K> comparator;

	/**
	 * The root of the pairing heap
	 */
	private PairingHandle root;

	/**
	 * Size of the pairing heap
	 */
	private long size;

	/**
	 * Constructs a new, empty pairing heap, using the natural ordering of its
	 * keys. All keys inserted into the heap must implement the
	 * {@link Comparable} interface. Furthermore, all such keys must be
	 * <em>mutually comparable</em>: {@code k1.compareTo(k2)} must not throw a
	 * {@code ClassCastException} for any keys {@code k1} and {@code k2} in the
	 * heap. If the user attempts to put a key into the heap that violates this
	 * constraint (for example, the user attempts to put a string key into a
	 * heap whose keys are integers), the {@code insert(Object key)} call will
	 * throw a {@code ClassCastException}.
	 */
	@ConstantTime
	public PairingHeap() {
		this(null);
	}

	/**
	 * Constructs a new, empty pairing heap, ordered according to the given
	 * comparator. All keys inserted into the heap must be <em>mutually
	 * comparable</em> by the given comparator: {@code comparator.compare(k1,
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
	@ConstantTime
	public PairingHeap(Comparator<? super K> comparator) {
		this.root = null;
		this.comparator = comparator;
		this.size = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@LogarithmicTime(amortized = true)
	public AddressableHeap.Handle<K> insert(K key) {
		if (key == null) {
			throw new NullPointerException("Null keys not permitted");
		}
		PairingHandle n = new PairingHandle(key);
		if (comparator == null) {
			root = link(root, n);
		} else {
			root = linkWithComparator(root, n);
		}
		size++;
		return n;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime(amortized = true)
	public AddressableHeap.Handle<K> findMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@LogarithmicTime(amortized = true)
	public void deleteMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		assert root.o_s == null && root.y_s == null;

		// cut all children, combine them and overwrite old root
		root = combine(cutChildren(root));

		// decrease size
		size--;
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
	@ConstantTime(amortized = true)
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@LogarithmicTime(amortized = true)
	public void meld(MergeableHeap<K> other) {
		PairingHeap<K> h = (PairingHeap<K>) other;

		// check same comparator
		if (comparator != null) {
			if (h.comparator == null || !h.comparator.equals(comparator)) {
				throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
			}
		} else if (h.comparator != null) {
			throw new IllegalArgumentException("Cannot meld heaps using different comparators!");
		}

		// perform the meld
		size += h.size;
		if (comparator == null) {
			root = link(root, h.root);
		} else {
			root = linkWithComparator(root, h.root);
		}

		// clear other
		h.size = 0;
		h.root = null;
	}

	// --------------------------------------------------------------------
	private class PairingHandle implements AddressableHeap.Handle<K>, Serializable {

		private final static long serialVersionUID = 1;

		K key;
		PairingHandle o_c; // older child
		PairingHandle y_s; // younger sibling
		PairingHandle o_s; // older sibling

		PairingHandle(K key) {
			this.key = key;
			o_c = y_s = o_s = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K getKey() {
			return key;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		@LogarithmicTime(amortized = true)
		public void decreaseKey(K newKey) {
			int c;
			if (comparator == null) {
				c = ((Comparable<? super K>) newKey).compareTo(key);
			} else {
				c = comparator.compare(newKey, key);
			}

			if (c > 0) {
				throw new IllegalArgumentException("Keys can only be decreased!");
			}
			key = newKey;
			if (c == 0 || root == this) {
				return;
			}

			if (o_s == null) {
				throw new IllegalArgumentException("Invalid handle!");
			}

			// unlink from parent
			if (y_s != null) {
				y_s.o_s = o_s;
			}
			if (o_s.o_c == this) { // I am the oldest :(
				o_s.o_c = y_s;
			} else { // I have an older sibling!
				o_s.y_s = y_s;
			}
			y_s = null;
			o_s = null;

			// merge with root
			if (comparator == null) {
				root = link(root, this);
			} else {
				root = linkWithComparator(root, this);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@LogarithmicTime(amortized = true)
		public void delete() {
			if (root == this) {
				deleteMin();
				o_c = y_s = o_s = null;
				return;
			}

			if (o_s == null) {
				throw new IllegalArgumentException("Invalid handle!");
			}

			// unlink from parent
			if (y_s != null) {
				y_s.o_s = o_s;
			}
			if (o_s.o_c == this) { // I am the oldest :(
				o_s.o_c = y_s;
			} else { // I have an older sibling!
				o_s.y_s = y_s;
			}
			y_s = null;
			o_s = null;

			// perform delete-min at tree rooted at this
			PairingHandle t = combine(cutChildren(this));

			// and merge with other cut tree
			if (comparator == null) {
				root = link(root, t);
			} else {
				root = linkWithComparator(root, t);
			}

			size--;
		}

	}

	private class Node {
		PairingHandle handle;
		Node next;
	}

	// two pass pair and compute root
	private PairingHandle combine(Node l) {
		if (l == null) {
			return null;
		}

		// left-right pass
		Node pairs = null;
		Node it = l, p_it;
		if (comparator == null) { // no comparator
			while (it != null) {
				p_it = it;
				it = it.next;

				if (it == null) {
					// append last node to pair list
					p_it.next = pairs;
					pairs = p_it;
				} else {
					// link trees
					p_it.handle = link(p_it.handle, it.handle);

					// append to pair list
					p_it.next = pairs;
					pairs = p_it;

					it = it.next;
				}
			}
		} else { // comparator version
			while (it != null) {
				p_it = it;
				it = it.next;

				if (it == null) {
					// append last node to pair list
					p_it.next = pairs;
					pairs = p_it;
				} else {
					// link trees
					p_it.handle = linkWithComparator(p_it.handle, it.handle);

					// append to pair list
					p_it.next = pairs;
					pairs = p_it;

					it = it.next;
				}
			}
		}

		// second pass (reverse order - due to add first)
		it = pairs;
		PairingHandle f = null;
		if (comparator == null) {
			while (it != null) {
				f = link(f, it.handle);
				it = it.next;
			}
		} else {
			while (it != null) {
				f = linkWithComparator(f, it.handle);
				it = it.next;
			}
		}

		return f;
	}

	private Node cutChildren(PairingHandle n) {
		Node head = null;
		Node tail = null;

		PairingHandle child = n.o_c;
		while (child != null) {
			PairingHandle next = child.y_s;
			child.y_s = null;
			child.o_s = null;

			// create new node
			Node newNode = new Node();
			newNode.handle = child;
			newNode.next = null;

			// append to list
			if (tail == null) {
				head = tail = newNode;
			} else {
				tail.next = newNode;
				tail = newNode;
			}

			child = next;
		}
		n.o_c = null;

		return head;
	}

	@SuppressWarnings("unchecked")
	private PairingHandle link(PairingHandle f, PairingHandle s) {
		if (s == null) {
			return f;
		}
		if (f == null) {
			return s;
		}

		if (((Comparable<? super K>) f.key).compareTo(s.key) <= 0) {
			s.y_s = f.o_c;
			s.o_s = f;
			if (f.o_c != null) {
				f.o_c.o_s = s;
			}
			f.o_c = s;
			return f;
		} else {
			f.y_s = s.o_c;
			f.o_s = s;
			if (s.o_c != null) {
				s.o_c.o_s = f;
			}
			s.o_c = f;
			return s;
		}
	}

	private PairingHandle linkWithComparator(PairingHandle f, PairingHandle s) {
		if (s == null) {
			return f;
		}
		if (f == null) {
			return s;
		}

		if (comparator.compare(f.key, s.key) <= 0) {
			s.y_s = f.o_c;
			s.o_s = f;
			if (f.o_c != null) {
				f.o_c.o_s = s;
			}
			f.o_c = s;
			return f;
		} else {
			f.y_s = s.o_c;
			f.o_s = s;
			if (s.o_c != null) {
				s.o_c.o_s = f;
			}
			s.o_c = f;
			return s;
		}
	}

}
