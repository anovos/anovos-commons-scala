organization := "anovos"
name := "anovos-commons-scala"

githubOwner := "anovos"
githubRepository := "anovos-commons-scala"
githubTokenSource := TokenSource.GitConfig("github.token")

version := "spark-3.0.0_scala-2.12.3"
scalaVersion := "2.12.3"

val sparkVersion = "3.0.0"

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