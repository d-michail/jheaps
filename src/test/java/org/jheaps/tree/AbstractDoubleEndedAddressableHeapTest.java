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

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.jheaps.DoubleEndedAddressableHeap;
import org.junit.Test;

public abstract class AbstractDoubleEndedAddressableHeapTest extends AbstractAddressableHeapTest {

    protected abstract DoubleEndedAddressableHeap<Integer, Void> createHeap();

    protected abstract DoubleEndedAddressableHeap<Integer, Void> createHeap(Comparator<Integer> comparator);

    protected abstract DoubleEndedAddressableHeap<Integer, String> createHeapWithStringValues();

    @Test
    @Override
    public void test() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        for (int i = 0; i < SIZE; i++) {
            h.insert(i);
            assertEquals(Integer.valueOf(0), h.findMin().getKey());
            assertEquals(Integer.valueOf(i), h.findMax().getKey());
            assertFalse(h.isEmpty());
            assertEquals(h.size(), i + 1);
        }

        for (int i = SIZE - 1; i >= 0; i--) {
            assertEquals(h.findMin().getKey(), Integer.valueOf(SIZE - i - 1));
            assertEquals(h.findMax().getKey(), Integer.valueOf(SIZE - 1));
            h.deleteMin();
        }
    }

    @Test
    @Override
    public void testOnlyInsert() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        for (int i = 0; i < SIZE; i++) {
            h.insert(SIZE - i);
            assertEquals(Integer.valueOf(SIZE - i), h.findMin().getKey());
            assertEquals(Integer.valueOf(SIZE), h.findMax().getKey());
            assertFalse(h.isEmpty());
            assertEquals(h.size(), i + 1);
        }
    }

    @Test
    @Override
    public void testComparator() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap(comparator);
        int i;

        for (i = 0; i < SIZE; i++) {
            h.insert(i);
            assertEquals(Integer.valueOf(i), h.findMin().getKey());
            assertEquals(Integer.valueOf(0), h.findMax().getKey());
            assertFalse(h.isEmpty());
            assertEquals(h.size(), i + 1);
        }

        for (i = SIZE - 1; i >= 0; i--) {
            assertEquals(h.findMin().getKey(), Integer.valueOf(i));
            assertEquals(h.findMax().getKey(), Integer.valueOf(0));
            h.deleteMin();
        }
    }

    @Test
    @Override
    public void testOnly4() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        assertTrue(h.isEmpty());

        h.insert(780);
        assertEquals(h.size(), 1);
        assertEquals(Integer.valueOf(780), h.findMin().getKey());
        assertEquals(Integer.valueOf(780), h.findMax().getKey());

        h.insert(-389);
        assertEquals(h.size(), 2);
        assertEquals(Integer.valueOf(-389), h.findMin().getKey());
        assertEquals(Integer.valueOf(780), h.findMax().getKey());

        h.insert(306);
        assertEquals(h.size(), 3);
        assertEquals(Integer.valueOf(-389), h.findMin().getKey());
        assertEquals(Integer.valueOf(780), h.findMax().getKey());

        h.insert(579);
        assertEquals(h.size(), 4);
        assertEquals(Integer.valueOf(-389), h.findMin().getKey());
        assertEquals(Integer.valueOf(780), h.findMax().getKey());

        h.deleteMin();
        assertEquals(h.size(), 3);
        assertEquals(Integer.valueOf(306), h.findMin().getKey());
        assertEquals(Integer.valueOf(780), h.findMax().getKey());

        h.deleteMax();
        assertEquals(h.size(), 2);
        assertEquals(Integer.valueOf(306), h.findMin().getKey());
        assertEquals(Integer.valueOf(579), h.findMax().getKey());

        h.deleteMin();
        assertEquals(h.size(), 1);
        assertEquals(Integer.valueOf(579), h.findMin().getKey());
        assertEquals(Integer.valueOf(579), h.findMax().getKey());

        h.deleteMax();
        assertEquals(h.size(), 0);

        assertTrue(h.isEmpty());
    }

    @Test
    @Override
    public void testOnly4Reverse() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap(comparator);

        assertTrue(h.isEmpty());

        h.insert(780);
        assertEquals(h.size(), 1);
        assertEquals(Integer.valueOf(780), h.findMin().getKey());
        assertEquals(Integer.valueOf(780), h.findMax().getKey());

        h.insert(-389);
        assertEquals(h.size(), 2);
        assertEquals(Integer.valueOf(780), h.findMin().getKey());
        assertEquals(Integer.valueOf(-389), h.findMax().getKey());

        h.insert(306);
        assertEquals(h.size(), 3);
        assertEquals(Integer.valueOf(780), h.findMin().getKey());
        assertEquals(Integer.valueOf(-389), h.findMax().getKey());

        h.insert(579);
        assertEquals(h.size(), 4);
        assertEquals(Integer.valueOf(780), h.findMin().getKey());
        assertEquals(Integer.valueOf(-389), h.findMax().getKey());

        h.deleteMin();
        assertEquals(h.size(), 3);
        assertEquals(Integer.valueOf(579), h.findMin().getKey());
        assertEquals(Integer.valueOf(-389), h.findMax().getKey());

        h.deleteMax();
        assertEquals(h.size(), 2);
        assertEquals(Integer.valueOf(579), h.findMin().getKey());
        assertEquals(Integer.valueOf(306), h.findMax().getKey());

        h.deleteMin();
        assertEquals(h.size(), 1);
        assertEquals(Integer.valueOf(306), h.findMin().getKey());
        assertEquals(Integer.valueOf(306), h.findMax().getKey());

        h.deleteMin();
        assertEquals(h.size(), 0);

        assertTrue(h.isEmpty());
    }

    @Test
    public void testSortRandomSeed1Max() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.findMax().getKey();
            h.deleteMax();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) >= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testSort1RandomSeed1Max() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.deleteMax().getKey();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) >= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testSortRandomSeed2Max() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        Random generator = new Random(2);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.findMax().getKey();
            h.deleteMax();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) >= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testSort2RandomSeed2Max() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        Random generator = new Random(2);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        Integer prev = null, cur;
        while (!h.isEmpty()) {
            cur = h.deleteMax().getKey();
            if (prev != null) {
                assertTrue(prev.compareTo(cur) >= 0);
            }
            prev = cur;
        }
    }

    @Test
    public void testFindMaxDeleteMaxSameObject() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        Random generator = new Random(1);

        for (int i = 0; i < SIZE; i++) {
            h.insert(generator.nextInt());
        }

        while (!h.isEmpty()) {
            assertEquals(h.findMax(), h.deleteMax());
        }
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testDelete() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        DoubleEndedAddressableHeap.Handle<Integer, Void> array[];
        array = new DoubleEndedAddressableHeap.Handle[15];
        for (int i = 0; i < 15; i++) {
            array[i] = h.insert(i);
        }

        array[5].delete();
        assertEquals(Integer.valueOf(0), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[7].delete();
        assertEquals(Integer.valueOf(0), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[0].delete();
        assertEquals(Integer.valueOf(1), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[2].delete();
        assertEquals(Integer.valueOf(1), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[1].delete();
        assertEquals(Integer.valueOf(3), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[3].delete();
        assertEquals(Integer.valueOf(4), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[9].delete();
        assertEquals(Integer.valueOf(4), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[4].delete();
        assertEquals(Integer.valueOf(6), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[8].delete();
        assertEquals(Integer.valueOf(6), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[11].delete();
        assertEquals(Integer.valueOf(6), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[6].delete();
        assertEquals(Integer.valueOf(10), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[12].delete();
        assertEquals(Integer.valueOf(10), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[10].delete();
        assertEquals(Integer.valueOf(13), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[13].delete();
        assertEquals(Integer.valueOf(14), h.findMin().getKey());
        assertEquals(Integer.valueOf(14), h.findMax().getKey());
        array[14].delete();
        assertTrue(h.isEmpty());
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testDelete1() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        DoubleEndedAddressableHeap.Handle<Integer, Void> array[];
        array = new DoubleEndedAddressableHeap.Handle[8];
        for (int i = 0; i < 8; i++) {
            array[i] = h.insert(i);
        }

        array[5].delete();
        assertEquals(Integer.valueOf(0), h.findMin().getKey());
        assertEquals(Integer.valueOf(7), h.findMax().getKey());
        array[7].delete();
        assertEquals(Integer.valueOf(0), h.findMin().getKey());
        assertEquals(Integer.valueOf(6), h.findMax().getKey());
        array[0].delete();
        assertEquals(Integer.valueOf(1), h.findMin().getKey());
        assertEquals(Integer.valueOf(6), h.findMax().getKey());
        array[2].delete();
        assertEquals(Integer.valueOf(1), h.findMin().getKey());
        assertEquals(Integer.valueOf(6), h.findMax().getKey());
        array[1].delete();
        assertEquals(Integer.valueOf(3), h.findMin().getKey());
        assertEquals(Integer.valueOf(6), h.findMax().getKey());
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testAddDelete() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        DoubleEndedAddressableHeap.Handle<Integer, Void> array[];
        array = new DoubleEndedAddressableHeap.Handle[SIZE];
        for (int i = 0; i < SIZE; i++) {
            array[i] = h.insert(i);
        }

        for (int i = SIZE - 1; i >= 0; i--) {
            array[i].delete();
            if (i > 0) {
                assertEquals(Integer.valueOf(0), h.findMin().getKey());
                assertEquals(Integer.valueOf(i - 1), h.findMax().getKey());
            }
        }
        assertTrue(h.isEmpty());
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testAddDeleteComparator() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap(comparator);

        DoubleEndedAddressableHeap.Handle<Integer, Void> array[];
        array = new DoubleEndedAddressableHeap.Handle[SIZE];
        for (int i = 0; i < SIZE; i++) {
            array[i] = h.insert(i);
        }

        for (int i = 0; i < SIZE; i++) {
            array[i].delete();
            if (i < SIZE - 1) {
                assertEquals(Integer.valueOf(SIZE - 1), h.findMin().getKey());
                assertEquals(Integer.valueOf(i + 1), h.findMax().getKey());
            }
        }
        assertTrue(h.isEmpty());
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testAddDecreaseKeyDeleteMin() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        DoubleEndedAddressableHeap.Handle<Integer, Void> array[];
        array = new DoubleEndedAddressableHeap.Handle[SIZE];

        for (int i = 0; i < SIZE; i++) {
            array[i] = h.insert(i);
        }

        assertEquals(0, h.findMin().getKey().intValue());
        assertEquals(SIZE - 1, h.findMax().getKey().intValue());

        for (int i = SIZE / 2; i < SIZE / 2 + 10; i++) {
            array[i].decreaseKey(i / 2);
        }

        array[0].delete();

        assertEquals(1, h.findMin().getKey().intValue());

        for (int i = SIZE / 2 + 10; i < SIZE / 2 + 20; i++) {
            array[i].decreaseKey(0);
        }

        assertEquals(0, h.deleteMin().getKey().intValue());

        array[SIZE - 1].delete();

        assertEquals(SIZE - 2, h.findMax().getKey().intValue());

        for (int i = SIZE / 2 + 20; i < SIZE / 2 + 30; i++) {
            array[i].increaseKey(SIZE - 1);
        }

        assertEquals(SIZE - 1, h.deleteMax().getKey().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddDecreaseKeyDeleteMinComparator() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap(comparator);

        DoubleEndedAddressableHeap.Handle<Integer, Void> array[];
        array = new DoubleEndedAddressableHeap.Handle[SIZE];
        for (int i = 0; i < SIZE; i++) {
            array[i] = h.insert(i);
        }

        assertEquals(SIZE - 1, h.findMin().getKey().intValue());
        assertEquals(0, h.findMax().getKey().intValue());

        for (int i = SIZE / 2; i < SIZE / 2 + 10; i++) {
            array[i].decreaseKey(SIZE - 1);
        }

        array[SIZE - 1].delete();

        for (int i = SIZE / 2 + 10; i < SIZE / 2 + 20; i++) {
            array[i].decreaseKey(SIZE - 1);
        }

        assertEquals(SIZE - 1, h.deleteMin().getKey().intValue());

        assertEquals(0, h.findMax().getKey().intValue());
        array[0].delete();
        assertEquals(1, h.findMax().getKey().intValue());

        for (int i = SIZE / 2 + 20; i < SIZE / 2 + 30; i++) {
            array[i].increaseKey(0);
        }

        assertEquals(0, h.findMax().getKey().intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteMaxDeleteTwice() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();
        h.insert(50);
        DoubleEndedAddressableHeap.Handle<Integer, Void> e1 = h.insert(100);
        h.deleteMax();
        e1.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteMaxDeleteTwice1() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        for (int i = 100; i < 200; i++) {
            h.insert(i);
        }

        h.deleteMax().delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteMaxDecreaseKey() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        for (int i = 100; i < 200; i++) {
            h.insert(i);
        }
        h.deleteMax().decreaseKey(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteMaxIncreaseKey() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        for (int i = 100; i < 200; i++) {
            h.insert(i);
        }
        h.deleteMax().increaseKey(200);
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoElementFindMax() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();
        h.findMax();
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoElementDeleteMax() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();
        h.deleteMax();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongIncreaseKey() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();
        h.insert(15).increaseKey(14);
    }

    @Test
    public void testSameKeyAfterIncreaseDecrease() {
        DoubleEndedAddressableHeap<Integer, Void> h = createHeap();

        assertTrue(h.isEmpty());

        DoubleEndedAddressableHeap.Handle<Integer, Void> handle = h.insert(780);
        handle.increaseKey(780);
        assertEquals(780, h.deleteMax().getKey().intValue());

        h.insert(500);
        h.insert(600);
        DoubleEndedAddressableHeap.Handle<Integer, Void> handle1 = h.insert(700);
        handle1.increaseKey(700);
        assertEquals(700, handle1.getKey().intValue());
        handle1.decreaseKey(700);
        assertEquals(700, handle1.getKey().intValue());
    }

}
