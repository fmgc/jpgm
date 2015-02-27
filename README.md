# jpgm

	Hic sunt dracones

`jpgm` is a simple java only probabilistic graphical models (pgm) library.

It exists only *while no clear open-source alternative exists* and to provide enough support for (my) research that needs some pgm related computations with sparse matrices in java.

Graphical models structure is supported by the [jgrapht](http://jgrapht.org/) library.

`jpgm` is nowhere near of being complete, fast, scalable or other usually looked-for niceties. Indeed, classes in `jpgm` are only a few:

* `IntArray` supports indexing/deindexing (for sparse matrices);
* `Factor` represents a sparse factor (aka potential) in a pgm;
* `Distribution` is an abstract class that defines a set of methods to handle (discrete) probability distributions;
* `BN` extends `Distribution` and represents the joint distribution defined by a (discrete) Bayesian network;
* `DBN` is mostly unfinished;
* `Lambda` defines a simple interface to represent anonymous functions, now obsolete in recent `java` versions;

## Building

There are a few `ant` tasks, with obvious function:

* `jar` (default)
* `build`
* `doc`
* `test` (a few text-book bn examples are constructed and evaluated)
* `clean`

## Documentation

* Either look at the source of the `test/*Test.java` files or
* Run the `ant doc` target and consult the generated file.
