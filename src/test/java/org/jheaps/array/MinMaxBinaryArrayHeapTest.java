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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.jheaps.Heap;
import org.jheaps.tree.AbstractHeapTest;
import org.junit.Test;

public class MinMaxBinaryArrayHeapTest extends AbstractHeapTest {

    protected Heap<Long> createHeap() {
        return new MinMaxBinaryArrayDoubleEndedHeap<Long>();
    }

    protected Heap<Long> createHeap(int capacity) {
        return new MinMaxBinaryArrayDoubleEndedHeap<Long>(capacity);
    }

    @Override
    protected Heap<Long> createHeap(Comparator<Long> comparator) {
        return new MinMaxBinaryArrayDoubleEndedHeap<Long>(comparator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSize() {
        Heap<Long> h = createHeap(-4);
        h.insert(1l);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSize1() {
        Heap<Long> h = createHeap(-1);
        h.insert(1l);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSize2() {
        Heap<Long> h = createHeap(Integer.MAX_VALUE - 8);
        h.insert(1l);
    }

    @Test
    public void testSimple() {
        MinMaxBinaryArrayDoubleEndedHeap<Integer> h = new MinMaxBinaryArrayDoubleEndedHeap<Integer>();
        h.insert(1);
        assertEquals(1, h.findMin().intValue());
        assertEquals(1, h.findMax().intValue());
        h.insert(2);
        assertEquals(1, h.findMin().intValue());
        assertEquals(2, h.findMax().intValue());
        h.insert(3);
        assertEquals(1, h.findMin().intValue());
        assertEquals(3, h.findMax().intValue());
        h.insert(10);
        assertEquals(1, h.findMin().intValue());
        assertEquals(10, h.findMax().intValue());
        h.insert(11);
        assertEquals(1, h.findMin().intValue());
        assertEquals(11, h.findMax().intValue());
        h.insert(12);
        assertEquals(1, h.findMin().intValue());
        assertEquals(12, h.findMax().intValue());
        h.insert(5);
        assertEquals(1, h.findMin().intValue());
        assertEquals(12, h.findMax().intValue());
        h.insert(7);
        assertEquals(1, h.findMin().intValue());
        assertEquals(12, h.findMax().intValue());
        h.insert(15);
        assertEquals(1, h.findMin().intValue());
        assertEquals(15, h.findMax().intValue());
        h.insert(100);
        assertEquals(1, h.findMin().intValue());
        assertEquals(100, h.findMax().intValue());
        h.insert(200);
        assertEquals(1, h.findMin().intValue());
        assertEquals(200, h.findMax().intValue());

        assertEquals(1, h.deleteMin().intValue());

        assertEquals(2, h.findMin().intValue());
        assertEquals(200, h.findMax().intValue());
    }

    @Test
    public void testonMinLevel() {
        MinMaxBinaryArrayDoubleEndedHeap<Long> h = new MinMaxBinaryArrayDoubleEndedHeap<Long>();
        assertTrue(h.onMinLevel(1));
        assertFalse(h.onMinLevel(2));
        assertFalse(h.onMinLevel(3));
        assertTrue(h.onMinLevel(4));
        assertTrue(h.onMinLevel(5));
        assertTrue(h.onMinLevel(6));
        assertTrue(h.onMinLevel(7));
        for (int i = 8; i < 16; i++) {
            assertFalse(h.onMinLevel(i));
        }
        for (int i = 16; i < 32; i++) {
            assertTrue(h.onMinLevel(i));
        }
        for (int i = 32; i < 64; i++) {
            assertFalse(h.onMinLevel(i));
        }
        for (int i = 64; i < 128; i++) {
            assertTrue(h.onMinLevel(i));
        }
    }

}
