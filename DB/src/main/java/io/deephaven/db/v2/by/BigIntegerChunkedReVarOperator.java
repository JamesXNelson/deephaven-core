/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.db.v2.by;

import io.deephaven.configuration.Configuration;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.ObjectArraySource;
import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.ChunkLengths;
import io.deephaven.db.v2.sources.chunk.Attributes.ChunkPositions;
import io.deephaven.db.v2.sources.chunk.Attributes.KeyIndices;
import io.deephaven.db.v2.sources.chunk.Attributes.Values;
import io.deephaven.db.v2.utils.OrderedKeys;
import io.deephaven.utils.BigDecimalUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

/**
 * Iterative average operator.
 */
class BigIntegerChunkedReVarOperator implements IterativeChunkedAggregationOperator {
    private static final int SCALE = Configuration.getInstance().getIntegerWithDefault("BigIntegerStdOperator.scale", 10);
    private final ObjectArraySource<BigDecimal> resultColumn;
    private final String name;
    private final boolean std;
    private final BigIntegerChunkedSumOperator sumSum;
    private final BigIntegerChunkedSumOperator sum2Sum;
    private final LongChunkedSumOperator nncSum;

    BigIntegerChunkedReVarOperator(String name, boolean std, BigIntegerChunkedSumOperator sumSum, BigIntegerChunkedSumOperator sum2sum, LongChunkedSumOperator nncSum) {
        this.name = name;
        this.std = std;
        this.sumSum = sumSum;
        this.sum2Sum = sum2sum;
        this.nncSum = nncSum;
        resultColumn = new ObjectArraySource<>(BigDecimal.class);
    }

    @Override
    public void addChunk(BucketedContext context, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, IntChunk<KeyIndices> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        doBucketedUpdate((ReVarContext)context, destinations, startPositions, stateModified);
    }

    @Override
    public void removeChunk(BucketedContext context, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, IntChunk<KeyIndices> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        doBucketedUpdate((ReVarContext)context, destinations, startPositions, stateModified);
    }

    @Override
    public void modifyChunk(BucketedContext context, Chunk<? extends Values> previousValues, Chunk<? extends Values> newValues, LongChunk<? extends KeyIndices> postShiftIndices, IntChunk<KeyIndices> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        doBucketedUpdate((ReVarContext)context, destinations, startPositions, stateModified);
    }

    @Override
    public boolean addChunk(SingletonContext context, int chunkSize, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, long destination) {
        return updateResult(destination);
    }

    @Override
    public boolean removeChunk(SingletonContext context, int chunkSize, Chunk<? extends Values> values, LongChunk<? extends KeyIndices> inputIndices, long destination) {
        return updateResult(destination);
    }

    @Override
    public boolean modifyChunk(SingletonContext context, int chunkSize, Chunk<? extends Values> previousValues, Chunk<? extends Values> newValues, LongChunk<? extends KeyIndices> postShiftIndices, long destination) {
        return updateResult(destination);
    }

    private void doBucketedUpdate(ReVarContext context, IntChunk<KeyIndices> destinations, IntChunk<ChunkPositions> startPositions, WritableBooleanChunk<Values> stateModified) {
        context.keyIndices.setSize(startPositions.size());
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int startPosition = startPositions.get(ii);
            context.keyIndices.set(ii, destinations.get(startPosition));
        }
        try (final OrderedKeys destinationOk = OrderedKeys.wrapKeyIndicesChunkAsOrderedKeys(context.keyIndices)) {
            updateResult(context, destinationOk, stateModified);
        }
    }

    private void updateResult(ReVarContext reVarContext, OrderedKeys destinationOk, WritableBooleanChunk<Values> stateModified) {
        final ObjectChunk<BigInteger, ? extends Values> sumSumChunk = sumSum.getChunk(reVarContext.sumSumContext, destinationOk).asObjectChunk();
        final ObjectChunk<BigInteger, ? extends Values> sum2SumChunk = sum2Sum.getChunk(reVarContext.sum2SumContext, destinationOk).asObjectChunk();
        final LongChunk<? extends Values> nncSumChunk = nncSum.getChunk(reVarContext.nncSumContext, destinationOk).asLongChunk();
        final int size = reVarContext.keyIndices.size();
        for (int ii = 0; ii < size; ++ii) {
            stateModified.set(ii, updateResult(reVarContext.keyIndices.get(ii), sumSumChunk.get(ii), sum2SumChunk.get(ii), nncSumChunk.get(ii)));
        }
    }

    private boolean updateResult(long destination) {
        final BigInteger newSum = sumSum.getResult(destination);
        final BigInteger newSum2 = sum2Sum.getResult(destination);
        final long nonNullCount = nncSum.getResult(destination);

        return updateResult(destination, newSum, newSum2, nonNullCount);
    }

    private boolean updateResult(long destination, BigInteger newSum, BigInteger newSum2, long nonNullCount) {
        if (nonNullCount <= 1) {
            return null == resultColumn.getAndSetUnsafe(destination, null);
        } else {
            if (newSum == null) {
                newSum = BigInteger.ZERO;
            }
            if (newSum2 == null) {
                newSum2 = BigInteger.ZERO;
            }
            final BigDecimal countMinus1 = BigDecimal.valueOf(nonNullCount - 1);
            final BigDecimal variance = new BigDecimal(newSum2).subtract(new BigDecimal(newSum.pow(2)).divide(BigDecimal.valueOf(nonNullCount), BigDecimal.ROUND_HALF_UP)).divide(countMinus1, BigDecimal.ROUND_HALF_UP);
            final BigDecimal result = std ? BigDecimalUtils.sqrt(variance, SCALE) : variance;
            return !result.equals(resultColumn.getAndSetUnsafe(destination, result));
        }
    }

    private class ReVarContext implements BucketedContext {
        final WritableLongChunk<Attributes.OrderedKeyIndices> keyIndices;
        final ChunkSource.GetContext sumSumContext;
        final ChunkSource.GetContext sum2SumContext;
        final ChunkSource.GetContext nncSumContext;

        private ReVarContext(int size) {
            keyIndices = WritableLongChunk.makeWritableChunk(size);
            sumSumContext = sumSum.makeGetContext(size);
            sum2SumContext = sum2Sum.makeGetContext(size);
            nncSumContext = nncSum.makeGetContext(size);
        }

        @Override
        public void close() {
            keyIndices.close();
            sumSumContext.close();
            sum2SumContext.close();
            nncSumContext.close();
        }
    }

    @Override
    public BucketedContext makeBucketedContext(int size) {
        return new ReVarContext(size);
    }

    @Override
    public void ensureCapacity(long tableSize) {
        resultColumn.ensureCapacity(tableSize);
    }

    @Override
    public Map<String, ColumnSource<?>> getResultColumns() {
        return Collections.singletonMap(name, resultColumn);
    }

    @Override
    public void startTrackingPrevValues() {
        resultColumn.startTrackingPrevValues();
    }
}
