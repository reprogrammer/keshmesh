Keshmesh
========

Keshmesh is an interactive tool for detecting and fixing concurrency bugs in
Java programs. Keshmesh looks for certain bug patterns that are common in
concurrent Java programs, offers automated fixes to the programmer whenever
possible.

Keshmesh uses [WALA](http://wala.sf.net) to analyze the program precisely, and
thus produces few false alarms. We have packaged Keshmesh as an add-on to
[Findbugs](http://findbugs.sf.net) for [Eclipse](http://eclipse.org).

Keshmesh is developed at the University of Illinois, and is licensed under the
Illinois/NCSA Open Source License. See LICENSE.txt for more details.

