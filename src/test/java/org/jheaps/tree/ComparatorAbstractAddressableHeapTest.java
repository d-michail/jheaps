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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class ComparatorAbstractAddressableHeapTest {

	private static final int SIZE = 100000;

	private static Comparator<Long> comparator;

	private static class TestComparator implements Comparator<Long>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Long o1, Long o2) {
			if (o1 < o2) {
				return 1;
			} else if (o1 > o2) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	@BeforeClass
	public static void setUpClass() {
		comparator = new TestComparator();
	}

	protected abstract AddressableHeap<Long, Void> createHeap(Comparator<Long> comparator);

	@Test
	public void testComparator() {

		AddressableHeap<Long, Void> h = createHeap(comparator);
		long i;

		for (i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Long.valueOf(i), h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin().getKey(), Long.valueOf(i));
			h.deleteMin();
		}

	}

	@Test
	public void testOnly4Reverse() {
		AddressableHeap<Long, Void> h = createHeap(comparator);

		assertTrue(h.isEmpty());

		h.insert(780l);
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(780), h.findMin().getKey());

		h.insert(-389l);
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(780), h.findMin().getKey());

		h.insert(306l);
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(780), h.findMin().getKey());

		h.insert(579l);
		assertEquals(h.size(), 4);
		assertEquals(Long.valueOf(780), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(579), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(306), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(-389), h.findMin().getKey());

		h.deleteMin();
		assertEquals(h.size(), 0);

		assertTrue(h.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {
		AddressableHeap<Long, Void> h = createHeap(comparator);

		AddressableHeap.Handle<Long, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(Long.valueOf(i));
		}

		assertEquals(Long.valueOf(14), h.findMin().getKey());
		array[5].decreaseKey(205l);
		assertEquals(Long.valueOf(205), h.findMin().getKey());
		array[1].decreaseKey(250l);
		assertEquals(Long.valueOf(250l), h.findMin().getKey());
		array[1].decreaseKey(300l);
		assertEquals(Long.valueOf(300l), h.findMin().getKey());
		array[5].delete();
		assertEquals(Long.valueOf(300l), h.findMin().getKey());
		array[10].decreaseKey(403l);
		assertEquals(Long.valueOf(403l), h.findMin().getKey());
		array[0].decreaseKey(1000l);
		assertEquals(Long.valueOf(1000l), h.findMin().getKey());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {
		AddressableHeap<Long, Void> h = createHeap(comparator);
		h.insert(10l).decreaseKey(9l);
	}

	@Test
	public void testDecreaseSame() {
		AddressableHeap<Long, Void> h = createHeap(comparator);
		h.insert(10l).decreaseKey(10l);
		assertEquals(10l, h.findMin().getKey().longValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidHandleDecreaseKey() {
		AddressableHeap<Long, Void> h = createHeap(comparator);
		Handle<Long, Void> handle = h.insert(10l);
		h.deleteMin();
		handle.decreaseKey(11l);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {
		AddressableHeap<Long, Void> h = createHeap(comparator);

		for (long i = 0; i < 15; i++) {
			h.insert(i);
		}

		// write
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(h);
		oos.close();
		byte[] data = baos.toByteArray();

		// read
		h = null;

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		h = (AddressableHeap<Long, Void>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Long.valueOf(15 - i - 1), h.findMin().getKey());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());
	}

	@Test
	public void testGetComparator() {
		AddressableHeap<Long, Void> h1 = createHeap(comparator);
		assertEquals(comparator, h1.comparator());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMeld() throws IOException, ClassNotFoundException {
		AddressableHeap<Long, Void> h1 = createHeap(comparator);
		if (h1 instanceof MergeableHeap) {
			AddressableHeap<Long, Void> h2 = createHeap(comparator);

			for (long i = 0; i < SIZE; i++) {
				if (i % 2 == 0) {
					h1.insert(i);
				} else {
					h2.insert(i);
				}
			}

			((MergeableHeap<Long>) h1).meld((MergeableHeap<Long>) h2);

			assertTrue(h2.isEmpty());
			assertEquals(0, h2.size());

			for (int i = 0; i < SIZE; i++) {
				assertEquals(Long.valueOf(SIZE - i - 1), h1.findMin().getKey());
				h1.deleteMin();
			}
			assertTrue(h1.isEmpty());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMeldBadComparator() throws IOException, ClassNotFoundException {
		AddressableHeap<Long, Void> h1 = createHeap(comparator);
		if (h1 instanceof MergeableHeap) {
			AddressableHeap<Long, Void> h2 = createHeap(new Comparator<Long>() {
				@Override
				public int compare(Long o1, Long o2) {
					return (int) (o1 - o2);
				}
			});

			for (long i = 0; i < SIZE; i++) {
				if (i % 2 == 0) {
					h1.insert(i);
				} else {
					h2.insert(i);
				}
			}

			try {
				((MergeableHeap<Long>) h1).meld((MergeableHeap<Long>) h2);
				fail("No!");
			} catch (IllegalArgumentException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMeldBadComparator1() throws IOException, ClassNotFoundException {
		AddressableHeap<Long, Void> h1 = createHeap(comparator);
		if (h1 instanceof MergeableHeap) {
			AddressableHeap<Long, Void> h2 = createHeap(null);
			try {
				((MergeableHeap<Long>) h1).meld((MergeableHeap<Long>) h2);
				fail("No!");
			} catch (IllegalArgumentException e) {
			}
		}
	}

}
