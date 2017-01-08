# Parallel Computing - pancake sorting

Finds the optimal / shortest solution for sorting a pancake stack with IDA*.
Can also count the number of all **optimal** solutions.

## Installation
* Download and install [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Download and install [MPJ](http://mpj-express.org). Tested with version v0.44.
* Set `MPJ_HOME` environment variable.

## Running
### Sequential Sorters
package: `pancake.sequential`

* `PancakeRecursive.java` is a recursive implementation. Run the `main()` method.
* `PancakeIterative.java` is a iterative implementation. Run the `main()` method.

### Parallel Sorter
package: `pancake.parallel`

* `PancakeParallel.java` has to be run through MPJ:

Main class: `runtime.starter.MPJRun`

VM options: `-jar <MPJ_HOME>/mpj-v0_44/lib/starter.jar pancake.parallel.PancakeParallel -np <number of processors>`

or via terminal: `<MPJ_HOME>/bin/mpjrun.sh pancake.parallel.PancakeParallel -np <number of processors>`

## Configuration
In the parallel and sequential programs you can set the following:

* `MODE` set mode to SOLVE or COUNT
* `N` length of pankcake stack
* `numbers`:  pankcake stack to solve, can be initialized as follows
  - enter array manually
  - generate random order -> Utility.randomOrder(N)
  - generate pair switched order -> Utility.switchedPairs(N)
