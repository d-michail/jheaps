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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.jheaps.Heap;
import org.jheaps.monotone.BigIntegerRadixHeap;
import org.junit.Test;

public class BigIntegerRadixHeapTest {

	private static final BigInteger SIZE = BigInteger.valueOf(100000);

	@Test
	public void test() {
		Heap<BigInteger> h = new BigIntegerRadixHeap(BigInteger.ZERO, SIZE);

		for (long i = 0; i < SIZE.longValue(); i++) {
			h.insert(BigInteger.valueOf(i));
			assertEquals(BigInteger.valueOf(0), h.findMin());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (long i = SIZE.longValue() - 1; i >= 0; i--) {
			assertEquals(h.findMin(), BigInteger.valueOf(SIZE.longValue() - i - 1));
			h.deleteMin();
		}
	}

	@Test
	public void testVerySmall() {
		Heap<BigInteger> h = new BigIntegerRadixHeap(BigInteger.valueOf(29), BigInteger.valueOf(36));

		h.insert(BigInteger.valueOf(29L));
		h.insert(BigInteger.valueOf(30L));
		h.insert(BigInteger.valueOf(31L));
		h.insert(BigInteger.valueOf(30L));
		h.insert(BigInteger.valueOf(33L));
		h.insert(BigInteger.valueOf(36L));
		h.insert(BigInteger.valueOf(35L));

		assertEquals(h.size(), 7);
		assertEquals(h.findMin().longValue(), 29L);
		assertEquals(h.size(), 7);
		assertEquals(h.deleteMin().longValue(), 29L);
		assertEquals(h.size(), 6);
		assertEquals(h.findMin().longValue(), 30L);
		assertEquals(h.deleteMin().longValue(), 30L);
		assertEquals(h.findMin().longValue(), 30L);
		assertEquals(h.deleteMin().longValue(), 30L);
		assertEquals(h.findMin().longValue(), 31L);
		assertEquals(h.deleteMin().longValue(), 31L);
		assertEquals(h.findMin().longValue(), 33L);
		assertEquals(h.deleteMin().longValue(), 33L);
		assertEquals(h.findMin().longValue(), 35L);
		assertEquals(h.deleteMin().longValue(), 35L);
		assertEquals(h.findMin().longValue(), 36L);
		assertEquals(h.deleteMin().longValue(), 36L);
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void testSortRandomSeed1() {
		Heap<BigInteger> h = new BigIntegerRadixHeap(BigInteger.valueOf(0), SIZE.add(BigInteger.ONE));

		Random generator = new Random(1);

		long[] a = new long[SIZE.intValue()];
		for (int i = 0; i < SIZE.intValue(); i++) {
			a[i] = (long) (SIZE.longValue() * generator.nextDouble());
		}
		Arrays.sort(a);
		for (int i = 0; i < SIZE.intValue(); i++) {
			h.insert(BigInteger.valueOf(a[i]));
		}

		BigInteger prev = null, cur;
		while (!h.isEmpty()) {
			cur = h.findMin();
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
	public void testClear() {
		Heap<BigInteger> h = new BigIntegerRadixHeap(BigInteger.ZERO, BigInteger.valueOf(15));

		for (long i = 0; i < 15; i++) {
			h.insert(BigInteger.valueOf(i));
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMonotone() {
		Heap<BigInteger> h = new BigIntegerRadixHeap(BigInteger.ZERO, BigInteger.valueOf(100));
		h.insert(BigInteger.valueOf(100L));
		h.insert(BigInteger.valueOf(99L));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new BigIntegerRadixHeap(BigInteger.ZERO.subtract(BigInteger.ONE), BigInteger.valueOf(100));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new BigIntegerRadixHeap(BigInteger.valueOf(100), BigInteger.valueOf(99));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction2() {
		new BigIntegerRadixHeap(null, BigInteger.valueOf(99));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction3() {
		new BigIntegerRadixHeap(BigInteger.valueOf(99), null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSerializable() throws IOException, ClassNotFoundException {
		Heap<BigInteger> h = new BigIntegerRadixHeap(BigInteger.ZERO, BigInteger.valueOf(15));

		for (long i = 0; i < 15; i++) {
			h.insert(BigInteger.valueOf(i));
		}

		// write
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(h);
		oos.close();
		byte[] data = baos.toByteArray();

		// read

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		h = (Heap<BigInteger>) o;

		for (long i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(BigInteger.valueOf(i), h.findMin());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());

	}

}
