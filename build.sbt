import Dependencies._

lazy val root = Project(
  "root",
  file("."),
  settings = Defaults.coreDefaultSettings ++ Seq(
    name := "scalameter-examples",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint")
  )
).aggregate(simpleReactiveMongoBenchmarks)

lazy val common = Project(
  "common",
  file("common"),
  settings = Defaults.coreDefaultSettings ++ Seq(
    name := "common",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      scalameter
    )
  )
)

lazy val simpleReactiveMongoBenchmarks = Project(
  "simple-reactivemongo-benchmarks",
  file("simple-reactivemongo-benchmarks"),
  settings = Defaults.coreDefaultSettings ++ Seq(
    name := "simple-reactivemongo-benchmarks",
    scalaVersion := "2.12.8",
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    libraryDependencies ++= Seq(
      simpleReactiveMongo % Test
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    parallelExecution in Test := false,
    logBuffered := false
  )
).dependsOn(common)