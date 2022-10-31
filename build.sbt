organization := "anovos"
name := "anovos-commons-scala"

githubOwner := "anovos"
githubRepository := "anovos-commons-scala"
githubTokenSource := TokenSource.GitConfig("github.token")

version := "spark-2.4.4_scala-2.11.12"
scalaVersion := "2.11.12"

val sparkVersion = "2.4.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" % "spark-mllib_2.11" % sparkVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.0.0" % "provided"
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