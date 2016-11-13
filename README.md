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

## A pairing heap? 

A Pairing Heap is an addressable heap whose performance is often faster in practice 
than array-based binary heaps and d-ary heaps, and almost always faster than other 
pointer-based heaps, including data structures like Fibonacci heaps that are 
theoretically more efficient.

For a detailed description see the following papers: 

* Michael L. Fredman, Robert Sedjewick, Daniel D. Sleator, and Robert E. Tarjan.  The Pairing Heap: A New Form of Self-Adjusting Heap.  Algorithmica 1:111-129, 1986. [[pdf]](https://www.cs.cmu.edu/~sleator/papers/pairing-heaps.pdf)
* John T. Stasko and Jeffrey Scott Vitter.  Pairing heaps: experiments and analysis.  Communications of the ACM, Volume 30, Issue 3, Pages 234-249, 1987.
* Michael L. Fredman.  On the efficiency of pairing heaps and related data structures.  Journal of the ACM, Volume 46, Issue 4, Pages 473-501, 1999.

The [wikipedia article](https://en.wikipedia.org/wiki/Pairing_heap) also contains a nice description. 

## Compatibility

The library requires JDK v1.6 and above. 


