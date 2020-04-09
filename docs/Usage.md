# Circe GeoJSON

## Quick-Start

Installation:

```scala
libraryDependencies += "works.worace" % "circe-geojson-core** % "@VERSION@**
```

**Basic Decoding Example**

```scala mdoc
import works.worace.geojson.{GeoJson, Point}

val example: Either[io.circe.Error, GeoJson] = GeoJson.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")

val encoded: io.circe.Json = GeoJson.asJson(example.right.get)

encoded.spaces2
```

**Using JTS Conversions**

```scala mdoc
import works.worace.geojson.Point
import works.worace.geojson.jts.Conversions.implicits.GeometryToJts

val point: Point = Point.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""").right.get

point.toJts
```

## Representing GeoJSON

The library models GeoJSON using an ADT with the following hierarchy:

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

So, for example, the companion object `GeoJson` implements `Codable[GeoJson]`, and could be used to parse a JSON string representing a point to an instance of `GeoJson`:

```scala mdoc
works.worace.geojson.GeoJson.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")
```

Similarly the `Point` companion object can be used to parse points (this can be useful if you know the type of your data in advance and want to narrow the output type):

```scala mdoc
works.worace.geojson.Point.parse("""{"type":"Point","coordinates":[1.0,-1.0]}""")
```

Finally, the Codable types also expose Circe decoder and encoder instances, which can be used directly with Circe's standard syntax if preferred.

```scala mdoc
import io.circe.syntax._
import io.circe.parser.decode
import works.worace.geojson.Point.codec.implicits._
import works.worace.geojson.GeoJson.codec.implicits._

// Example Encoder and Decoder instances
works.worace.geojson.Point.codec.implicits.pointEncoder
works.worace.geojson.Point.codec.implicits.pointDecoder

// Decoding using standard Circe methods,
// along with the provided decoders
decode[Point]("""{"type":"Point","coordinates":[1.0,-1.0]}""")
decode[GeoJson]("""{"type":"Point","coordinates":[1.0,-1.0]}""")


// Re-encoding the decoded Point using asJson from circe's syntax
decode[GeoJson]("""{"type":"Point","coordinates":[1.0,-1.0]}""")
  .map(_.asJson)
```

### GeoJSON Nuances and Gotchas

The full GeoJSON Spec has several nuances that don't necessarily come up in common usage but are important in order to be fully compliant with the spec. For example many of the commonly used fields are actually optional or nullable, and there are conventions for including dynamic fields outside of the standard set.

This library tries to adhere to the full spec, but unfortunately doing so adds some complexity to our representation of the standard GeoJSON types.

This section goes over some of these nuances and describes some techniques to make working with them more convenient in the common cases.

### Foreign Members

