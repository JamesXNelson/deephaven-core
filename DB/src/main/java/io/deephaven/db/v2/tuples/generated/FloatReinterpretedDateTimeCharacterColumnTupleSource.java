package io.deephaven.db.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.db.tables.utils.DBDateTime;
import io.deephaven.db.tables.utils.DBTimeUtils;
import io.deephaven.db.util.tuples.generated.FloatLongCharTuple;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.CharChunk;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.FloatChunk;
import io.deephaven.db.v2.sources.chunk.LongChunk;
import io.deephaven.db.v2.sources.chunk.ObjectChunk;
import io.deephaven.db.v2.sources.chunk.WritableChunk;
import io.deephaven.db.v2.sources.chunk.WritableObjectChunk;
import io.deephaven.db.v2.tuples.AbstractTupleSource;
import io.deephaven.db.v2.tuples.ThreeColumnTupleSourceFactory;
import io.deephaven.db.v2.tuples.TupleSource;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Float, Long, and Character.
 * <p>Generated by {@link io.deephaven.db.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FloatReinterpretedDateTimeCharacterColumnTupleSource extends AbstractTupleSource<FloatLongCharTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link FloatReinterpretedDateTimeCharacterColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<FloatLongCharTuple, Float, Long, Character> FACTORY = new Factory();

    private final ColumnSource<Float> columnSource1;
    private final ColumnSource<Long> columnSource2;
    private final ColumnSource<Character> columnSource3;

    public FloatReinterpretedDateTimeCharacterColumnTupleSource(
            @NotNull final ColumnSource<Float> columnSource1,
            @NotNull final ColumnSource<Long> columnSource2,
            @NotNull final ColumnSource<Character> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final FloatLongCharTuple createTuple(final long indexKey) {
        return new FloatLongCharTuple(
                columnSource1.getFloat(indexKey),
                columnSource2.getLong(indexKey),
                columnSource3.getChar(indexKey)
        );
    }

    @Override
    public final FloatLongCharTuple createPreviousTuple(final long indexKey) {
        return new FloatLongCharTuple(
                columnSource1.getPrevFloat(indexKey),
                columnSource2.getPrevLong(indexKey),
                columnSource3.getPrevChar(indexKey)
        );
    }

    @Override
    public final FloatLongCharTuple createTupleFromValues(@NotNull final Object... values) {
        return new FloatLongCharTuple(
                TypeUtils.unbox((Float)values[0]),
                DBTimeUtils.nanos((DBDateTime)values[1]),
                TypeUtils.unbox((Character)values[2])
        );
    }

    @Override
    public final FloatLongCharTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new FloatLongCharTuple(
                TypeUtils.unbox((Float)values[0]),
                TypeUtils.unbox((Long)values[1]),
                TypeUtils.unbox((Character)values[2])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final FloatLongCharTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) DBTimeUtils.nanosToTime(tuple.getSecondElement()));
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationIndexKey, tuple.getThirdElement());
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportToExternalKey(@NotNull final FloatLongCharTuple tuple) {
        return new SmartKey(
                TypeUtils.box(tuple.getFirstElement()),
                DBTimeUtils.nanosToTime(tuple.getSecondElement()),
                TypeUtils.box(tuple.getThirdElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final FloatLongCharTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return DBTimeUtils.nanosToTime(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final FloatLongCharTuple tuple, int elementIndex) {
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
        WritableObjectChunk<FloatLongCharTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        FloatChunk<Attributes.Values> chunk1 = chunks[0].asFloatChunk();
        LongChunk<Attributes.Values> chunk2 = chunks[1].asLongChunk();
        CharChunk<Attributes.Values> chunk3 = chunks[2].asCharChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new FloatLongCharTuple(chunk1.get(ii), chunk2.get(ii), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link FloatReinterpretedDateTimeCharacterColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<FloatLongCharTuple, Float, Long, Character> {

        private Factory() {
        }

        @Override
        public TupleSource<FloatLongCharTuple> create(
                @NotNull final ColumnSource<Float> columnSource1,
                @NotNull final ColumnSource<Long> columnSource2,
                @NotNull final ColumnSource<Character> columnSource3
        ) {
            return new FloatReinterpretedDateTimeCharacterColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
