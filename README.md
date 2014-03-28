h2o-matrix
==========

Demonstration of Mahout compatible matrix and vector types based on h2o

The basic idea here is that I have cloned DenseMatrix and DenseMatrix as ProtoMatrix and ProtoVector and
have written test cases that have near 100% coverage over these classes.  The cool thing is that the
test cases are actually just extensions of abstract test cases that you can extend with methods that
create your kinds of matrices and vectors.

## Compile and run tests

To run the tests here, just do the usual

    mvn test

## Extend and run more tests

To add your own implementations, create your classes FooMatrix and FooVector.  Then create FooMatrixTest by copying ProtoMatrixTest and define the two create methods so they create FooMatrix instead of ProtoMatrix.

You can now run the tests on your classes and you should get near 100% coverage on FooMatrix.  Do the same with FooVector and you should get a bit less coverage, but only trivial functions will lack coverage.
