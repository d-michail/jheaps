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
package org.jheaps.monotone;

import java.io.Serializable;
import java.util.Comparator;

import org.jheaps.Heap;
import org.jheaps.MapHeap;

/**
 * A {@link MapHeap} to {@link Heap} adapter.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
class MapHeapAsHeapAdapter<K, V> implements Heap<K>, Serializable {

	private final static long serialVersionUID = 1;

	private MapHeap<K, V> heap;

	public MapHeapAsHeapAdapter(MapHeap<K, V> heap) {
		this.heap = heap;
	}

	@Override
	public Comparator<? super K> comparator() {
		return heap.comparator();
	}

	@Override
	public void insert(K key) {
		heap.insert(key, null);
	}

	@Override
	public K findMin() {
		return heap.findMin().getKey();
	}

	@Override
	public K deleteMin() {
		return heap.deleteMin().getKey();
	}

	@Override
	public boolean isEmpty() {
		return heap.isEmpty();
	}

	@Override
	public long size() {
		return heap.size();
	}

	@Override
	public void clear() {
		heap.clear();
	}

	@Override
	public String toString() {
		return heap.toString();
	}

}
