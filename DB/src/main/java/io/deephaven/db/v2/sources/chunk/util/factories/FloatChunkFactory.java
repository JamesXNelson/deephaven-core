/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharChunkFactory and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sources.chunk.util.factories;

import io.deephaven.db.tables.dbarrays.*;
import io.deephaven.db.v2.sources.chunk.*;
import io.deephaven.db.v2.sources.chunk.Attributes.Any;
import io.deephaven.db.v2.sources.chunk.page.FloatChunkPage;

import org.jetbrains.annotations.NotNull;
import java.util.function.IntFunction;

public class FloatChunkFactory implements ChunkFactory {
    @NotNull
    @Override
    public final float[] makeArray(int capacity) {
        return FloatChunk.makeArray(capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunk<ATTR>[] makeChunkArray(int capacity) {
        return FloatChunkChunk.makeArray(capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunk<ATTR> getEmptyChunk() {
        return FloatChunk.getEmptyChunk();
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunkChunk<ATTR> getEmptyChunkChunk() {
        return FloatChunkChunk.getEmptyChunk();
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunk<ATTR> chunkWrap(Object array) {
        final float[] typedArray = (float[])array;
        return FloatChunk.chunkWrap(typedArray);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunk<ATTR> chunkWrap(Object array, int offset, int capacity) {
        final float[] typedArray = (float[])array;
        return FloatChunk.chunkWrap(typedArray, offset, capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunkChunk<ATTR> chunkChunkWrap(Chunk<ATTR>[] array) {
        FloatChunk<ATTR>[] typedArray = (FloatChunk<ATTR>[])array;
        return FloatChunkChunk.chunkWrap(typedArray);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunkChunk<ATTR> chunkChunkWrap(Chunk<ATTR>[] array, int offset, int capacity) {
        FloatChunk<ATTR>[] typedArray = (FloatChunk<ATTR>[])array;
        return FloatChunkChunk.chunkWrap(typedArray, offset, capacity);
    }

    // region dbArrayWrap
    @NotNull
    @Override
    public final DbFloatArrayDirect dbArrayWrap(Object array) {
        final float[] typedArray = (float[])array;
        return new DbFloatArrayDirect(typedArray);
    }

    @NotNull
    @Override
    public DbFloatArraySlice dbArrayWrap(Object array, int offset, int capacity) {
        DbFloatArrayDirect dbFloatArrayDirect = dbArrayWrap(array);
        return new DbFloatArraySlice(dbFloatArrayDirect, offset, capacity);
    }
    // endregion dbArrayWrap

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunkPage<ATTR> pageWrap(long beginRow, Object array, long mask) {
        float[] typedArray = (float[]) array;
        return FloatChunkPage.pageWrap(beginRow, typedArray, mask);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> FloatChunkPage<ATTR> pageWrap(long beginRow, Object array, int offset, int capacity, long mask) {
        float[] typedArray = (float[]) array;
        return FloatChunkPage.pageWrap(beginRow, typedArray, offset, capacity, mask);
    }


    @NotNull
    @Override
    public final <ATTR extends Any> ResettableReadOnlyChunk<ATTR> makeResettableReadOnlyChunk() {
        return ResettableFloatChunk.makeResettableChunk();
    }

    @NotNull
    @Override
    public final <ATTR extends Any> ResettableChunkChunk<ATTR> makeResettableChunkChunk() {
        return ResettableFloatChunkChunk.makeResettableChunk();
    }

    @NotNull
    @Override
    public final <ATTR extends Any> WritableFloatChunk<ATTR> makeWritableChunk(int capacity) {
        return WritableFloatChunk.makeWritableChunk(capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> WritableFloatChunkChunk<ATTR> makeWritableChunkChunk(int capacity) {
        return WritableFloatChunkChunk.makeWritableChunk(capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> WritableFloatChunk<ATTR> writableChunkWrap(Object array, int offset, int capacity) {
        final float[] realType = (float[])array;
        return WritableFloatChunk.writableChunkWrap(realType, offset, capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> WritableChunkChunk<ATTR> writableChunkChunkWrap(WritableChunk<ATTR>[] array, int offset, int capacity) {
        WritableFloatChunk<ATTR>[] actual = (WritableFloatChunk<ATTR>[])array;
        return WritableFloatChunkChunk.writableChunkWrap(actual, offset, capacity);
    }

    @NotNull
    @Override
    public final <ATTR extends Any> ResettableWritableChunk<ATTR> makeResettableWritableChunk() {
        return ResettableWritableFloatChunk.makeResettableChunk();
    }

    @NotNull
    @Override
    public final <ATTR extends Any> ResettableWritableChunkChunk<ATTR> makeResettableWritableChunkChunk() {
        return ResettableWritableFloatChunkChunk.makeResettableChunk();
    }

    @NotNull
    @Override
    public final IntFunction<Chunk[]> chunkArrayBuilder() {
        return FloatChunk[]::new;
    }


    @NotNull
    public final IntFunction<WritableChunk[]> writableChunkArrayBuilder() {
        return WritableFloatChunk[]::new;
    }
}
