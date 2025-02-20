package io.deephaven.db.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.db.util.BooleanUtils;
import io.deephaven.db.util.tuples.generated.CharByteByteTuple;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.CharChunk;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.ObjectChunk;
import io.deephaven.db.v2.sources.chunk.WritableChunk;
import io.deephaven.db.v2.sources.chunk.WritableObjectChunk;
import io.deephaven.db.v2.tuples.AbstractTupleSource;
import io.deephaven.db.v2.tuples.ThreeColumnTupleSourceFactory;
import io.deephaven.db.v2.tuples.TupleSource;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Character, Boolean, and Boolean.
 * <p>Generated by {@link io.deephaven.db.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CharacterBooleanBooleanColumnTupleSource extends AbstractTupleSource<CharByteByteTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link CharacterBooleanBooleanColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<CharByteByteTuple, Character, Boolean, Boolean> FACTORY = new Factory();

    private final ColumnSource<Character> columnSource1;
    private final ColumnSource<Boolean> columnSource2;
    private final ColumnSource<Boolean> columnSource3;

    public CharacterBooleanBooleanColumnTupleSource(
            @NotNull final ColumnSource<Character> columnSource1,
            @NotNull final ColumnSource<Boolean> columnSource2,
            @NotNull final ColumnSource<Boolean> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final CharByteByteTuple createTuple(final long indexKey) {
        return new CharByteByteTuple(
                columnSource1.getChar(indexKey),
                BooleanUtils.booleanAsByte(columnSource2.getBoolean(indexKey)),
                BooleanUtils.booleanAsByte(columnSource3.getBoolean(indexKey))
        );
    }

    @Override
    public final CharByteByteTuple createPreviousTuple(final long indexKey) {
        return new CharByteByteTuple(
                columnSource1.getPrevChar(indexKey),
                BooleanUtils.booleanAsByte(columnSource2.getPrevBoolean(indexKey)),
                BooleanUtils.booleanAsByte(columnSource3.getPrevBoolean(indexKey))
        );
    }

    @Override
    public final CharByteByteTuple createTupleFromValues(@NotNull final Object... values) {
        return new CharByteByteTuple(
                TypeUtils.unbox((Character)values[0]),
                BooleanUtils.booleanAsByte((Boolean)values[1]),
                BooleanUtils.booleanAsByte((Boolean)values[2])
        );
    }

    @Override
    public final CharByteByteTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new CharByteByteTuple(
                TypeUtils.unbox((Character)values[0]),
                BooleanUtils.booleanAsByte((Boolean)values[1]),
                BooleanUtils.booleanAsByte((Boolean)values[2])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final CharByteByteTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) BooleanUtils.byteAsBoolean(tuple.getSecondElement()));
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) BooleanUtils.byteAsBoolean(tuple.getThirdElement()));
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportToExternalKey(@NotNull final CharByteByteTuple tuple) {
        return new SmartKey(
                TypeUtils.box(tuple.getFirstElement()),
                BooleanUtils.byteAsBoolean(tuple.getSecondElement()),
                BooleanUtils.byteAsBoolean(tuple.getThirdElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final CharByteByteTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return BooleanUtils.byteAsBoolean(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final CharByteByteTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return BooleanUtils.byteAsBoolean(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    protected void convertChunks(@NotNull WritableChunk<? super Attributes.Values> destination, int chunkSize, Chunk<Attributes.Values> [] chunks) {
        WritableObjectChunk<CharByteByteTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        CharChunk<Attributes.Values> chunk1 = chunks[0].asCharChunk();
        ObjectChunk<Boolean, Attributes.Values> chunk2 = chunks[1].asObjectChunk();
        ObjectChunk<Boolean, Attributes.Values> chunk3 = chunks[2].asObjectChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new CharByteByteTuple(chunk1.get(ii), BooleanUtils.booleanAsByte(chunk2.get(ii)), BooleanUtils.booleanAsByte(chunk3.get(ii))));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link CharacterBooleanBooleanColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<CharByteByteTuple, Character, Boolean, Boolean> {

        private Factory() {
        }

        @Override
        public TupleSource<CharByteByteTuple> create(
                @NotNull final ColumnSource<Character> columnSource1,
                @NotNull final ColumnSource<Boolean> columnSource2,
                @NotNull final ColumnSource<Boolean> columnSource3
        ) {
            return new CharacterBooleanBooleanColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
