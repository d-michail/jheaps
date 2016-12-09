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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.AddressableHeap;
import org.jheaps.MergeableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.tree.BinaryTreeSoftAddressableHeap;
import org.jheaps.tree.BinaryTreeSoftAddressableHeap.RootListNode;
import org.jheaps.tree.BinaryTreeSoftAddressableHeap.SoftHandle;
import org.jheaps.tree.BinaryTreeSoftAddressableHeap.TreeNode;
import org.junit.BeforeClass;
import org.junit.Test;

public class BinaryTreeSoftAddressableHeapTest {

    private static final int SIZE = 100000;

    private static Comparator<Integer> comparator;

    @BeforeClass
    public static void setUpClass() {
        comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1 < o2) {
                    return 1;
                } else if (o1 > o2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

    @Test
    public void testNoCorruptionAsHeap() {
        final int n = SIZE;
        double epsilon = 1.0 / (n + 1);

        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(epsilon);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        for (int i = 0; i < n; i++) {
            assertEquals(i, a.deleteMin().getKey().intValue());
        }

        assertTrue(a.isEmpty());
    }

    @Test
    public void testNoCorruptionWithComparatorAsHeap() {
        final int n = SIZE;
        double epsilon = 1.0 / (n + 1);

        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(epsilon,
                comparator);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        for (int i = SIZE - 1; i >= 0; i--) {
            assertEquals(i, a.deleteMin().getKey().intValue());
        }

        assertTrue(a.isEmpty());
    }

    @Test
    public void testSort1RandomSeed1() {
        AddressableHeap<Integer, String> h = new BinaryTreeSoftAddressableHeap<Integer, String>(1.0 / (SIZE + 1));

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.deleteMin().getKey();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) <= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testSort1RandomSeed1WithComparator() {
        AddressableHeap<Integer, String> h = new BinaryTreeSoftAddressableHeap<Integer, String>(1.0 / (SIZE + 1),
                comparator);

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.deleteMin().getKey();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) >= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testComparator() {
        AddressableHeap<Long, Void> h = new BinaryTreeSoftAddressableHeap<Long, Void>(0.5);

        assertNull(h.comparator());
    }

    @Test
    public void testFindMinDeleteMinSameObject() {
        AddressableHeap<Long, String> h = new BinaryTreeSoftAddressableHeap<Long, String>(0.5);

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextLong());
        }

