/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharPartitionKernelBenchmark and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sort.partition;

import io.deephaven.db.util.tuples.generated.ShortLongTuple;
import io.deephaven.db.v2.sort.timsort.BaseTestShortTimSortKernel;
import io.deephaven.db.v2.sort.timsort.TestTimSortKernel;
import io.deephaven.db.v2.utils.Index;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ShortPartitionKernelBenchmark {
    @Param({"32"})
    private int chunkSize;

    @Param({"128"})
    private int dataSize;

    @Param({"random", "runs", "ascending", "descending"})
    private String runType;

    @Param({"true", "false"})
    private boolean preserveEquality;

    @Param({"sqrt", "2", "8", "1024"})
    private String numPartitions;

    private Runnable doPartition;

    @TearDown(Level.Trial)
    public void finishTrial() {
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        System.out.println("Size = " + chunkSize);

        final TestTimSortKernel.GenerateTupleList<ShortLongTuple> generate;
        switch (runType) {
            case "random":
                generate = BaseTestShortTimSortKernel::generateShortRandom;
                break;
            case "runs":
                generate = BaseTestShortTimSortKernel::generateShortRuns;
                break;
            case "ascending":
                generate = BaseTestShortTimSortKernel::generateAscendingShortRuns;
                break;
            case "descending":
                generate = BaseTestShortTimSortKernel::generateDescendingShortRuns;
                break;
            default:
                throw new IllegalArgumentException("Bad runType: " + runType);
        }

        // i would prefer to update the seed here
        final Random random = new Random(0);
        final List<ShortLongTuple> stuffToSort = generate.generate(random, dataSize);

        final Index.SequentialBuilder sequentialBuilder = Index.FACTORY.getSequentialBuilder();
        stuffToSort.stream().mapToLong(ShortLongTuple::getSecondElement).forEach(sequentialBuilder::appendKey);
        final Index index = sequentialBuilder.getIndex();
        final int numPartitionsValue;
        if ("sqrt".equals(numPartitions)) {
            numPartitionsValue = (int) Math.sqrt(stuffToSort.size());
        } else {
            numPartitionsValue = Integer.parseInt(numPartitions);
        }
        final BaseTestShortTimSortKernel.ShortPartitionKernelStuff partitionStuff = new BaseTestShortTimSortKernel.ShortPartitionKernelStuff(stuffToSort, index, chunkSize, numPartitionsValue, preserveEquality);
        doPartition = partitionStuff::run;
    }

    @Benchmark
    public void partition() {
        doPartition.run();
    }
}