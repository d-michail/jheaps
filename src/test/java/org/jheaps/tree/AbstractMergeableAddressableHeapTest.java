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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Comparator;

import org.jheaps.AddressableHeap.Handle;
import org.jheaps.MergeableAddressableHeap;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractMergeableAddressableHeapTest {

    protected static final int SIZE = 100000;

    private static Comparator<Integer> comparator;

    private static class TestComparator implements Comparator<Integer>, Serializable {
        private static final long serialVersionUID = 1L;

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
    }

    @BeforeClass
    public static void setUpClass() {
        comparator = new TestComparator();
    }

    protected abstract MergeableAddressableHeap<Integer, String> createHeap(Comparator<Integer> comparator);

    protected abstract MergeableAddressableHeap<Integer, String> createHeap();

    @Test
    public void testMeld1() {
        MergeableAddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        MergeableAddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        Handle<Integer, String> b4 = b.insert(17);

        a.meld(b);

        assertEquals(8, a.size());
        assertTrue(b.isEmpty());
        assertEquals(0, b.size());

        b4.decreaseKey(9);
        assertEquals(Integer.valueOf(9), a.findMin().getKey());
    }

    @Test
    public void testMeld2() {
        MergeableAddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        MergeableAddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        Handle<Integer, String> b4 = b.insert(17);

        MergeableAddressableHeap<Integer, String> c = createHeap();
        c.insert(18);
        c.insert(19);
        c.insert(20);
        Handle<Integer, String> c4 = c.insert(21);

        a.meld(b);
        a.meld(c);

        assertEquals(12, a.size());
        assertTrue(b.isEmpty());
        assertEquals(0, b.size());

        assertTrue(c.isEmpty());
        assertEquals(0, c.size());

        assertEquals(Integer.valueOf(10), a.findMin().getKey());
        b4.decreaseKey(9);
        assertEquals(Integer.valueOf(9), a.findMin().getKey());
        c4.decreaseKey(8);
        assertEquals(Integer.valueOf(8), a.findMin().getKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleMelds() {
        MergeableAddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        MergeableAddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        MergeableAddressableHeap<Integer, String> c = createHeap();
        c.insert(18);
        c.insert(19);
        c.insert(20);
        c.insert(21);

        a.meld(b);
        a.meld(b);
    }

    @Test(expected = IllegalStateException.class)
    public void testInsertAfterAMeld() {
        MergeableAddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        MergeableAddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        a.meld(b);
        b.insert(30);
    }

    @Test
    public void testCascadingMelds() {
        MergeableAddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        MergeableAddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        MergeableAddressableHeap<Integer, String> c = createHeap();
        c.insert(18);
        c.insert(19);
        c.insert(20);
        c.insert(21);

        MergeableAddressableHeap<Integer, String> d = createHeap();
        d.insert(22);
        d.insert(23);
        d.insert(24);
        d.insert(25);

        MergeableAddressableHeap<Integer, String> e = createHeap();
        e.insert(26);
        e.insert(27);
        Handle<Integer, String> e3 = e.insert(28);
        Handle<Integer, String> e4 = e.insert(29);

        d.meld(e);
        c.meld(d);
        b.meld(c);
        a.meld(b);

        assertEquals(20, a.size());
        assertEquals(0, b.size());
        assertEquals(0, c.size());
        assertEquals(0, d.size());
        assertEquals(0, e.size());

        assertEquals(Integer.valueOf(10), a.findMin().getKey());
        e4.decreaseKey(9);
        assertEquals(Integer.valueOf(9), a.findMin().getKey());
        e3.decreaseKey(8);
        assertEquals(Integer.valueOf(8), a.findMin().getKey());
        e3.delete();
        assertEquals(Integer.valueOf(9), a.findMin().getKey());
    }

    @Test
    public void testMeldGeneric() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap();

        for (int i = 0; i < SIZE; i++) {
            h1.insert(2 * i);
        }

        MergeableAddressableHeap<Integer, String> h2 = createHeap();
        for (int i = 0; i < SIZE; i++) {
            h2.insert(2 * i + 1);
        }

        h1.meld(h2);

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

    @Test
    public void testMeldGeneric1() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap();

        MergeableAddressableHeap<Integer, String> h2 = createHeap();
        for (int i = 0; i < SIZE; i++) {
            h2.insert(i);
        }

        h1.meld(h2);

        assertEquals(h1.size(), SIZE);
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

    @Test
    public void testMeldGeneric2() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap();

        MergeableAddressableHeap<Integer, String> h2 = createHeap();
        for (int i = 0; i < SIZE; i++) {
            h1.insert(i);
        }

        h1.meld(h2);

        assertEquals(h1.size(), SIZE);
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

    @Test
    public void testMeld() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap();
        MergeableAddressableHeap<Integer, String> h2 = createHeap();

        for (int i = 0; i < SIZE; i++) {
            if (i % 2 == 0) {
                h1.insert(i);
            } else {
                h2.insert(i);
            }
        }

        h1.meld(h2);

        assertTrue(h2.isEmpty());
        assertEquals(0, h2.size());

        for (int i = 0; i < SIZE; i++) {
            assertEquals(Integer.valueOf(i), h1.findMin().getKey());
            h1.deleteMin();
        }
        assertTrue(h1.isEmpty());
    }

    @Test
    public void testMeldWithComparator() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap(comparator);
        MergeableAddressableHeap<Integer, String> h2 = createHeap(comparator);

        for (int i = 0; i < SIZE; i++) {
            if (i % 2 == 0) {
                h1.insert(i);
            } else {
                h2.insert(i);
            }
        }

        h1.meld(h2);

        assertTrue(h2.isEmpty());
        assertEquals(0, h2.size());

        for (int i = 0; i < SIZE; i++) {
            assertEquals(Integer.valueOf(SIZE - i - 1), h1.findMin().getKey());
            h1.deleteMin();
        }
        assertTrue(h1.isEmpty());
    }

    @Test
    public void testMeldBadComparator() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap(comparator);
        MergeableAddressableHeap<Integer, String> h2 = createHeap(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return (int) (o1 - o2);
            }
        });

        for (int i = 0; i < SIZE; i++) {
            if (i % 2 == 0) {
                h1.insert(i);
            } else {
                h2.insert(i);
            }
        }

        try {
            h1.meld(h2);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testMeldBadComparator1() {
        MergeableAddressableHeap<Integer, String> h1 = createHeap(comparator);
        MergeableAddressableHeap<Integer, String> h2 = createHeap(null);
        try {
            h1.meld(h2);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

}
