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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.array.BinaryArrayAddressableHeap;
import org.junit.Test;

public class AddressableHeapsRandomTest {

    private static final int SIZE = 250000;

    @Test
    public void test() {
        test(new Random());
    }

    @Test
    public void testSeed13() {
        test(new Random(13));
    }

    @Test
    public void testSeed37() {
        test(new Random(37));
    }

    @Test
    public void testRandomDeletesSeed37() {
        testRandomDeletes(37);
    }

    @Test
    public void testRandomDelete() {
        testRandomDeletes(new Random().nextLong());
    }

    private void test(Random rng) {

        final int classes = 8;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, Void>[] h = (AddressableHeap<Integer, Void>[]) Array.newInstance(AddressableHeap.class,
                classes);
        h[0] = new PairingHeap<Integer, Void>();
        h[1] = new BinaryTreeAddressableHeap<Integer, Void>();
        h[2] = new FibonacciHeap<Integer, Void>();
        h[3] = new BinaryArrayAddressableHeap<Integer, Void>();
        h[4] = new CostlessMeldPairingHeap<Integer, Void>();
        h[5] = new SkewHeap<Integer, Void>();
        h[6] = new ReflectedPairingHeap<Integer, Void>();
        h[7] = new LeftistHeap<Integer, Void>();

        @SuppressWarnings("unchecked")
        List<Handle<Integer, Void>>[] s = (List<Handle<Integer, Void>>[]) Array.newInstance(List.class, classes);
        for (int j = 0; j < classes; j++) {
            s[j] = new ArrayList<Handle<Integer, Void>>();
        }

        for (int i = 0; i < SIZE; i++) {
            Integer k = rng.nextInt();
            for (int j = 0; j < classes; j++) {
                s[j].add(h[j].insert(k, null));
            }
            for (int j = 1; j < classes; j++) {
                assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
            }
        }

        for (int i = 0; i < 5; i++) {
            @SuppressWarnings("unchecked")
            Iterator<Handle<Integer, Void>>[] it = (Iterator<Handle<Integer, Void>>[]) Array.newInstance(Iterator.class,
                    classes);
            for (int j = 0; j < classes; j++) {
                it[j] = s[j].iterator();
            }

            while (true) {
                boolean shouldStop = false;
                for (int j = 0; j < classes; j++) {
                    if (!it[j].hasNext()) {
                        shouldStop = true;
                        break;
                    }
                }

                if (shouldStop) {
                    break;
                }

                @SuppressWarnings("unchecked")
                Handle<Integer, Void>[] handle = (Handle<Integer, Void>[]) Array.newInstance(Handle.class, classes);
                for (int j = 0; j < classes; j++) {
                    handle[j] = it[j].next();
                }
                int newKey = handle[0].getKey() / 2;
                if (newKey < handle[0].getKey()) {
                    for (int j = 0; j < classes; j++) {
                        handle[j].decreaseKey(newKey);
                    }
                }

                for (int j = 1; j < classes; j++) {
                    assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
                }
            }
        }

        while (!h[0].isEmpty()) {
            for (int j = 1; j < classes; j++) {
                assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
            }
            for (int j = 0; j < classes; j++) {
                h[j].deleteMin();
            }
        }

    }

    private void testRandomDeletes(long seed) {

        final int classes = 8;

        @SuppressWarnings("unchecked")
        AddressableHeap<Integer, Void>[] h = (AddressableHeap<Integer, Void>[]) Array.newInstance(AddressableHeap.class,
                classes);
        h[0] = new PairingHeap<Integer, Void>();
        h[1] = new BinaryTreeAddressableHeap<Integer, Void>();
        h[2] = new FibonacciHeap<Integer, Void>();
        h[3] = new BinaryArrayAddressableHeap<Integer, Void>();
        h[4] = new CostlessMeldPairingHeap<Integer, Void>();
        h[5] = new SkewHeap<Integer, Void>();
        h[6] = new ReflectedPairingHeap<Integer, Void>();
        h[7] = new LeftistHeap<Integer, Void>();

        @SuppressWarnings("unchecked")
        List<Handle<Integer, Void>>[] s = (List<Handle<Integer, Void>>[]) Array.newInstance(List.class, classes);
        for (int i = 0; i < classes; i++) {
            s[i] = new ArrayList<Handle<Integer, Void>>();
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < classes; j++) {
                s[j].add(h[j].insert(i, null));
            }
            for (int j = 1; j < classes; j++) {
                assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
            }
        }

        for (int j = 0; j < classes; j++) {
            Collections.shuffle(s[j], new Random(seed));
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 1; j < classes; j++) {
                assertEquals(h[0].findMin().getKey().intValue(), h[j].findMin().getKey().intValue());
            }
            for (int j = 0; j < classes; j++) {
                s[j].get(i).delete();
            }
        }

        for (int j = 0; j < classes; j++) {
            assertTrue(h[j].isEmpty());
        }

    }

}
