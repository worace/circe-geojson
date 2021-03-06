# circe-geojson

![Build Status](https://github.com/worace/circe-geojson/workflows/CI/badge.svg)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-core_2.12/)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-jts_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-jts_2.12/)

A library for working with GeoJSON in idiomatic Scala.

```scala
import works.worace.geojson.GeoJson

GeoJson.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")
// res: Either[io.circe.Error, GeoJson] = Right(
//   Point(Coordinate(1.0, -1.0, None, None), None, None)
// )
```

Includes:

* An Algebraic Data Type (`works.worace.geojson.GeoJson`) for representing GeoJSON data in accordance with the RFC 7946 Spec
* Circe-based encoders and decoders for converting GeoJSON to and from JSON
* Optional extensions for converting between `GeoJson` types and Java Topology Suite (JTS) geometries

### See the full [Docs](https://github.com/worace/circe-geojson/blob/master/usage/target/mdoc/Usage.md) for usage examples and more information

### [Scaladoc](http://circe-geojson.worace.works/api/)

## Development

* Run tests with `sbt test`
* Releases are pushed by CI (Github Actions) using [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release)
* Markdown doc examples are compiled using [mdoc](https://github.com/scalameta/mdoc)
* Build Mdoc: `sbt docs/mdoc`
* Run Mdoc live server: `sbt docs/mdoc --watch`
* Push Scaladoc to GitHub Pages: `sbt ghpagesPushSite`
* Run scalafmt: `sbt scalafmtAll`

## Releasing

* Check the last version with `git tag`
* Tag the next one, e.g. `git tag v0.1.3`
* Push `git push origin v0.1.3` and sbt-ci-release will push the release to sonatype

### Upcoming / TODOs

* [ ] Docs
  * [x] Readme Usage examples (tut/md - compile-time check)
  * [x] Scaladoc
  * [x] Publish scaladoc to github pages somehow? (sbt-site - `sbt ghpagesPushSite`)
  * [ ] Push github pages site from CI (may need a token or ssh key)
* [x] Cross-build (2.11, 2.12, 2.13? Will these even work?)
