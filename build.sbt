import Dependencies._


lazy val root = Project(
    "root",
    file("."),
    settings = Defaults.coreDefaultSettings ++ Seq(
        name := "scalameter-examples",
        scalaVersion := "2.12.13",
        scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
        publishArtifact := false
    )
).aggregate(benchmarksSuite)

lazy val common = Project(
    "common",
    file("common"),
    settings = Defaults.coreDefaultSettings ++ Seq(
        name := "common",
        scalaVersion := "2.12.13",
        libraryDependencies ++= Seq(
            scalameter
        )
    )
)

lazy val benchmarksSuite = Project(
    "benchmarksSuite",
    file("benchmarks-suite"),
    settings = Defaults.coreDefaultSettings ++ Seq(
        name := "benchmarks-suite",
        scalaVersion := "2.12.13",
        testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
        parallelExecution in Test := false,
        logBuffered := false
    )
).dependsOn(common)


