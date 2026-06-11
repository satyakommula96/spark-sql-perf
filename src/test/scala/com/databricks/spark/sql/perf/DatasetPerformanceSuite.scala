package com.databricks.spark.sql.perf

import org.scalatest.funsuite.AnyFunSuite

class DatasetPerformanceSuite extends AnyFunSuite {
  ignore("run benchmark") {
    val benchmark = new DatasetPerformance() {
      override val numLongs = 100
    }
    import benchmark._

    val exp = runExperiment(allBenchmarks)
    exp.waitForFinish(10000)
  }
}
