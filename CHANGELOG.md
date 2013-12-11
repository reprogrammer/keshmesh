v1.0.0.201312111715
===================
- Fixed the compilation problem in `KeshmeshFindBugsDetector` (issue #73).

v1.0.0.201312111624
===================
- Fixed the reporting of bug patterns inside inner classes (issue #41).
- Added the option to skip dumping the call and heap graphs (issue #68).
- Fixed a bug that caused Keshmesh fail on Cassandra (issue #69).
- Made it possible to debug Keshmesh within Eclipse (issue #70).
- Reported the list of entry points (issue #71).

v1.0.0.201312101057
===================
- Reported the number of CGNodes per class loader (issue #66).

v1.0.0.201312091449
===================
- Added the support for a light-weight type-based context sensitivity (issue #64).

v1.0.0.201311301706
===================
- Allowed infinite object sensitivity (issue #62).
- Renamed `*.md` to `*.txt` (issue #61).

v1.0.0.201311291936
===================
- Dumped the call graph and heap graphs to files (issue #59).
- Avoided the creation of the Keshmesh folder if it doesn't exist (issue #58).

v1.0.0.201311271834
===================
- Closed the configuration file after reading it (issue #54).
- Made sure the stopwatch is started properly (issue #55).
- Reported the number of entry points (issue #57).

v1.0.0.201305071705
===================
- Added a dependency on Guava (issue #53).
- Made Keshmesh read configuration options from a file and report profiling
  information to several files (issue #47).

v1.0.0.201305071705
===================
- Made the VNA00-J detector consider accesses to final fields safe (issue #48).

v1.0.0.201111231854
===================
- Published the first release of Keshmesh that provides detectors for LCK01-J, LCK02-J, LCK03-J, LCK06-J, and VNA00-J and fixers for LCK02-J and LCK03-J (issues #1, #2, #4, #5, #6, #7, #8, #9, #10, #11, #12, #14, #15, #17, #18, #19, #22, #23, #25, #27, #28, #30, #31, #32, #33, #35, #37, #38, #43).

