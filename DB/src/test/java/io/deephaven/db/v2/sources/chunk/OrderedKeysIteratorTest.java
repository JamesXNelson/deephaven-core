package io.deephaven.db.v2.sources.chunk;


import io.deephaven.db.v2.sources.chunk.Attributes.OrderedKeyIndices;
import io.deephaven.db.v2.sources.chunk.Attributes.OrderedKeyRanges;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.OrderedKeys;
import org.junit.Test;

import java.util.Random;

import static junit.framework.TestCase.assertEquals;

public class OrderedKeysIteratorTest {

    private static final int MAX_STEP = 65536;

    private long[] indexDataGenerator(Random random, int size, double gapProbability) {
        long result[] = new long[size];
        if (size == 0) {
            return result;
        }
        result[0] = Math.min(Math.abs(random.nextLong()), Long.MAX_VALUE - MAX_STEP * size);

        for (int i = 1; i < result.length; i++) {
            if (random.nextDouble() < gapProbability) {
                result[i] = result[i - 1] + 2 + random.nextInt(MAX_STEP);
            } else {
                result[i] = result[i - 1] + 1;
            }
        }
        return result;
    }

    private long[] valuesForIndex(Index index) {
        long result[] = new long[(int) index.size()];
        Index.Iterator it = index.iterator();
        for (int i = 0; i < result.length; i++) {
            result[i] = it.nextLong();
        }
        return result;
    }

    private void genericTest(Index index, int chunkSize) {
        final long[] values = valuesForIndex(index);
        int vPos = 0;
        final OrderedKeys.Iterator wrapper = index.getOrderedKeysIterator();
        int step = 0;
        while (wrapper.hasMore()) {
            final OrderedKeys ok = wrapper.getNextOrderedKeysWithLength(chunkSize);
            final int localVPos = vPos;
            LongChunk<OrderedKeyIndices> indices = ok.asKeyIndicesChunk();
            for (int i = 0; i < indices.size(); i++) {
                assertEquals(indices.get(i), values[vPos++]);
            }
            vPos = localVPos;
            LongChunk<OrderedKeyRanges> ranges = ok.asKeyRangesChunk();
            for (int i = 0; i < ranges.size(); i += 2) {
                for (long idx = ranges.get(i); idx <= ranges.get(i + 1); idx++) {
                    assertEquals(idx, values[vPos++]);
                }
            }
            vPos = localVPos;
            indices = ok.asKeyIndicesChunk();
            for (int i = 0; i < indices.size(); i++) {
                assertEquals(indices.get(i), values[vPos++]);
            }
            vPos = localVPos;
            ranges = ok.asKeyRangesChunk();
            for (int i = 0; i < ranges.size(); i += 2) {
                for (long idx = ranges.get(i); idx <= ranges.get(i + 1); idx++) {
                    assertEquals(idx, values[vPos++]);
                }
            }
            step++;
            assertEquals(vPos, Math.min(chunkSize * step, index.size()));
        }
        assertEquals(vPos, index.size());
    }

    //

    @Test
    public void testAll() {
        Random random = new Random(0);
        for (int chunkSize = 1; chunkSize < 17; chunkSize++) {
            genericTest(Index.FACTORY.getIndexByValues(), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0, 1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 4), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 5), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 50, 51), chunkSize);
        }
        for (int i = 1; i < 25; i++) {
            for (double p = 0; p <= 1.1; p += .1) {
                long[] d = indexDataGenerator(random, i, p);
                Index index = Index.FACTORY.getIndexByValues(d);
                for (int chunkSize = 1; chunkSize < 17; chunkSize++) {
                    genericTest(index, chunkSize);
                }
                for (int chunkSize = 8; chunkSize < 65536; chunkSize *= 2) {
                    genericTest(index, chunkSize + 1);
                    genericTest(index, chunkSize);
                    genericTest(index, chunkSize - 1);

                }
            }
        }

        for (int chunkSize = 8; chunkSize < 65536; chunkSize *= 2) {
            genericTest(Index.FACTORY.getIndexByValues(), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0, 1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 4), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 5), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 50, 51), chunkSize);
            chunkSize++;
            genericTest(Index.FACTORY.getIndexByValues(), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0, 1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 4), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 5), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 50, 51), chunkSize);
            chunkSize -= 2;
            genericTest(Index.FACTORY.getIndexByValues(), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(0, 1), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 4), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 5), chunkSize);
            genericTest(Index.FACTORY.getIndexByValues(2, 3, 50, 51), chunkSize);
            chunkSize++;
        }

        for (int i = 16; i < 6536; i *= 2) {
            for (double p = 0; p <= 1.1; p += .1) {
                long[] d = indexDataGenerator(random, i, p);
                Index index = Index.FACTORY.getIndexByValues(d);
                for (int chunkSize = 1; chunkSize < 17; chunkSize++) {
                    genericTest(index, chunkSize);
                }
                for (int chunkSize = 8; chunkSize < 65536; chunkSize *= 2) {
                    genericTest(index, chunkSize);
                    genericTest(index, chunkSize - 1);
                    genericTest(index, chunkSize + 1);
                }
            }
            for (double p = 0; p <= 1.1; p += .1) {
                long[] d = indexDataGenerator(random, i + 1, p);
                Index index = Index.FACTORY.getIndexByValues(d);
                for (int chunkSize = 1; chunkSize < 17; chunkSize++) {
                    genericTest(index, chunkSize);
                }
                for (int chunkSize = 8; chunkSize < 65536; chunkSize *= 2) {
                    genericTest(index, chunkSize);
                    genericTest(index, chunkSize - 1);
                    genericTest(index, chunkSize + 1);
                }
            }
            for (double p = 0; p <= 1.1; p += .1) {
                long[] d = indexDataGenerator(random, i + 1, p);
                Index index = Index.FACTORY.getIndexByValues(d);
                for (int chunkSize = 1; chunkSize < 17; chunkSize++) {
                    genericTest(index, chunkSize);
                }
                for (int chunkSize = 8; chunkSize < 65536; chunkSize *= 2) {
                    genericTest(index, chunkSize);
                    genericTest(index, chunkSize - 1);
                    genericTest(index, chunkSize + 1);
                }
            }
        }
    }

}
