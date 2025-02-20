package io.deephaven.db.v2.sources.regioned;

import io.deephaven.db.v2.locations.TableDataException;
import io.deephaven.db.v2.locations.parquet.ColumnChunkPageStore;
import io.deephaven.db.v2.sources.chunk.Attributes.Any;
import io.deephaven.db.v2.sources.chunk.Attributes.DictionaryKeys;
import io.deephaven.db.v2.sources.chunk.page.ChunkPage;
import io.deephaven.db.v2.sources.chunk.page.Page;
import io.deephaven.db.v2.utils.ReadOnlyIndex;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * {@link ColumnRegionObject} implementation for regions that support fetching objects from
 * {@link ColumnChunkPageStore column chunk page stores}.
 */
public final class ParquetColumnRegionObject<DATA_TYPE, ATTR extends Any> extends ParquetColumnRegionBase<ATTR>
        implements ColumnRegionObject<DATA_TYPE, ATTR>, ParquetColumnRegion<ATTR>, Page<ATTR> {

    private volatile Supplier<ColumnRegionLong<DictionaryKeys>> dictionaryKeysRegionSupplier;
    private volatile Supplier<ColumnRegionObject<DATA_TYPE, ATTR>> dictionaryValuesRegionSupplier;

    private ColumnRegionLong<DictionaryKeys> dictionaryKeysRegion;
    private ColumnRegionObject<DATA_TYPE, ATTR> dictionaryValuesRegion;

    public ParquetColumnRegionObject(@NotNull final ColumnChunkPageStore<ATTR> columnChunkPageStore,
                                     @NotNull final Supplier<ColumnRegionLong<DictionaryKeys>> dictionaryKeysRegionSupplier,
                                     @NotNull final Supplier<ColumnRegionObject<DATA_TYPE, ATTR>> dictionaryValuesRegionSupplier) {
        super(columnChunkPageStore.mask(), columnChunkPageStore);
        this.dictionaryKeysRegionSupplier = dictionaryKeysRegionSupplier;
        this.dictionaryValuesRegionSupplier = dictionaryValuesRegionSupplier;
    }

    public DATA_TYPE getObject(final long elementIndex) {
        final ChunkPage<ATTR> page = getChunkPageContaining(elementIndex);
        try {
            return page.<DATA_TYPE>asObjectChunk().get(page.getChunkOffset(elementIndex));
        } catch (Exception e) {
            throw new TableDataException("Error retrieving object at table object index " + elementIndex
                    + ", from a parquet table", e);
        }
    }

    @Override
    public RegionVisitResult supportsDictionaryFormat(@NotNull final ReadOnlyIndex.SearchIterator keysToVisit) {
        if (!columnChunkPageStore.usesDictionaryOnEveryPage()) {
            return RegionVisitResult.FAILED;
        }
        return advanceToNextPage(keysToVisit) ? RegionVisitResult.CONTINUE : RegionVisitResult.COMPLETE;
    }

    @Override
    public ColumnRegionLong<DictionaryKeys> getDictionaryKeysRegion() {
        if (dictionaryKeysRegionSupplier != null) {
            synchronized (this) {
                if (dictionaryKeysRegionSupplier != null) {
                    dictionaryKeysRegion = dictionaryKeysRegionSupplier.get();
                    dictionaryKeysRegionSupplier = null;
                }
            }
        }
        return dictionaryKeysRegion;
    }

    @Override
    public ColumnRegionObject<DATA_TYPE, ATTR> getDictionaryValuesRegion() {
        if (dictionaryValuesRegionSupplier != null) {
            synchronized (this) {
                if (dictionaryValuesRegionSupplier != null) {
                    dictionaryValuesRegion = dictionaryValuesRegionSupplier.get();
                    dictionaryValuesRegionSupplier = null;
                }
            }
        }
        return dictionaryValuesRegion;
    }
}
