import Dependencies._

lazy val root = (project in file("."))
  .aggregate(simpleReactiveMongoBenchmarks)

lazy val simpleReactiveMongoBenchmarks = (project in file("simple-reactivemongo-benchmarks"))
  .settings(
    scalaVersion := "2.12.8",
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    libraryDependencies ++= Seq(
      simpleReactiveMongo
    ),
    mainClass in (Jmh, run) := Some("benchmarks.DefaultRunner")
  )
  .enablePlugins(JmhPlugin)
