h2o-matrix
==========

Demonstration of Mahout compatible matrix and vector types based on h2o

The basic idea here is that I have cloned DenseMatrix and DenseMatrix as ProtoMatrix and ProtoVector and
have written test cases that have near 100% coverage over these classes.  The cool thing is that the
test cases are actually just extensions of abstract test cases that you can extend with methods that
create your kinds of matrices and vectors.
