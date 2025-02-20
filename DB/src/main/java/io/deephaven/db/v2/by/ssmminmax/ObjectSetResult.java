/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharSetResult and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.by.ssmminmax;

import java.util.Objects;

import io.deephaven.db.v2.sources.ArrayBackedColumnSource;
import io.deephaven.db.v2.sources.ObjectArraySource;
import io.deephaven.db.v2.ssms.ObjectSegmentedSortedMultiset;
import io.deephaven.db.v2.ssms.SegmentedSortedMultiSet;


public class ObjectSetResult implements SsmChunkedMinMaxOperator.SetResult {
    private final boolean minimum;
    private final ObjectArraySource resultColumn;

    public ObjectSetResult(boolean minimum, ArrayBackedColumnSource resultColumn) {
        this.minimum = minimum;
        this.resultColumn = (ObjectArraySource) resultColumn;
    }

    @Override
    public boolean setResult(SegmentedSortedMultiSet ssm, long destination) {
        final Object newResult;
        if (ssm.size() == 0) {
            newResult = null;
        } else {
            final ObjectSegmentedSortedMultiset ObjectSsm = (ObjectSegmentedSortedMultiset) ssm;
            newResult = minimum ? ObjectSsm.getMinObject() : ObjectSsm.getMaxObject();
        }
        return setResult(destination, newResult);
    }

    @Override
    public boolean setResultNull(long destination) {
        return setResult(destination, null);
    }

    private boolean setResult(long destination, Object newResult) {
        final Object oldResult = resultColumn.getAndSetUnsafe(destination, newResult);
        return oldResult != newResult;
    }
}
