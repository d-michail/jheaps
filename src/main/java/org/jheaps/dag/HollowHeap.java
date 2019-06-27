package org.jheaps.dag;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableAddressableHeap;
import org.jheaps.annotations.ConstantTime;
import org.jheaps.annotations.LogarithmicTime;

/**
 * Hollow heaps. The heap is sorted according to the {@linkplain Comparable
 * natural ordering} of its keys, or by a {@link Comparator} provided at heap
 * creation time, depending on which constructor is used.
 *
 * <p>
 * This is the hollow heap described in detail in the following
 * <a href="https://doi.org/10.1145/3093240">paper</a>:
 * <ul>
 * <li>TD Hansen, H Kaplan, RE Tarjan and U Zwick. Hollow heaps. ACM
 * Transactions on Algorithms (TALG), 13(3), 42, 2017.</li>
 * </ul>
 *
 * <p>
 * This implementation provides amortized O(1) time for operations that do not
 * involve deleting an element such as {@code insert}, and {@code decreaseKey}.
 * Operations {@code deleteMin} and {@code delete} are amortized O(log(n)). The
 * operation {@code meld} is also amortized O(1).
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
 * Note that the ordering maintained by this heap, like any heap, and whether or
 * not an explicit comparator is provided, must be <em>consistent with
 * {@code equals}</em> if this heap is to correctly implement the
 * {@code AddressableHeap} interface. (See {@code Comparable} or
 * {@code Comparator} for a precise definition of <em>consistent with
 * equals</em>.) This is so because the {@code AddressableHeap} interface is
 * defined in terms of the {@code equals} operation, but this heap performs all
 * key comparisons using its {@code compareTo} (or {@code compare}) method, so
 * two keys that are deemed equal by this method are, from the standpoint of the
 * Fibonacci heap, equal. The behavior of a heap <em>is</em> well-defined even
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
 * @param <K> the type of keys maintained by this heap
 * @param <V> the type of values maintained by this heap
 *
 * @author Dimitrios Michail
 *
 */
public class HollowHeap<K, V> implements MergeableAddressableHeap<K, V>, Serializable {

	private final static long serialVersionUID = 1;

	/**
	 * Size of bucket array. Based on maximum rank.
	 */
	private final static int AUX_BUCKET_ARRAY_SIZE = 65;

	/**
	 * The comparator used to maintain order in this heap, or null if it uses the
	 * natural ordering of its keys.
	 *
	 * @serial
	 */
	private final Comparator<? super K> comparator;

	/**
	 * The last node in the root list
	 */
	private HollowNode<K, V> root;

	/**
	 * Size of the pairing heap
	 */
	private long size;

	/**
	 * Auxiliary array for consolidation.
	 */
	private HollowNode<K, V>[] aux;

	/**
	 * Used to reference the current heap or some other pairing heap in case of
	 * melding, so that handles remain valid even after a meld, without having to
	 * iterate over them.
	 * 
	 * In order to avoid maintaining a full-fledged union-find data structure, we
	 * disallow a heap to be used in melding more than once. We use however,
	 * path-compression in case of cascading melds, that it, a handle moves from one
	 * heap to another and then another.
	 */
	private HollowHeap<K, V> other;

	/**
	 * Constructs a new, empty heap, using the natural ordering of its keys. All
	 * keys inserted into the heap must implement the {@link Comparable} interface.
	 * Furthermore, all such keys must be <em>mutually comparable</em>:
	 * {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any
	 * keys {@code k1} and {@code k2} in the heap. If the user attempts to put a key
	 * into the heap that violates this constraint (for example, the user attempts
	 * to put a string key into a heap whose keys are integers), the
	 * {@code insert(Object key)} call will throw a {@code ClassCastException}.
	 */
	@ConstantTime
	public HollowHeap() {
		this(null);
	}

