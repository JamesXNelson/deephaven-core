package io.deephaven.db.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.db.util.tuples.generated.DoubleFloatShortTuple;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.DoubleChunk;
import io.deephaven.db.v2.sources.chunk.FloatChunk;
import io.deephaven.db.v2.sources.chunk.ObjectChunk;
import io.deephaven.db.v2.sources.chunk.ShortChunk;
import io.deephaven.db.v2.sources.chunk.WritableChunk;
import io.deephaven.db.v2.sources.chunk.WritableObjectChunk;
import io.deephaven.db.v2.tuples.AbstractTupleSource;
import io.deephaven.db.v2.tuples.ThreeColumnTupleSourceFactory;
import io.deephaven.db.v2.tuples.TupleSource;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Double, Float, and Short.
 * <p>Generated by {@link io.deephaven.db.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DoubleFloatShortColumnTupleSource extends AbstractTupleSource<DoubleFloatShortTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link DoubleFloatShortColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<DoubleFloatShortTuple, Double, Float, Short> FACTORY = new Factory();

    private final ColumnSource<Double> columnSource1;
    private final ColumnSource<Float> columnSource2;
    private final ColumnSource<Short> columnSource3;

    public DoubleFloatShortColumnTupleSource(
            @NotNull final ColumnSource<Double> columnSource1,
            @NotNull final ColumnSource<Float> columnSource2,
            @NotNull final ColumnSource<Short> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final DoubleFloatShortTuple createTuple(final long indexKey) {
        return new DoubleFloatShortTuple(
                columnSource1.getDouble(indexKey),
                columnSource2.getFloat(indexKey),
                columnSource3.getShort(indexKey)
        );
    }

    @Override
    public final DoubleFloatShortTuple createPreviousTuple(final long indexKey) {
        return new DoubleFloatShortTuple(
                columnSource1.getPrevDouble(indexKey),
                columnSource2.getPrevFloat(indexKey),
                columnSource3.getPrevShort(indexKey)
        );
    }

    @Override
    public final DoubleFloatShortTuple createTupleFromValues(@NotNull final Object... values) {
        return new DoubleFloatShortTuple(
                TypeUtils.unbox((Double)values[0]),
                TypeUtils.unbox((Float)values[1]),
                TypeUtils.unbox((Short)values[2])
        );
    }

    @Override
    public final DoubleFloatShortTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new DoubleFloatShortTuple(
                TypeUtils.unbox((Double)values[0]),
                TypeUtils.unbox((Float)values[1]),
                TypeUtils.unbox((Short)values[2])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final DoubleFloatShortTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationIndexKey, tuple.getSecondElement());
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationIndexKey, tuple.getThirdElement());
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportToExternalKey(@NotNull final DoubleFloatShortTuple tuple) {
        return new SmartKey(
                TypeUtils.box(tuple.getFirstElement()),
                TypeUtils.box(tuple.getSecondElement()),
                TypeUtils.box(tuple.getThirdElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final DoubleFloatShortTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return TypeUtils.box(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final DoubleFloatShortTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return TypeUtils.box(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    protected void convertChunks(@NotNull WritableChunk<? super Attributes.Values> destination, int chunkSize, Chunk<Attributes.Values> [] chunks) {
        WritableObjectChunk<DoubleFloatShortTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        DoubleChunk<Attributes.Values> chunk1 = chunks[0].asDoubleChunk();
        FloatChunk<Attributes.Values> chunk2 = chunks[1].asFloatChunk();
        ShortChunk<Attributes.Values> chunk3 = chunks[2].asShortChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new DoubleFloatShortTuple(chunk1.get(ii), chunk2.get(ii), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link DoubleFloatShortColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<DoubleFloatShortTuple, Double, Float, Short> {

        private Factory() {
        }

        @Override
        public TupleSource<DoubleFloatShortTuple> create(
                @NotNull final ColumnSource<Double> columnSource1,
                @NotNull final ColumnSource<Float> columnSource2,
                @NotNull final ColumnSource<Short> columnSource3
        ) {
            return new DoubleFloatShortColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
