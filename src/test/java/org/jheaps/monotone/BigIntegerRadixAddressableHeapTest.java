/*
 * (C) Copyright 2014-2018, by Dimitrios Michail
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

import org.jheaps.AddressableHeap;
import org.junit.Test;

public class BigIntegerRadixAddressableHeapTest {

	private static final BigInteger SIZE = BigInteger.valueOf(100000);

	@Test
    public void testBug2() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO, BigInteger.valueOf(100));
        h.insert(BigInteger.ZERO);
        assertEquals(BigInteger.ZERO, h.findMin().getKey());
        assertEquals(BigInteger.ZERO, h.deleteMin().getKey());
        h.insert(BigInteger.valueOf(15));
        assertEquals(BigInteger.valueOf(15), h.findMin().getKey());
    }
	
	@Test
	public void testVerySmall() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(29),
				BigInteger.valueOf(36));

		h.insert(BigInteger.valueOf(29));
		h.insert(BigInteger.valueOf(30));
		h.insert(BigInteger.valueOf(31));
		h.insert(BigInteger.valueOf(30));
		h.insert(BigInteger.valueOf(33));
		h.insert(BigInteger.valueOf(36));
		h.insert(BigInteger.valueOf(35));

		assertEquals(h.size(), 7);
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(29));
		assertEquals(h.size(), 7);
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(29));
		assertEquals(h.size(), 6);
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(30));
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(30));
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(30));
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(30));
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(31));
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(31));
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(33));
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(33));
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(35));
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(35));
		assertEquals(h.findMin().getKey(), BigInteger.valueOf(36));
		assertEquals(h.deleteMin().getKey(), BigInteger.valueOf(36));
		assertEquals(h.size(), 0);
		assertTrue(h.isEmpty());
	}

	@Test
	public void test() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(0), SIZE);

		for (int i = 0; i < SIZE.intValue(); i++) {
			h.insert(BigInteger.valueOf(i));
			assertEquals(BigInteger.ZERO, h.findMin().getKey());
			assertFalse(h.isEmpty());
			assertEquals(h.size(), i + 1);
		}

		for (int i = SIZE.intValue() - 1; i >= 0; i--) {
			assertEquals(SIZE.subtract(BigInteger.valueOf(i + 1)), h.findMin().getKey());
			h.deleteMin();
		}
	}

	@Test
	public void testSortRandomSeed1() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(0),
				SIZE.add(BigInteger.ONE));

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
			cur = h.findMin().getKey();
			h.deleteMin();
			if (prev != null) {
				assertTrue(prev.compareTo(cur) <= 0);
			}
			prev = cur;
		}
	}

	@Test
    public void testMonotoneOkOnLastDeleted() {
        AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
                BigInteger.valueOf(100));
        h.insert(BigInteger.valueOf(100L));
        assertEquals(BigInteger.valueOf(100L), h.findMin().getKey());
        h.insert(BigInteger.valueOf(99L));
        assertEquals(BigInteger.valueOf(99L), h.findMin().getKey());
    }
	
	@Test(expected = IllegalArgumentException.class)
	public void testMonotoneNotOkOnLastDeleted() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(100));
		h.insert(BigInteger.valueOf(100L));
		h.deleteMin();
		h.insert(BigInteger.valueOf(99L));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction() {
		new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO.subtract(BigInteger.ONE), BigInteger.valueOf(100));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction1() {
		new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(100), BigInteger.valueOf(99));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction2() {
		new BigIntegerRadixAddressableHeap<Void>(null, BigInteger.valueOf(99));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstruction3() {
		new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(99), null);
	}

	@Test
	public void testSameMinMax() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ONE, BigInteger.ONE);

		for (int i = 0; i < 15; i++) {
			h.insert(BigInteger.valueOf(1));
		}

		assertEquals(15, h.size());
		for (int i = 0; i < 15; i++) {
			assertEquals(BigInteger.ONE, h.deleteMin().getKey());
		}
		assertEquals(0, h.size());
	}

	@Test
	public void testBigDifference() {
		BigInteger longMax = BigInteger.valueOf(Long.MAX_VALUE);
		BigInteger bigValue = longMax.multiply(longMax);
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO, bigValue);

		h.insert(BigInteger.ZERO);
		h.insert(longMax);
		h.insert(bigValue);

		assertEquals(3, h.size());
		assertEquals(BigInteger.ZERO, h.deleteMin().getKey());
		assertEquals(longMax, h.deleteMin().getKey());
		assertEquals(bigValue, h.deleteMin().getKey());
		assertEquals(0, h.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(0),
				BigInteger.valueOf(15));

		AddressableHeap.Handle<BigInteger, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(BigInteger.valueOf(i));
		}

		array[5].delete();
		assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(BigInteger.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(BigInteger.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(BigInteger.valueOf(3), h.findMin().getKey());
		array[3].delete();
		assertEquals(BigInteger.valueOf(4), h.findMin().getKey());
		array[9].delete();
		assertEquals(BigInteger.valueOf(4), h.findMin().getKey());
		array[4].delete();
		assertEquals(BigInteger.valueOf(6), h.findMin().getKey());
		array[8].delete();
		assertEquals(BigInteger.valueOf(6), h.findMin().getKey());
		array[11].delete();
		assertEquals(BigInteger.valueOf(6), h.findMin().getKey());
		array[6].delete();
		assertEquals(BigInteger.valueOf(10), h.findMin().getKey());
		array[12].delete();
		assertEquals(BigInteger.valueOf(10), h.findMin().getKey());
		array[10].delete();
		assertEquals(BigInteger.valueOf(13), h.findMin().getKey());
		array[13].delete();
		assertEquals(BigInteger.valueOf(14), h.findMin().getKey());
		array[14].delete();
		assertTrue(h.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDelete1() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(0),
				BigInteger.valueOf(10));

		AddressableHeap.Handle<BigInteger, Void> array[];
		array = new AddressableHeap.Handle[8];
		for (int i = 0; i < 8; i++) {
			array[i] = h.insert(BigInteger.valueOf(i));
		}

		array[5].delete();
		assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(BigInteger.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(BigInteger.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(BigInteger.valueOf(3), h.findMin().getKey());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddDelete() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO, SIZE);

		AddressableHeap.Handle<BigInteger, Void> array[];
		array = new AddressableHeap.Handle[SIZE.intValue()];
		for (int i = 0; i < SIZE.intValue(); i++) {
			array[i] = h.insert(BigInteger.valueOf(i));
		}

		for (int i = SIZE.intValue() - 1; i >= 0; i--) {
			array[i].delete();
			if (i > 0) {
				assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
			}
		}
		assertTrue(h.isEmpty());
	}

	@Test
	public void testClear() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(15));

		for (int i = 0; i < 15; i++) {
			h.insert(BigInteger.valueOf(i));
		}

		h.clear();
		assertEquals(0L, h.size());
		assertTrue(h.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTwice() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(15));

		AddressableHeap.Handle<BigInteger, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(BigInteger.valueOf(i));
		}

		array[5].delete();
		assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
		array[7].delete();
		assertEquals(BigInteger.valueOf(0), h.findMin().getKey());
		array[0].delete();
		assertEquals(BigInteger.valueOf(1), h.findMin().getKey());
		array[2].delete();
		assertEquals(BigInteger.valueOf(1), h.findMin().getKey());
		array[1].delete();
		assertEquals(BigInteger.valueOf(3), h.findMin().getKey());
		array[3].delete();
		assertEquals(BigInteger.valueOf(4), h.findMin().getKey());
		array[9].delete();
		assertEquals(BigInteger.valueOf(4), h.findMin().getKey());
		array[4].delete();
		assertEquals(BigInteger.valueOf(6), h.findMin().getKey());

		// again
		array[2].delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(100));
		AddressableHeap.Handle<BigInteger, Void> e1 = h.insert(BigInteger.valueOf(50));
		h.insert(BigInteger.valueOf(100));
		h.deleteMin();
		e1.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDeleteTwice1() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(99),
				BigInteger.valueOf(200));

		for (int i = 100; i < 200; i++) {
			h.insert(BigInteger.valueOf(i));
		}

		h.deleteMin().delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMinDecreaseKey() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.valueOf(100),
				BigInteger.valueOf(200));

		for (int i = 100; i < 200; i++) {
			h.insert(BigInteger.valueOf(i));
		}
		h.deleteMin().decreaseKey(BigInteger.ZERO);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDecreaseKey() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(200));

		AddressableHeap.Handle<BigInteger, Void> array[];
		array = new AddressableHeap.Handle[15];

		h.insert(BigInteger.ZERO); // monotone

		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(BigInteger.valueOf(i).add(BigInteger.valueOf(100)));
		}

		array[5].decreaseKey(BigInteger.valueOf(5));
		array[1].decreaseKey(BigInteger.valueOf(50));
		array[10].decreaseKey(BigInteger.valueOf(3));
		array[0].decreaseKey(BigInteger.valueOf(1));
		array[5].delete();
		array[2].delete();
		array[11].delete();
		array[9].delete();

		assertEquals(BigInteger.valueOf(0), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(1), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(3), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(50), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(103), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(104), h.deleteMin().getKey());

		array[14].decreaseKey(BigInteger.valueOf(111));
		array[13].decreaseKey(BigInteger.valueOf(109));

		assertEquals(BigInteger.valueOf(106), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(107), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(108), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(109), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(111), h.deleteMin().getKey());
		assertEquals(BigInteger.valueOf(112), h.deleteMin().getKey());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testIncreaseKey() {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(200));

		AddressableHeap.Handle<BigInteger, Void> array[];
		array = new AddressableHeap.Handle[15];
		for (int i = 0; i < 15; i++) {
			array[i] = h.insert(BigInteger.valueOf(i).add(BigInteger.valueOf(100)));
		}

		assertEquals(BigInteger.valueOf(100), h.findMin().getKey());
		array[5].decreaseKey(BigInteger.valueOf(5));
		assertEquals(BigInteger.valueOf(5), h.findMin().getKey());
		array[1].decreaseKey(BigInteger.valueOf(102));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSerializable() throws IOException, ClassNotFoundException {
		AddressableHeap<BigInteger, Void> h = new BigIntegerRadixAddressableHeap<Void>(BigInteger.ZERO,
				BigInteger.valueOf(15));

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
		h = (AddressableHeap<BigInteger, Void>) o;

		for (long i = 0; i < 15; i++) {
			assertEquals(15 - i, h.size());
			assertEquals(BigInteger.valueOf(i), h.findMin().getKey());
			h.deleteMin();
		}
		assertTrue(h.isEmpty());
	}

}
