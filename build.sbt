val scala3Version = "3.3.1"

enablePlugins(JmhPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-benchmarks",
    version := "0.2.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  )
