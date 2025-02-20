package io.deephaven.db.util.tuples.generated;

import io.deephaven.db.tables.lang.DBLanguageFunctionUtil;
import io.deephaven.db.util.serialization.SerializationUtils;
import io.deephaven.db.util.serialization.StreamingExternalizable;
import io.deephaven.db.util.tuples.CanonicalizableTuple;
import gnu.trove.map.TIntObjectMap;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * <p>2-Tuple (double) key class composed of Object and long elements.
 * <p>Generated by {@link io.deephaven.db.util.tuples.TupleCodeGenerator}.
 */
public class ObjectLongTuple implements Comparable<ObjectLongTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<ObjectLongTuple> {

    private static final long serialVersionUID = 1L;

    private Object element1;
    private long element2;

    private transient int cachedHashCode;

    public ObjectLongTuple(
            final Object element1,
            final long element2
    ) {
        initialize(
                element1,
                element2
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public ObjectLongTuple() {
    }

    private void initialize(
            final Object element1,
            final long element2
    ) {
        this.element1 = element1;
        this.element2 = element2;
        cachedHashCode = (31 +
                Objects.hashCode(element1)) * 31 +
                Long.hashCode(element2);
    }

    public final Object getFirstElement() {
        return element1;
    }

    public final long getSecondElement() {
        return element2;
    }

    @Override
    public final int hashCode() {
        return cachedHashCode;
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final ObjectLongTuple typedOther = (ObjectLongTuple) other;
        // @formatter:off
        return Objects.equals(element1, typedOther.element1) &&
               element2 == typedOther.element2;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final ObjectLongTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = DBLanguageFunctionUtil.compareTo((Comparable)element1, (Comparable)other.element1)) ? comparison :
               DBLanguageFunctionUtil.compareTo(element2, other.element2);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeObject(element1);
        out.writeLong(element2);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readObject(),
                in.readLong()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        StreamingExternalizable.writeObjectElement(out, cachedWriters, 0, element1);
        out.writeLong(element2);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                StreamingExternalizable.readObjectElement(in, cachedReaders, 0),
                in.readLong()
        );
    }

    @Override
    public String toString() {
        return "ObjectLongTuple{" +
                element1 + ", " +
                element2 + '}';
    }

    @Override
    public ObjectLongTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        final Object canonicalizedElement1 = canonicalizer.apply(element1);
        return canonicalizedElement1 == element1
                ? this : new ObjectLongTuple(canonicalizedElement1, element2);
    }
}
