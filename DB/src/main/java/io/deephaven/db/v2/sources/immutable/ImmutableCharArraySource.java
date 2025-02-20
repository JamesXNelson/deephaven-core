package io.deephaven.db.v2.sources.immutable;

import io.deephaven.db.v2.sources.AbstractColumnSource;
import io.deephaven.db.v2.sources.ImmutableColumnSourceGetDefaults;
import io.deephaven.util.type.TypeUtils;

import static io.deephaven.util.QueryConstants.NULL_CHAR;

/**
 * Simple array source for Immutable Char.
 * <p>
 * The ImmutableC-harArraySource is replicated to all other types with
 * io.deephaven.db.v2.sources.Replicate.
 *
 * (C-har is deliberately spelled that way in order to prevent Replicate from altering this very comment).
 */
public class ImmutableCharArraySource extends AbstractColumnSource<Character> implements ImmutableColumnSourceGetDefaults.ForChar {
    private final char[] data;

    public ImmutableCharArraySource(char[] source) {
        super(char.class);
        this.data = source;
    }

    @Override
    public char getChar(long index) {
        if (index < 0 || index >= data.length) {
            return NULL_CHAR;
        }

        return data[(int)index];
    }

    @Override
    public Character get(long index) {
        if (index < 0 || index >= data.length) {
            return null;
        }

        return TypeUtils.box(data[(int)index]);
    }

    @Override
    public boolean isImmutable() {
        return true;
    }
}
