/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit ResettableCharChunk and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sources.chunk;
import io.deephaven.db.tables.utils.ArrayUtils;
import io.deephaven.db.v2.sources.chunk.Attributes.Any;
import io.deephaven.db.v2.sources.chunk.util.pools.MultiChunkPool;
import io.deephaven.db.v2.utils.ChunkUtils;

/**
 * {@link ResettableReadOnlyChunk} implementation for double data.
 */
public final class ResettableDoubleChunk<ATTR_UPPER extends Any> extends DoubleChunk implements ResettableReadOnlyChunk<ATTR_UPPER> {

    public static <ATTR_BASE extends Any> ResettableDoubleChunk<ATTR_BASE> makeResettableChunk() {
        return MultiChunkPool.forThisThread().getDoubleChunkPool().takeResettableDoubleChunk();
    }

    public static <ATTR_BASE extends Any> ResettableDoubleChunk<ATTR_BASE> makeResettableChunkForPool() {
        return new ResettableDoubleChunk<>();
    }

    private ResettableDoubleChunk(double[] data, int offset, int capacity) {
        super(data, offset, capacity);
    }

    private ResettableDoubleChunk() {
        this(ArrayUtils.EMPTY_DOUBLE_ARRAY, 0, 0);
    }

    @Override
    public final ResettableDoubleChunk slice(int offset, int capacity) {
        ChunkUtils.checkSliceArgs(size, offset, capacity);
        return new ResettableDoubleChunk(data, this.offset + offset, capacity);
    }

    @Override
    public final <ATTR extends ATTR_UPPER> DoubleChunk<ATTR> resetFromChunk(Chunk<? extends ATTR> other, int offset, int capacity) {
        return resetFromTypedChunk(other.asDoubleChunk(), offset, capacity);
    }

    @Override
    public final <ATTR extends ATTR_UPPER> DoubleChunk<ATTR> resetFromArray(Object array, int offset, int capacity) {
        final double[] typedArray = (double[])array;
        return resetFromTypedArray(typedArray, offset, capacity);
    }

    @Override
    public final <ATTR extends ATTR_UPPER> DoubleChunk<ATTR> resetFromArray(Object array) {
        final double[] typedArray = (double[])array;
        return resetFromTypedArray(typedArray, 0, typedArray.length);
    }

    @Override
    public final <ATTR extends ATTR_UPPER> DoubleChunk<ATTR> clear() {
        return resetFromArray(ArrayUtils.EMPTY_DOUBLE_ARRAY, 0, 0);
    }

    public final <ATTR extends ATTR_UPPER> DoubleChunk<ATTR> resetFromTypedChunk(DoubleChunk<? extends ATTR> other, int offset, int capacity) {
        ChunkUtils.checkSliceArgs(other.size, offset, capacity);
        return resetFromTypedArray(other.data, other.offset + offset, capacity);
    }

    public final <ATTR extends ATTR_UPPER> DoubleChunk<ATTR> resetFromTypedArray(double[] data, int offset, int capacity) {
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
        MultiChunkPool.forThisThread().getDoubleChunkPool().giveResettableDoubleChunk(this);
    }
}
