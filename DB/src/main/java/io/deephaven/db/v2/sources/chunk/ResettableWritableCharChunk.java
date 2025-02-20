package io.deephaven.db.v2.sources.chunk;
import io.deephaven.db.tables.utils.ArrayUtils;
import io.deephaven.db.v2.sources.chunk.Attributes.Any;
import io.deephaven.db.v2.sources.chunk.util.pools.MultiChunkPool;
import io.deephaven.db.v2.utils.ChunkUtils;

/**
 * {@link ResettableWritableChunk} implementation for char data.
 */
public final class ResettableWritableCharChunk<ATTR_BASE extends Any> extends WritableCharChunk implements ResettableWritableChunk<ATTR_BASE> {

    public static <ATTR_BASE extends Any> ResettableWritableCharChunk<ATTR_BASE> makeResettableChunk() {
        return MultiChunkPool.forThisThread().getCharChunkPool().takeResettableWritableCharChunk();
    }

    public static <ATTR_BASE extends Any> ResettableWritableCharChunk<ATTR_BASE> makeResettableChunkForPool() {
        return new ResettableWritableCharChunk<>();
    }

    private ResettableWritableCharChunk(char[] data, int offset, int capacity) {
        super(data, offset, capacity);
    }

    private ResettableWritableCharChunk() {
        this(ArrayUtils.EMPTY_CHAR_ARRAY, 0, 0);
    }

    @Override
    public final ResettableWritableCharChunk slice(int offset, int capacity) {
        ChunkUtils.checkSliceArgs(size, offset, capacity);
        return new ResettableWritableCharChunk<>(data, this.offset + offset, capacity);
    }

    @Override
    public final <ATTR extends ATTR_BASE> WritableCharChunk<ATTR> resetFromChunk(WritableChunk<ATTR> other, int offset, int capacity) {
        return resetFromTypedChunk(other.asWritableCharChunk(), offset, capacity);
    }

    @Override
    public final <ATTR extends ATTR_BASE> WritableCharChunk<ATTR> resetFromArray(Object array, int offset, int capacity) {
        final char[] typedArray = (char[])array;
        return resetFromTypedArray(typedArray, offset, capacity);
    }

    public final <ATTR extends ATTR_BASE> WritableCharChunk<ATTR> resetFromArray(Object array) {
        final char[] typedArray = (char[])array;
        return resetFromTypedArray(typedArray, 0, typedArray.length);
    }

    @Override
    public final <ATTR extends ATTR_BASE> WritableCharChunk<ATTR> clear() {
        return resetFromArray(ArrayUtils.EMPTY_CHAR_ARRAY, 0, 0);
    }

    public final <ATTR extends ATTR_BASE> WritableCharChunk<ATTR> resetFromTypedChunk(WritableCharChunk<ATTR> other, int offset, int capacity) {
        ChunkUtils.checkSliceArgs(other.size, offset, capacity);
        return resetFromTypedArray(other.data, other.offset + offset, capacity);
    }

    public final <ATTR extends ATTR_BASE> WritableCharChunk<ATTR> resetFromTypedArray(char[] data, int offset, int capacity) {
        ChunkUtils.checkArrayArgs(data.length, offset, capacity);
        this.data = data;
        this.offset = offset;
        this.capacity = capacity;
        this.size = capacity;
        //noinspection unchecked
        return this;
    }

    @Override
    public final void close() {
        MultiChunkPool.forThisThread().getCharChunkPool().giveResettableWritableCharChunk(this);
    }
}
