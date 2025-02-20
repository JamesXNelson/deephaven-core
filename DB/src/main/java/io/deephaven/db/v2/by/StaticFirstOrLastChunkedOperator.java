package io.deephaven.db.v2.by;

import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.select.MatchPair;
import io.deephaven.util.QueryConstants;
import io.deephaven.db.v2.sources.chunk.Attributes.ChunkLengths;
import io.deephaven.db.v2.sources.chunk.Attributes.ChunkPositions;
import io.deephaven.db.v2.sources.chunk.Attributes.KeyIndices;
import io.deephaven.db.v2.sources.chunk.Attributes.Values;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.IntChunk;
import io.deephaven.db.v2.sources.chunk.LongChunk;
import io.deephaven.db.v2.sources.chunk.WritableBooleanChunk;
import io.deephaven.db.v2.utils.Index;

public class StaticFirstOrLastChunkedOperator extends BaseAddOnlyFirstOrLastChunkedOperator {
    StaticFirstOrLastChunkedOperator(boolean isFirst, MatchPair[] resultPairs, Table originalTable,
            String exposeRedirectionAs) {
        super(isFirst, resultPairs, originalTable, exposeRedirectionAs);
    }

    @Override
    public void addChunk(BucketedContext bucketedContext, Chunk<? extends Values> values,
            LongChunk<? extends KeyIndices> inputIndices, IntChunk<KeyIndices> destinations,
            IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length,
            WritableBooleanChunk<Values> stateModified) {
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int startPosition = startPositions.get(ii);
            final int runLength = length.get(ii);
            final long destination = destinations.get(startPosition);

            stateModified.set(ii, addChunk(inputIndices, startPosition, runLength, destination));
        }
    }

    @Override
    public boolean addChunk(SingletonContext singletonContext, int chunkSize, Chunk<? extends Values> values,
            LongChunk<? extends KeyIndices> inputIndices, long destination) {
        return addChunk(inputIndices, 0, inputIndices.size(), destination);
    }

    private boolean addChunk(LongChunk<? extends KeyIndices> indices, int start, int length, long destination) {
        if (length == 0) {
            return false;
        }

        if (isFirst) {
            final long oldValue = redirections.getUnsafe(destination);
            final long candidate = indices.get(start);
            if (oldValue == QueryConstants.NULL_LONG) {
                redirections.set(destination, candidate);
                return true;
            } else {
                return false;
            }
        }

        final long candidate = indices.get(start + length - 1);
        redirections.set(destination, candidate);
        return true;
    }

    @Override
    public boolean addIndex(SingletonContext context, Index index, long destination) {
        if (index.empty()) {
            return false;
        }

        redirections.set(destination, isFirst ? index.firstKey() : index.lastKey());

        return true;
    }

    @Override
    public boolean unchunkedIndex() {
        return true;
    }
}
