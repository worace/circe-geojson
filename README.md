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
import works.worace.geojson.{GeoJson, Point}

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
import works.worace.geojson.{GeoJson, Point, Geometry}
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

### GeoJson Types and ADT

The library represents GeoJSON using the following ADT hierarchy:

* `works.worace.geojson.GeoJson`
  * `works.worace.geojson.FeatureCollection`
  * `works.worace.geojson.Feature`
  * `works.worace.geojson.Geometry`
    * `works.worace.geojson.Point`
    * `works.worace.geojson.LineString`
    * `works.worace.geojson.Polygon`
    * `works.worace.geojson.MultiPoint`
    * `works.worace.geojson.MultiLineString`
    * `works.worace.geojson.MultiPolygon`
    * `works.worace.geojson.GeometryCollection`

Where necessary, embedded JSON values (such as the `properties` field on a GeoJSON Feature) are represented using Circe's `io.circe.JsonObject`.

Additionally, 2 the types `works.worace.geojson.BBox` and `works.worace.geojson.Coordinate` represent these respective GeoJSON components.

`Coordinate` represents a coordinate with X,Y, and optional Z and M coordinates.

A `BBox` is a bounding rectangle represented by a min and max Coordinate.

### Codec Organization and Parsing

All of the main GeoJSON types under the `works.worace.geojson.GeoJson` ADT include circe-based codecs for encoding and decoding to and from JSON.

These are organized using a `works.worace.geojson.Codable` trait (TODO doc link) which looks like:

```scala
trait Codable[T <: GeoJson] {
  def parse(rawJson: String): Either[io.circe.Error, T]
  def fromJson(json: Json): Either[io.circe.Error, T]
  def asJson(gj: T): Json
}
```

So, for example, the companion object `GeoJson` implements `Codable[GeoJson]`, and could be used to parse a JSON string representing a point:

```scala
works.worace.geojson.GeoJson.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")
```

Similarly the `Point` companion object can be used to parse points (this can be useful if you know the type of your data in advance and want to narrow the output type):

```scala
works.worace.geojson.Point.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")
```

Finally, the Codable types also expose Circe decoder and encoder instances, which can be used with Circe's standard syntax directly if preferred.

```scala
import io.circe.syntax._
import io.circe.parser.parse
import works.wroace.geojson.Point.codec.implicits._
import works.wroace.geojson.GeoJson.codec.implicits._

parse("""{"type":"Point","coordinates":[1.0,-1.0]}""").as[Point]
parse("""{"type":"Point","coordinates":[1.0,-1.0]}""").as[GeoJson]
```

## GeoJSON Nuances and Gotchas

This library attempts to fully implement the GeoJSON Spec, which in some cases is more nuanced than it seems at first glance. In particular many of the GeoJSON types contain optional or dynamic fields, and accounting for these adds some complexities to the provided types.

This section goes over some of these nuances and describes some techniques to make working with them easier.

### Foreign Members

One of the trickier aspects of the GeoJSON Spec (defined in [section 6](https://tools.ietf.org/html/rfc7946#section-6)) is the provision for "Foreign Members". Objects are allowed to include non-standard keys at the top-level, such as a `Feature` object with the key of "title" alongside the standard "id", "type", "properties", and "geometry" keys. This is less of a problem in dynamic languages that can represent GeoJson as nested dictionaries or objects, but is a little tricky to represent in Scala. To this end, each of the `GeoJson` types in the library includes a `foreignMembers: Option[JsonObject]` member to capture these additional keys, if any are present.

### Feature Nullability and the "Simple" Feature Interface

`Feature` is the most common and useful GeoJSON type because it lets us combine spatial geometries with arbitrary metadata. However, there are several nuances about the definition of Features in the GeoJSON spec which make them more complicated to work with than expected:

* The `geometry` property of a feature is nullable: `{"type": "Feature", "geometry": null}` is actually a valid Feature.
* Feature `properties` are optional, even though we may think of the default case as being simply an empty JSON Object
* `id` is also an optional field, but if present it can be _either_ a number or a string.
* Like other GeoJSON types, features can optionally include a `bbox` field as well as any number of "foreign member" top-level keys.

For the core GeoJson ADT types, this library tries to stay as true to the spec as possible. Therefore the core `Feature` implementation looks something like this:

```scala
case class Feature(
  id: Option[Either[JsonNumber, String]],
  properties: Option[JsonObject],
  geometry: Option[Geometry],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
)
```

While this is accurate, and allows us to properly decode all variants of GeoJSON according to the spec, it can be tedious to work with because it doesn't match common conventions of how GeoJSON Features are actually used.

To help with this mismatch, this library also includes another type `works.worace.geojson.SimpleFeature` which looks like this:

```scala
case class SimpleFeature(
  id: Option[String]
  properties: JsonObject,
  geometry: Geometry
)
```

A Feature can be converted to an optional `SimpleFeature` via the interface:

```scala
val simple: Option[SimpleFeature] = feature.simple
```

According to the following rules:

* A `Feature` with an empty geometry will yield an empty `SimpleFeature`
* Features with empty properties receive an empty `io.circe.JsonObject` for their properties
* Any foreign members on the feature will be merged into the `properties` field on the simple version
* Numeric Feature ids will be converted to strings (so that the type can be `Option[String]` rather than `Option[Either[JsonNumber, String]]`)

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
