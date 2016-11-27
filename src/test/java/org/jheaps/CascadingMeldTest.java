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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jheaps.AddressableHeap.Handle;
import org.junit.Test;

public abstract class CascadingMeldTest {

    protected abstract AddressableHeap<Integer, String> createHeap();

    @Test
    @SuppressWarnings("unchecked")
    public void testPairingHeapMeld1() throws IOException, ClassNotFoundException {
        AddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        AddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        Handle<Integer, String> b4 = b.insert(17);

        if (a instanceof MergeableHeap) {
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) b);

            assertEquals(8, a.size());
            assertTrue(b.isEmpty());
            assertEquals(0, b.size());

            b4.decreaseKey(9);
            assertEquals(Integer.valueOf(9), a.findMin().getKey());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMeld2() throws IOException, ClassNotFoundException {
        AddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        AddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        Handle<Integer, String> b4 = b.insert(17);

        AddressableHeap<Integer, String> c = createHeap();
        c.insert(18);
        c.insert(19);
        c.insert(20);
        Handle<Integer, String> c4 = c.insert(21);

        if (a instanceof MergeableHeap) {
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) b);
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) c);

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
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void testMultipleMelds() {
        AddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        AddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        AddressableHeap<Integer, String> c = createHeap();
        c.insert(18);
        c.insert(19);
        c.insert(20);
        c.insert(21);

        if (a instanceof MergeableHeap) {
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) b);
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) b);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void testInsertAfterAMeld() {
        AddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        AddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        if (a instanceof MergeableHeap) {
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) b);
            b.insert(30);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCascadingMelds() throws IOException, ClassNotFoundException {
        AddressableHeap<Integer, String> a = createHeap();
        a.insert(10);
        a.insert(11);
        a.insert(12);
        a.insert(13);

        AddressableHeap<Integer, String> b = createHeap();
        b.insert(14);
        b.insert(15);
        b.insert(16);
        b.insert(17);

        AddressableHeap<Integer, String> c = createHeap();
        c.insert(18);
        c.insert(19);
        c.insert(20);
        c.insert(21);

        AddressableHeap<Integer, String> d = createHeap();
        d.insert(22);
        d.insert(23);
        d.insert(24);
        d.insert(25);

        AddressableHeap<Integer, String> e = createHeap();
        e.insert(26);
        e.insert(27);
        Handle<Integer, String> e3 = e.insert(28);
        Handle<Integer, String> e4 = e.insert(29);

        if (a instanceof MergeableHeap) {
            ((MergeableHeap<Integer>) d).meld((MergeableHeap<Integer>) e);
            ((MergeableHeap<Integer>) c).meld((MergeableHeap<Integer>) d);
            ((MergeableHeap<Integer>) b).meld((MergeableHeap<Integer>) c);
            ((MergeableHeap<Integer>) a).meld((MergeableHeap<Integer>) b);

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
    }

}