	/**
	 * Constructs a new, empty heap, ordered according to the given comparator. All
	 * keys inserted into the heap must be <em>mutually comparable</em> by the given
	 * comparator: {@code comparator.compare(k1,
	 * k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the heap. If the user attempts to put a key into the heap that
	 * violates this constraint, the {@code insert(Object key)} call will throw a
	 * {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If
	 *                   {@code null}, the {@linkplain Comparable natural ordering}
	 *                   of the keys will be used.
	 */
	@ConstantTime
	@SuppressWarnings("unchecked")
	public HollowHeap(Comparator<? super K> comparator) {
		this.root = null;
		this.comparator = comparator;
		this.size = 0;
		this.aux = (HollowNode<K, V>[]) Array.newInstance(HollowNode.class, AUX_BUCKET_ARRAY_SIZE);
		this.other = this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException if the heap has already been used in the right
	 *                               hand side of a meld
	 */
	@Override
	@ConstantTime(amortized = true)
	public AddressableHeap.Handle<K, V> insert(K key, V value) {
		if (other != this) {
			throw new IllegalStateException("A heap cannot be used after a meld");
		}
		if (key == null) {
			throw new NullPointerException("Null keys not permitted");
		}

		HollowNode<K, V> hollowNode = new HollowNode<K, V>(this, key, value); 
		Item<K, V> n = new Item<K, V>(hollowNode);
		hollowNode.item = n;
		
		if (root == null) { 
			root = hollowNode;
		} else { 
			root = link(root, hollowNode);
		}
		
		size++;
		return n;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException if the heap has already been used in the right
	 *                               hand side of a meld
	 */
	@Override
	@ConstantTime(amortized = true)
	public AddressableHeap.Handle<K, V> insert(K key) {
		return insert(key, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime(amortized = false)
	public AddressableHeap.Handle<K, V> findMin() {
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
	public AddressableHeap.Handle<K, V> deleteMin() {
		if (size == 0) {
			throw new NoSuchElementException();
		}

		throw new UnsupportedOperationException();
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
	@ConstantTime(amortized = false)
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ConstantTime
	public void meld(MergeableAddressableHeap<K, V> other) {
		throw new UnsupportedOperationException();
	}

	// --------------------------------------------------------------------
	static class Item<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

		private final static long serialVersionUID = 1;

		private HollowNode<K, V> delegate;

		public Item(HollowNode<K, V> delegate) {
			this.delegate = delegate;
		}

		@Override
		public K getKey() {
			return delegate.getKey();
		}

		@Override
		public V getValue() {
			return delegate.getValue();
		}

		@Override
		public void setValue(V value) {
			delegate.setValue(value);
		}

		@Override
		public void decreaseKey(K newKey) {
			delegate.decreaseKey(newKey);
		}

		@Override
		public void delete() {
			delegate.delete();
		}

	}

	static class HollowNode<K, V> implements AddressableHeap.Handle<K, V>, Serializable {

		private final static long serialVersionUID = 1;

		/*
		 * We maintain explicitly the belonging heap, instead of using an inner class
		 * due to possible cascading melding.
		 */
		HollowHeap<K, V> heap;

		K key;
		V value;
		HollowNode<K, V> child; // child
		HollowNode<K, V> next; // next sibling in list of children of first parent
		HollowNode<K, V> sp; // second parent
		int rank;
		
		Item<K, V> item;

		HollowNode(HollowHeap<K, V> heap, K key, V value) {
			this.heap = heap;
			this.key = key;
			this.item = null;
			this.value = value;
			this.child = null;
			this.next = null;
			this.sp = null;
			this.rank = 0;
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
		public V getValue() {
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setValue(V value) {
			this.value = value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@ConstantTime(amortized = true)
		public void decreaseKey(K newKey) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@LogarithmicTime(amortized = true)
		public void delete() {
			throw new UnsupportedOperationException();
		}

		/*
		 * Get the owner heap of the handle. This is union-find with path-compression
		 * between heaps.
		 */
		HollowHeap<K, V> getOwner() {
			if (heap.other != heap) {
				// find root
				HollowHeap<K, V> root = heap;
				while (root != root.other) {
					root = root.other;
				}
				// path-compression
				HollowHeap<K, V> cur = heap;
				while (cur.other != root) {
					HollowHeap<K, V> next = cur.other;
					cur.other = root;
					cur = next;
				}
				heap = root;
			}
			return heap;
		}
	}

	@SuppressWarnings("unchecked")
	private HollowNode<K, V> link(HollowNode<K, V> v, HollowNode<K, V> w) {
		int c;
		if (comparator == null) {
			c = ((Comparable<? super K>) v.key).compareTo(w.key);
		} else {
			c = comparator.compare(v.key, w.key);
		}
		if (c >= 0) {
			// v a child of w
			v.next = w.child;
			w.child = v;
			return w;
		} else {
			// w a child of v
			w.next = v.child;
			v.child = w;
			return v;
		}
	}

}
