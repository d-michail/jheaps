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
package org.jheaps.array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Random;

import org.jheaps.AddressableHeap;
import org.jheaps.Heap;
import org.junit.BeforeClass;
import org.junit.Test;

public class HeapifyTest {

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
    public void testHeapifySort() {
        Random generator = new Random(1);

        final int classes = 8;

        Integer[] a = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) {
            a[i] = generator.nextInt();
        }

        @SuppressWarnings("unchecked")
        Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, classes);

        h[0] = BinaryArrayHeap.heapify(a);
        h[1] = DaryArrayHeap.heapify(2, a);
        h[2] = DaryArrayHeap.heapify(3, a);
        h[3] = DaryArrayHeap.heapify(4, a);
        h[4] = DaryArrayHeap.heapify(5, a);
        h[5] = BinaryArrayWeakHeap.heapify(a);
        h[6] = BinaryArrayBulkInsertWeakHeap.heapify(a);
        h[7] = MinMaxBinaryArrayDoubleEndedHeap.heapify(a);

        int elements = SIZE;
        Integer prev = null, cur;
        while (elements > 0) {
            cur = h[0].findMin();
            for (int i = 1; i < classes; i++) {
                assertEquals(cur.intValue(), h[i].findMin().intValue());
            }
            for (int i = 0; i < classes; i++) {
                h[i].deleteMin();
            }
            if (prev != null) {
                assertTrue(prev.compareTo(cur) <= 0);
            }
            prev = cur;
            elements--;
        }
    }

    @Test
    public void testHeapifySortWithComparator() {
        Random generator = new Random(1);

        final int classes = 8;

        Integer[] a = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) {
            a[i] = generator.nextInt();
        }

        @SuppressWarnings("unchecked")
        Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, classes);

        h[0] = BinaryArrayHeap.heapify(a, comparator);
        h[1] = DaryArrayHeap.heapify(2, a, comparator);
        h[2] = DaryArrayHeap.heapify(3, a, comparator);
        h[3] = DaryArrayHeap.heapify(4, a, comparator);
        h[4] = DaryArrayHeap.heapify(5, a, comparator);
        h[5] = BinaryArrayWeakHeap.heapify(a, comparator);
        h[6] = BinaryArrayBulkInsertWeakHeap.heapify(a, comparator);
        h[7] = MinMaxBinaryArrayDoubleEndedHeap.heapify(a, comparator);

        int elements = SIZE;
        Integer prev = null, cur;
        while (elements > 0) {
            cur = h[0].findMin();
            for (int i = 1; i < classes; i++) {
                assertEquals(cur.intValue(), h[i].findMin().intValue());
            }
            for (int i = 0; i < classes; i++) {
                h[i].deleteMin();
            }
            if (prev != null) {
                assertTrue(comparator.compare(prev, cur) <= 0);
            }
            prev = cur;
            elements--;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHeapifyZeroLengthArray() {
        Integer[] a = new Integer[0];

        final int nonfixed = 8;

        Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, nonfixed);

        h[0] = BinaryArrayHeap.heapify(a);
        h[1] = DaryArrayHeap.heapify(2, a);
        h[2] = DaryArrayHeap.heapify(3, a);
        h[3] = DaryArrayHeap.heapify(4, a);
        h[4] = DaryArrayHeap.heapify(5, a);
        h[5] = BinaryArrayWeakHeap.heapify(a);
        h[6] = BinaryArrayBulkInsertWeakHeap.heapify(a);
        h[7] = MinMaxBinaryArrayDoubleEndedHeap.heapify(a);

        for (int i = 0; i < nonfixed; i++) {
            assertTrue(h[i].isEmpty());
            try {
                h[i].insert(1);
            } catch (IllegalStateException e) {
                fail("No!");
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHeapifyZeroLengthArrayComparator() {
        Integer[] a = new Integer[0];

        final int nonfixed = 8;

        Heap<Integer>[] h = (Heap<Integer>[]) Array.newInstance(Heap.class, nonfixed);

        h[0] = BinaryArrayHeap.heapify(a, comparator);
        h[1] = DaryArrayHeap.heapify(2, a, comparator);
        h[2] = DaryArrayHeap.heapify(3, a, comparator);
        h[3] = DaryArrayHeap.heapify(4, a, comparator);
        h[4] = DaryArrayHeap.heapify(5, a, comparator);
        h[5] = BinaryArrayWeakHeap.heapify(a, comparator);
        h[6] = BinaryArrayBulkInsertWeakHeap.heapify(a, comparator);
        h[7] = MinMaxBinaryArrayDoubleEndedHeap.heapify(a, comparator);

        for (int i = 0; i < nonfixed; i++) {
            assertTrue(h[i].isEmpty());
            try {
                h[i].insert(1);
            } catch (IllegalStateException e) {
                fail("No!");
            }
        }
    }

    @Test
    public void testHeapifyZeroLengthArray1() {
        Integer[] a = new Integer[0];

        final int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);

        h[0] = BinaryArrayAddressableHeap.heapify(a, null);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, null);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, null);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, null);

        for (int i = 0; i < classes; i++) {
            assertTrue(h[i].isEmpty());
            try {
                assertEquals(1, h[i].insert(1).getKey().intValue());
            } catch (IllegalStateException e) {
                fail("No!");
            }
        }
    }

    @Test
    public void testHeapifyZeroLengthArray2() {
        Integer[] a = new Integer[0];

        final int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);
        h[0] = BinaryArrayAddressableHeap.heapify(a, null);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, null);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, null);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, null);

        for (int i = 0; i < classes; i++) {
            h[i].insert(1);
            h[i].insert(2);
            h[i].insert(3);
            h[i].insert(4);
            assertEquals(4, h[i].size());
        }
    }

    @Test
    public void testHeapifyZeroLengthArrayComparator1() {
        Integer[] a = new Integer[0];

        final int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);
        h[0] = BinaryArrayAddressableHeap.heapify(a, null, comparator);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, null, comparator);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, null, comparator);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, null, comparator);

        for (int i = 0; i < classes; i++) {
            assertTrue(h[i].isEmpty());
            try {
                assertEquals(1, h[i].insert(1).getKey().intValue());
            } catch (IllegalStateException e) {
                fail("No!");
            }
        }
    }

    @Test
    public void testHeapifyBadParameters() {
        Integer[] a = new Integer[0];

        try {
            BinaryArrayHeap.heapify(null);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayHeap.heapify(null, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayHeap.heapify(1, a);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayHeap.heapify(1, a, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayHeap.heapify(2, null);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayHeap.heapify(2, null, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayAddressableHeap.heapify(null, null);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayAddressableHeap.heapify(null, null, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayAddressableHeap.heapify(new Integer[2], new Integer[3]);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayAddressableHeap.heapify(new Integer[2], new Integer[3], comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayAddressableHeap.heapify(1, new Integer[2], new Integer[2]);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayAddressableHeap.heapify(3, null, new Integer[2]);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayAddressableHeap.heapify(3, new Integer[3], new Integer[2]);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayAddressableHeap.heapify(1, new Integer[2], new Integer[2], comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayAddressableHeap.heapify(3, null, new Integer[2], comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            DaryArrayAddressableHeap.heapify(3, new Integer[3], new Integer[2], comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayWeakHeap.heapify(null);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayWeakHeap.heapify(null, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayBulkInsertWeakHeap.heapify(null);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BinaryArrayBulkInsertWeakHeap.heapify(null, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            MinMaxBinaryArrayDoubleEndedHeap.heapify(null);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            MinMaxBinaryArrayDoubleEndedHeap.heapify(null, comparator);
            fail("No!");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testArrayAddressableHeapifySort() {
        Random generator = new Random(1);

        Integer[] a = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) {
            a[i] = generator.nextInt();
        }

        int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);
        h[0] = BinaryArrayAddressableHeap.heapify(a, null);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, null);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, null);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, null);

        int elements = SIZE;
        Integer[] prev = new Integer[classes];
        Integer[] cur = new Integer[classes];
        while (elements > 0) {
            for (int i = 0; i < classes; i++) {
                cur[i] = h[i].findMin().getKey();
                if (i > 0) {
                    assertEquals(cur[0], cur[i]);
                }
            }
            for (int i = 0; i < classes; i++) {
                h[i].deleteMin();
            }
            for (int i = 0; i < classes; i++) {
                if (prev[i] != null) {
                    assertTrue(prev[i].compareTo(cur[i]) <= 0);
                }
                prev[i] = cur[i];
            }
            elements--;
        }
    }

    @Test
    public void testArrayAddressableHeapifySortWithValues() {
        Random generator = new Random(1);

        Integer[] a = new Integer[SIZE];
        String[] b = new String[SIZE];
        for (int i = 0; i < SIZE; i++) {
            a[i] = generator.nextInt();
            b[i] = a[i].toString();
        }

        int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);
        h[0] = BinaryArrayAddressableHeap.heapify(a, b);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, b);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, b);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, b);

        int elements = SIZE;
        Integer[] prev = new Integer[classes];
        Integer[] cur = new Integer[classes];
        while (elements > 0) {
            for (int i = 0; i < classes; i++) {
                cur[i] = h[i].findMin().getKey();
                assertEquals(h[i].findMin().getValue(), h[i].findMin().getKey().toString());
                if (i > 0) {
                    assertEquals(cur[0], cur[i]);
                }
            }
            for (int i = 0; i < classes; i++) {
                h[i].deleteMin();
            }
            for (int i = 0; i < classes; i++) {
                if (prev[i] != null) {
                    assertTrue(prev[i].compareTo(cur[i]) <= 0);
                }
                prev[i] = cur[i];
            }
            elements--;
        }
    }

    @Test
    public void testArrayAddressableHeapifySortComparator() {
        Random generator = new Random(1);

        Integer[] a = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) {
            a[i] = generator.nextInt();
        }

        int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);
        h[0] = BinaryArrayAddressableHeap.heapify(a, null, comparator);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, null, comparator);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, null, comparator);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, null, comparator);

        int elements = SIZE;
        Integer[] prev = new Integer[classes];
        Integer[] cur = new Integer[classes];
        while (elements > 0) {
            for (int i = 0; i < classes; i++) {
                cur[i] = h[i].findMin().getKey();
                if (i > 0) {
                    assertEquals(cur[0], cur[i]);
                }
            }
            for (int i = 0; i < classes; i++) {
                h[i].deleteMin();
            }
            for (int i = 0; i < classes; i++) {
                if (prev[i] != null) {
                    assertTrue(comparator.compare(prev[i], cur[i]) <= 0);
                }
                prev[i] = cur[i];
            }
            elements--;
        }
    }

    @Test
    public void testArrayAddressableHeapifySortComparatorWithValues() {
        Random generator = new Random(1);

        Integer[] a = new Integer[SIZE];
        String[] b = new String[SIZE];
        for (int i = 0; i < SIZE; i++) {
            a[i] = generator.nextInt();
            b[i] = a[i].toString();
        }

        int classes = 4;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, String>[] h = (AddressableHeap<Integer, String>[]) Array
                .newInstance(AddressableHeap.class, classes);
        h[0] = BinaryArrayAddressableHeap.heapify(a, b, comparator);
        h[1] = DaryArrayAddressableHeap.heapify(3, a, b, comparator);
        h[2] = DaryArrayAddressableHeap.heapify(4, a, b, comparator);
        h[3] = DaryArrayAddressableHeap.heapify(5, a, b, comparator);

        int elements = SIZE;
        Integer[] prev = new Integer[classes];
        Integer[] cur = new Integer[classes];
        while (elements > 0) {
            for (int i = 0; i < classes; i++) {
                cur[i] = h[i].findMin().getKey();
                assertEquals(h[i].findMin().getValue(), h[i].findMin().getKey().toString());
                if (i > 0) {
                    assertEquals(cur[0], cur[i]);
                }
            }
            for (int i = 0; i < classes; i++) {
                h[i].deleteMin();
            }
            for (int i = 0; i < classes; i++) {
                if (prev[i] != null) {
                    assertTrue(comparator.compare(prev[i], cur[i]) <= 0);
                }
                prev[i] = cur[i];
            }
            elements--;
        }
    }
}
