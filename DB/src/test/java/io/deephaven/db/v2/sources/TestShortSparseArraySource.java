/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit TestCharacterSparseArraySource and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sources;

import io.deephaven.db.tables.live.LiveTableMonitor;
import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.Values;
import io.deephaven.db.v2.sources.chunk.ShortChunk;
import io.deephaven.db.v2.sources.chunk.WritableShortChunk;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.OrderedKeys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

// region boxing imports
import static io.deephaven.util.QueryConstants.NULL_SHORT;
// endregion boxing imports

import static junit.framework.TestCase.*;

public class TestShortSparseArraySource {
    @Before
    public void setUp() throws Exception {
        LiveTableMonitor.DEFAULT.enableUnitTestMode();
        LiveTableMonitor.DEFAULT.resetForUnitTests(false);
    }

    @After
    public void tearDown() throws Exception {
        LiveTableMonitor.DEFAULT.resetForUnitTests(true);
    }

    @Test
    public void testFillChunk() {
        final Random random = new Random(0);

        for (int chunkSize = 1024; chunkSize <= 16384; chunkSize *= 2) {
            testFill(random, chunkSize);
        }
    }

    private void testFill(Random random, int chunkSize) {
        final ShortSparseArraySource source = new ShortSparseArraySource();

        final ColumnSource.FillContext fillContext = source.makeFillContext(chunkSize);
        final WritableShortChunk<Values> dest = WritableShortChunk.makeWritableChunk(chunkSize);

        source.fillChunk(fillContext, dest, Index.FACTORY.getIndexByRange(0, 1023));
        for (int ii = 0; ii < 1024; ++ii) {
            checkFromSource("null check: " + ii, NULL_SHORT, dest.get(ii));
        }

        final int expectedBlockSize = 1024;
        final short [] expectations = new short[16384];
        // region arrayFill
        Arrays.fill(expectations, NULL_SHORT);
        // endregion arrayFill
        final short [] randomShorts = ArrayGenerator.randomShorts(random, expectations.length / 2);
        for (int ii = 0; ii < expectations.length; ++ii) {
            final int block = ii / expectedBlockSize;
            if (block % 2 == 0) {
                final short randomShort = randomShorts[(block / 2 * expectedBlockSize) + (ii % expectedBlockSize)];
                expectations[ii] = randomShort;
                source.set(ii, randomShort);
            }
        }

        // before we have the previous tracking enabled, prev should just fall through to get
        for (boolean usePrev : new boolean[]{false, true}) {
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 0, expectations.length - 1, usePrev);
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 100, expectations.length - 100, usePrev);
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 200, expectations.length - 1124, usePrev);
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 100, 700, usePrev);
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 100, 1024, usePrev);
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 250, 250, usePrev);
            checkRangeFill(chunkSize, source, fillContext, dest, expectations, 250, 251, usePrev);

            // lets make a few random indices
            for (int seed = 0; seed < 100; ++seed) {
                final Index index = generateIndex(random, expectations.length, 1 + random.nextInt(31));
                checkRandomFill(chunkSize, source, fillContext, dest, expectations, index, usePrev);
            }
        }

        fillContext.close();
    }

    @Test
    public void testGetChunk() {
        final Random random = new Random(0);

        for (int chunkSize = 1024; chunkSize <= 16384; chunkSize *= 2) {
            testGet(random, chunkSize);
        }
    }

    private void testGet(Random random, int chunkSize) {
        final ShortSparseArraySource source = new ShortSparseArraySource();

        final ColumnSource.GetContext getContext = source.makeGetContext(chunkSize);

        // the asChunk is not needed here, but it's needed when replicated to Boolean
        final ShortChunk<Values> result = source.getChunk(getContext, Index.FACTORY.getIndexByRange(0, 1023)).asShortChunk();
        for (int ii = 0; ii < 1024; ++ii) {
            checkFromSource("null check: " + ii, NULL_SHORT, result.get(ii));
        }

        final int expectedBlockSize = 1024;
        final short [] expectations = new short[16384];
        // region arrayFill
        Arrays.fill(expectations, NULL_SHORT);
        // endregion arrayFill
        final short [] randomShorts = ArrayGenerator.randomShorts(random, expectations.length / 2);
        for (int ii = 0; ii < expectations.length; ++ii) {
            final int block = ii / expectedBlockSize;
            if (block % 2 == 0) {
                final short randomShort = randomShorts[(block / 2 * expectedBlockSize) + (ii % expectedBlockSize)];
                expectations[ii] = randomShort;
                source.set(ii, randomShort);
            }
        }

        // before we have the previous tracking enabled, prev should just fall through to get
        for (boolean usePrev : new boolean[]{false, true}) {
            checkRangeGet(chunkSize, source, getContext, expectations, 0, expectations.length - 1, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 100, expectations.length - 100, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 200, expectations.length - 1124, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 100, 700, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 100, 1024, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 250, 250, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 250, 251, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 0, 1023, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 1024, 2047, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 1100, 1200, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 1200, 1200, usePrev);
            checkRangeGet(chunkSize, source, getContext, expectations, 1200, 1201, usePrev);
        }

        getContext.close();
    }

    private Index generateIndex(Random random, int maxsize, int runLength) {
        final Index.SequentialBuilder builder = Index.FACTORY.getSequentialBuilder();
        int nextKey = random.nextInt(runLength);
        while (nextKey < maxsize) {
            int lastKey;
            if (random.nextBoolean()) {
                final int length = Math.min(random.nextInt(runLength) + 1, maxsize - nextKey);
                lastKey =  nextKey + length - 1;
                builder.appendRange(nextKey, lastKey);
            } else {
                builder.appendKey(lastKey = nextKey);
            }
            nextKey = lastKey + 1 + random.nextInt(runLength + 1);
        }

        return builder.getIndex();
    }

    private void checkRandomFill(int chunkSize, ShortSparseArraySource source, ColumnSource.FillContext fillContext,
                                 WritableShortChunk<Values> dest, short[] expectations, Index index, boolean usePrev) {
        for (final OrderedKeys.Iterator okIt = index.getOrderedKeysIterator(); okIt.hasMore(); ) {
            final OrderedKeys nextOk = okIt.getNextOrderedKeysWithLength(chunkSize);

            if (usePrev) {
                source.fillChunk(fillContext, dest, nextOk);
            } else {
                source.fillPrevChunk(fillContext, dest, nextOk);
            }

            int ii = 0;
            for (final Index.Iterator indexIt = nextOk.asIndex().iterator(); indexIt.hasNext(); ii++) {
                final long next = indexIt.nextLong();
                checkFromValues("expectations[" + next + "] vs. dest[" + ii + "]", expectations[(int)next], dest.get(ii));
            }
        }
    }

    private void checkRangeFill(int chunkSize, ShortSparseArraySource source, ColumnSource.FillContext fillContext,
                                WritableShortChunk<Values> dest, short[] expectations, int firstKey, int lastKey, boolean usePrev) {
        int offset;
        final Index index = Index.FACTORY.getIndexByRange(firstKey, lastKey);
        offset = firstKey;
        for (final OrderedKeys.Iterator it = index.getOrderedKeysIterator(); it.hasMore(); ) {
            final OrderedKeys nextOk = it.getNextOrderedKeysWithLength(chunkSize);

            if (usePrev) {
                source.fillPrevChunk(fillContext, dest, nextOk);
            } else {
                source.fillChunk(fillContext, dest, nextOk);
            }
            checkRangeResults(expectations, offset, nextOk, dest);
            offset += nextOk.size();
        }
    }

    private void checkRangeGet(int chunkSize, ShortSparseArraySource source, ColumnSource.GetContext getContext, short[] expectations, int firstKey, int lastKey, boolean usePrev) {
        int offset;
        final Index index = Index.FACTORY.getIndexByRange(firstKey, lastKey);
        offset = firstKey;
        for (final OrderedKeys.Iterator it = index.getOrderedKeysIterator(); it.hasMore(); ) {
            final OrderedKeys nextOk = it.getNextOrderedKeysWithLength(chunkSize);

            final ShortChunk<Values> result;
            if (usePrev) {
                result = source.getPrevChunk(getContext, nextOk).asShortChunk();
            } else {
                result = source.getChunk(getContext, nextOk).asShortChunk();
            }
            checkRangeResults(expectations, offset, nextOk, result);
            // region samecheck
            final int firstBlock = firstKey / 1024;
            final int lastBlock = lastKey / 1024;
            if (!usePrev && (firstBlock == lastBlock) && (firstBlock % 2 == 0)) {
                assertTrue(DefaultGetContext.isMyResettableChunk(getContext, result));
            }
            // endregion samecheck
            offset += nextOk.size();
        }
    }

    private void checkRangeResults(short[] expectations, int offset, OrderedKeys nextOk, ShortChunk<Values> result) {
        for (int ii = 0; ii < nextOk.size(); ++ii) {
            checkFromValues("expectations[" + offset + " + " + ii + " = " + (ii + offset) + "] vs. dest[" + ii + "]", expectations[ii + offset], result.get(ii));
        }
    }

    // region fromvalues
    private void checkFromValues(String msg, short fromValues, short fromChunk) {
        assertEquals(msg, fromValues, fromChunk);
    }
    // endregion fromvalues

    // region fromsource
    private void checkFromSource(String msg, short fromSource, short fromChunk) {
        assertEquals(msg, fromSource, fromChunk);
    }
    // endregion fromsource

    @Test
    public void testSourceSink() {
        TestSourceSink.runTests(ChunkType.Short, size -> {
            final ShortSparseArraySource src = new ShortSparseArraySource();
            src.ensureCapacity(size);
            return src;
        });
    }

    @Test
    public void confirmAliasingForbidden() {
        final Random rng = new Random(438269476);
        final int arraySize = 100;
        final int rangeStart = 20;
        final int rangeEnd = 80;
        final ShortSparseArraySource source = new ShortSparseArraySource();
        source.ensureCapacity(arraySize);

        final short[] data = ArrayGenerator.randomShorts(rng, arraySize);
        for (int ii = 0; ii < data.length; ++ii) {
            source.set(ii, data[ii]);
        }
        // super hack
        final short[] peekedBlock = source.ensureBlock(0, 0, 0);

        try (Index srcKeys = Index.FACTORY.getIndexByRange(rangeStart, rangeEnd)) {
            try (Index destKeys = Index.FACTORY.getIndexByRange(rangeStart + 1, rangeEnd + 1)) {
                try (ChunkSource.GetContext srcContext = source.makeGetContext(arraySize)) {
                    try (WritableChunkSink.FillFromContext destContext = source.makeFillFromContext(arraySize)) {
                        Chunk chunk = source.getChunk(srcContext, srcKeys);
                        if (chunk.isAlias(peekedBlock)) {
                            // If the ArraySource gives out aliases of its blocks, then it should throw when we try to
                            // fill from that aliased chunk
                            boolean testFailed;
                            try {
                                source.fillFromChunk(destContext, chunk, destKeys);
                                testFailed = true;
                            } catch (UnsupportedOperationException uoe) {
                                testFailed = false;
                            }
                            assertFalse(testFailed);
                        }
                    }
                }
            }
        }
    }

    // This code tickles a bug where the act of trying to fill a chunk activates the prevFlusher, but the fact that
    // there's no data in the chunk means that the prev arrays were never changed from null. This would trigger a
    // null reference exception at commit time. The fix is to have the chunk methods bail out early if there is nothing
    // to do.
    @Test
    public void testFilllEmptyChunkWithPrev() {
        final ShortSparseArraySource src = new ShortSparseArraySource();
        src.startTrackingPrevValues();
        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        try (final Index keys = Index.FACTORY.getEmptyIndex();
             final WritableShortChunk<Values> chunk = WritableShortChunk.makeWritableChunk(0)) {
            // Fill from an empty chunk
            src.fillFromChunkByKeys(keys, chunk);
        }
        // NullPointerException in ShortSparseArraySource.commitUpdates()
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();
    }

    @Test
    public void testSerialization() throws Exception {
        final Random random = new Random(36403335);
        final int numValues = 100_000;
        final long[] randomKeys = new long[numValues];
        for (int ii = 0; ii < numValues; ++ii) {
            // No negative keys please.
            randomKeys[ii] = random.nextLong() & ~(1L << 63);
        }
        final short[] randomValues = ArrayGenerator.randomShorts(random, numValues);
        // Make every one out of 10 null
        for (int ii = 0; ii < numValues; ii += 10) {
            // region arrayFill
            randomValues[ii] = NULL_SHORT;
            // endregion arrayFill
        }

        final ShortSparseArraySource source = new ShortSparseArraySource();
        for (int ii = 0; ii < numValues; ++ii) {
            source.set(randomKeys[ii], randomValues[ii]);
        }

        final byte[] bytes;
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
            oos.flush();
            bytes = bos.toByteArray();
        }

        final ShortSparseArraySource dest;
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            final ObjectInputStream ois = new ObjectInputStream(bis)) {
            dest = (ShortSparseArraySource) ois.readObject();
        }

        // region elementGet
        for (long key : randomKeys) {
            final short srcKey = source.getShort(key);
            final short destKey = dest.getShort(key);
            assertEquals(srcKey, destKey);
        }
        // endregion elementGet
    }
}
