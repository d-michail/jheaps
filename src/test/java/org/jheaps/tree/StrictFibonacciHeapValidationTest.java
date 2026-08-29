/*
 * (C) Copyright 2014-2026, by Dimitrios Michail
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jheaps.AddressableHeap;
import org.jheaps.tree.StrictFibonacciHeap.HeapRecord;
import org.jheaps.tree.StrictFibonacciHeap.Node;
import org.jheaps.tree.StrictFibonacciHeap.Rank;
import org.junit.Test;

/**
 * An extra, non-mandatory stress test for {@link StrictFibonacciHeap}, ported from the
 * structural-invariant validator ({@code Heap.validate}) and random-operation driver
 * ({@code test_random_operations}) of the paper's own reference implementation
 * (https://github.com/gsbrodal/strict-fibonacci-heaps). Given how intricate the
 * fix-list bookkeeping of this data structure is, this exercises a long random sequence
 * of every operation and, after each one, walks the whole internal representation of
 * every live heap re-checking invariants I1-I4 from the paper (node ranks/loss/degree
 * bounds, fix-list partition consistency, rank-list consistency), rather than only
 * checking externally observable sortedness like the shared {@code AbstractAddressableHeapTest}
 * suite does.
 */
public class StrictFibonacciHeapValidationTest {

    @Test
    public void testRandomOperationsSmall() {
        randomOperations(2000, 1, 50);
    }

    @Test
    public void testRandomOperationsMedium() {
        randomOperations(4000, 2, 300);
    }

    @Test
    public void testRandomOperationsLarge() {
        randomOperations(20000, 3, 5000);
    }

    @Test
    public void testRandomOperationsLargeFewDistinctKeys() {
        // stresses the tiebreak logic with many equal keys
        randomOperations(20000, 4, 5);
    }

    // ------------------------------------------------------------------
    // random operation driver
    // ------------------------------------------------------------------

    private static class TrackedHeap {
        final StrictFibonacciHeap<Integer, Void> heap = new StrictFibonacciHeap<Integer, Void>();
        final List<Integer> keys = new ArrayList<Integer>();
        final List<AddressableHeap.Handle<Integer, Void>> handles = new ArrayList<AddressableHeap.Handle<Integer, Void>>();
    }

    private static TrackedHeap popRandom(List<TrackedHeap> list, Random random) {
        int idx = random.nextInt(list.size());
        TrackedHeap h = list.get(idx);
        int last = list.size() - 1;
        list.set(idx, list.get(last));
        list.remove(last);
        return h;
    }

    private void randomOperations(int opCount, long seed, int keyBound) {
        Random random = new Random(seed);
        List<TrackedHeap> heaps = new ArrayList<TrackedHeap>();

        for (int iteration = 0; iteration < opCount; iteration++) {
            double p = random.nextDouble();
            if (heaps.isEmpty() || p < 0.05) {
                heaps.add(new TrackedHeap());
            } else if (p < 0.10) {
                if (heaps.size() >= 2) {
                    TrackedHeap h1 = popRandom(heaps, random);
                    TrackedHeap h2 = popRandom(heaps, random);
                    h1.heap.meld(h2.heap);
                    h1.keys.addAll(h2.keys);
                    h1.handles.addAll(h2.handles);
                    validate(h1.heap);
                    heaps.add(h1);
                }
            } else if (p < 0.5) {
                TrackedHeap h = heaps.get(random.nextInt(heaps.size()));
                if (!h.handles.isEmpty()) {
                    int idx = random.nextInt(h.handles.size());
                    AddressableHeap.Handle<Integer, Void> handle = h.handles.get(idx);
                    int oldKey = handle.getKey();
                    int newKey = oldKey - 1 - random.nextInt(25);
                    handle.decreaseKey(newKey);
                    h.keys.remove(Integer.valueOf(oldKey));
                    h.keys.add(newKey);
                    validate(h.heap);
                }
            } else if (p < 0.8) {
                TrackedHeap h = heaps.get(random.nextInt(heaps.size()));
                int key = random.nextInt(keyBound);
                AddressableHeap.Handle<Integer, Void> handle = h.heap.insert(key);
                h.keys.add(key);
                h.handles.add(handle);
                validate(h.heap);
            } else {
                TrackedHeap h = heaps.get(random.nextInt(heaps.size()));
                if (!h.keys.isEmpty()) {
                    int min = Collections.min(h.keys);
                    assertEquals(Integer.valueOf(min), h.heap.findMin().getKey());
                    AddressableHeap.Handle<Integer, Void> deleted = h.heap.deleteMin();
                    h.keys.remove(Integer.valueOf(deleted.getKey()));
                    h.handles.remove(deleted);
                    validate(h.heap);
                }
            }

            for (TrackedHeap h : heaps) {
                assertEquals(h.keys.size(), h.heap.size());
                List<Integer> actual = new ArrayList<Integer>();
                for (Node<Integer, Void> n : collectAll(h.heap.rec.root)) {
                    actual.add(n.key);
                }
                List<Integer> expected = new ArrayList<Integer>(h.keys);
                Collections.sort(expected);
                Collections.sort(actual);
                assertEquals(expected, actual);
            }
        }
    }

