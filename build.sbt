val scala3Version = "3.3.1"

enablePlugins(JmhPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "list-benchmarks",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
