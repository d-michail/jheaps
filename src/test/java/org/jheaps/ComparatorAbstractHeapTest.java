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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;

import org.junit.BeforeClass;
import org.junit.Test;

public abstract class ComparatorAbstractHeapTest {

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

	protected abstract Heap<Long> createHeap(Comparator<Long> comparator);

	@Test
	public void testComparator() {

		Heap<Long> h = createHeap(comparator);
		long i;

		for (i = 0; i < SIZE; i++) {
			h.insert(i);
			assertEquals(Long.valueOf(i), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (i = SIZE - 1; i >= 0; i--) {
			assertEquals(h.findMin(), Long.valueOf(i));
			h.deleteMin();
		}

	}

	@Test
	public void testOnly4Reverse() {
		Heap<Long> h = createHeap(comparator);

		assertTrue(h.isEmpty());

		h.insert(780l);
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(780), h.findMin());

		h.insert(-389l);
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(780), h.findMin());

		h.insert(306l);
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(780), h.findMin());

		h.insert(579l);
		assertEquals(h.size(), 4);
		assertEquals(Long.valueOf(780), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 3);
		assertEquals(Long.valueOf(579), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 2);
		assertEquals(Long.valueOf(306), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 1);
		assertEquals(Long.valueOf(-389), h.findMin());

		h.deleteMin();
		assertEquals(h.size(), 0);

		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializable() throws IOException, ClassNotFoundException {
		Heap<Long> h = createHeap(comparator);

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
		h = (Heap<Long>) o;

		for (int i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(Long.valueOf(15 - i - 1), h.findMin());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

}
