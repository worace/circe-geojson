## circe-geojson

![Build Status](https://github.com/worace/circe-geojson/workflows/CI/badge.svg)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-core_2.12/)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-jts_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-jts_2.12/)

Library for working with GeoJSON in idiomatic Scala.

Includes:

* A `GeoJson` Algebraic Data Type for representing GeoJSON data in accordance with the RFC 7946 Spec
* Circe-based encoders and decoders for de/serializing GeoJSON to and from JSON
* Optional extensions for converting GeoJSON geometries to and from Java Topology Suite (JTS) geometries

### Quick-Start

### Usage

```scala
import works.worace.geojson.core.{GeoJson, Point}

val point: Either[io.circe.Error, GeoJson] = GeoJson.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")

val encoded: io.circe.Json = GeoJson.asJson(point.right.get)

val pretty: String = encoded.spaces2

println(pretty)
// {
//   "type" : "Point",
//   "coordinates" : [
//     1.0,
//     -1.0
//   ]
// }
```

#### Using JTS Conversions

```scala
import works.worace.geojson.core.{GeoJson, Point, Geometry}
import org.locationtech.jts.geom.{Geometry => JtsGeom}
import works.worace.geojson.jts.Conversions.implicits.GeometryToJts

val point: GeoJson = GeoJson.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""").right.get

val jts: Option[JtsGeom] = point match {
  case p: Geometry => Some(p.toJts)
  case _ => None
}
// jts: Option[org.locationtech.jts.geom.Geometry] = Some(POINT (1 -1))
```

#### More Examples

For more detailed instructions, see [Usage Examples](https://github.com/worace/circe-geojson/blob/master/docs/Usage.md).

## Upcoming / TODOs

* [ ] Hoist namespaces (`works.worace.geojson.jts.Conversions.implicits.GeometryToJts` => `works.worace.geojson.JtsConversions._`)
* Publishing Improvements - https://docs.scala-lang.org/overviews/contributors/index.html
* [ ] Readme
  * [ ] Install instructions
  * [ ] Background / Description
* [ ] Docs
  * [ ] Readme Usage examples (tut/md - compile-time check)
  * [ ] Scaladoc
  * [ ] Publish scaladoc to github pages somehow?
* [ ] Cross-build (2.11, 2.12, 2.13? Will these even work?)
