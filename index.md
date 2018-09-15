---
# You don't need to edit this file, it's empty on purpose.
# Edit theme's home layout instead if you wanna make some changes
# See: https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: page
---

### What is the JHeaps library?

JHeaps is a free library that provides various heap implementations written in Java.

Key features:

* It is easy to use
* The data structures have a well defined interface
* It is fast and well documented
* The heaps are written in a similar way as in the JDK
* It does not depend on other libraries, so classpathing 'jheaps.jar' is sufficient
  to use in your project.
* Supports JDK v1.6 and above

JHeaps development happens on [github](https://github.com/d-michail/jheaps). Report issues and/or bugs
at the [issue tracker](https://github.com/d-michail/jheaps/issues).

### Available Heaps

The library contains an extensive collection of heap data structures such as:

* Tree-based
  * Fibonacci mergeable and addressable heaps
  * Simple Fibonacci heaps
  * Pairing mergeable and addressable heaps
  * Costless-meld variable of Pairing heaps
  * Leftist mergeable and addressable heaps
  * Explicit binary tree addressable heaps
  * Binary tree soft heaps
  * Skew heaps
* Double-ended mergeable and addressable heaps
  * Reflected Fibonacci heaps
  * Reflected Pairing heaps
* Array-based
  * Binary heaps
  * Binary addressable heaps
  * D-ary heaps
  * D-ary addressable heaps
  * Binary weak heaps
  * Binary weak heaps supporting bulk insertion
  * Highly optimized binary heaps for integer keys using the Wegener
   bottom-up heuristic and sentinel values
* Double-ended array-based
  * Binary MinMax heaps
* Monotone heaps
  * Addressable radix heaps with double, integer or BigInteger keys
  * Non-addressable radix heaps with double, integer or BigInteger keys

### Latest version & Requirements

The latest version of JHeaps is 0.9.

Every JHeaps release is published to the Maven Central Repository. You can add a dependency from your project as follows:

```
  <groupId>org.jheaps</groupId>
  <artifactId>jheaps</artifactId>
  <version>0.9</version>
```

### Documentation

Extensive documentation of the available classes and interfaces can be found in the latest version's
[javadoc]({{ "/apidocs" | prepend:site.baseurl }}).

### Compatibility

The library requires JDK v1.6 and above.
