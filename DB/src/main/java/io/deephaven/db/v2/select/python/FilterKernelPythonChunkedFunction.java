package io.deephaven.db.v2.select.python;

import io.deephaven.db.v2.select.ConditionFilter.FilterKernel;
import io.deephaven.db.v2.sources.chunk.Attributes.OrderedKeyIndices;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.LongChunk;
import org.jpy.PyObject;

import java.util.Objects;

/**
 * A python filter kernel which is implemented by passing the chunks as arrays into the python function.
 *
 * @see io.deephaven.db.v2.select.python.FilterKernelPythonSingularFunction
 */
class FilterKernelPythonChunkedFunction implements FilterKernel<FilterKernel.Context> {

    private static final String CALL_METHOD = "__call__";

    // this is a python function whose arguments can accept arrays
    private final PyObject function;

    FilterKernelPythonChunkedFunction(PyObject function) {
        this.function = Objects.requireNonNull(function, "function");
    }

    @Override
    public Context getContext(int maxChunkSize) {
        return new Context(maxChunkSize);
    }

    @Override
    public LongChunk<OrderedKeyIndices> filter(
            Context context,
            LongChunk<OrderedKeyIndices> indices,
            Chunk... inputChunks) {
        final int size = indices.size();
        final io.deephaven.db.v2.select.python.ArgumentsChunked arguments =
                io.deephaven.db.v2.select.python.ArgumentsChunked.buildArguments(inputChunks);
        final boolean[] results = function
                .call(boolean[].class, CALL_METHOD, arguments.getParamTypes(), arguments.getParams());
        if (size != results.length) {
            throw new IllegalStateException(
                    "FilterKernelPythonChunkedFunction returned results are not the proper size");
        }
        context.resultChunk.setSize(0);
        for (int i = 0; i < size; ++i) {
            if (results[i]) {
                context.resultChunk.add(indices.get(i));
            }
        }
        return context.resultChunk;
    }
}
