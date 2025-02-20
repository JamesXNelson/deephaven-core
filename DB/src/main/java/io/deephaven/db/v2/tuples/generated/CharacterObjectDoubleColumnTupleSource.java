package io.deephaven.db.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.db.util.tuples.generated.CharObjectDoubleTuple;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.CharChunk;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.DoubleChunk;
import io.deephaven.db.v2.sources.chunk.ObjectChunk;
import io.deephaven.db.v2.sources.chunk.WritableChunk;
import io.deephaven.db.v2.sources.chunk.WritableObjectChunk;
import io.deephaven.db.v2.tuples.AbstractTupleSource;
import io.deephaven.db.v2.tuples.ThreeColumnTupleSourceFactory;
import io.deephaven.db.v2.tuples.TupleSource;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Character, Object, and Double.
 * <p>Generated by {@link io.deephaven.db.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CharacterObjectDoubleColumnTupleSource extends AbstractTupleSource<CharObjectDoubleTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link CharacterObjectDoubleColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<CharObjectDoubleTuple, Character, Object, Double> FACTORY = new Factory();

    private final ColumnSource<Character> columnSource1;
    private final ColumnSource<Object> columnSource2;
    private final ColumnSource<Double> columnSource3;

    public CharacterObjectDoubleColumnTupleSource(
            @NotNull final ColumnSource<Character> columnSource1,
            @NotNull final ColumnSource<Object> columnSource2,
            @NotNull final ColumnSource<Double> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final CharObjectDoubleTuple createTuple(final long indexKey) {
        return new CharObjectDoubleTuple(
                columnSource1.getChar(indexKey),
                columnSource2.get(indexKey),
                columnSource3.getDouble(indexKey)
        );
    }

    @Override
    public final CharObjectDoubleTuple createPreviousTuple(final long indexKey) {
        return new CharObjectDoubleTuple(
                columnSource1.getPrevChar(indexKey),
                columnSource2.getPrev(indexKey),
                columnSource3.getPrevDouble(indexKey)
        );
    }

    @Override
    public final CharObjectDoubleTuple createTupleFromValues(@NotNull final Object... values) {
        return new CharObjectDoubleTuple(
                TypeUtils.unbox((Character)values[0]),
                values[1],
                TypeUtils.unbox((Double)values[2])
        );
    }

    @Override
    public final CharObjectDoubleTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new CharObjectDoubleTuple(
                TypeUtils.unbox((Character)values[0]),
                values[1],
                TypeUtils.unbox((Double)values[2])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final CharObjectDoubleTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) tuple.getSecondElement());
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationIndexKey, tuple.getThirdElement());
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportToExternalKey(@NotNull final CharObjectDoubleTuple tuple) {
        return new SmartKey(
                TypeUtils.box(tuple.getFirstElement()),
                tuple.getSecondElement(),
                TypeUtils.box(tuple.getThirdElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final CharObjectDoubleTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return tuple.getSecondElement();
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final CharObjectDoubleTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return tuple.getSecondElement();
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    protected void convertChunks(@NotNull WritableChunk<? super Attributes.Values> destination, int chunkSize, Chunk<Attributes.Values> [] chunks) {
        WritableObjectChunk<CharObjectDoubleTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        CharChunk<Attributes.Values> chunk1 = chunks[0].asCharChunk();
        ObjectChunk<Object, Attributes.Values> chunk2 = chunks[1].asObjectChunk();
        DoubleChunk<Attributes.Values> chunk3 = chunks[2].asDoubleChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new CharObjectDoubleTuple(chunk1.get(ii), chunk2.get(ii), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link CharacterObjectDoubleColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<CharObjectDoubleTuple, Character, Object, Double> {

        private Factory() {
        }

        @Override
        public TupleSource<CharObjectDoubleTuple> create(
                @NotNull final ColumnSource<Character> columnSource1,
                @NotNull final ColumnSource<Object> columnSource2,
                @NotNull final ColumnSource<Double> columnSource3
        ) {
            return new CharacterObjectDoubleColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
