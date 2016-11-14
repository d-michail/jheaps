# Java Heaps Library

Copyright (C) 2014-2016 Dimitrios Michail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and

***

## What is this library?

This library contains various heap implementations written in Java.

* It is easy to use
* The data structures have a well defined interface
* It is fast and well documented
* The heaps are written in a similar way as in the JDK
* It does not depend on other libraries, so classpathing 'jheaps.jar' is sufficient
  to use in your project.

## What is a heap?

A heap is a priority queue data type which contains elements with keys (duplicate keys
are permitted) from a totally-ordered universe. A min-oriented heap 
supports the following core operations: 

* MAKE-HEAP(): create an empty heap
* INSERT(H,x): insert an element x into the heap
* FIND-MIN(H): return an element with the smallest key
* EXTRACT-MIN(H): remove the element with the smallest key
* IS-EMPTY(H): is the heap empty?
* SIZE(H): return the number of elements of the heap
* CLEAR(H): remove all elements of the heap

A heap does not support a search operation. A special type of heap called explicit or 
addressable resolves this issue by returning a handle when inserting a new element. This
handle can later be used to additionally perform the following operations: 

* DECREASE-KEY(H,x,k): decrease the key of element x to k
* DELETE(H,x): delete the element x from the heap

Implicit heaps are represented using arrays. They are not addressable as the location of the elements
in memory can change.

Some heaps are meldable, that is they efficiently support the union operation: 

* MELD(H1,H2): add all elements of H2 into H1 and destroy H2

## Applications?

* A* search.
* Heapsort.
* Online median.
* Hiffman encoding.
* Minimum Spanning Tree algorithms.
* Discrete event-driven simulation.
* Network bandwidth management.
* Dijkstra's shortest-paths algorithm.
* ...

## Pairing heaps?

Pairing Heaps are addressable heaps whose performance is often faster in practice
than array-based binary heaps and d-ary heaps, and almost always faster than other 
pointer-based heaps, including data structures like Fibonacci heaps that are 
theoretically more efficient. Pairing heaps are a self-adjusting variant of Fibonacci Heaps.

For a detailed description see the following papers: 

* Michael L. Fredman, Robert Sedjewick, Daniel D. Sleator, and Robert E. Tarjan.  The Pairing Heap: A New Form of Self-Adjusting Heap.  Algorithmica 1:111-129, 1986. [[pdf]](https://www.cs.cmu.edu/~sleator/papers/pairing-heaps.pdf)
* John T. Stasko and Jeffrey Scott Vitter.  Pairing heaps: experiments and analysis.  Communications of the ACM, Volume 30, Issue 3, Pages 234-249, 1987.
* Michael L. Fredman.  On the efficiency of pairing heaps and related data structures.  Journal of the ACM, Volume 46, Issue 4, Pages 473-501, 1999.

The [wikipedia article](https://en.wikipedia.org/wiki/Pairing_heap) also contains a nice description. 

## Compatibility

The library requires JDK v1.6 and above. 


