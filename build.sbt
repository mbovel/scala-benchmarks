val scala3Version = "3.3.1"

enablePlugins(JmhPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-benchmarks",
    version := "0.2.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
