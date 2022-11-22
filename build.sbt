organization := "io.github.anovos"
name := "anovos-commons-scala"
version := "0.1"

scalaVersion := "2.12.3"
val sparkVersion = "3.0.0"

homepage := Some(url("https://github.com/anovos/anovos-commons-scala"))
scmInfo := Some(ScmInfo(url("https://github.com/anovos/anovos-commons-scala"), "git@github.com:anovos/anovos-commons-scala.git"))
publishMavenStyle := true

// disable publish with scala version, otherwise artifact name will include scala version
// e.g anovos-commons-scala_2.12
crossPaths := true

// add sonatype repository settings
// snapshot versions publish to sonatype snapshot repository
// other versions publish to sonatype staging repository
/*publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)*/
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}


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