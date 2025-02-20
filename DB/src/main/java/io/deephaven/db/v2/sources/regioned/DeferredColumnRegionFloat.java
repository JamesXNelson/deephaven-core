/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit DeferredColumnRegionChar and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.db.v2.sources.regioned;

import io.deephaven.db.v2.sources.chunk.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * {@link ColumnRegionFloat} implementation for deferred regions, i.e. regions that will be properly constructed on
 * first access.
 */
public class DeferredColumnRegionFloat<ATTR extends Attributes.Any>
        extends DeferredColumnRegionBase<ATTR, ColumnRegionFloat<ATTR>>
        implements ColumnRegionFloat<ATTR> {

    DeferredColumnRegionFloat(final long pageMask, @NotNull Supplier<ColumnRegionFloat<ATTR>> resultRegionFactory) {
        super(pageMask, resultRegionFactory);
    }

    @Override
    public float getFloat(final long elementIndex) {
        return getResultRegion().getFloat(elementIndex);
    }

    @Override
    public float getFloat(@NotNull final FillContext context, final long elementIndex) {
        return getResultRegion().getFloat(context, elementIndex);
    }
}
