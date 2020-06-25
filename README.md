# JHeaps Library

Copyright (C) 2014-2020 Dimitrios Michail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

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
in memory can change. However, they can be made to be addressable by using an additional layer of indirection.
In this case we store handles inside the array. Each handle contains an additional integer property, designating
the location in the array where the handle is stored. 

Some heaps are meldable, that is they efficiently support the union operation: 

* MELD(H1,H2): add all elements of H2 into H1 and destroy H2

As a general rule, heaps using an array representation are not meldable.

## Compatibility

The library requires JDK v1.8 and above. 


