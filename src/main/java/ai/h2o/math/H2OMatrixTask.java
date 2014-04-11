package ai.h2o.math;

import water.Iced;
import water.MRTask2;
import water.fvec.Chunk;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Matrix;

public abstract class H2OMatrixTask<MType extends Iced> extends MRTask2<H2OMatrixTask<MType>> {
  public abstract MType map (Vector v);
  public abstract MType reduce (MType a, MType b);
  MType _res;
  public void map(Chunk[] chunks) {
    H2ORowView h2orv = new H2ORowView(chunks);
    MType res = map(h2orv.ofRow(0));
    for( int row = 1; row < chunks[0]._len; row++)
      res = reduce(res, map(h2orv.ofRow(row)));
    _res = res;
  }

  public void reduce( H2OMatrixTask<MType> other) { _res = reduce (_res, other._res); }
  public MType mapreduce( H2OMatrix matrix ) { return doAll(matrix._fr)._res; }
}
