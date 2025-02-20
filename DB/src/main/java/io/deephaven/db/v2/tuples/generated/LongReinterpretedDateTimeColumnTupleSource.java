package io.deephaven.db.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.db.tables.utils.DBDateTime;
import io.deephaven.db.tables.utils.DBTimeUtils;
import io.deephaven.db.util.tuples.generated.LongLongTuple;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.LongChunk;
import io.deephaven.db.v2.sources.chunk.ObjectChunk;
import io.deephaven.db.v2.sources.chunk.WritableChunk;
import io.deephaven.db.v2.sources.chunk.WritableObjectChunk;
import io.deephaven.db.v2.tuples.AbstractTupleSource;
import io.deephaven.db.v2.tuples.TupleSource;
import io.deephaven.db.v2.tuples.TwoColumnTupleSourceFactory;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Long and Long.
 * <p>Generated by {@link io.deephaven.db.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LongReinterpretedDateTimeColumnTupleSource extends AbstractTupleSource<LongLongTuple> {

    /** {@link TwoColumnTupleSourceFactory} instance to create instances of {@link LongReinterpretedDateTimeColumnTupleSource}. **/
    public static final TwoColumnTupleSourceFactory<LongLongTuple, Long, Long> FACTORY = new Factory();

    private final ColumnSource<Long> columnSource1;
    private final ColumnSource<Long> columnSource2;

    public LongReinterpretedDateTimeColumnTupleSource(
            @NotNull final ColumnSource<Long> columnSource1,
            @NotNull final ColumnSource<Long> columnSource2
    ) {
        super(columnSource1, columnSource2);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
    }

    @Override
    public final LongLongTuple createTuple(final long indexKey) {
        return new LongLongTuple(
                columnSource1.getLong(indexKey),
                columnSource2.getLong(indexKey)
        );
    }

    @Override
    public final LongLongTuple createPreviousTuple(final long indexKey) {
        return new LongLongTuple(
                columnSource1.getPrevLong(indexKey),
                columnSource2.getPrevLong(indexKey)
        );
    }

    @Override
    public final LongLongTuple createTupleFromValues(@NotNull final Object... values) {
        return new LongLongTuple(
                TypeUtils.unbox((Long)values[0]),
                DBTimeUtils.nanos((DBDateTime)values[1])
        );
    }

    @Override
    public final LongLongTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new LongLongTuple(
                TypeUtils.unbox((Long)values[0]),
                TypeUtils.unbox((Long)values[1])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final LongLongTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) DBTimeUtils.nanosToTime(tuple.getSecondElement()));
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportToExternalKey(@NotNull final LongLongTuple tuple) {
        return new SmartKey(
                TypeUtils.box(tuple.getFirstElement()),
                DBTimeUtils.nanosToTime(tuple.getSecondElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final LongLongTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return DBTimeUtils.nanosToTime(tuple.getSecondElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 2 element tuple: " + elementIndex);
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final LongLongTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return TypeUtils.box(tuple.getSecondElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 2 element tuple: " + elementIndex);
    }

    protected void convertChunks(@NotNull WritableChunk<? super Attributes.Values> destination, int chunkSize, Chunk<Attributes.Values> [] chunks) {
        WritableObjectChunk<LongLongTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        LongChunk<Attributes.Values> chunk1 = chunks[0].asLongChunk();
        LongChunk<Attributes.Values> chunk2 = chunks[1].asLongChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new LongLongTuple(chunk1.get(ii), chunk2.get(ii)));
        }
        destination.setSize(chunkSize);
    }

    /** {@link TwoColumnTupleSourceFactory} for instances of {@link LongReinterpretedDateTimeColumnTupleSource}. **/
    private static final class Factory implements TwoColumnTupleSourceFactory<LongLongTuple, Long, Long> {

        private Factory() {
        }

        @Override
        public TupleSource<LongLongTuple> create(
                @NotNull final ColumnSource<Long> columnSource1,
                @NotNull final ColumnSource<Long> columnSource2
        ) {
            return new LongReinterpretedDateTimeColumnTupleSource(
                    columnSource1,
                    columnSource2
            );
        }
    }
}
