/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharCompactKernel and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.utils.compact;

import io.deephaven.db.util.DhLongComparisons;
import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.Any;

import static io.deephaven.util.QueryConstants.NULL_LONG;

public class LongCompactKernel implements CompactKernel {
    static LongCompactKernel INSTANCE = new LongCompactKernel();
    private LongCompactKernel() {} // use the instance

    /**
     * Compact the values in values by retaining only the positions where retainValues is true.
     *
     * @param values the input and output chunk of values
     * @param retainValues a chunk parallel to values, a value is retained in the output iff retainedValues is true
     */
     public static void compact(WritableLongChunk<? extends Any> values, BooleanChunk<Any> retainValues) {
        int writePosition = 0;
        for (int ii = 0; ii < retainValues.size(); ++ii) {
            if (retainValues.get(ii)) {
                values.set(writePosition++, values.get(ii));
            }
        }
        values.setSize(writePosition);
    }

    @Override
    public void compact(WritableChunk<? extends Any> values, BooleanChunk<Any> retainValues) {
        compact(values.asWritableLongChunk(), retainValues);
    }


    @Override
    public void compactAndCount(WritableChunk<? extends Attributes.Values> valueChunk, WritableIntChunk<Attributes.ChunkLengths> counts, boolean countNull) {
        compactAndCount(valueChunk.asWritableLongChunk(), counts, countNull);
    }

    @Override
    public void compactAndCount(WritableChunk<? extends Attributes.Values> valueChunk, WritableIntChunk<Attributes.ChunkLengths> counts, IntChunk<Attributes.ChunkPositions> startPositions, WritableIntChunk<Attributes.ChunkLengths> lengths, boolean countNull) {
        compactAndCount(valueChunk.asWritableLongChunk(), counts, startPositions, lengths, countNull);
    }

    public static void compactAndCount(WritableLongChunk<? extends Attributes.Values> valueChunk, WritableIntChunk<Attributes.ChunkLengths> counts) {
         compactAndCount(valueChunk, counts, false);
    }

    public static void compactAndCount(WritableLongChunk<? extends Attributes.Values> valueChunk, WritableIntChunk<Attributes.ChunkLengths> counts, boolean countNull) {
        final int newSize = compactAndCount(valueChunk, counts, 0, valueChunk.size(), countNull);
        valueChunk.setSize(newSize);
        counts.setSize(newSize);
    }

    public static void compactAndCount(WritableLongChunk<? extends Attributes.Values> valueChunk, WritableIntChunk<Attributes.ChunkLengths> counts, IntChunk<Attributes.ChunkPositions> startPositions, WritableIntChunk<Attributes.ChunkLengths> lengths, boolean countNull) {
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int newSize = compactAndCount(valueChunk, counts, startPositions.get(ii), lengths.get(ii), countNull);
            lengths.set(ii, newSize);
        }
    }

    public static int compactAndCount(WritableLongChunk<? extends Attributes.Values> valueChunk, WritableIntChunk<Attributes.ChunkLengths> counts, final int start, final int length, boolean countNull) {
        int wpos = -1;
        // region compactAndCount
        valueChunk.sort(start, length);
        long lastValue = NULL_LONG;
        int currentCount = -1;
        final int end = start + length;
        for (int rpos = start; rpos < end; ++rpos) {
            final long nextValue = valueChunk.get(rpos);
            if (!countNull && shouldIgnore(nextValue)) {
                continue;
            }
            if (wpos == -1 || !DhLongComparisons.eq(nextValue, lastValue)) {
                valueChunk.set(++wpos + start, nextValue);
                counts.set(wpos + start, currentCount = 1);
                lastValue = nextValue;
                continue;
            }
            counts.set(wpos + start, ++currentCount);
        }
        // endregion compactAndCount
        return wpos + 1;
    }

    private static boolean shouldIgnore(long value) {
         // region shouldIgnore
        return value == NULL_LONG;
        // endregion shouldIgnore
    }
}
