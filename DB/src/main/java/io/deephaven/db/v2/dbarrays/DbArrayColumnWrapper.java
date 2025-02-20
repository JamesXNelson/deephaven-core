/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.db.v2.dbarrays;

import io.deephaven.base.ClampUtil;
import io.deephaven.base.verify.Assert;
import io.deephaven.db.util.LongSizedDataStructure;
import io.deephaven.db.tables.dbarrays.DbArray;
import io.deephaven.db.tables.dbarrays.DbArrayBase;
import io.deephaven.db.tables.dbarrays.DbArrayDirect;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.IndexBuilder;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class DbArrayColumnWrapper<T> extends DbArray.Indirect<T> {

    private static final long serialVersionUID = -5944424618636079377L;

    private final ColumnSource<T> columnSource;
    private final Index index;
    private final long startPadding;
    private final long endPadding;

    public DbArrayColumnWrapper(@NotNull final ColumnSource<T> columnSource, @NotNull final Index index) {
        this(columnSource, index, 0, 0);
    }

    public DbArrayColumnWrapper(@NotNull final ColumnSource<T> columnSource, @NotNull final Index index,
            final long startPadding, final long endPadding) {
        Assert.neqNull(index, "index");
        this.columnSource = columnSource;
        this.index = index;
        this.startPadding = startPadding;
        this.endPadding = endPadding;
    }

    @Override
    public T get(long i) {
        i -= startPadding;

        if (i < 0 || i > index.size() - 1) {
            return null;
        }

        return columnSource.get(index.get(i));
    }

    @Override
    public DbArray<T> subArray(long fromIndexInclusive, long toIndexExclusive) {
        fromIndexInclusive -= startPadding;
        toIndexExclusive -= startPadding;

        final long realFrom = ClampUtil.clampLong(0, index.size(), fromIndexInclusive);
        final long realTo = ClampUtil.clampLong(0, index.size(), toIndexExclusive);

        long newStartPadding =
                toIndexExclusive < 0 ? toIndexExclusive - fromIndexInclusive : Math.max(0, -fromIndexInclusive);
        long newEndPadding = fromIndexInclusive >= index.size() ? toIndexExclusive - fromIndexInclusive
                : (int) Math.max(0, toIndexExclusive - index.size());

        return new DbArrayColumnWrapper<>(columnSource, index.subindexByPos(realFrom, realTo), newStartPadding,
                newEndPadding);
    }

    public DbArray<T> subArrayByPositions(long[] positions) {
        IndexBuilder builder = Index.FACTORY.getRandomBuilder();

        for (long position : positions) {
            final long realPos = position - startPadding;

            if (realPos < index.size()) {
                builder.addKey(index.get(realPos));
            }
        }

        return new DbArrayColumnWrapper<>(columnSource, builder.getIndex(), 0, 0);
    }

    @Override
    public T[] toArray() {
        return toArray(false, Long.MAX_VALUE);
    }

    public T[] toArray(boolean shouldBeNullIfOutofBounds, long maxSize) {
        if (shouldBeNullIfOutofBounds && (startPadding > 0 || endPadding > 0)) {
            return null;
        }

        final long sz = Math.min(size(), maxSize);

        @SuppressWarnings("unchecked")
        final T result[] = (T[]) Array.newInstance(TypeUtils.getBoxedType(columnSource.getType()),
                LongSizedDataStructure.intSize("toArray", sz));
        for (int i = 0; i < sz; i++) {
            result[i] = get(i);
        }

        return result;
    }

    @Override
    public long size() {
        return startPadding + index.size() + endPadding;
    }

    @Override
    public Class<T> getComponentType() {
        return columnSource.getType();
    }

    @Override
    public DbArray<T> getDirect() {
        if (DbArrayBase.class.isAssignableFrom(getComponentType())) {
            // recursion!
            final long size = size();
            // noinspection unchecked
            final T[] array =
                    (T[]) Array.newInstance(getComponentType(), LongSizedDataStructure.intSize("toArray", size));
            for (int ii = 0; ii < size; ++ii) {
                final T arrayBase = get(ii);
                if (arrayBase == null) {
                    array[ii] = null;
                } else {
                    // noinspection unchecked
                    array[ii] = (T) ((DbArrayBase<?>) arrayBase).getDirect();
                }
            }
            return new DbArrayDirect<>(array);
        } else {
            return new DbArrayDirect<>(toArray());
        }
    }

    @Override
    public T getPrev(long i) {
        i -= startPadding;

        if (i < 0 || i > index.size() - 1) {
            return null;
        }

        return columnSource.getPrev(index.getPrev(i));
    }
}
