import Dependencies.*

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.7.2",
    organization := "io.github.sceredi",
    description := "A template for Scala3 projects",
    homepage := Some(
      url(
        "https://github.com/sceredi/template-for-scala3-projects",
      ),
    ),
    licenses := List(
      "MIT" -> url("https://mit-license.org/"),
    ),
    versionScheme := Some("early-semver"),
    developers := List(
      Developer(
        "sceredi",
        "Simone Ceredi",
        "ceredi.simone@gmail.com",
        url("https://github.com/sceredi"),
      ),
    ),
    scalacOptions ++= Seq(
      "-Werror",
      "-Wunused:all",
      "-Wvalue-discard",
      "-Wnonunit-statement",
      "-Yexplicit-nulls",
      "-Wsafe-init",
      "-Ycheck-reentrant",
      "-Xcheck-macros",
      "-rewrite",
      "-indent",
      "-unchecked",
      "-explain",
      "-feature",
      "-language:strictEquality",
      "-language:implicitConversions",
    ),
    coverageEnabled := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    wartremoverErrors ++= Warts.all,

    /*
     * Dependencies
     */
    libraryDependencies ++= scalaTestBundle,
    libraryDependencies += scalaTestJUnit5,
  )
