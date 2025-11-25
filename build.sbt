// Your sbt build file. Guides on how to write one can be found at
// http://www.scala-sbt.org/0.13/docs/index.html

name := "spark-sql-perf"

organization := "com.databricks"

scalaVersion := "2.12.18"

crossScalaVersions := Seq("2.12.18")

// Remove publishing configuration for now - focus on compilation
// sparkPackageName := "databricks/spark-sql-perf"

// All Spark Packages need a license
licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

// Spark version - define it manually since we removed the spark-packages plugin
val sparkVersion = "3.5.1"

// Add Spark dependencies manually
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)


initialCommands / console :=
  """
    |import org.apache.spark.sql._
    |import org.apache.spark.sql.functions._
    |import org.apache.spark.sql.types._
    |import org.apache.spark.sql.SparkSession
    |
    |val spark = SparkSession.builder().appName("spark-sql-perf").getOrCreate()
    |val sqlContext = spark.sqlContext
    |import sqlContext.implicits._
  """.stripMargin

libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"

libraryDependencies += "com.twitter" %% "util-jvm" % "24.2.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "org.yaml" % "snakeyaml" % "2.5"

fork := true

// Remove Databricks Cloud configuration for now
// dbcUsername := sys.env.getOrElse("DBC_USERNAME", "")
// dbcPassword := sys.env.getOrElse("DBC_PASSWORD", "")
// dbcApiUrl := sys.env.getOrElse ("DBC_URL", sys.error("Please set DBC_URL"))
// dbcClusters += sys.env.getOrElse("DBC_USERNAME", "")
// dbcLibraryPath := s"/Users/${sys.env.getOrElse("DBC_USERNAME", "")}/lib"

val runBenchmark = inputKey[Unit]("runs a benchmark")

runBenchmark := {
  import complete.DefaultParsers._
  val args = spaceDelimited("[args]").parsed
  val scalaRun = (Compile / run / runner).value
  val classpath = (Compile / fullClasspath).value
  scalaRun.run("com.databricks.spark.sql.perf.RunBenchmark", classpath.map(_.data), args,
    streams.value.log)
}


val runMLBenchmark = inputKey[Unit]("runs an ML benchmark")

runMLBenchmark := {
  import complete.DefaultParsers._
  val args = spaceDelimited("[args]").parsed
  val scalaRun = (Compile / run / runner).value
  val classpath = (Compile / fullClasspath).value
  scalaRun.run("com.databricks.spark.sql.perf.mllib.MLLib", classpath.map(_.data), args,
    streams.value.log)
}


// Comment out release configuration for now
/*
import ReleaseTransformations._

/** Push to the team directory instead of the user's homedir for releases. */
lazy val setupDbcRelease = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    val newSettings = extracted.structure.allProjectRefs.map { ref =>
      dbcLibraryPath in ref := "/databricks/spark/sql/lib"
    }

    reapply(newSettings, st)
  }
)

/********************
 * Release settings *
 ********************/

publishMavenStyle := true

releaseCrossBuild := true

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

pomExtra := (
      <url>https://github.com/databricks/spark-sql-perf</url>
      <scm>
        <url>git@github.com:databricks/spark-sql-perf.git</url>
        <connection>scm:git:git@github.com:databricks/spark-sql-perf.git</connection>
      </scm>
      <developers>
        <developer>
          <id>marmbrus</id>
          <name>Michael Armbrust</name>
          <url>https://github.com/marmbrus</url>
        </developer>
        <developer>
          <id>yhuai</id>
          <name>Yin Huai</name>
          <url>https://github.com/yhuai</url>
        </developer>
        <developer>
          <id>nongli</id>
          <name>Nong Li</name>
          <url>https://github.com/nongli</url>
        </developer>
        <developer>
          <id>andrewor14</id>
          <name>Andrew Or</name>
          <url>https://github.com/andrewor14</url>
        </developer>
        <developer>
          <id>davies</id>
          <name>Davies Liu</name>
          <url>https://github.com/davies</url>
        </developer>
      </developers>
    )

bintrayReleaseOnPublish in ThisBuild := false

// Add publishing to spark packages as another step.
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setupDbcRelease,
  releaseStepTask(dbcUpload),
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
*/