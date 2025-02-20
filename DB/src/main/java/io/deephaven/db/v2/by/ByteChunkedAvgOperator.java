/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharChunkedAvgOperator and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.db.v2.by;

import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.DoubleArraySource;
import io.deephaven.db.v2.sources.LongArraySource;
import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.*;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.deephaven.db.util.NullSafeAddition.plusLong;
import static io.deephaven.db.util.NullSafeAddition.minusLong;
import static io.deephaven.db.v2.by.ComboAggregateFactory.*;

/**
 * Iterative average operator.
 */
class ByteChunkedAvgOperator implements IterativeChunkedAggregationOperator {
    private final DoubleArraySource resultColumn;
    private final LongArraySource runningSum;
    private final NonNullCounter nonNullCount;
    private final String name;
    private final boolean exposeInternalColumns;

    ByteChunkedAvgOperator(String name, boolean exposeInternalColumns) {
        this.name = name;
        this.exposeInternalColumns = exposeInternalColumns;
        resultColumn = new DoubleArraySource();
        runningSum = new LongArraySource();
        nonNullCount = new NonNullCounter();
    }

    @Override
    public void addChunk(BucketedContext context, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, IntChunk<KeyIndices> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        final ByteChunk<? extends Values> asByteChunk = values.asByteChunk();
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int startPosition = startPositions.get(ii);
            final long destination = destinations.get(startPosition);
            stateModified.set(ii, addChunk(asByteChunk, destination, startPosition, length.get(ii)));
        }
    }

    @Override
    public void removeChunk(BucketedContext context, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, IntChunk<KeyIndices> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        final ByteChunk<? extends Values> asByteChunk = values.asByteChunk();
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int startPosition = startPositions.get(ii);
            final long destination = destinations.get(startPosition);
            stateModified.set(ii, removeChunk(asByteChunk, destination, startPosition, length.get(ii)));
        }
    }

    @Override
    public boolean addChunk(SingletonContext context, int chunkSize, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, long destination) {
        return addChunk(values.asByteChunk(), destination, 0, values.size());
    }

    @Override
    public boolean removeChunk(SingletonContext context, int chunkSize, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, long destination) {
        return removeChunk(values.asByteChunk(), destination, 0, values.size());
    }
    
    private boolean addChunk(ByteChunk<? extends Values> values, long destination, int chunkStart, int chunkSize) {
        final MutableInt chunkNonNull = new MutableInt(0);
        final ByteChunk<? extends Values> asByteChunk = values.asByteChunk();
        final long chunkSum = SumByteChunk.sumByteChunk(asByteChunk, chunkStart, chunkSize, chunkNonNull);

        if (chunkNonNull.intValue() > 0) {
            final long newCount = nonNullCount.addNonNullUnsafe(destination, chunkNonNull.intValue());
            final long newSum = plusLong(runningSum.getUnsafe(destination), chunkSum);
            runningSum.set(destination, newSum);
            resultColumn.set(destination, (double)newSum / newCount);
        } else if (nonNullCount.onlyNullsUnsafe(destination)) {
            resultColumn.set(destination, Double.NaN);
        } else {
            return false;
        }
        return true;
    }

    private boolean removeChunk(ByteChunk<? extends Values> values, long destination, int chunkStart, int chunkSize) {
        final MutableInt chunkNonNull = new MutableInt(0);
        final long chunkSum = SumByteChunk.sumByteChunk(values, chunkStart, chunkSize, chunkNonNull);

        if (chunkNonNull.intValue() == 0) {
            return false;
        }

        final long newCount = nonNullCount.addNonNullUnsafe(destination, -chunkNonNull.intValue());
        final long newSum = minusLong(runningSum.getUnsafe(destination), chunkSum);
        runningSum.set(destination, newSum);
        resultColumn.set(destination, (double)newSum / newCount);

        return true;
    }

    @Override
    public void ensureCapacity(long tableSize) {
        resultColumn.ensureCapacity(tableSize);
        nonNullCount.ensureCapacity(tableSize);
        runningSum.ensureCapacity(tableSize);
    }

    @Override
    public Map<String, ? extends ColumnSource<?>> getResultColumns() {
        if (exposeInternalColumns) {
            final Map<String, ColumnSource<?>> results = new LinkedHashMap<>();
            results.put(name, resultColumn);
            results.put(name + ROLLUP_RUNNING_SUM_COLUMN_ID + ROLLUP_COLUMN_SUFFIX, runningSum);
            results.put(name + ROLLUP_NONNULL_COUNT_COLUMN_ID + ROLLUP_COLUMN_SUFFIX, nonNullCount.getColumnSource());
            return results;
        } else {
            return Collections.singletonMap(name, resultColumn);
        }
    }

    @Override
    public void startTrackingPrevValues() {
        resultColumn.startTrackingPrevValues();
        if (exposeInternalColumns) {
            runningSum.startTrackingPrevValues();
            nonNullCount.startTrackingPrevValues();
        }
    }
}
