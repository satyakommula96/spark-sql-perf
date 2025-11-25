// You may use this file to add plugin dependencies for sbt.

resolvers ++= Seq(
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Spark Packages Repo" at "https://repos.spark-packages.org/"
)

// Remove incompatible plugins for now
// addSbtPlugin("org.spark-packages" % "sbt-spark-package" % "0.2.3")

// addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

// addSbtPlugin("com.databricks" %% "sbt-databricks" % "0.1.5")

// addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")

// addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
