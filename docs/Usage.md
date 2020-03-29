# Circe GeoJSON

To install my project

```scala
libraryDependencies += "works.worace" % "circe-geojson-core" % "@VERSION@"
```

### Decoding GeoJSON

```scala mdoc
import works.worace.geojson.core.GeoJson

val encodedPoint = """{"type":"Point","coordinates":[1.0,-1.0]}"""
GeoJson.parse(encodedPoint)

val encodedFeature = """{"type":"Feature","geometry": {"type":"Point","coordinates":[1.0,-1.0]}}"""
GeoJson.parse(encodedFeature)

val encodedComplexFeature = """
{
  "type": "Feature",
  "id": 123,
  "properties": {"a": "b"},
  "bbox": [101.0,1.0,101.0,1.0],
  "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
}
"""

GeoJson.parse(encodedComplexFeature)
```

### Creating and Encoding GeoJSON

```scala mdoc
import works.worace.geojson.core.{BBox, Coordinate, Feature, Point}
import io.circe.{Json, JsonObject}

val feature = Feature(
  id=Some(Right("String ID")),
  properties=Some(JsonObject("a" -> Json.fromString("b"))),
  geometry=Some(Point(Coordinate(101.0,1.0))),
  bbox=Some(BBox(Coordinate(101,1.0),Coordinate(101,1.0))),
  foreignMembers=Some(JsonObject("topLevel" -> Json.fromString("properties")))
)
feature.asJson
```
