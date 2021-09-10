/*
 * Copyright (c) 2016-2020 Deephaven Data Labs and Patent Pending
 */
#include "tests/catch.hpp"
#include "tests/test_util.h"
#include "deephaven/client/utility/utility.h"

using deephaven::client::highlevel::aggAvg;
using deephaven::client::highlevel::aggSum;
using deephaven::client::highlevel::aggMin;
using deephaven::client::highlevel::aggMax;
using deephaven::client::highlevel::aggCount;
using deephaven::client::highlevel::aggCombo;
using deephaven::client::highlevel::Aggregate;
using deephaven::client::highlevel::AggregateCombo;
using deephaven::client::highlevel::TableHandleManager;
using deephaven::client::highlevel::TableHandle;
using deephaven::client::highlevel::SortPair;
using deephaven::client::highlevel::DeephavenConstants;
using deephaven::client::utility::streamf;
using deephaven::client::utility::stringf;
using deephaven::client::utility::flight::statusOrDie;
using deephaven::client::utility::flight::valueOrDie;

namespace deephaven {
namespace client {
namespace tests {
TEST_CASE("Various aggregates", "[aggregates]") {
  auto tm = TableMakerForTests::create();
  auto table = tm.table();

  auto importDate = table.getStrCol("ImportDate");
  auto ticker = table.getStrCol("Ticker");
  auto close = table.getNumCol("Close");
  table = table.where(importDate == "2017-11-01");
  auto zngaTable = table.where(ticker == "ZNGA");

  std::cout << zngaTable.headBy(5, ticker).stream(true) << '\n';
  std::cout << zngaTable.tailBy(5, ticker).stream(true) << '\n';

  auto aggTable1 = zngaTable.view(close)
      .by(AggregateCombo::create({
          Aggregate::avg("AvgClose=Close"),
          Aggregate::sum("SumClose=Close"),
          Aggregate::min("MinClose=Close"),
          Aggregate::max("MaxClose=Close"),
          Aggregate::count("Count")}));
  std::cout << aggTable1.stream(true) << '\n';

  auto aggTable2 = zngaTable.view(close)
      .by(aggCombo({
          aggAvg(close.as("AvgClose")),
          aggSum(close.as("SumClose")),
          aggMin(close.as("MinClose")),
          aggMax(close.as("MaxClose")),
          aggCount("Count")}));
  std::cout << aggTable2.stream(true) << '\n';

  std::vector<std::string> tickerData = {"AAPL", "AAPL", "AAPL"};
  std::vector<double> avgCloseData = {541.55};
  std::vector<double> sumCloseData = {1083.1};
  std::vector<double> minCloseData = {538.2};
  std::vector<double> maxCloseData = {544.9};
  std::vector<int64_t> countData = {2};

  const TableHandle *tables[] = {&aggTable1, &aggTable2};
  for (const auto *t : tables) {
    compareTable(
        *t,
        "AvgClose", avgCloseData,
        "SumClose", sumCloseData,
        "MinClose", minCloseData,
        "MaxClose", maxCloseData,
        "Count", countData
        );
  }
}
}  // namespace tests
}  // namespace client
}  // namespace deephaven
