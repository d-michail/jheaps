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

import java.io.Serializable;

/**
 * Default implementation of heap map entry.
 * 
 * @author Dimitrios Michail
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
class DefaultMapHeapEntryImpl<K, V> implements MapHeap.Entry<K, V>, Serializable {

	private final static long serialVersionUID = 1;

	private K key;
	private V value;

	public DefaultMapHeapEntryImpl(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('{');
		if (key != null) {
			sb.append(key.toString());
		}
		if (value != null) {
			sb.append(',').append(value.toString());
		}
		sb.append('}');
		return sb.toString();
	}

}
