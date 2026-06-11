package com.databricks.spark.sql.perf

import java.io.File
import java.nio.file.Files

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class OpenTableFormatTests extends AnyFunSuite with BeforeAndAfterAll {

  var tempDir: File = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    tempDir = Files.createTempDirectory("spark-sql-perf-tests").toFile
  }

  override def afterAll(): Unit = {
    def deleteRecursively(file: File): Unit = {
      if (file.isDirectory) file.listFiles.foreach(deleteRecursively)
      if (file.exists && !file.delete) {
        file.deleteOnExit()
      }
    }
    if (tempDir != null) {
      deleteRecursively(tempDir)
    }
    super.afterAll()
  }

  class DummyDataGenerator extends DataGenerator {
    override def generate(
        sparkContext: SparkContext,
        name: String,
        partitions: Int,
        scaleFactor: String
    ): RDD[String] =
      sparkContext.parallelize(
        Seq(
          "1|apple|red|",
          "2|banana|yellow|",
          "3|cherry|red|"
        ),
        partitions
      )
  }

  class MockTables(sqlContext: SQLContext, scaleFactor: String)
      extends Tables(sqlContext, scaleFactor, false, false) {
    override val dataGenerator: DataGenerator = new DummyDataGenerator()
    import sqlContext.implicits._
    override val tables: Seq[Table] = Seq(
      Table(
        "dummy_fruits",
        partitionColumns = "color" :: Nil,
        'id.int,
        'fruit.string,
        'color.string
      )
    )
  }

  test("Delta data generation") {
    val spark = SparkSession
      .builder()
      .appName("DeltaTest")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      .getOrCreate()

    try {
      val tables   = new MockTables(spark.sqlContext, "1")
      val location = new File(tempDir, "delta_data").getAbsolutePath

      tables.genData(
        location = location,
        format = "delta",
        overwrite = true,
        partitionTables = true,
        clusterByPartitionColumns = false,
        filterOutNullPartitionValues = false,
        tableFilter = "",
        numPartitions = 1
      )

      val tableLoc = s"$location/dummy_fruits"
      val df       = spark.read.format("delta").load(tableLoc)
      assert(df.count() == 3)

      tables.createExternalTables(
        location = location,
        format = "delta",
        databaseName = "default",
        overwrite = true,
        discoverPartitions = true,
        tableFilter = "",
        isPartitioned = true
      )

      val readTable = spark.sql("SELECT * FROM default.dummy_fruits")
      assert(readTable.count() == 3)
    } finally
      spark.stop()
  }

  test("Iceberg data generation") {
    val spark = SparkSession
      .builder()
      .appName("IcebergTest")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config(
        "spark.sql.extensions",
        "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions"
      )
      .config("spark.sql.catalog.spark_catalog", "org.apache.iceberg.spark.SparkSessionCatalog")
      .config("spark.sql.catalog.spark_catalog.type", "hadoop")
      .config(
        "spark.sql.catalog.spark_catalog.warehouse",
        new File(tempDir, "iceberg_warehouse").getAbsolutePath
      )
      .getOrCreate()

    try {
      val tables   = new MockTables(spark.sqlContext, "1")
      val location = new File(tempDir, "iceberg_data").getAbsolutePath

      tables.genData(
        location = location,
        format = "iceberg",
        overwrite = true,
        partitionTables = true,
        clusterByPartitionColumns = false,
        filterOutNullPartitionValues = false,
        tableFilter = "",
        numPartitions = 1
      )

      val df = spark.read.table("default.dummy_fruits")
      assert(df.count() == 3)
    } finally
      spark.stop()
  }

  test("Hudi data generation") {
    val spark = SparkSession
      .builder()
      .appName("HudiTest")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
      .getOrCreate()

    try {
      val tables   = new MockTables(spark.sqlContext, "1")
      val location = new File(tempDir, "hudi_data").getAbsolutePath

      tables.genData(
        location = location,
        format = "hudi",
        overwrite = true,
        partitionTables = true,
        clusterByPartitionColumns = false,
        filterOutNullPartitionValues = false,
        tableFilter = "",
        numPartitions = 1
      )

      val tableLoc = s"$location/dummy_fruits"
      val df       = spark.read.format("hudi").load(tableLoc)
      assert(df.count() == 3)

      tables.createExternalTables(
        location = location,
        format = "hudi",
        databaseName = "default",
        overwrite = true,
        discoverPartitions = true,
        tableFilter = "",
        isPartitioned = true
      )

      val readTable = spark.sql("SELECT * FROM default.dummy_fruits")
      assert(readTable.count() == 3)
    } finally
      spark.stop()
  }
}
