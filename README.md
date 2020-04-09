# circe-geojson

![Build Status](https://github.com/worace/circe-geojson/workflows/CI/badge.svg)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-core_2.12/)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-jts_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-jts_2.12/)

A library for working with GeoJSON in idiomatic Scala.

Includes:

* An Algebraic Data Type (`works.worace.geojson.GeoJson`) for representing GeoJSON data in accordance with the RFC 7946 Spec
* Circe-based encoders and decoders for converting GeoJSON to and from JSON
* Optional extensions for converting between `GeoJson` types and Java Topology Suite (JTS) geometries

### For usage examples and more information about the library, see the [Docs](https://github.com/worace/circe-geojson/blob/master/usage/target/mdoc/Usage.md)

## Further Reading

* If you're new to working with GeoJSON in general, Tom MacWright's [More than you ever wanted to know about GeoJSON](https://macwright.org/2015/03/23/geojson-second-bite.html) is a great overview.
* [IETF RFC 7946 (GeoJSON Spec)](https://tools.ietf.org/html/rfc7946)
* [georust/geojson](https://github.com/georust/geojson) - A Rust implementation of GeoJSON which inspired the ADT hierarchy and type definitions for this package

## Development

* Run tests with `sbt test`
* Releases are pushed by CI (Github Actions) using [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release)
* Markdown doc examples are compiled using [mdoc](https://github.com/scalameta/mdoc)

### Upcoming / TODOs

* [ ] Docs
  * [x] Readme Usage examples (tut/md - compile-time check)
  * [ ] Scaladoc
  * [ ] Publish scaladoc to github pages somehow?
* [ ] Cross-build (2.11, 2.12, 2.13? Will these even work?)
