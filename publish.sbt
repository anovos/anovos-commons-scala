ThisBuild / organization := "io.github.anovos"
ThisBuild / organizationName := "anovos"
ThisBuild / organizationHomepage := Some(url("https://www.anovos.io"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/anovos/anovos-commons-scala"),
    "scm:git@github.cshekhar17/project.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "cshekhar17",
    name  = "Chandra Shekhar",
    email = "chandra@mobilewalla.com",
    url   = url("https://www.mobilewalla.com")
  )
)

ThisBuild / description := "Anovos Commons Scala"
ThisBuild / licenses := List("The Unlicense" -> new URL("https://unlicense.org/"))
ThisBuild / homepage := Some(url("https://github.com/anovos/anovos-commons-scala"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://maven.wordsterbeta.com/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "content/repositories/mwrepo/")
}

ThisBuild / publishMavenStyle := true

ThisBuild / versionScheme := Some("early-semver")
