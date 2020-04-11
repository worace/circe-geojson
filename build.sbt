import xerial.sbt.Sonatype._

val SCALA_VERSION = "2.12.10"
val CIRCE_VERSION = "0.13.0"

val commonSettings = Seq(
  organization := "works.worace",
  homepage := Some(url("https://github.com/worace/circe-geojson")),
  scalaVersion := SCALA_VERSION,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "0.7.1" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  scalacOptions ++= Seq("-Xfatal-warnings", "-feature", "-deprecation"),
  licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  sonatypeProjectHosting := Some(GitHubHosting("worace", "circe-geojson", "horace@worace.works")),
  developers := List(
    Developer(
      "worace",
      "Horace Williams",
      "horace@worace.works",
      url("https://worace.works")
    )
  )
)

val cnameFilter = new FileFilter{
  def accept(f: File) = {
    println("check file")
    println(f.getName)
    f.getName == "CNAME"
  }
}

lazy val root = Project(
  id = "root",
  base = file(".")
).settings(commonSettings: _*)
  .aggregate(core, jts)
  .enablePlugins(GhpagesPlugin)
  .enablePlugins(SiteScaladocPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(
    excludeFilter in ghpagesCleanSite := cnameFilter,
    siteSubdirName in ScalaUnidoc := "api",
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
    git.remoteRepo := "git@github.com:worace/circe-geojson.git",
    ghpagesNoJekyll := true,
    skip in publish := true,
    publishArtifact := false,
    publishLocal := {},
    publish := { }
  )

lazy val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "circe-geojson-core",
    description := "GeoJSON ADT and Circe encoders/decoders"
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % CIRCE_VERSION,
      "io.circe" %% "circe-generic" % CIRCE_VERSION,
      "io.circe" %% "circe-generic-extras" % CIRCE_VERSION,
      "io.circe" %% "circe-parser" % CIRCE_VERSION
    )
  )

lazy val jts = project
  .dependsOn(core % "test->test;compile->compile")
  .settings(commonSettings: _*)
  .settings(
    name := "circe-geojson-jts",
    description := "Conversions to and from circe-geojson-core types for JTS Geometries"
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.locationtech.jts" % "jts-core" % "1.16.1"
    )
  )

lazy val docs = project
  .dependsOn(core, jts)
  .in(file("usage"))
  .enablePlugins(MdocPlugin)
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value
    )
  )
