/* ---------------------------------------------------------------------------------------------------------------------
import java.util.Objects;
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit BaseTestCharTimSortKernel and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sort.timsort;

import java.util.Objects;

import io.deephaven.util.QueryConstants;
import io.deephaven.db.util.tuples.generated.ObjectLongLongTuple;
import io.deephaven.db.util.tuples.generated.ObjectLongTuple;
import io.deephaven.db.v2.sort.findruns.ObjectFindRunsKernel;
import io.deephaven.db.v2.sort.partition.ObjectPartitionKernel;
import io.deephaven.db.v2.sources.AbstractColumnSource;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.*;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.OrderedKeys;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class BaseTestObjectTimSortKernel extends TestTimSortKernel {
    // region getJavaComparator
    static Comparator<ObjectLongTuple> getJavaComparator() {
        // noinspection unchecked
        return Comparator.comparing(x -> (Comparable)x.getFirstElement());
    }
    // endregion getJavaComparator

    // region getJavaMultiComparator
    static Comparator<ObjectLongLongTuple> getJavaMultiComparator() {
        // noinspection unchecked
        return Comparator.comparing(x -> (Comparable)((ObjectLongLongTuple)x).getFirstElement()).thenComparing(x -> ((ObjectLongLongTuple)x).getSecondElement());
    }
    // endregion getJavaMultiComparator


    static class ObjectSortKernelStuff extends SortKernelStuff<ObjectLongTuple> {
        final WritableObjectChunk<Object, Any> ObjectChunk;
        private final ObjectLongTimsortKernel.ObjectLongSortKernelContext context;

        ObjectSortKernelStuff(List<ObjectLongTuple> javaTuples) {
            super(javaTuples.size());
            final int size = javaTuples.size();
            ObjectChunk = WritableObjectChunk.makeWritableChunk(size);
            context = ObjectLongTimsortKernel.createContext(size);

            prepareObjectChunks(javaTuples, ObjectChunk, indexKeys);
        }

        @Override
        void run() {
            ObjectLongTimsortKernel.sort(context, indexKeys, ObjectChunk);
        }

        @Override
        void check(List<ObjectLongTuple> expected) {
            verify(expected.size(), expected, ObjectChunk, indexKeys);
        }
    }

    public static class ObjectPartitionKernelStuff extends PartitionKernelStuff<ObjectLongTuple> {
        final WritableObjectChunk valuesChunk;
        private final ObjectPartitionKernel.PartitionKernelContext context;
        private final Index index;
        private final ColumnSource<Object> columnSource;

        public ObjectPartitionKernelStuff(List<ObjectLongTuple> javaTuples, Index index, int chunkSize, int nPartitions, boolean preserveEquality) {
            super(javaTuples.size());
            this.index = index;
            final int size = javaTuples.size();
            valuesChunk = WritableObjectChunk.makeWritableChunk(size);

            for (int ii = 0; ii < javaTuples.size(); ++ii) {
                final long indexKey = javaTuples.get(ii).getSecondElement();
                if (indexKey != ii * 10) {
                    throw new IllegalStateException();
                }
            }

            columnSource = new AbstractColumnSource.DefaultedImmutable<Object>(Object.class) {
                // region tuple column source
                @Override
                public Object get(long index) {
                    return javaTuples.get(((int)index) / 10).getFirstElement();
                }
                // endregion tuple column source
            };

            context = ObjectPartitionKernel.createContext(index, columnSource, chunkSize, nPartitions, preserveEquality);

            prepareObjectChunks(javaTuples, valuesChunk, indexKeys);
        }

        @Override
        public void run() {
            ObjectPartitionKernel.partition(context, indexKeys, valuesChunk);
        }

        @Override
        void check(List<ObjectLongTuple> expected) {
            verifyPartition(context, index, expected.size(), expected, valuesChunk, indexKeys, columnSource);
        }
    }

    static class ObjectMergeStuff extends MergeStuff<ObjectLongTuple> {
        final Object arrayValues[];
        ObjectMergeStuff(List<ObjectLongTuple> javaTuples) {
            super(javaTuples);
            arrayValues = new Object[javaTuples.size()];
            for (int ii = 0; ii < javaTuples.size(); ++ii) {
                arrayValues[ii] = javaTuples.get(ii).getFirstElement();
            }
        }

        void run() {
            // region mergesort
            // noinspection unchecked
            MergeSort.mergeSort(posarray, posarray2, 0, arrayValues.length, 0, (pos1, pos2) -> Objects.compare((Comparable)arrayValues[(int)pos1], (Comparable)arrayValues[(int)pos2], Comparator.naturalOrder()));
            // endregion mergesort
        }
    }

    static class ObjectMultiSortKernelStuff extends SortMultiKernelStuff<ObjectLongLongTuple> {
        final WritableObjectChunk<Object, Any> primaryChunk;
        final WritableLongChunk<Any> secondaryChunk;
        final WritableLongChunk<Any> secondaryChunkPermuted;

        final LongIntTimsortKernel.LongIntSortKernelContext sortIndexContext;
        final WritableLongChunk<KeyIndices> indicesToFetch;
        final WritableIntChunk<ChunkPositions> originalPositions;

        final ColumnSource<Long> secondaryColumnSource;

        private final ObjectLongTimsortKernel.ObjectLongSortKernelContext context;
        private final LongLongTimsortKernel.LongLongSortKernelContext secondarySortContext;
        private final ColumnSource.FillContext secondaryColumnSourceContext;

        ObjectMultiSortKernelStuff(List<ObjectLongLongTuple> javaTuples) {
            super(javaTuples.size());
            final int size = javaTuples.size();
            primaryChunk = WritableObjectChunk.makeWritableChunk(size);
            secondaryChunk = WritableLongChunk.makeWritableChunk(size);
            secondaryChunkPermuted = WritableLongChunk.makeWritableChunk(size);
            context = ObjectLongTimsortKernel.createContext(size);

            sortIndexContext = LongIntTimsortKernel.createContext(size);

            indicesToFetch = WritableLongChunk.makeWritableChunk(size);
            originalPositions = WritableIntChunk.makeWritableChunk(size);

            secondarySortContext = io.deephaven.db.v2.sort.timsort.LongLongTimsortKernel.createContext(size);

            prepareMultiObjectChunks(javaTuples, primaryChunk, secondaryChunk, indexKeys);

            secondaryColumnSource = new AbstractColumnSource.DefaultedImmutable<Long>(long.class) {
                @Override
                public Long get(long index) {
                    final long result = getLong(index);
                    return result == QueryConstants.NULL_LONG ? null : result;
                }

                @Override
                public long getLong(long index) {
                    final ObjectLongLongTuple ObjectLongLongTuple = javaTuples.get((int) (index / 10));
                    return ObjectLongLongTuple.getSecondElement();
                }
            };

            secondaryColumnSourceContext = secondaryColumnSource.makeFillContext(size);
        }

        @Override
        void run() {
            ObjectLongTimsortKernel.sort(context, indexKeys, primaryChunk, offsets, lengths);
            ObjectFindRunsKernel.findRuns(primaryChunk, offsets, lengths, offsetsOut, lengthsOut);
//            dumpChunk(primaryChunk);
//            dumpOffsets(offsetsOut, lengthsOut);
            if (offsetsOut.size() > 0) {
                // the secondary context is actually just bogus at this point, it is no longer parallel,
                // what we need to do is fetch the things from that columnsource, but only the things needed to break
                // ties, and then put them in chunks that would be parallel to the index chunk based on offsetsOut and lengthsOut
                //
                // after some consideration, I think the next stage of the sort is:
                // (1) using the chunk of index keys that are relevant, build a second chunk that indicates their position
                // (2) use the LongTimsortKernel to sort by the index key; using the position keys as our as our "indexKeys"
                //     argument.  The sorted index keys can be used as input to an index builder for filling a chunk.
                // (3) After the chunk of secondary keys is filled, the second sorted indexKeys (really positions that
                //     we care about), will then be used to permute the resulting chunk into a parallel chunk
                //     to our actual indexKeys.
                // (4) We can call this kernel; and do the sub region sorts

                indicesToFetch.setSize(0);
                originalPositions.setSize(0);

                for (int ii = 0; ii < offsetsOut.size(); ++ii) {
                    final int runStart = offsetsOut.get(ii);
                    final int runLength = lengthsOut.get(ii);

                    for (int jj = 0; jj < runLength; ++jj) {
                        indicesToFetch.add(indexKeys.get(runStart + jj));
                        originalPositions.add(runStart +jj);
                    }
                }

                sortIndexContext.sort(originalPositions, indicesToFetch);

                // now we have the indices that we need to fetch from the secondary column source, in sorted order
                secondaryColumnSource.fillChunk(secondaryColumnSourceContext, WritableLongChunk.downcast(secondaryChunk), OrderedKeys.wrapKeyIndicesChunkAsOrderedKeys(WritableLongChunk.downcast(indicesToFetch)));

                // permute the results back to the order that we would like them in the subsequent sort
                secondaryChunkPermuted.setSize(secondaryChunk.size());
                for (int ii = 0; ii < originalPositions.size(); ++ii) {
                    secondaryChunkPermuted.set(originalPositions.get(ii), secondaryChunk.get(ii));
                }

                // and we can sort the stuff within the run now
                LongLongTimsortKernel.sort(secondarySortContext, indexKeys, secondaryChunkPermuted, offsetsOut, lengthsOut);
            }
        }

        @Override
        void check(List<ObjectLongLongTuple> expected) {
            verify(expected.size(), expected, primaryChunk, secondaryChunk, indexKeys);
        }
    }

    static private void prepareObjectChunks(List<ObjectLongTuple> javaTuples, WritableObjectChunk valueChunk, WritableLongChunk<Attributes.KeyIndices> indexKeys) {
        for (int ii = 0; ii < javaTuples.size(); ++ii) {
            valueChunk.set(ii, javaTuples.get(ii).getFirstElement());
            indexKeys.set(ii, javaTuples.get(ii).getSecondElement());
        }
    }

    static private void prepareMultiObjectChunks(List<ObjectLongLongTuple> javaTuples, WritableObjectChunk valueChunk, WritableLongChunk secondaryChunk, WritableLongChunk<Attributes.KeyIndices> indexKeys) {
        for (int ii = 0; ii < javaTuples.size(); ++ii) {
            valueChunk.set(ii, javaTuples.get(ii).getFirstElement());
            secondaryChunk.set(ii, javaTuples.get(ii).getSecondElement());
            indexKeys.set(ii, javaTuples.get(ii).getThirdElement());
        }
    }

    @NotNull
    public static List<ObjectLongTuple> generateObjectRandom(Random random, int size) {
        final List<ObjectLongTuple> javaTuples = new ArrayList<>();
        for (int ii = 0; ii < size; ++ii) {
            final Object value = generateObjectValue(random);
            final long longValue = ii * 10;

            final ObjectLongTuple tuple = new ObjectLongTuple(value, longValue);

            javaTuples.add(tuple);
        }
        return javaTuples;
    }

    @NotNull
    static List<ObjectLongLongTuple> generateMultiObjectRandom(Random random, int size) {
        final List<ObjectLongTuple> primaryTuples = generateObjectRandom(random, size);

        return primaryTuples.stream().map(clt -> new ObjectLongLongTuple(clt.getFirstElement(), random.nextLong(), clt.getSecondElement())).collect(Collectors.toList());
    }

    @NotNull
    public static List<ObjectLongTuple> generateObjectRuns(Random random, int size) {
        return generateObjectRuns(random, size, true, true);
    }

    @NotNull
    public static List<ObjectLongTuple> generateAscendingObjectRuns(Random random, int size) {
        return generateObjectRuns(random, size, true, false);
    }

    @NotNull
    public static  List<ObjectLongTuple> generateDescendingObjectRuns(Random random, int size) {
        return generateObjectRuns(random, size, false, true);
    }

    @NotNull
    static private List<ObjectLongTuple> generateObjectRuns(Random random, int size, boolean allowAscending, boolean allowDescending) {
        final List<ObjectLongTuple> javaTuples = new ArrayList<>();
        int runStart = 0;
        while (runStart < size) {
            final int maxrun = size - runStart;
            final int runSize = Math.max(random.nextInt(200), maxrun);

            Object value = generateObjectValue(random);
            final boolean descending = !allowAscending || (allowDescending && random.nextBoolean());

            for (int ii = 0; ii < runSize; ++ii) {
                final long indexValue = (runStart + ii) * 10;
                final ObjectLongTuple tuple = new ObjectLongTuple(value, indexValue);

                javaTuples.add(tuple);

                if (descending) {
                    value = decrementObjectValue(random, value);
                } else {
                    value = incrementObjectValue(random, value);
                }
            }

            runStart += runSize;
        }
        return javaTuples;
    }

    static private void verify(int size, List<ObjectLongTuple> javaTuples, ObjectChunk ObjectChunk, LongChunk indexKeys) {
//        System.out.println("Verify: " + javaTuples);
//        dumpChunk(valuesChunk);

        for (int ii = 0; ii < size; ++ii) {
            final Object timSorted = ObjectChunk.get(ii);
            final Object javaSorted = javaTuples.get(ii).getFirstElement();

            final long timIndex = indexKeys.get(ii);
            final long javaIndex = javaTuples.get(ii).getSecondElement();

            TestCase.assertEquals("values[" + ii + "]", javaSorted, timSorted);
            TestCase.assertEquals("index[" + ii + "]", javaIndex, timIndex);
        }
    }

    static private void verifyPartition(ObjectPartitionKernel.PartitionKernelContext context, Index source, int size, List<ObjectLongTuple> javaTuples, ObjectChunk ObjectChunk, LongChunk indexKeys, ColumnSource<Object> columnSource) {

        final ObjectLongTuple [] pivots = context.getPivots();

        final Index [] results = context.getPartitions(true);

        final Index reconstructed = Index.FACTORY.getEmptyIndex();

        // make sure that each partition is a subset of the index and is disjoint
        for (int ii = 0; ii < results.length; ii++) {
            final Index partition = results[ii];
            TestCase.assertTrue("partition[" + ii + "].subsetOf(source)", partition.subsetOf(source));
            TestCase.assertFalse("reconstructed[\" + ii + \"]..overlaps(partition)", reconstructed.overlaps(partition));
            reconstructed.insert(partition);
        }

        TestCase.assertEquals(source, reconstructed);

        // now verify that each partition has keys less than the next larger partition

//        System.out.println(javaTuples);


//        System.out.println(javaTuples);

        for (int ii = 0; ii < results.length - 1; ii++) {
            final Index partition = results[ii];

            final Object expectedPivotValue = pivots[ii].getFirstElement();
            final long expectedPivotKey = pivots[ii].getSecondElement();

            for (int jj = 0; jj < partition.size(); ++jj) {
                final long index = partition.get(jj);
                final Object value = columnSource.get(index);
                if (gt(value, expectedPivotValue)) {
                    TestCase.fail("pivot[" + ii + "] = " + expectedPivotValue + ", " + expectedPivotKey + ": is exceeded by" + value);
                } else if (value == expectedPivotValue && index > expectedPivotKey) {
                    TestCase.fail("pivot[" + ii + "] = " + expectedPivotValue + ", " + expectedPivotKey + ": is exceeded by" + value + ", "  + index);
                }
            }
        }

        final List<ObjectLongTuple> sortedTuples = new ArrayList<>(javaTuples);
        sortedTuples.sort(getJavaComparator());

        int lastSize = 0;

        for (int ii = 0; ii < results.length; ii++) {
            final Index partition = results[ii];

//            System.out.println("Partition[" + ii + "] " + partition.size());

//            System.out.println("(" + lastSize + ", " + (lastSize + partition.intSize()) + ")");
            final List<ObjectLongTuple> expectedPartition = sortedTuples.subList(lastSize, lastSize + partition.intSize());
            lastSize += partition.intSize();
//            System.out.println("Expected Partition Max: " + expectedPartition.get(expectedPartition.size() - 1));

            final Index.RandomBuilder builder = Index.FACTORY.getRandomBuilder();
            expectedPartition.stream().mapToLong(ObjectLongTuple::getSecondElement).forEach(builder::addKey);
            final Index expectedIndex = builder.getIndex();

            if (!expectedIndex.equals(partition)) {
                System.out.println("partition.minus(expected): " + partition.minus(expectedIndex));
                System.out.println("expectedIndex.minus(partition): " + expectedIndex.minus(partition));
            }

            TestCase.assertEquals(expectedIndex, partition);
        }

//
//        for (int ii = 0; ii < size; ++ii) {
//            final Object timSorted = valuesChunk.get(ii);
//            final Object javaSorted = javaTuples.get(ii).getFirstElement();
//
//            final long timIndex = indexKeys.get(ii);
//            final long javaIndex = javaTuples.get(ii).getSecondElement();
//
//            TestCase.assertEquals("values[" + ii + "]", javaSorted, timSorted);
//            TestCase.assertEquals("index[" + ii + "]", javaIndex, timIndex);
//        }
    }

    static private void verify(int size, List<ObjectLongLongTuple> javaTuples, ObjectChunk primaryChunk, LongChunk secondaryChunk, LongChunk<Attributes.KeyIndices> indexKeys) {
//        System.out.println("Verify: " + javaTuples);
//        dumpChunks(primaryChunk, indexKeys);

        for (int ii = 0; ii < size; ++ii) {
            final Object timSortedPrimary = primaryChunk.get(ii);
            final Object javaSorted = javaTuples.get(ii).getFirstElement();

            final long timIndex = indexKeys.get(ii);
            final long javaIndex = javaTuples.get(ii).getThirdElement();

            TestCase.assertEquals("values[" + ii + "]", javaSorted, timSortedPrimary);
            TestCase.assertEquals("index[" + ii + "]", javaIndex, timIndex);
        }
    }

//    private static void dumpChunk(ObjectChunk chunk) {
//        System.out.println("[" + IntStream.range(0, chunk.size()).mapToObj(chunk::get).map(c -> (short)(Object)c).map(Object::toString).collect(Collectors.joining(",")) + "]");
//    }
//
//    private static void dumpOffsets(IntChunk starts, IntChunk lengths) {
//        System.out.println("[" + IntStream.range(0, starts.size()).mapToObj(idx -> "(" + starts.get(idx) + " -> " + lengths.get(idx) + ")").collect(Collectors.joining(",")) + "]");
//    }
//
//    private static void dumpChunks(ObjectChunk primary, LongChunk secondary) {
//        System.out.println("[" + IntStream.range(0, primary.size()).mapToObj(ii -> new ObjectLongTuple(primary.get(ii), secondary.get(ii)).toString()).collect(Collectors.joining(",")) + "]");
//    }

    // region comparison functions
    // ascending comparison
    private static int doComparison(Object lhs, Object rhs) {
       if (lhs == rhs) {
            return 0;
        }
        if (lhs == null) {
            return -1;
        }
        if (rhs == null) {
            return 1;
        }
        //noinspection unchecked
        return ((Comparable)lhs).compareTo(rhs);
    }

    // endregion comparison functions

    private static boolean gt(Object lhs, Object rhs) {
        return doComparison(lhs, rhs) > 0;
    }
}
