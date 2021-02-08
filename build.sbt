import Dependencies._

lazy val root = (project in file("."))
  .aggregate(simpleReactiveMongoBenchmarks)

lazy val common = (project in file("common"))
  .settings(
    scalaVersion := "2.12.8",
  ).enablePlugins(JmhPlugin)

lazy val simpleReactiveMongoBenchmarks = (project in file("simple-reactivemongo-benchmarks"))
  .settings(
    scalaVersion := "2.12.8",
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    libraryDependencies ++= Seq(
      simpleReactiveMongo
    ),
    mainClass in (Jmh, run) := Some("runner.DefaultRunner")
  )
  .dependsOn(common)
  .enablePlugins(JmhPlugin)