One of the trickier aspects of the GeoJSON Spec (defined in [section 6](https://tools.ietf.org/html/rfc7946#section-6)) is the provision for "Foreign Members". GeoJSON objects contain a standard set of fields like `geometry` or `type`. But they are also allowed to include non-standard keys at the top-level: for example a `Feature` object with the extra key of "title" alongside its standard "id", "type", "properties", and "geometry" keys. The spec describes these extra fields as "Foreign Members."

This is less problematic for dynamic languages that can represent GeoJson as nested dictionaries or objects, but is a little tricky to represent in Scala. To this end, each of the `GeoJson` types in the library includes a `foreignMembers: Option[JsonObject]` member to capture these additional keys, if any are present.

So, for example a GeoJSON Point with an extra field "title" will decode to a Point instance with a `foreignMembers` JsonObject containing the key "title":

```scala mdoc
val fMemberPoint = works.worace.geojson.Point.parse("""{"type":"Point", "title": "using a foreign member", "coordinates":[1.0,-1.0]}""")


fMemberPoint.map(_.foreignMembers)

// foreignMember fields will be merged back in to the
// top level of the JSON object when encoded
fMemberPoint.map(Point.asJson).map(_.spaces2)
```

### Feature Nullability and the "Simple" Feature Interface

`Feature` is the most common and useful GeoJSON type because it lets us combine spatial geometries with arbitrary metadata.

However, it's also one of the most complicated, because the spec allows many of its fields to be optional or nullable:

* The `geometry` property of a feature is nullable: `{"type": "Feature", "geometry": null}` is actually a valid Feature.
* Feature `properties` are both omittable and nullable, even though we may think of the default case as being an empty JSON Object rather than `None`
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

In practice:

* Strings are more common as IDs than Numbers
* Properties and Foreign Members are often treated as as a single set of fields
* The `bbox` field is relatively uncommon
* Geometries are not missing, so representing them as an option makes downstream usage tedious

To help with this mismatch, we also include another type `works.worace.geojson.SimpleFeature` which looks like this:

```scala
case class SimpleFeature(
  id: Option[String]
  properties: JsonObject,
  geometry: Geometry
)
```

A Feature can be converted to an optional `SimpleFeature` via the `simple` method:

```scala mdoc
// Simple Feature Conversion:
// ID is stringified, if present
// BBox is dropped
// Foreign members are merged into properties
// Geometry is non-optional
works.worace.geojson.Feature.parse(
  """
{
  "type": "Feature",
  "id": 123,
  "foreign": "member",
  "properties": {"a": "b"},
  "bbox": [101.0,1.0,101.0,1.0],
  "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
}"""
).map(_.simple)
```

According to the following rules:

* A `Feature` with an empty geometry will yield `None`. This way `geometry` can be non-optional on `SimpleFeature`s
* Features with empty properties receive an empty `io.circe.JsonObject` for their properties
* Any foreign members on the feature will be merged into the `properties` field on the simple version
* Numeric Feature ids will be converted to strings (so that the type can be `Option[String]` rather than `Option[Either[JsonNumber, String]]`)

## GeoJSON the "Right Way"

This section is purely opinion-based, but here are some thoughts on the best way to work with GeoJSON, based on experience from dealing with it quite a bit.

**TL;DR: for larger datasets, use newline-delimited GeoJSON Features**

A very common use-case for GeoJSON (especially if you're using it in Scala) is to feed geospatial data + metadata into a data pipeline.

For the spatial data you'll need one of the `Geometry` types, and to combine it with metadata you'll need to use a `Feature`. And because you're doing data pipeline work, it's possible that the size of your dataset will get large.

For this reason, the GeoJSON `FeatureCollection` type is relatively useless for data engineering. This is because it requires you to parse the entire (potentially large) string before you can begin processing any of the features -- there is no streaming.

So, for example, when Microsoft releases their [US Building Footprints Dataset](https://github.com/microsoft/USBuildingFootprints) (which is awesome, and very nice of them to share) as `.geojson` files containing `FeatureCollections`, this is less awesome, because they force everyone to load (in the case of California) a 2GB+ file into memory before they can start parsing it.

Instead, it's better to store data as newline-delimited GeoJSON Features, so that you can stream the dataset and handle each feature in turn without loading the whole thing into memory.

This type of encoding is sometimes called "geojsonseq" or "ndjson", and it actually has its own special RFC definition ([IETF RFC 8142](https://tools.ietf.org/html/rfc8142)).

So, coming back to this library, the top-level `GeoJson` type is useful if you need to decode arbitrary GeoJSON whose type you may not know in advance.

But, if you're working in a data engineering context where you might have some control over the input formats, it's more useful to store data as newline-delimited Features (i.e. a `.geojsonseq` file) and then stream the input line by line (e.g. using `scala.io.Source`) and parse each one as a Feature:

```scala
import scala.io.Source
import works.worace.geojson.Feature

Source
  .fromFile("/tmp/msft_california.geojsonseq")
  .getLines
  .map(Feature.parse)

// res: Iterator[Either[io.circe.Error, Feature]]
```

Then you can flatten out the error cases (or just throw exceptions on them), and maybe even convert to `SimpleFeature`s and be on your way.

This approach also works great for processing large files in a distributed context like Spark:

```scala
import works.worace.geojson.Feature

spark.read.textFile("hdfs:///big_honkin_dataset.geojsonseq")
  .map(Feature.parse)
  .flatMap(_.toOption)
// res: RDD[Feature]
```