    // ------------------------------------------------------------------
    // structural invariant validation, ported from the reference's Heap.validate()
    // ------------------------------------------------------------------

    private static <K, V> List<Node<K, V>> children(Node<K, V> node) {
        List<Node<K, V>> result = new ArrayList<Node<K, V>>();
        Node<K, V> child = node.leftChild;
        if (child != null) {
            result.add(child);
            Node<K, V> cur = child.right;
            while (cur != child) {
                result.add(cur);
                cur = cur.right;
            }
        }
        return result;
    }

    private static <K, V> List<Node<K, V>> collectAll(Node<K, V> node) {
        List<Node<K, V>> out = new ArrayList<Node<K, V>>();
        if (node != null) {
            collectAll(node, out);
        }
        return out;
    }

    private static <K, V> void collectAll(Node<K, V> node, List<Node<K, V>> out) {
        out.add(node);
        for (Node<K, V> child : children(node)) {
            collectAll(child, out);
        }
    }

    private static <K, V> void validate(StrictFibonacciHeap<K, V> outer) {
        HeapRecord<K, V> heap = outer.rec;
        assertTrue(heap.isActive());
        validateRankList(heap);
        validateFixList(heap);
        if (heap.size == 0) {
            assertNull(heap.root);
            return;
        }
        assertNotNull(heap.root);
        List<Node<K, V>> all = collectAll(heap.root);
        assertEquals(heap.size, (long) all.size());

        int passive = 0;
        int free = 0;
        long lossSum = 0;
        for (Node<K, V> n : all) {
            if (n.passive()) {
                passive++;
            }
            if (n.free()) {
                free++;
            }
            if (n.fixed()) {
                lossSum += n.loss;
            }
        }
        double r = 1.25 * (Math.log(all.size()) / Math.log(2)) + 6;
        double delta = 2.5 * (Math.log(3.0 * all.size() - passive) / Math.log(2)) + 14;
        // Invariant I2
        assertTrue(free <= r + 1);
        // Invariant I3
        assertTrue(lossSum <= r + 1);

        validateTree(heap.root, null, heap, r, delta);
    }

    private static <K, V> void validateTree(Node<K, V> node, Node<K, V> parent, HeapRecord<K, V> heap, double r,
            double delta) {
        assertSame(parent, node.parent);
        if (parent != null) {
            assertTrue(heap.less(parent, node));
        }
        assertSame(node, node.right.left);
        assertSame(node, node.left.right);
        if (node.passive()) {
            assertEquals(0, node.heap().size);
        }
        if (node.active()) {
            assertSame(heap, node.heap());
        }
        // Invariant I1: fixed nodes have an active parent
        if (node.fixed()) {
            assertNotNull(parent);
            assertTrue(parent.active());
        }

        List<Node<K, V>> kids = children(node);
        int fixedCount = 0;
        boolean allowPassive = true;
        for (int i = kids.size() - 1; i >= 0; i--) {
            Node<K, V> child = kids.get(i);
            if (child.active()) {
                if (child.fixed()) {
                    fixedCount++;
                    // I1: the i'th rightmost fixed child has rank + loss >= i - 1
                    assertTrue(child.rank.rank + child.loss >= fixedCount - 1);
                }
                allowPassive = false;
            } else {
                assertTrue(child.passive());
                assertTrue(allowPassive);
            }
        }
        if (node.active()) {
            assertEquals(fixedCount, node.rank.rank);
        }
        // Invariant I4
        int degree = kids.size();
        if (node.active()) {
            assertTrue(degree <= delta);
        } else {
            assertTrue(degree <= delta - 1);
        }
        if (node.active()) {
            assertTrue(node.rank.rank <= r);
        }
        for (Node<K, V> child : kids) {
            validateTree(child, node, heap, r, delta);
        }
    }

    private static <K, V> void validateRankList(HeapRecord<K, V> heap) {
        Rank<K, V> rank = heap.rankList;
        if (heap.size == 0) {
            assertNull(rank);
            return;
        }
        assertNotNull(rank);
        assertNull(rank.prev);
        while (rank != null) {
            assertTrue(rank.referenceCount > 0);
            assertSame(heap, rank.heap);
            Node<K, V> free = rank.free;
            if (free != null) {
                assertTrue(free.free());
                assertSame(rank, free.rank);
            }
            Node<K, V> lossOne = rank.lossOne;
            if (lossOne != null) {
                assertTrue(lossOne.fixed());
                assertEquals(1, lossOne.loss);
                assertSame(rank, lossOne.rank);
            }
            if (rank.next != null) {
                assertSame(rank, rank.next.prev);
                assertTrue(rank.next.rank > rank.rank);
            }
            rank = rank.next;
        }
    }

