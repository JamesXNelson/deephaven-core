/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit TestCharacterDeltaAwareColumnSource and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sources.deltaaware;

import io.deephaven.db.tables.live.LiveTableMonitor;
import io.deephaven.db.v2.sources.ArrayGenerator;
import io.deephaven.db.v2.sources.chunk.ChunkSource;
import io.deephaven.db.v2.sources.chunk.ShortChunk;
import io.deephaven.db.v2.sources.chunk.Attributes.Values;
import io.deephaven.db.v2.utils.Index;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static io.deephaven.util.QueryConstants.*;
import static junit.framework.TestCase.*;

public class TestShortDeltaAwareColumnSource {
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
    public void simple1() {
        final Random rng = new Random(832952914);
        final long key0 = 5;
        final long key1 = 6;
        final short expected1 = ArrayGenerator.randomShorts(rng, 1)[0];

        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        final DeltaAwareColumnSource<Short> source = new DeltaAwareColumnSource<>(short.class);
        source.ensureCapacity(10);

        source.set(key1, expected1);
        final short actual0 = source.getShort(key0);
        final short actual1 = source.getShort(key1);
        assertEquals(NULL_SHORT, actual0);
        assertEquals(expected1, actual1);
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();
    }

    @Test
    public void simple2() {
        final Random rng = new Random(275128810);
        final long key0 = 5;
        final long key1 = 6;
        final short[] values = ArrayGenerator.randomShorts(rng, 3);
        final short expected0_0 = values[0];
        final short expected0_1 = values[1];
        final short expected1 = values[2];
        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        final DeltaAwareColumnSource<Short> source = new DeltaAwareColumnSource<>(short.class);
        source.ensureCapacity(10);
        source.set(key0, expected0_0);
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();

        source.startTrackingPrevValues();

        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        source.set(key0, expected0_1);
        source.set(key1, expected1);

        final short actual0_0 = source.getPrevShort(key0);
        final short actual0_1 = source.getShort(key0);
        final short actual1_0 = source.getPrevShort(key1);
        final short actual1_1 = source.getShort(key1);

        assertEquals(expected0_0, actual0_0);
        assertEquals(expected0_1, actual0_1);
        assertEquals(NULL_SHORT, actual1_0);
        assertEquals(expected1, actual1_1);

        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();
    }

    /**
     * We make a structure that looks like this. Then we query the whole thing with one range and see if we get what
     * we expect. Then we query with three subranges and again see if we get what we expect. Then in a second generation,
     * we write some new values, which creates a baseline/delta situation. We do those same queries again (note that
     * the subranges have been carefully chosen to span baseline and delta, so they're challenging) and again see if we
     * get what we expect.
     * Pictorially, the situation looks like this (best viewed with a monospace font).
     * baseline: BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
     * query1:   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
     * query2:             ^^^^^^^^^^^^^^^^^^^^               ^^^^^^^^^^               ^^^^^^^^^^^^^^^^^^^^
     * delta:                        DDDDDDDDDDDDDDDDDDDD                    DDDDDDDDDDDDDDDDDDDD
     */
    @Test
    public void overlapping() {
        final Random rng = new Random(912366186);
        final int length = 100;
        final short[] valuesPhase1 = ArrayGenerator.randomShorts(rng, length);
        final short[] valuesPhase2 = ArrayGenerator.randomShorts(rng, length);
        final HashMap<Long, Short> expectedPrev = new HashMap<>();
        final HashMap<Long, Short> expectedCurrent = new HashMap<>();
        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        final DeltaAwareColumnSource<Short> source = new DeltaAwareColumnSource<>(short.class);
        source.ensureCapacity(length);
        for (long ii = 0; ii < length; ++ii) {
            final short value = valuesPhase1[(int)ii];
            source.set(ii, value);
            expectedPrev.put(ii, value);
            expectedCurrent.put(ii, value);
        }
        source.startTrackingPrevValues();
        // Check the whole range using individual "get" calls.
        checkUsingGet(source, expectedCurrent, expectedPrev, 0, length);
        // Check the whole range in a chunked fashion using a single range.
        final long[] singleRange = {0, length};
        checkUsingChunk(source, expectedCurrent, expectedPrev, singleRange);

        // Check some subranges using three ranges.
        final long[] threeRanges = {10, 30, 45, 55, 70, 90};
        checkUsingChunk(source, expectedCurrent, expectedPrev, threeRanges);
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();

        // Now start the second cycle so we have different current and prev values.
        LiveTableMonitor.DEFAULT.startCycleForUnitTests();
        for (long ii = 20; ii < 40; ++ii) {
            final short value = valuesPhase2[(int)ii];
            source.set(ii, value);
            expectedCurrent.put(ii, value);
        }
        for (long ii = 60; ii < 80; ++ii) {
            final short value = valuesPhase2[(int)ii];
            source.set(ii, value);
            expectedCurrent.put(ii, value);
        }
        checkUsingGet(source, expectedCurrent, expectedPrev, 0, length);
        checkUsingChunk(source, expectedCurrent, expectedPrev, singleRange);
        checkUsingChunk(source, expectedCurrent, expectedPrev, threeRanges);
        LiveTableMonitor.DEFAULT.completeCycleForUnitTests();
    }

