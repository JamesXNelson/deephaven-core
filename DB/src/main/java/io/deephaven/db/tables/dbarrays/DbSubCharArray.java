/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.db.tables.dbarrays;

import io.deephaven.db.util.LongSizedDataStructure;
import io.deephaven.util.QueryConstants;
import org.jetbrains.annotations.NotNull;

public class DbSubCharArray extends DbCharArray.Indirect {

    private static final long serialVersionUID = 1L;

    private final DbCharArray innerArray;
    private final long positions[];

    public DbSubCharArray(@NotNull final DbCharArray innerArray, @NotNull final long[] positions) {
        this.innerArray = innerArray;
        this.positions = positions;
    }

    @Override
    public char get(final long index) {
        if (index < 0 || index >= positions.length) {
            return QueryConstants.NULL_CHAR;
        }
        return innerArray.get(positions[LongSizedDataStructure.intSize("SubArray get", index)]);
    }

    @Override
    public DbCharArray subArray(final long fromIndex, final long toIndex) {
        return innerArray.subArrayByPositions(DbArrayBase.mapSelectedPositionRange(positions, fromIndex, toIndex));
    }

    @Override
    public DbCharArray subArrayByPositions(final long[] positions) {
        return innerArray.subArrayByPositions(DbArrayBase.mapSelectedPositions(this.positions, positions));
    }

    @Override
    public char[] toArray() {
        //noinspection unchecked
        final char[] result = new char[positions.length];
        for (int ii = 0; ii < positions.length; ++ii) {
            result[ii] = get(ii);
        }
        return result;
    }

    @Override
    public long size() {
        return positions.length;
    }

    @Override
    public char getPrev(final long index) {
        if (index < 0 || index >= positions.length) {
            return QueryConstants.NULL_CHAR;
        }
        return innerArray.getPrev(positions[LongSizedDataStructure.intSize("getPrev", index)]);
    }

    @Override
    public boolean isEmpty() {
        return positions.length == 0;
    }
}
