package io.deephaven.db.v2.sort;

import io.deephaven.compilertools.ReplicatePrimitiveCode;
import io.deephaven.compilertools.ReplicateUtilities;
import io.deephaven.db.util.DhCharComparisons;
import io.deephaven.db.v2.sort.findruns.CharFindRunsKernel;
import io.deephaven.db.v2.sort.partition.CharPartitionKernel;
import io.deephaven.db.v2.sort.permute.CharPermuteKernel;
import io.deephaven.db.v2.sort.radix.BooleanLongRadixSortKernel;
import io.deephaven.db.v2.sort.timsort.CharIntTimsortKernel;
import io.deephaven.db.v2.sort.megamerge.CharLongMegaMergeKernel;
import io.deephaven.db.v2.sort.timsort.CharLongTimsortKernel;
import io.deephaven.util.QueryConstants;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.deephaven.compilertools.ReplicateUtilities.*;

public class ReplicateSortKernel {
    public static void main(String[] args) throws IOException {
        replicateLongToInt();
        doCharReplication(CharLongTimsortKernel.class);
        doCharReplication(CharIntTimsortKernel.class);


        doCharMegaMergeReplication(CharLongMegaMergeKernel.class);
        ReplicatePrimitiveCode.charToAllButBoolean(CharFindRunsKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        final String objectRunPath =
                ReplicatePrimitiveCode.charToObject(CharFindRunsKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        fixupObjectRuns(objectRunPath);

        ReplicatePrimitiveCode.charToAllButBoolean(CharPartitionKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        final String objectPartitionPath =
                ReplicatePrimitiveCode.charToObject(CharPartitionKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        fixupObjectPartition(objectPartitionPath);

        ReplicatePrimitiveCode.charToAllButBoolean(CharPermuteKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        fixupObjectPermute(
                ReplicatePrimitiveCode.charToObject(CharPermuteKernel.class, ReplicatePrimitiveCode.MAIN_SRC));
    }

    private static void doCharReplication(Class<?> sourceClass) throws IOException {
        // replicate char to each of the other types
        final List<String> timsortPaths =
                ReplicatePrimitiveCode.charToAllButBoolean(sourceClass, ReplicatePrimitiveCode.MAIN_SRC);
        final String objectSortPath = ReplicatePrimitiveCode.charToObject(sourceClass, ReplicatePrimitiveCode.MAIN_SRC);
        timsortPaths.add(ReplicatePrimitiveCode.pathForClass(sourceClass, ReplicatePrimitiveCode.MAIN_SRC));
        timsortPaths.add(objectSortPath);

        // now replicate each type to a descending kernel, and swap the sense of gt, lt, geq, and leq
        for (final String path : timsortPaths) {
            final String descendingPath = path.replace("TimsortKernel", "TimsortDescendingKernel");

            if (path.contains("Double") || path.contains("Float")) {
                FileUtils.copyFile(new File(path), new File(descendingPath));

                // first we need to figure out what to do with the NaNs in our ascending kernel
                fixupNanComparisons(path, true);

                // we still need a descending kernel
                System.out.println("Descending FP Path: " + descendingPath);
                // we are going to fix it up ascending, then follow it up with a sense inversion
                fixupNanComparisons(descendingPath, true);
                invertSense(path, descendingPath);
            } else if (path.contains("Char")) {
                final String sourceName = sourceClass.getSimpleName();
                final String nullAwareAscendingName = "NullAware" + sourceName;
                final String nullAwarePath = path.replace(sourceName, nullAwareAscendingName);
                final String nullAwareDescendingPath =
                        nullAwarePath.replaceAll("TimsortKernel", "TimsortDescendingKernel");

                fixupCharNullComparisons(sourceClass, path, nullAwarePath, sourceName, nullAwareAscendingName, true);
                // we are going to fix it up ascending, then follow it up with a sense inversion
                fixupCharNullComparisons(sourceClass, path, nullAwareDescendingPath, sourceName, nullAwareAscendingName,
                        true);
                invertSense(nullAwareDescendingPath, nullAwareDescendingPath);
            } else if (path.contains("Object")) {
                FileUtils.copyFile(new File(path), new File(descendingPath));

                fixupObjectTimSort(path, true);
                System.out.println("Descending Object Path: " + descendingPath);
                fixupObjectTimSort(descendingPath, false);
            } else {
                System.out.println("Descending Path: " + descendingPath);
                invertSense(path, descendingPath);
            }
        }
    }

    private static void doCharMegaMergeReplication(Class<?> sourceClass) throws IOException {
        // replicate char to each of the other types
        final List<String> megaMergePaths =
                ReplicatePrimitiveCode.charToAllButBoolean(sourceClass, ReplicatePrimitiveCode.MAIN_SRC);
        final String objectSortPath = ReplicatePrimitiveCode.charToObject(sourceClass, ReplicatePrimitiveCode.MAIN_SRC);
        megaMergePaths.add(ReplicatePrimitiveCode.pathForClass(sourceClass, ReplicatePrimitiveCode.MAIN_SRC));
        megaMergePaths.add(objectSortPath);

        // now replicate each type to a descending kernel, and swap the sense of gt, lt, geq, and leq
        for (final String path : megaMergePaths) {
            final String descendingPath = path.replace("LongMegaMergeKernel", "LongMegaMergeDescendingKernel");
            if (path.contains("Object")) {
                FileUtils.copyFile(new File(path), new File(descendingPath));
                fixupObjectMegaMerge(objectSortPath, true);
                fixupObjectMegaMerge(descendingPath, false);
            } else {
                invertSense(path, descendingPath);
            }
        }
    }

    private static void replicateLongToInt() throws IOException {
        final String intSortKernelPath =
                ReplicatePrimitiveCode.longToInt(LongSortKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        fixupIntSortKernel(intSortKernelPath);
        ReplicatePrimitiveCode.longToInt(CharLongTimsortKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        ReplicatePrimitiveCode.longToInt(BooleanLongRadixSortKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
    }

    private static void fixupIntSortKernel(String intSortKernelPath) throws IOException {
        final List<String> longCase = Arrays.asList("case Long:",
                "if (order == SortingOrder.Ascending) {",
                "    return LongIntTimsortKernel.createContext(size);",
                "} else {",
                "    return LongIntTimsortDescendingKernel.createContext(size);",
                "}");

        final File file = new File(intSortKernelPath);
        final List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
        FileUtils.writeLines(file, replaceRegion(lines, "lngcase", indent(longCase, 16)));

    }

    private static void replicateLongInt() throws IOException {
        // our special fancy LongInt sort kernel for use in a multicolumn sort
        final String targetName =
                ReplicatePrimitiveCode.charLongToLongInt(CharLongTimsortKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        fixupLongInt(targetName);
        final File longIntDest = new File(targetName.replace("LongTimsortKernel", "LongIntTimsortKernel"));
        // noinspection ResultOfMethodCallIgnored
        longIntDest.delete();
        FileUtils.moveFile(new File(targetName), longIntDest);
    }

    private static void replicateIntInt() throws IOException {
        final String targetName =
                ReplicatePrimitiveCode.charLongToIntInt(CharLongTimsortKernel.class, ReplicatePrimitiveCode.MAIN_SRC);
        fixupIntInt(targetName);
        final File intIntDest = new File(targetName.replace("IntTimsortKernel", "IntIntTimsortKernel"));
        // noinspection ResultOfMethodCallIgnored
        intIntDest.delete();
        FileUtils.moveFile(new File(targetName), intIntDest);
    }

    private static void invertSense(String path, String descendingPath) throws IOException {
        final File file = new File(path);

        final List<String> lines = ascendingNameToDescendingName(FileUtils.readLines(file, Charset.defaultCharset()));

        FileUtils.writeLines(new File(descendingPath), invertComparisons(lines));
    }

    @NotNull
    private static List<String> ascendingNameToDescendingName(List<String> lines) {
        // we should skip the replicate header
        return globalReplacements(3, lines, "TimsortKernel", "TimsortDescendingKernel", "\\BLongMegaMergeKernel",
                "LongMegaMergeDescendingKernel");
    }

    private static void fixupObjectTimSort(String objectPath, boolean ascending) throws IOException {
        final File objectFile = new File(objectPath);
        List<String> lines = FileUtils.readLines(objectFile, Charset.defaultCharset());

        if (!ascending) {
            lines = ascendingNameToDescendingName(lines);
        }

        lines = fixupChunkAttributes(lines);

        FileUtils.writeLines(objectFile, fixupObjectComparisons(lines, ascending));
    }

    private static void fixupObjectMegaMerge(String objectPath, boolean ascending) throws IOException {
        final File objectFile = new File(objectPath);
        List<String> lines = FileUtils.readLines(objectFile, Charset.defaultCharset());

        if (!ascending) {
            lines = ascendingNameToDescendingName(lines);
            lines = invertComparisons(lines);
        }

        lines = fixupChunkAttributes(lines);

        FileUtils.writeLines(objectFile, fixupColumnSourceGetObject(lines));
    }

    private static List<String> fixupColumnSourceGetObject(List<String> lines) {
        return globalReplacements(lines, "getObject\\(", "get\\(");
    }

    private static void fixupObjectPermute(String objectPath) throws IOException {
        final File objectFile = new File(objectPath);
        List<String> lines = FileUtils.readLines(objectFile, Charset.defaultCharset());

        lines = fixupTypedChunkAttributes(lines);
        lines = lines.stream()
                .map(x -> x.replaceAll("asObjectChunk\\(\\)", "<Object>asObjectChunk()"))
                .map(x -> x.replaceAll("asWritableObjectChunk\\(\\)", "<Object>asWritableObjectChunk()"))
                .collect(Collectors.toList());

        FileUtils.writeLines(objectFile, lines);
    }

    @NotNull
    private static List<String> fixupTypedChunkAttributes(List<String> lines) {
        lines = lines.stream()
                .map(x -> x.replaceAll("static <T extends Any>", "static<TYPE_T, T extends Any>"))
                .map(x -> x.replaceAll("ObjectChunk<([^>]*)>", "ObjectChunk<TYPE_T, $1>"))
                .collect(Collectors.toList());
        return lines;
    }

    private static void fixupObjectPartition(String objectPath) throws IOException {
        final File objectFile = new File(objectPath);
        final List<String> lines = FileUtils.readLines(objectFile, Charset.defaultCharset());

        final List<String> complines = fixupChunkAttributes(fixupObjectComparisons(lines));

        FileUtils.writeLines(objectFile, complines);
    }

    private static void fixupObjectRuns(String objectPath) throws IOException {
        final File objectFile = new File(objectPath);
        List<String> lines = FileUtils.readLines(objectFile, Charset.defaultCharset());

        lines = fixupNeq(addImport(lines, java.util.Objects.class));

        FileUtils.writeLines(objectFile, lines);
    }

    private static void fixupLongInt(String path) throws IOException {
        final File file = new File(path);

        List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());

        lines = removeImport(lines, LongSortKernel.class);

        lines = lines.stream().map(x -> x.replaceAll("LongTimsortKernel", "LongIntTimsortKernel"))
                .map(x -> x.replaceAll("LongSortKernelContext", "LongIntSortKernelContext"))
                .map(x -> x.replaceAll(
                        "static class LongIntSortKernelContext<ATTR extends Any, KEY_INDICES extends Keys> implements SortKernel<ATTR, KEY_INDICES>",
                        "static class LongIntSortKernelContext<ATTR extends Any, KEY_INDICES extends Keys> implements AutoCloseable"))
                .map(x -> x.replaceAll("IntChunk<KeyIndices>", "IntChunk"))
                .collect(Collectors.toList());

        lines = applyFixup(lines, "Context", "\\s+@Override", (m) -> Collections.singletonList(""));

        FileUtils.writeLines(new File(path), lines);
    }

    private static void fixupIntInt(String path) throws IOException {
        final File file = new File(path);

        List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());

        lines = removeImport(lines, LongSortKernel.class);

        lines = lines.stream().map(x -> x.replaceAll("IntTimsortKernel", "IntIntTimsortKernel"))
                .map(x -> x.replaceAll("IntSortKernelContext", "IntIntSortKernelContext"))
                .map(x -> x.replaceAll(
                        "static class IntIntSortKernelContext<ATTR extends Any, KEY_INDICES extends Keys> implements SortKernel<ATTR, KEY_INDICES>",
                        "static class IntIntSortKernelContext<ATTR extends Any, KEY_INDICES extends Keys> implements AutoCloseable"))
                .map(x -> x.replaceAll("IntChunk<KeyIndices>", "IntChunk"))
                .collect(Collectors.toList());

        lines = applyFixup(lines, "Context", "\\s+@Override", (m) -> Collections.singletonList(""));

        FileUtils.writeLines(new File(path), lines);
    }


    public static void fixupNanComparisons(String path, boolean ascending) throws IOException {
        final File file = new File(path);

        final List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());

        FileUtils.writeLines(new File(path),
                fixupNanComparisons(lines, path.contains("Double") ? "Double" : "Float", ascending));
    }

    public static List<String> fixupNanComparisons(List<String> lines, String type, boolean ascending) {
        final String lcType = type.toLowerCase();

        lines = ReplicateUtilities.addImport(lines, "import io.deephaven.db.util.Dh" + type + "Comparisons;");

        lines = replaceRegion(lines, "comparison functions",
                Arrays.asList("    private static int doComparison(" + lcType + " lhs, " + lcType + " rhs) {",
                        "        return " + (ascending ? "" : "-1 * ") + "Dh" + type + "Comparisons.compare(lhs, rhs);",
                        "    }"));
        return lines;
    }

    @SuppressWarnings("SameParameterValue")
    private static void fixupCharNullComparisons(Class sourceClass, String path, String newPath, String oldName,
            String newName, boolean ascending) throws IOException {
        final File file = new File(path);

        List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());

        lines = ReplicateUtilities.addImport(lines, QueryConstants.class, DhCharComparisons.class);

        lines = globalReplacements(fixupCharNullComparisons(lines, ascending), oldName, newName);

        lines.addAll(0, Arrays.asList(
                "/* ---------------------------------------------------------------------------------------------------------------------",
                " * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit " + sourceClass.getSimpleName()
                        + " and regenerate",
                " * ------------------------------------------------------------------------------------------------------------------ */"));

        FileUtils.writeLines(new File(newPath), lines);
    }

    public static List<String> fixupCharNullComparisons(List<String> lines, boolean ascending) {
        lines = replaceRegion(lines, "comparison functions",
                Arrays.asList("    private static int doComparison(char lhs, char rhs) {",
                        "        return " + (ascending ? "" : "-1 * ") + "DhCharComparisons.compare(lhs, rhs);",
                        "    }"));
        return lines;
    }

    public static List<String> fixupObjectComparisons(List<String> lines) {
        return fixupObjectComparisons(lines, true);
    }

    public static List<String> fixupObjectComparisons(List<String> lines, boolean ascending) {
        final List<String> ascendingComparision = Arrays.asList(
                "    // ascending comparison",
                "    private static int doComparison(Object lhs, Object rhs) {",
                "       if (lhs == rhs) {",
                "            return 0;",
                "        }",
                "        if (lhs == null) {",
                "            return -1;",
                "        }",
                "        if (rhs == null) {",
                "            return 1;",
                "        }",
                "        //noinspection unchecked",
                "        return ((Comparable)lhs).compareTo(rhs);",
                "    }",
                "");
        final List<String> descendingComparision = Arrays.asList(
                "    // descending comparison",
                "    private static int doComparison(Object lhs, Object rhs) {",
                "        if (lhs == rhs) {",
                "            return 0;",
                "        }",
                "        if (lhs == null) {",
                "            return 1;",
                "        }",
                "        if (rhs == null) {",
                "            return -1;",
                "        }",
                "        //noinspection unchecked",
                "        return ((Comparable)rhs).compareTo(lhs);",
                "    }");

        return addImport(simpleFixup(
                replaceRegion(lines, "comparison functions", ascending ? ascendingComparision : descendingComparision),
                "equality function", "lhs == rhs", "Objects.equals(lhs, rhs)"), Objects.class);
    }

    public static List<String> invertComparisons(List<String> lines) {
        final List<String> descendingComment = Collections.singletonList(
                "    // note that this is a descending kernel, thus the comparisons here are backwards (e.g., the lt function is in terms of the sort direction, so is implemented by gt)");
        return insertRegion(
                applyFixup(lines, "comparison functions", "(\\s+return )(.*compare.*;)",
                        m -> Collections.singletonList(m.group(1) + "-1 * " + m.group(2))),
                "comparison functions", descendingComment);
    }

    private static List<String> fixupNeq(List<String> lines) {
        return applyFixup(lines, "neq", "\\s+return next != last;",
                m -> Collections.singletonList("        return !Objects.equals(next, last);"));
    }

}