    private static <K, V> void validateFixList(HeapRecord<K, V> heap) {
        if (heap.size == 0) {
            assertNull(heap.fixPassive);
            assertNull(heap.fixFreeMultiple);
            assertNull(heap.fixFreeSingle);
            assertNull(heap.fixLossZero);
            assertNull(heap.fixLossOneMultiple);
            assertNull(heap.fixLossOneSingle);
            assertNull(heap.fixLossTwo);
            return;
        }

        Node<K, V> head = heap.fixListHead();
        assertNotNull(head);

        // verify the cyclic fix-list links
        Node<K, V> node = head;
        assertSame(node, node.fixNext.fixPrev);
        node = node.fixNext;
        while (node != head) {
            assertSame(node, node.fixNext.fixPrev);
            node = node.fixNext;
        }

        // verify links from the heap record to the fix-list and its grouping
        node = head;
        if (heap.fixPassive != null) {
            assertSame(heap.fixPassive, node);
            assertTrue(node.passive());
            node = node.fixNext;
            while (node != head && node.passive()) {
                node = node.fixNext;
            }
        }

        Set<Integer> freeRanks = new HashSet<Integer>();
        if (heap.fixFreeMultiple != null) {
            assertSame(heap.fixFreeMultiple, node);
            assertNotSame(node, node.fixNext);
            assertNotSame(head, node.fixNext);
            assertTrue(node.free());
            assertTrue(node.fixNext.free());
            assertSame(node.rank, node.fixNext.rank);
            assertSame(node, node.rank.free);
            freeRanks.add(node.rank.rank);
            node = node.fixNext;
            while (node != head && node.free() && node.rank == node.fixPrev.rank) {
                node = node.fixNext;
            }
            while (node != head && node.free() && node.fixNext != head && node.fixNext.free()
                    && node.rank == node.fixNext.rank) {
                assertTrue(freeRanks.add(node.rank.rank));
                node = node.fixNext;
                while (node != head && node.free() && node.rank == node.fixPrev.rank) {
                    node = node.fixNext;
                }
            }
        }
        if (heap.fixFreeSingle != null) {
            assertSame(heap.fixFreeSingle, node);
            assertTrue(node.free());
            assertFalse(freeRanks.contains(node.rank.rank));
            assertTrue(heap.fixFreeMultiple != null || node.rank.free == node);
            freeRanks.add(node.rank.rank);
            node = node.fixNext;
            while (node != head && node.free()) {
                assertFalse(freeRanks.contains(node.rank.rank));
                freeRanks.add(node.rank.rank);
                node = node.fixNext;
            }
        }
        if (heap.fixLossZero != null) {
            assertSame(heap.fixLossZero, node);
            assertTrue(node.fixed());
            assertEquals(0, node.loss);
            node = node.fixNext;
            while (node != head && node.fixed() && node.loss == 0) {
                node = node.fixNext;
            }
        }
        Set<Integer> lossOneRanks = new HashSet<Integer>();
        if (heap.fixLossOneMultiple != null) {
            assertSame(heap.fixLossOneMultiple, node);
            assertNotSame(node, node.fixNext);
            assertNotSame(head, node.fixNext);
            assertTrue(node.fixed());
            assertTrue(node.fixNext.fixed());
            assertEquals(1, node.loss);
            assertEquals(1, node.fixNext.loss);
            assertSame(node.rank, node.fixNext.rank);
            assertSame(node, node.rank.lossOne);
            lossOneRanks.add(node.rank.rank);
            node = node.fixNext;
            while (node != head && node.fixed() && node.loss == 1 && node.rank == node.fixPrev.rank) {
                node = node.fixNext;
            }
            while (node != head && node.fixed() && node.loss == 1 && node.fixNext != head && node.fixNext.fixed()
                    && node.fixNext.loss == 1 && node.rank == node.fixNext.rank) {
                assertTrue(lossOneRanks.add(node.rank.rank));
                node = node.fixNext;
                while (node != head && node.fixed() && node.loss == 1 && node.rank == node.fixPrev.rank) {
                    node = node.fixNext;
                }
            }
        }
        if (heap.fixLossOneSingle != null) {
            assertSame(heap.fixLossOneSingle, node);
            assertTrue(node.fixed());
            assertEquals(1, node.loss);
            assertFalse(lossOneRanks.contains(node.rank.rank));
            assertTrue(heap.fixLossOneMultiple != null || node.rank.lossOne == node);
            lossOneRanks.add(node.rank.rank);
            node = node.fixNext;
            while (node != head && node.fixed() && node.loss == 1) {
                assertFalse(lossOneRanks.contains(node.rank.rank));
                lossOneRanks.add(node.rank.rank);
                node = node.fixNext;
            }
        }
        if (heap.fixLossTwo != null) {
            assertSame(heap.fixLossTwo, node);
            assertTrue(node.fixed());
            assertTrue(node.loss >= 2);
            node = node.fixNext;
            while (node != head && node.fixed() && node.loss >= 2) {
                node = node.fixNext;
            }
        }
        assertSame(head, node);
    }

}
