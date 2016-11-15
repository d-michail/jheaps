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

JHeaps development happens on [github](https://github.com/d-michail/jheaps). Report issues and/or bugs 
at the [issue tracker](https://github.com/d-michail/jheaps/issues).

### Latest version & Requirements

The latest version of JHeaps is 0.5-SNAPSHOT. 

Every JHeaps release is published to the Maven Central Repository. You can add a dependency from your project as follows:

```
  <groupId>org.jheaps</groupId>
  <artifactId>jheaps</artifactId>
  <version>0.5-SNAPSHOT</version>
```

### Documentation

Extensive documentation of the available classes and interfaces can be found in the latest version's
[javadoc]({{ "/apidocs" | prepend:site.baseurl }}).

### Compatibility

The library requires JDK v1.6 and above.
