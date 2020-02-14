val SCALA_VERSION = "2.12.12"

val commonSettings = Seq(
  organization := "works.worace.geojson",
  publishMavenStyle := true,
  scalaVersion := SCALA_VERSION,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
)

lazy val core = project
  .settings(commonSettings:_*)
  .settings(
    organization := "works.worace.geojson.core",
    name := "circe-geojson Core",
    version := "0.1.0-SNAPSHOT"
  )

