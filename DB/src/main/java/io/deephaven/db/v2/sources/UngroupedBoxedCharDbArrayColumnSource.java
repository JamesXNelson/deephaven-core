package io.deephaven.db.v2.sources;

import io.deephaven.db.tables.dbarrays.DbArray;

import static io.deephaven.util.QueryConstants.NULL_CHAR;

/**
 * An Ungrouped Column sourced for the Boxed Type Character.
 * <p>
 * The UngroupedBoxedC-harDbArrayColumnSource is replicated to all other types with
 * io.deephaven.db.v2.sources.Replicate.
 *
 * (C-har is deliberately spelled that way in order to prevent Replicate from altering this very comment).
 */
public class UngroupedBoxedCharDbArrayColumnSource extends UngroupedDbArrayColumnSource<Character> {

    public UngroupedBoxedCharDbArrayColumnSource(ColumnSource<DbArray<Character>> innerSource) {
        super(innerSource);
    }

    @Override
    public char getChar(long index) {
        final Character result = get(index);
        return result == null ? NULL_CHAR : result;
    }

    @Override
    public char getPrevChar(long index) {
        final Character result = getPrev(index);
        return result == null ? NULL_CHAR : result;
    }
}