    private static void checkUsingGet(DeltaAwareColumnSource<Short> source, Map<Long, Short> expectedCurrent,
                                      Map<Long, Short> expectedPrev, int begin, int end) {
        // Check the whole thing by using individual get calls: current and prev.
        // current...
        for (long ii = begin; ii < end; ++ii) {
            final short expectedValue = expectedCurrent.get(ii);
            final short actualValue = source.getShort(ii);
            assertEquals(expectedValue, actualValue);
        }
        // prev...
        for (long ii = begin; ii < end; ++ii) {
            final short expectedValue = expectedPrev.get(ii);
            final short actualValue = source.getPrevShort(ii);
            assertEquals(expectedValue, actualValue);
        }
    }

    private static void checkUsingChunk(DeltaAwareColumnSource<Short> dacs, Map<Long, Short> expectedCurrent,
                                        Map<Long, Short> expectedPrev, long[] ranges) {
        final Index index = rangesToIndex(ranges);
        assertEquals(index.size() % 2, 0);

        // We're going to get creative and pull down the data as two slices
        final int chunkSize = (int) (index.size() / 2);

        // So we'll also split the index in half
        final Index index0 = index.subindexByPos(0, chunkSize);
        final Index index1 = index.subindexByPos(chunkSize, index.size());

        // Current...
        try (ChunkSource.GetContext context = dacs.makeGetContext(chunkSize)) {
            ShortChunk<? extends Values> chunk;

            chunk = dacs.getChunk(context, index0).asShortChunk();
            checkChunk(chunk, expectedCurrent, index0);
            chunk = dacs.getChunk(context, index1).asShortChunk();
            checkChunk(chunk, expectedCurrent, index1);

            chunk = dacs.getPrevChunk(context, index0).asShortChunk();
            checkChunk(chunk, expectedPrev, index0);
            chunk = dacs.getPrevChunk(context, index1).asShortChunk();
            checkChunk(chunk, expectedPrev, index1);
        }
    }

    private static void checkChunk(ShortChunk<? extends Values> values, Map<Long, Short> expected, Index keys) {
        int sliceOffset = 0;
        for (final Index.Iterator it = keys.iterator(); it.hasNext(); ) {
            final long key = it.nextLong();
            final short expectedValue = expected.get(key);
            final short actualValue = values.get(sliceOffset++);
            assertEquals(expectedValue, actualValue);
        }
    }

    private static Index rangesToIndex(long[] ranges) {
        Index.SequentialBuilder builder = Index.FACTORY.getSequentialBuilder();
        for (int ii = 0; ii < ranges.length; ii += 2) {
            builder.appendRange(ranges[ii], ranges[ii + 1] - 1);
        }
        return builder.getIndex();
    }
}
