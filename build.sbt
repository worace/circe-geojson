import xerial.sbt.Sonatype._

val SCALA_VERSION = "2.12.10"
val CIRCE_VERSION = "0.13.0"

val commonSettings = Seq(
  organization := "works.worace",
  publishMavenStyle := true,
  scalaVersion := SCALA_VERSION,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  ),
  scalacOptions ++= Seq("-Xfatal-warnings", "-feature", "-deprecation"),
  licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  publishTo := sonatypePublishTo.value,
  sonatypeProjectHosting := Some(GitHubHosting("worace", "circe-geojson", "horace@worace.works"))
)

lazy val core = project
  .settings(commonSettings:_*)
  .settings(
    name := "circe-geojson-core",
    version := "0.1.0-SNAPSHOT",
    description := "GeoJSON ADT and Circe encoders/decoders"
  ).settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  )

lazy val jts = project
  .dependsOn(core % "test->test;compile->compile")
  .settings(commonSettings:_*)
  .settings(
    name := "circe-geojson-jts",
    version := "0.1.0-SNAPSHOT",
    description := "Conversions to and from circe-geojson-core types for JTS Geometries"
  ).settings(
    libraryDependencies ++= Seq(
      "org.locationtech.jts" % "jts-core" % "1.16.1"
    )
  )
