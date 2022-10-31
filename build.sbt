organization := "anovos"
name := "anovos-commons-scala"
version := "2.12.3"

scalaVersion := "2.12.3"
val sparkVersion = "3.0.0"

publishTo := Some("MW Nexus" at "https://maven.wordsterbeta.com/content/repositories/mwrepo/")
resolvers += ("MW Nexus" at "https://maven.wordsterbeta.com/content/repositories/mwrepo/").withAllowInsecureProtocol(true)
credentials += Credentials("MW Nexus", "maven.wordsterbeta.com", "mw-nexus", "Wordster2009")


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

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

releaseIgnoreUntrackedFiles := true