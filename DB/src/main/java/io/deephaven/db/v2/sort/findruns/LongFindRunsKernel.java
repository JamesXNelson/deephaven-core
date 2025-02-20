/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharFindRunsKernel and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sort.findruns;

import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.*;

public class LongFindRunsKernel {
    /**
     * Find runs of two or more identical values in a sorted chunk.  This is used as part of an overall sort, after the
     * timsort (or other sorting) kernel to identify the runs that must be sorted according to secondary keys.
     *
     * @param sortedValues a chunk of sorted values
     * @param offsetsOut an output chunk, with offsets of starting locations that a run occurred
     * @param lengthsOut an output chunk, parallel to offsetsOut, with the lengths of found runs
     *
     * Note, lengthsOut only contain values greater than one.
     */
    static void findRuns(LongChunk sortedValues, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut) {
        offsetsOut.setSize(0);
        lengthsOut.setSize(0);

        findRuns(sortedValues, 0, sortedValues.size(), offsetsOut, lengthsOut, false);
    }

    /**
     * Find runs of identical values in a sorted chunk.  If a single value exists, it is included as run of length 1.
     *
     * @param sortedValues a chunk of sorted values
     * @param offsetsOut an output chunk, with offsets of starting locations that a run occurred
     * @param lengthsOut an output chunk, parallel to offsetsOut, with the lengths of found runs
     */
    public static void findRunsSingles(LongChunk sortedValues, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut) {
        offsetsOut.setSize(0);
        lengthsOut.setSize(0);

        findRuns(sortedValues, 0, sortedValues.size(), offsetsOut, lengthsOut, true);
    }

    /**
     * Find runs of two or more identical values in a sorted chunk.  This is used as part of an overall sort, after the
     * timsort (or other sorting) kernel to identify the runs that must be sorted according to secondary keys.
     *
     * @param sortedValues a chunk of sorted values
     * @param offsetsIn the offsets within the chunk to check for runs
     * @param lengthsIn the lengths parallel to offsetsIn for run checking
     * @param offsetsOut an output chunk, with offsets of starting locations that a run occurred
     * @param lengthsOut an output chunk, parallel to offsetsOut, with the lengths of found runs
     *
     * Note, that lengthsIn must contain values greater than 1, and lengthsOut additionally only contain values greater than one
     */
    public static void findRuns(LongChunk sortedValues, IntChunk<ChunkPositions> offsetsIn, IntChunk<ChunkLengths> lengthsIn, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut) {
        offsetsOut.setSize(0);
        lengthsOut.setSize(0);

        final int numberRuns = offsetsIn.size();
        for (int run = 0; run < numberRuns; ++run) {
            final int offset = offsetsIn.get(run);
            final int length = lengthsIn.get(run);

            findRuns(sortedValues, offset, length, offsetsOut, lengthsOut, false);
        }
    }

    private static void findRuns(LongChunk sortedValues, int offset, int length, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut, boolean includeSingles) {
        if (length == 0) {
            return;
        }
        int startRun = offset;
        int cursor = startRun;
        long last = sortedValues.get(cursor++);
        while (cursor < offset + length) {
            final long next = sortedValues.get(cursor);
            if (neq(last, next)) {
                if (includeSingles || cursor != startRun + 1) {
                    offsetsOut.add(startRun);
                    lengthsOut.add(cursor - startRun);
                }
                startRun = cursor;
            }
            cursor++;
            last = next;
        }
        if (includeSingles || cursor != startRun + 1) {
            offsetsOut.add(startRun);
            lengthsOut.add(cursor - startRun);
        }
    }

    private static boolean neq(long last, long next) {
        // region neq
        return next != last;
        // endregion neq
    }

    private static class LongFindRunsKernelContext implements FindRunsKernel {
        @Override
        public void findRuns(Chunk sortedValues, IntChunk<ChunkPositions> offsetsIn, IntChunk<ChunkLengths> lengthsIn, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut) {
            LongFindRunsKernel.findRuns(sortedValues.asLongChunk(), offsetsIn, lengthsIn, offsetsOut, lengthsOut);
        }

        @Override
        public void findRuns(Chunk sortedValues, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut) {
            LongFindRunsKernel.findRuns(sortedValues.asLongChunk(), offsetsOut, lengthsOut);
        }

        @Override
        public void findRunsSingles(Chunk sortedValues, WritableIntChunk<ChunkPositions> offsetsOut, WritableIntChunk<ChunkLengths> lengthsOut) {
            LongFindRunsKernel.findRunsSingles(sortedValues.asLongChunk(), offsetsOut, lengthsOut);
        }
    }

    private final static FindRunsKernel INSTANCE = new LongFindRunsKernelContext();

    public static FindRunsKernel createContext() {
        return INSTANCE;
    }
}
