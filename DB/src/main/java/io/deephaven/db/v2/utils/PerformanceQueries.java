package io.deephaven.db.v2.utils;

import io.deephaven.db.tables.Table;

import io.deephaven.util.annotations.ScriptApi;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

import static io.deephaven.db.v2.by.ComboAggregateFactory.*;

public class PerformanceQueries {
    private static final boolean formatPctColumns = true;

    /**
     * <p>
     * Takes in a query id and returns a view for that query's performance data.
     * </p>
     *
     * <p>
     * The query performance log contains data on how long each query takes to run. Examples of what constitutes one
     * individual query, for performance logging purposes, include:
     * <ul>
     * <li>A new command in the console (i.e. type something, then press the return key)</li>
     * <li>A sort, filter, or custom column generated by a UI</li>
     * <li>A call from a client API external application</li>
     * </ul>
     * </p>
     *
     * @param evaluationNumber evaluation number
     * @return query performance table.
     */
    @ScriptApi
    public static Table queryPerformance(final long evaluationNumber) {

        final long workerHeapSizeBytes = getWorkerHeapSizeBytes();
        Table queryPerformanceLog = TableLoggers.queryPerformanceLog()
                .where(whereConditionForEvaluationNumber(evaluationNumber))
                .updateView(
                        "WorkerHeapSize = " + workerHeapSizeBytes + "L",
                        "TimeSecs = nanosToMillis(EndTime - StartTime) / 1000d", // How long this query ran for, in
                                                                                 // seconds
                        "NetMemoryChange = FreeMemoryChange - TotalMemoryChange",
                        "QueryMemUsed = TotalMemoryUsed - TotalMemoryFree", // Memory in use by the query. (Only
                                                                            // includes active heap memory.)
                        "QueryMemUsedPct = QueryMemUsed / WorkerHeapSize", // Memory usage as a percenage of max heap
                                                                           // size (-Xmx)
                        "QueryMemFree = WorkerHeapSize - QueryMemUsed" // Remaining memory until the query runs into the
                                                                       // max heap size
                )
                .moveUpColumns(
                        "ProcessUniqueId", "EvaluationNumber",
                        "QueryMemUsed", "QueryMemFree", "QueryMemUsedPct",
                        "EndTime", "TimeSecs", "NetMemoryChange");
        if (formatPctColumns) {
            queryPerformanceLog = formatColumnsAsPct(queryPerformanceLog, "QueryMemUsedPct");
        }
        return queryPerformanceLog;
    }

    /**
     * <p>
     * Takes in a query id and returns a view for that query's individual operations's performance data.
     * </p>
     *
     * <p>
     * The query operation performance log contains data on how long each individual operation of a query (where(),
     * update(), naturalJoin(), etc., as well as internal functions) takes to execute, and the change in resource
     * consumption while each was executing.
     * </p>
     *
     * @param evaluationNumber evaluation number
     * @return query operation performance table.
     */
    @ScriptApi
    public static Table queryOperationPerformance(final long evaluationNumber) {
        final Table queryOps = TableLoggers.queryOperationPerformanceLog()
                .where(whereConditionForEvaluationNumber(evaluationNumber))
                .updateView(
                        "TimeSecs = nanosToMillis(EndTime - StartTime) / 1000d",
                        "NetMemoryChange = FreeMemoryChange - TotalMemoryChange" // Change in memory usage delta while
                                                                                 // this query was executing
                )
                .moveUpColumns(
                        "ProcessUniqueId", "EvaluationNumber", "OperationNumber",
                        "EndTime", "TimeSecs", "NetMemoryChange");

        return queryOps;
    }

