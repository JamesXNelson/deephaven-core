package io.deephaven.db.v2.by;

import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.chunk.Attributes.KeyIndices;
import io.deephaven.db.v2.sources.chunk.WritableIntChunk;
import io.deephaven.db.v2.utils.OrderedKeys;
import io.deephaven.util.SafeCloseable;
import org.apache.commons.lang3.mutable.MutableInt;

interface ChunkedOperatorAggregationStateManager {

    SafeCloseable makeAggregationStateBuildContext(ColumnSource<?>[] buildSources, long maxSize);

    void add(final SafeCloseable bc, OrderedKeys orderedKeys, ColumnSource<?>[] sources, MutableInt nextOutputPosition, WritableIntChunk<KeyIndices> outputPositions);

    ColumnSource[] getKeyHashTableSources();

    int findPositionForKey(Object key);
}
