organization := "anovos"
name := "anovos-commons-scala"
version := "2.12.3"

scalaVersion := "2.12.3"
val sparkVersion = "3.0.0"

publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += Resolver.mavenCentral

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" % "spark-mllib_2.12" % sparkVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.0.0"
)

lazy val root = (project in file("."))
  .settings(
    name := "anovos-commons-scala"
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}