Keshmesh
========

Keshmesh is an interactive tool for detecting and fixing concurrency bugs in Java programs. Keshmesh looks for certain bug patterns that are common in concurrent Java programs, offers automated fixes to the programmer whenever possible.

Keshmesh uses [WALA](http://wala.sf.net) to analyze the program precisely, and thus produces few false alarms. And, it uses [Findbugs](http://findbugs.sf.net) to report bugs and offer automated fixes to the users of [Eclipse](http://eclipse.org).

Keshmesh is developed at the University of Illinois, and is licensed under the Illinois/NCSA Open Source License. See [LICENSE.TXT](https://github.com/reprogrammer/keshmesh/raw/master/LICENSE.TXT) for more details.