        while (!h.isEmpty()) {
            assertEquals(h.findMin(), h.deleteMin());
        }
    }

    @Test
    public void testSoftHeap0() {
        testSoftHeapInsert(0.01, null);
        testSoftHeapInsert(0.01, comparator);
        testSoftHeapInsertDeleteMin(0.01, null);
        testSoftHeapInsertDeleteMin(0.01, comparator);
        testSoftHeapInsertDelete(0.01, null);
        testSoftHeapInsertDelete(0.01, comparator);
        testSoftHeapInsertDeleteDeleteMin(0.01, null);
        testSoftHeapInsertDeleteDeleteMin(0.01, comparator);
    }

    @Test
    public void testSoftHeap1() {
        testSoftHeapInsert(0.25, null);
        testSoftHeapInsert(0.25, comparator);
        testSoftHeapInsertDeleteMin(0.25, null);
        testSoftHeapInsertDeleteMin(0.25, comparator);
        testSoftHeapInsertDelete(0.25, null);
        testSoftHeapInsertDelete(0.25, comparator);
        testSoftHeapInsertDeleteDeleteMin(0.25, null);
        testSoftHeapInsertDeleteDeleteMin(0.25, comparator);
    }

    @Test
    public void testSoftHeap2() {
        testSoftHeapInsert(0.5, null);
        testSoftHeapInsert(0.5, comparator);
        testSoftHeapInsertDeleteMin(0.5, null);
        testSoftHeapInsertDeleteMin(0.5, comparator);
        testSoftHeapInsertDelete(0.5, null);
        testSoftHeapInsertDelete(0.5, comparator);
        testSoftHeapInsertDeleteDeleteMin(0.5, null);
        testSoftHeapInsertDeleteDeleteMin(0.5, comparator);
    }

    @Test
    public void testSoftHeap3() {
        testSoftHeapInsert(0.75, null);
        testSoftHeapInsert(0.75, comparator);
        testSoftHeapInsertDeleteMin(0.75, null);
        testSoftHeapInsertDeleteMin(0.75, comparator);
        testSoftHeapInsertDelete(0.75, null);
        testSoftHeapInsertDelete(0.75, comparator);
        testSoftHeapInsertDeleteDeleteMin(0.75, null);
        testSoftHeapInsertDeleteDeleteMin(0.75, comparator);
    }

    @Test
    public void testSoftHeap4() {
        testSoftHeapInsert(0.99, null);
        testSoftHeapInsert(0.99, comparator);
        testSoftHeapInsertDeleteMin(0.99, null);
        testSoftHeapInsertDeleteMin(0.99, comparator);
        testSoftHeapInsertDelete(0.99, null);
        testSoftHeapInsertDelete(0.99, comparator);
        testSoftHeapInsertDeleteDeleteMin(0.99, null);
        testSoftHeapInsertDeleteDeleteMin(0.99, comparator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction1() {
        new BinaryTreeSoftAddressableHeap<Integer, Void>(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction2() {
        new BinaryTreeSoftAddressableHeap<Integer, Void>(1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction3() {
        new BinaryTreeSoftAddressableHeap<Integer, Void>(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction4() {
        new BinaryTreeSoftAddressableHeap<Integer, Void>(2.0);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIllegalDeleteMin() {
        new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5).deleteMin();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIllegalFindMin() {
        new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5).findMin();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalDeleteTwice() {
        BinaryTreeSoftAddressableHeap<Integer, Void> h = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5);
        Handle<Integer, Void> e = h.insert(1);
        e.delete();
        e.delete();
    }

    @Test
    public void testGetValue() {
        AddressableHeap<Integer, String> h = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        Handle<Integer, String> e = h.insert(1, "999");
        assertEquals("999", e.getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNoDecreaseKey() {
        AddressableHeap<Integer, String> h = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        Handle<Integer, String> e = h.insert(1, "999");
        e.decreaseKey(0);
    }

    @Test
    public void testIsEmpty() {
        AddressableHeap<Integer, Void> h = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5);
        assertTrue(h.isEmpty());
        h.insert(1);
        assertFalse(h.isEmpty());
    }

    @Test
    public void testClear() {
        AddressableHeap<Integer, Void> h = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5);
        assertTrue(h.isEmpty());
        h.insert(1);
        h.insert(2);
        h.insert(3);
        h.insert(4);
        assertFalse(h.isEmpty());
        assertEquals(4, h.size());
        h.clear();
        assertTrue(h.isEmpty());
        assertEquals(0, h.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {
        AddressableHeap<Long, Void> h = new BinaryTreeSoftAddressableHeap<Long, Void>(1.0 / 16);

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

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        h = (AddressableHeap<Long, Void>) o;

        for (int i = 0; i < 15; i++) {
            assertEquals(15 - i, h.size());
            assertEquals(Long.valueOf(i), h.findMin().getKey());
            h.deleteMin();
        }
        assertTrue(h.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMeldGeneric() {
        AddressableHeap<Integer, String> h1 = new BinaryTreeSoftAddressableHeap<Integer, String>(1.0 / (SIZE + 1));

        if (h1 instanceof MergeableHeap) {
            for (int i = 0; i < SIZE; i++) {
                h1.insert(2 * i);
            }

            AddressableHeap<Integer, String> h2 = new BinaryTreeSoftAddressableHeap<Integer, String>(1.0 / (SIZE + 1));
            for (int i = 0; i < SIZE; i++) {
                h2.insert(2 * i + 1);
            }

            ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

            assertEquals(h1.size(), SIZE * 2);
            assertEquals(h2.size(), 0);

            Integer prev = null, cur;
            while (!h1.isEmpty()) {
                cur = h1.findMin().getKey();
                h1.deleteMin();
                if (prev != null) {
                    assertTrue(prev.compareTo(cur) <= 0);
                }
                prev = cur;
            }
        }
    }

    @Test
    public void testMeld1() {
        testSoftHeapMeld(SIZE, 0.10, null);
        testSoftHeapMeld(SIZE, 0.10, comparator);
        testSoftHeapMeld(SIZE, 0.25, null);
        testSoftHeapMeld(SIZE, 0.25, comparator);
        testSoftHeapMeld(SIZE, 0.5, null);
        testSoftHeapMeld(SIZE, 0.5, comparator);
        testSoftHeapMeld(SIZE, 0.75, null);
        testSoftHeapMeld(SIZE, 0.75, comparator);
        testSoftHeapMeld(SIZE, 0.95, null);
        testSoftHeapMeld(SIZE, 0.95, comparator);
    }

    @Test
    public void testMeld2() {
        testSoftHeapMeldSmallLarge(SIZE, 0.10, null);
        testSoftHeapMeldSmallLarge(SIZE, 0.10, comparator);
        testSoftHeapMeldSmallLarge(SIZE, 0.25, null);
        testSoftHeapMeldSmallLarge(SIZE, 0.25, comparator);
        testSoftHeapMeldSmallLarge(SIZE, 0.5, null);
        testSoftHeapMeldSmallLarge(SIZE, 0.5, comparator);
        testSoftHeapMeldSmallLarge(SIZE, 0.75, null);
        testSoftHeapMeldSmallLarge(SIZE, 0.75, comparator);
        testSoftHeapMeldSmallLarge(SIZE, 0.95, null);
        testSoftHeapMeldSmallLarge(SIZE, 0.95, comparator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong1() {
        BinaryTreeSoftAddressableHeap<Integer, Void> h1 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5,
                comparator);
        BinaryTreeSoftAddressableHeap<Integer, Void> h2 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.5);

        h1.meld(h2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong2() {
        BinaryTreeSoftAddressableHeap<Integer, Void> h1 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.4);
        BinaryTreeSoftAddressableHeap<Integer, Void> h2 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.7);

        h1.meld(h2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong3() {
        BinaryTreeSoftAddressableHeap<Integer, Void> h1 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.4);
        BinaryTreeSoftAddressableHeap<Integer, Void> h2 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.4,
                comparator);

        h1.meld(h2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong4() {
        BinaryTreeSoftAddressableHeap<Integer, Void> h1 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.4,
                comparator);
        BinaryTreeSoftAddressableHeap<Integer, Void> h2 = new BinaryTreeSoftAddressableHeap<Integer, Void>(0.4,
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return comparator.compare(o1, o2);
                    }
                });

        h1.meld(h2);
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleMelds() {
        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        BinaryTreeSoftAddressableHeap<Integer, String> b = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        BinaryTreeSoftAddressableHeap<Integer, String> c = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        c.insert(18);
        c.insert(19);
        c.insert(20);
        c.insert(21);

        a.meld(b);
        a.meld(b);
        validateSoftHeap(a, 0.5, 12L);
    }

    @Test
    public void testDeleteAfterMultipleMelds() {
        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        BinaryTreeSoftAddressableHeap<Integer, String> b = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        b.insert(14);
        b.insert(15);
        Handle<Integer, String> b3 = b.insert(16);
        b.insert(17);

        BinaryTreeSoftAddressableHeap<Integer, String> c = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        c.insert(18);
        c.insert(19);
        Handle<Integer, String> c3 = c.insert(20);
        c.insert(21);

        a.meld(b);
        a.meld(c);
        validateSoftHeap(a, 0.5, 12L);
        b3.delete();
        validateSoftHeap(a, 0.5, 12L);
        c3.delete();
        validateSoftHeap(a, 0.5, 12L);
    }

    @Test(expected = IllegalStateException.class)
    public void testInsertAfterAMeld() {
        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        BinaryTreeSoftAddressableHeap<Integer, String> b = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        a.meld(b);
        b.insert(30);
    }

    @Test
    public void testCascadingMelds() {
        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        BinaryTreeSoftAddressableHeap<Integer, String> b = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        BinaryTreeSoftAddressableHeap<Integer, String> c = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        c.insert(18);
        c.insert(19);
        c.insert(20);
        c.insert(21);

        BinaryTreeSoftAddressableHeap<Integer, String> d = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        d.insert(22);
        d.insert(23);
        d.insert(24);
        d.insert(25);

        BinaryTreeSoftAddressableHeap<Integer, String> e = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        e.insert(26);
        e.insert(27);
        e.insert(28);
        e.insert(29);

        d.meld(e);
        c.meld(d);
        b.meld(c);
        a.meld(b);

        assertEquals(20, a.size());
        assertEquals(0, b.size());
        assertEquals(0, c.size());
        assertEquals(0, d.size());
        assertEquals(0, e.size());

        validateSoftHeap(a, 0.5, 20L);
    }

    @Test
    public void testDeleteAfterCascadingMelds() {
        BinaryTreeSoftAddressableHeap<Integer, String> a = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        a.insert(10);
        a.insert(11);
        Handle<Integer, String> a3 = a.insert(12);
        a.insert(13);

        BinaryTreeSoftAddressableHeap<Integer, String> b = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        b.insert(14);
        b.insert(15);
        Handle<Integer, String> b3 = b.insert(16);
        b.insert(17);

        BinaryTreeSoftAddressableHeap<Integer, String> c = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        c.insert(18);
        c.insert(19);
        Handle<Integer, String> c3 = c.insert(20);
        c.insert(21);

        BinaryTreeSoftAddressableHeap<Integer, String> d = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        d.insert(22);
        d.insert(23);
        Handle<Integer, String> d3 = d.insert(24);
        d.insert(25);

        BinaryTreeSoftAddressableHeap<Integer, String> e = new BinaryTreeSoftAddressableHeap<Integer, String>(0.5);
        e.insert(26);
        e.insert(27);
        Handle<Integer, String> e3 = e.insert(28);
        e.insert(29);

        d.meld(e);
        c.meld(d);
        b.meld(c);
        a.meld(b);

        assertEquals(20, a.size());
        assertEquals(0, b.size());
        assertEquals(0, c.size());
        assertEquals(0, d.size());
        assertEquals(0, e.size());

        e3.delete();
        validateSoftHeap(a, 0.5, 20L);
        d3.delete();
        validateSoftHeap(a, 0.5, 20L);
        c3.delete();
        validateSoftHeap(a, 0.5, 20L);
        b3.delete();
        validateSoftHeap(a, 0.5, 20L);
        a3.delete();
        validateSoftHeap(a, 0.5, 20L);
    }

    @Test(expected = NullPointerException.class)
    public void testInsertNullKey() {
        new BinaryTreeSoftAddressableHeap<Integer, String>(0.5).insert(null);
    }

    private void testSoftHeapMeld(int n, double epsilon, Comparator<Integer> comparator) {
        BinaryTreeSoftAddressableHeap<Integer, Void> h1 = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);
        BinaryTreeSoftAddressableHeap<Integer, Void> h2 = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);

        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                h1.insert(i);
            } else {
                h2.insert(i);
            }
        }

        validateSoftHeap(h1, epsilon, n / 2);
        assertEquals(n / 2, h1.size());
        validateSoftHeap(h2, epsilon, n / 2);
        assertEquals(n / 2, h2.size());

        h1.meld(h2);

        validateSoftHeap(h1, epsilon, n);
        validateSoftHeap(h2, epsilon, n);
    }

    private void testSoftHeapMeldSmallLarge(int n, double epsilon, Comparator<Integer> comparator) {
        BinaryTreeSoftAddressableHeap<Integer, Void> h1 = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);
        BinaryTreeSoftAddressableHeap<Integer, Void> h2 = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);

        for (int i = 0; i < n / 3; i++) {
            h1.insert(i);
        }

        for (int i = n / 3; i < n; i++) {
            h2.insert(i);
        }

        validateSoftHeap(h1, epsilon, n / 3);
        assertEquals(n / 3, h1.size());
        validateSoftHeap(h2, epsilon, 2 * n / 3);
        assertEquals((int) Math.ceil(2.0 * n / 3.0), h2.size());

        h1.meld(h2);

        validateSoftHeap(h1, epsilon, n);
        validateSoftHeap(h2, epsilon, n);
    }

    private void testSoftHeapInsert(double epsilon, Comparator<Integer> comparator) {
        final int n = SIZE;

        BinaryTreeSoftAddressableHeap<Integer, Void> a = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        validateSoftHeap(a, epsilon, n);
    }

    private void testSoftHeapInsertDeleteMin(double epsilon, Comparator<Integer> comparator) {
        final int n = SIZE;

        BinaryTreeSoftAddressableHeap<Integer, Void> a = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        for (int i = 0; i < n / 4; i++) {
            a.deleteMin();
        }

        validateSoftHeap(a, epsilon, n);
    }

    @SuppressWarnings("unchecked")
    private void testSoftHeapInsertDelete(double epsilon, Comparator<Integer> comparator) {
        final int n = SIZE;

        BinaryTreeSoftAddressableHeap<Integer, Void> h = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);

        AddressableHeap.Handle<Integer, Void> array[];
        array = new AddressableHeap.Handle[n];
        for (int i = 0; i < n; i++) {
            array[i] = h.insert(i);
        }

        for (int i = 0; i < n / 4; i++) {
            array[i].delete();
        }

        validateSoftHeap(h, epsilon, n);
    }

    @SuppressWarnings("unchecked")
    private void testSoftHeapInsertDeleteDeleteMin(double epsilon, Comparator<Integer> comparator) {
        final int n = SIZE;

        BinaryTreeSoftAddressableHeap<Integer, Void> h = new BinaryTreeSoftAddressableHeap<Integer, Void>(epsilon,
                comparator);

        AddressableHeap.Handle<Integer, Void> array[];
        array = new AddressableHeap.Handle[n];
        for (int i = 0; i < n; i++) {
            array[i] = h.insert(i);
        }

        for (int i = 0; i < n / 4; i++) {
            h.deleteMin();
        }

        for (int i = n - 1; i >= 3 * n / 4; i--) {
            try {
                array[i].delete();
            } catch (IllegalArgumentException e) {
                // ignore, already deleted due to corruption
            }
        }

        validateSoftHeap(h, epsilon, n);
    }

    /**
     * Validate the invariants of a soft heap.
     * 
     * @param h
     *            the soft heap
     * @param epsilon
     *            the error rate of the soft heap
     * @param totalInsertedElements
     *            the total number of elements added in the heap
     */
    @SuppressWarnings("unchecked")
    private static <K, V> void validateSoftHeap(BinaryTreeSoftAddressableHeap<K, V> h, double epsilon,
            long totalInsertedElements) {
        long total = 0;
        long corrupted = 0;
        Comparator<? super K> comparator = h.comparator();
        RootListNode<K, V> cur = h.rootList.head;
        while (cur != null) {
            // validate each heap
            KeyCount kc = validateRoot(cur.root, comparator);
            // keep total count of total and corrupted elements
            total += kc.total;
            corrupted += kc.corrupted;
            cur = cur.next;
        }
        assertEquals(total, h.size());
        assertTrue("Too many corrupted elemenets", corrupted <= totalInsertedElements * epsilon);

        // validate suffix min pointers
        K minSoFar = null;
        cur = h.rootList.tail;
        while (cur != null) {
            // find min by hand
            if (minSoFar == null) {
                minSoFar = cur.root.cKey;
            } else {
                if (comparator == null) {
                    if (((Comparable<? super K>) cur.root.cKey).compareTo(minSoFar) <= 0) {
                        minSoFar = cur.root.cKey;
                    }
                } else {
                    if (comparator.compare(cur.root.cKey, minSoFar) <= 0) {
                        minSoFar = cur.root.cKey;
                    }
                }
            }

            // compare with suffix min
            assertEquals(minSoFar, cur.suffixMin.root.cKey);

            // keep total count of total and corrupted elements
            cur = cur.prev;
        }

    }

    @SuppressWarnings("unchecked")
    private static <K, V> KeyCount validateRoot(TreeNode<K, V> root, Comparator<? super K> comparator) {
        assertTrue(root.parent != null);
        if (root.left != null) {
            assertEquals(root.left.parent, root);
            assertTrue(root.rank > root.left.rank);
            if (comparator == null) {
                assertTrue(((Comparable<? super K>) root.cKey).compareTo(root.left.cKey) <= 0);
            } else {
                assertTrue(comparator.compare(root.cKey, root.left.cKey) <= 0);
            }
        }
        if (root.right != null) {
            assertEquals(root.right.parent, root);
            assertTrue(root.rank > root.right.rank);
            if (comparator == null) {
                assertTrue(((Comparable<? super K>) root.cKey).compareTo(root.right.cKey) <= 0);
            } else {
                assertTrue(comparator.compare(root.cKey, root.right.cKey) <= 0);
            }
        }
        if (root.left != null && root.right != null) {
            assertEquals(root.left.rank, root.right.rank);
        }
        assertTrue(root.cKey != null);
        assertTrue(root.cHead != null);
        assertTrue(root.cHead.tree == root);

        assertTrue(root.cSize > 0);
        long total = 0;
        long corrupted = 0;
        SoftHandle<K, V> e = root.cHead;
        while (e != null) {
            total++;
            if (comparator == null) {
                if (((Comparable<? super K>) e.key).compareTo(root.cKey) < 0) {
                    corrupted++;
                }
            } else {
                if (comparator.compare(e.key, root.cKey) < 0) {
                    corrupted++;
                }
            }
            e = e.next;
        }
        /*
         * Due to ghost elements (if delete operation is used)
         */
        assertTrue(total > 0 && total <= root.cSize);

        if (root.left != null) {
            KeyCount leftKeyCount = validateRoot(root.left, comparator);
            total += leftKeyCount.total;
            corrupted += leftKeyCount.corrupted;
        }

        if (root.right != null) {
            KeyCount rightKeyCount = validateRoot(root.right, comparator);
            total += rightKeyCount.total;
            corrupted += rightKeyCount.corrupted;
        }
        return new KeyCount(total, corrupted);
    }

    private static class KeyCount {
        long total;
        long corrupted;

        public KeyCount(long total, long corrupted) {
            this.total = total;
            this.corrupted = corrupted;
        }
    }

}
