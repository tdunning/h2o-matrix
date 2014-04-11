package ai.h2o.math;

import water.MRTask2;
import water.fvec.Chunk;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Matrix;

public abstract class H2OMatrixTask<MType> extends MRTask2<H2OMatrixTask<MType>> {

  public abstract MType map (Vector v);
  public abstract MType reduce (MType a, MType b);

  MType precipitate;

  public void map(Chunk[] chunks) {
    H2ORowView h2orv = new H2ORowView(chunks);

    precipitate = map((Vector) h2orv.ofRow(0));
    for (int row = 1; row < chunks[0]._len; row++) {
      precipitate = reduce (precipitate, map((Vector) h2orv.ofRow(row)));
    }
  }

  public void reduce( H2OMatrixTask<MType> other) {
    precipitate = reduce (precipitate, other.precipitate);
  }

  public MType mapreduce(Matrix matrix) {
    doAll(((H2OMatrix)matrix)._fr);
    return precipitate;
  }
}