    /**
     * Gets the information for a process.
     *
     * @param processInfoId id
     * @param type type
     * @param key key
     * @return process information
     */
    public static String processInfo(final String processInfoId, final String type, final String key) {
        final Table processInfo = TableLoggers.processInfoLog()
                .where("Id = `" + processInfoId + "`", "Type = `" + type + "`", "Key = `" + key + "`")
                .select("Value");
        try {
            return (String) processInfo.getColumn(0).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Takes in a query id and returns a view for that query's update performance data.
     *
     * @param evaluationNumber evaluation number
     * @return query update performance table.
     */
    @ScriptApi
    public static Table queryUpdatePerformance(final long evaluationNumber) {
        final String whereCondition = whereConditionForEvaluationNumber(evaluationNumber);
        final long workerHeapSizeBytes = getWorkerHeapSizeBytes();
        Table queryUpdatePerformance = TableLoggers.updatePerformanceLog()
                .where(whereCondition)
                .updateView(
                        "WorkerHeapSize = " + workerHeapSizeBytes + "L",
                        "Ratio = EntryIntervalUsage / IntervalDurationNanos", // % of time during this interval that the
                                                                              // operation was using CPU
                        "QueryMemUsed = TotalMemoryUsed - TotalMemoryFree", // Memory in use by the query. (Only
                                                                            // includes active heap memory.)
                        "QueryMemUsedPct = QueryMemUsed / WorkerHeapSize", // Memory usage as a percenage of the max
                                                                           // heap size (-Xmx)
                        "QueryMemFree = WorkerHeapSize - QueryMemUsed", // Remaining memory until the query runs into
                                                                        // the max heap size
                        "NRows = EntryIntervalAdded + EntryIntervalRemoved + EntryIntervalModified", // Total number of
                                                                                                     // changed rows
                        "RowsPerSec = round(NRows / IntervalDurationNanos * 1.0e9)", // Average rate data is ticking at
                        "RowsPerCPUSec = round(NRows / EntryIntervalUsage * 1.0e9)" // Approximation of how fast CPU
                                                                                    // handles row changes
                )
                .moveUpColumns(
                        "ProcessUniqueId", "EvaluationNumber", "OperationNumber",
                        "Ratio", "QueryMemUsed", "QueryMemUsedPct", "IntervalEndTime",
                        "RowsPerSec", "RowsPerCPUSec", "EntryDescription");
        if (formatPctColumns) {
            queryUpdatePerformance = formatColumnsAsPct(queryUpdatePerformance, "Ratio", "QueryMemUsedPct");
        }
        return queryUpdatePerformance;
    }

    /**
     * Creates a map of query update performance tables.
     *
     * @param evaluationNumber evaluation number
     * @return map of query update performance tables.
     */
    public static Map<String, Table> queryUpdatePerformanceMap(final long evaluationNumber) {
        final Map<String, Table> resultMap = new HashMap<>();
        final Table qup = queryUpdatePerformance(evaluationNumber);
        resultMap.put("QueryUpdatePerformance", qup);

        final Table worstInterval = qup
                .by("IntervalStartTime", "IntervalDurationNanos")
                .sort("IntervalDurationNanos")
                .tail(1)
                .ungroup()
                .view("IntervalStartTime",
                        "IntervalEndTime",
                        "EntryId",
                        "EntryDescription",
                        "IntervalDurationNanos",
                        "Ratio",
                        "EntryIntervalUsage",
                        "EntryIntervalAdded",
                        "EntryIntervalRemoved",
                        "EntryIntervalModified",
                        "NRows");

        resultMap.put("WorstInterval", worstInterval);

        // Create a table showing the 'worst' updates, i.e. the operations with the greatest 'Ratio'
        final Table updateWorst = qup.sortDescending("Ratio");
        resultMap.put("UpdateWorst", updateWorst);

        // Create a table with updates from the most recent performance recording. interval at the top. (Within each
        // interval, operations are still sorted with the greatest Ratio at the top.)

        final Table updateMostRecent = updateWorst.sortDescending("IntervalEndTime").moveUpColumns("IntervalEndTime");
        resultMap.put("UpdateMostRecent", updateMostRecent);


        // Create a table that summarizes the update performance data within each interval
        Table updateAggregate = qup.by(
                AggCombo(
                        AggSum("NRows", "EntryIntervalUsage"),
                        AggFirst("QueryMemUsed", "WorkerHeapSize", "QueryMemUsedPct", "IntervalDurationNanos")),
                "IntervalStartTime", "IntervalEndTime", "ProcessUniqueId")
                .updateView("Ratio = EntryIntervalUsage / IntervalDurationNanos")
                .moveUpColumns("IntervalStartTime", "IntervalEndTime", "Ratio");
        if (formatPctColumns) {
            updateAggregate = formatColumnsAsPct(updateAggregate, "Ratio", "QueryMemUsedPct");
        }
        resultMap.put("UpdateAggregate", updateAggregate);


        final Table updateSummaryStats = updateAggregate.by(
                AggCombo(
                        AggPct(0.99, "Ratio_99_Percentile = Ratio", "QueryMemUsedPct_99_Percentile = QueryMemUsedPct"),
                        AggPct(0.90, "Ratio_90_Percentile = Ratio", "QueryMemUsedPct_90_Percentile = QueryMemUsedPct"),
                        AggPct(0.75, "Ratio_75_Percentile = Ratio", "QueryMemUsedPct_75_Percentile = QueryMemUsedPct"),
                        AggPct(0.50, "Ratio_50_Percentile = Ratio", "QueryMemUsedPct_50_Percentile = QueryMemUsedPct"),
                        AggMax("Ratio_Max = Ratio", "QueryMemUsedPct_Max = QueryMemUsedPct")));

        resultMap.put("UpdateSummaryStats", updateSummaryStats);
        return resultMap;
    }

    private static Table formatColumnsAsPct(final Table t, final String... cols) {
        final String[] formats = new String[cols.length];
        for (int i = 0; i < cols.length; ++i) {
            formats[i] = cols[i] + "=Decimal(`#0.##%`)";
        }
        return t.formatColumns(formats);
    }

    private static long getWorkerHeapSizeBytes() {
        final OptionalLong opt = MemoryTableLoggers.getInstance().getProcessInfo().getMemoryInfo().heap().max();
        return opt.orElse(0);
    }

    private static String whereConditionForEvaluationNumber(final long evaluationNumber) {
        return "EvaluationNumber = " + evaluationNumber + "";
    }
}
