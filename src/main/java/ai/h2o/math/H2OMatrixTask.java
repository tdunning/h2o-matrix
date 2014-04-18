package ai.h2o.math;

import water.Iced;
import water.MRTask2;
import water.fvec.Chunk;
import org.apache.mahout.math.Vector;

public abstract class H2OMatrixTask<MType extends Iced> extends MRTask2<H2OMatrixTask<MType>> {
  public abstract MType map(Vector v, MType tmp);
  public abstract void reduce(MType a, MType b);
  MType _res;

  public void map(Chunk[] chunks) {
    H2ORowView h2orv = new H2ORowView(chunks);
    MType tmp = map(h2orv.ofRow(0), null);
    for( int row = 1; row < chunks[0]._len; row++) {
      MType tmp2 = map(h2orv.ofRow(row), tmp);
      assert tmp2==tmp;         // For performance reasons, update tmp in-place, never return a new tmp
    }
    _res = tmp;
  }

  public void reduce( H2OMatrixTask<MType> other) { reduce(_res, other._res); }
  public MType mapreduce( H2OMatrix matrix ) { return doAll(matrix._fr)._res; }
}
