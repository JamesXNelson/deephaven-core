package io.deephaven.db.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.db.util.BooleanUtils;
import io.deephaven.db.util.tuples.generated.ByteFloatShortTuple;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.ByteChunk;
import io.deephaven.db.v2.sources.chunk.Chunk;
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
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Byte, Float, and Short.
 * <p>Generated by {@link io.deephaven.db.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ReinterpretedBooleanFloatShortColumnTupleSource extends AbstractTupleSource<ByteFloatShortTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link ReinterpretedBooleanFloatShortColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<ByteFloatShortTuple, Byte, Float, Short> FACTORY = new Factory();

    private final ColumnSource<Byte> columnSource1;
    private final ColumnSource<Float> columnSource2;
    private final ColumnSource<Short> columnSource3;

    public ReinterpretedBooleanFloatShortColumnTupleSource(
            @NotNull final ColumnSource<Byte> columnSource1,
            @NotNull final ColumnSource<Float> columnSource2,
            @NotNull final ColumnSource<Short> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final ByteFloatShortTuple createTuple(final long indexKey) {
        return new ByteFloatShortTuple(
                columnSource1.getByte(indexKey),
                columnSource2.getFloat(indexKey),
                columnSource3.getShort(indexKey)
        );
    }

    @Override
    public final ByteFloatShortTuple createPreviousTuple(final long indexKey) {
        return new ByteFloatShortTuple(
                columnSource1.getPrevByte(indexKey),
                columnSource2.getPrevFloat(indexKey),
                columnSource3.getPrevShort(indexKey)
        );
    }

    @Override
    public final ByteFloatShortTuple createTupleFromValues(@NotNull final Object... values) {
        return new ByteFloatShortTuple(
                BooleanUtils.booleanAsByte((Boolean)values[0]),
                TypeUtils.unbox((Float)values[1]),
                TypeUtils.unbox((Short)values[2])
        );
    }

    @Override
    public final ByteFloatShortTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new ByteFloatShortTuple(
                TypeUtils.unbox((Byte)values[0]),
                TypeUtils.unbox((Float)values[1]),
                TypeUtils.unbox((Short)values[2])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final ByteFloatShortTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) BooleanUtils.byteAsBoolean(tuple.getFirstElement()));
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
    public final Object exportToExternalKey(@NotNull final ByteFloatShortTuple tuple) {
        return new SmartKey(
                BooleanUtils.byteAsBoolean(tuple.getFirstElement()),
                TypeUtils.box(tuple.getSecondElement()),
                TypeUtils.box(tuple.getThirdElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final ByteFloatShortTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return BooleanUtils.byteAsBoolean(tuple.getFirstElement());
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
    public final Object exportElementReinterpreted(@NotNull final ByteFloatShortTuple tuple, int elementIndex) {
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
        WritableObjectChunk<ByteFloatShortTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        ByteChunk<Attributes.Values> chunk1 = chunks[0].asByteChunk();
        FloatChunk<Attributes.Values> chunk2 = chunks[1].asFloatChunk();
        ShortChunk<Attributes.Values> chunk3 = chunks[2].asShortChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new ByteFloatShortTuple(chunk1.get(ii), chunk2.get(ii), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link ReinterpretedBooleanFloatShortColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<ByteFloatShortTuple, Byte, Float, Short> {

        private Factory() {
        }

        @Override
        public TupleSource<ByteFloatShortTuple> create(
                @NotNull final ColumnSource<Byte> columnSource1,
                @NotNull final ColumnSource<Float> columnSource2,
                @NotNull final ColumnSource<Short> columnSource3
        ) {
            return new ReinterpretedBooleanFloatShortColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
