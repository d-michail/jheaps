---
layout: page
title: Heaps
permalink: /heaps/
---

### What is a heap?

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

### Applications?

Heaps have a lot of applications, among a few are the efficient implementation of the following techniques
and/or algorithms.

* A* search.
* Heapsort.
* Online median.
* Hiffman encoding.
* Minimum Spanning Tree algorithms.
* Discrete event-driven simulation.
* Network bandwidth management.
* Dijkstra's shortest-paths algorithm.
* ...

