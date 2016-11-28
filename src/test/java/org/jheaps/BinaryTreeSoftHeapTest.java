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

import org.jheaps.BinaryTreeSoftHeap.SoftHandle;
import org.jheaps.BinaryTreeSoftHeap.RootListNode;
import org.jheaps.BinaryTreeSoftHeap.TreeNode;
import org.junit.BeforeClass;
import org.junit.Test;

public class BinaryTreeSoftHeapTest {

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

        BinaryTreeSoftHeap<Integer> a = new BinaryTreeSoftHeap<Integer>(epsilon);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        for (int i = 0; i < n; i++) {
            assertEquals(i, a.deleteMin().intValue());
        }

        assertTrue(a.isEmpty());
    }

    @Test
    public void testNoCorruptionWithComparatorAsHeap() {
        final int n = SIZE;
        double epsilon = 1.0 / (n + 1);

        BinaryTreeSoftHeap<Integer> a = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        for (int i = SIZE - 1; i >= 0; i--) {
            assertEquals(i, a.deleteMin().intValue());
        }

        assertTrue(a.isEmpty());
    }

    @Test
    public void testSort1RandomSeed1() {
        Heap<Integer> h = new BinaryTreeSoftHeap<Integer>(1.0 / (SIZE + 1));

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.deleteMin();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) <= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testSort1RandomSeed1WithComparator() {
        Heap<Integer> h = new BinaryTreeSoftHeap<Integer>(1.0 / (SIZE + 1), comparator);

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.deleteMin();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) >= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testComparator() {
        Heap<Long> h = new BinaryTreeSoftHeap<Long>(0.5);

        assertNull(h.comparator());
    }

    @Test
    public void testFindMinDeleteMinSameObject() {
        Heap<Long> h = new BinaryTreeSoftHeap<Long>(0.5);

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
    }

    @Test
    public void testSoftHeap1() {
        testSoftHeapInsert(0.25, null);
        testSoftHeapInsert(0.25, comparator);
        testSoftHeapInsertDeleteMin(0.25, null);
        testSoftHeapInsertDeleteMin(0.25, comparator);
    }

    @Test
    public void testSoftHeap2() {
        testSoftHeapInsert(0.5, null);
        testSoftHeapInsert(0.5, comparator);
        testSoftHeapInsertDeleteMin(0.5, null);
        testSoftHeapInsertDeleteMin(0.5, comparator);
    }

    @Test
    public void testSoftHeap3() {
        testSoftHeapInsert(0.75, null);
        testSoftHeapInsert(0.75, comparator);
        testSoftHeapInsertDeleteMin(0.75, null);
        testSoftHeapInsertDeleteMin(0.75, comparator);
    }

    @Test
    public void testSoftHeap4() {
        testSoftHeapInsert(0.99, null);
        testSoftHeapInsert(0.99, comparator);
        testSoftHeapInsertDeleteMin(0.99, null);
        testSoftHeapInsertDeleteMin(0.99, comparator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction1() {
        new BinaryTreeSoftHeap<Integer>(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction2() {
        new BinaryTreeSoftHeap<Integer>(1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction3() {
        new BinaryTreeSoftHeap<Integer>(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstruction4() {
        new BinaryTreeSoftHeap<Integer>(2.0);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIllegalDeleteMin() {
        new BinaryTreeSoftHeap<Integer>(0.5).deleteMin();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIllegalFindMin() {
        new BinaryTreeSoftHeap<Integer>(0.5).findMin();
    }

    @Test
    public void testIsEmpty() {
        BinaryTreeSoftHeap<Integer> h = new BinaryTreeSoftHeap<Integer>(0.5);
        assertTrue(h.isEmpty());
        h.insert(1);
        assertFalse(h.isEmpty());
    }

    @Test
    public void testClear() {
        BinaryTreeSoftHeap<Integer> h = new BinaryTreeSoftHeap<Integer>(0.5);
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
        Heap<Long> h = new BinaryTreeSoftHeap<Long>(1.0 / 16);

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
            assertEquals(Long.valueOf(i), h.findMin());
            h.deleteMin();
        }
        assertTrue(h.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMeldGeneric() {
        Heap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(1.0 / (SIZE + 1));

        if (h1 instanceof MergeableHeap) {
            for (int i = 0; i < SIZE; i++) {
                h1.insert(2 * i);
            }

            Heap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(1.0 / (SIZE + 1));
            for (int i = 0; i < SIZE; i++) {
                h2.insert(2 * i + 1);
            }

            ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

            assertEquals(h1.size(), SIZE * 2);
            assertEquals(h2.size(), 0);

            Integer prev = null, cur;
            while (!h1.isEmpty()) {
                cur = h1.findMin();
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

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong1() {
        Heap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(0.5, comparator);
        Heap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(0.5);

        ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong2() {
        Heap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(0.4);
        Heap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(0.7);

        ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong3() {
        Heap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(0.4);
        Heap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(0.4, comparator);

        ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMeldWrong4() {
        Heap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(0.4, comparator);
        Heap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(0.4, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return comparator.compare(o1, o2);
            }
        });

        ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);
    }

    private void testSoftHeapMeld(int n, double epsilon, Comparator<Integer> comparator) {
        BinaryTreeSoftHeap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);
        BinaryTreeSoftHeap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);

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

        ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

        validateSoftHeap(h1, epsilon, n);
        validateSoftHeap(h2, epsilon, n);
    }

    private void testSoftHeapMeldSmallLarge(int n, double epsilon, Comparator<Integer> comparator) {
        BinaryTreeSoftHeap<Integer> h1 = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);
        BinaryTreeSoftHeap<Integer> h2 = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);

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

        ((MergeableHeap<Integer>) h1).meld((MergeableHeap<Integer>) h2);

        validateSoftHeap(h1, epsilon, n);
        validateSoftHeap(h2, epsilon, n);
    }

    private void testSoftHeapInsert(double epsilon, Comparator<Integer> comparator) {
        final int n = SIZE;

        BinaryTreeSoftHeap<Integer> a = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        validateSoftHeap(a, epsilon, n);
    }

    private void testSoftHeapInsertDeleteMin(double epsilon, Comparator<Integer> comparator) {
        final int n = SIZE;

        BinaryTreeSoftHeap<Integer> a = new BinaryTreeSoftHeap<Integer>(epsilon, comparator);

        for (int i = 0; i < n; i++) {
            a.insert(i);
        }

        for (int i = 0; i < n / 4; i++) {
            a.deleteMin();
        }

        validateSoftHeap(a, epsilon, n);
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
    private static <K> void validateSoftHeap(BinaryTreeSoftHeap<K> h, double epsilon, long totalInsertedElements) {
        long total = 0;
        long corrupted = 0;
        Comparator<? super K> comparator = h.comparator();
        RootListNode<K> cur = h.rootList.head;
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
    private static <K> KeyCount validateRoot(TreeNode<K> root, Comparator<? super K> comparator) {
        if (root.left != null) {
            assertTrue(root.rank > root.left.rank);
            if (comparator == null) {
                assertTrue(((Comparable<? super K>) root.cKey).compareTo(root.left.cKey) <= 0);
            } else {
                assertTrue(comparator.compare(root.cKey, root.left.cKey) <= 0);
            }
        }
        if (root.right != null) {
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

        assertTrue(root.cSize > 0);
        long total = 0;
        long corrupted = 0;
        SoftHandle<K> e = root.cHead;
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
        assertEquals(total, root.cSize);

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
