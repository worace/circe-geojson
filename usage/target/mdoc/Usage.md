# Circe GeoJSON

Installation:

```scala
libraryDependencies += "works.worace" % "circe-geojson-core" % "0.0.0+83-6e75dc6e+20200406-1131-SNAPSHOT"
```

### Decoding GeoJSON

```scala
import works.worace.geojson.GeoJson

val encodedPoint = """{"type":"Point","coordinates":[1.0,-1.0]}"""
// encodedPoint: String = "{\"type\":\"Point\",\"coordinates\":[1.0,-1.0]}"
GeoJson.parse(encodedPoint)
// res0: Either[io.circe.Error, GeoJson] = Right(
//   Point(Coordinate(1.0, -1.0, None, None), None, None)
// )

val encodedFeature = """{"type":"Feature","geometry": {"type":"Point","coordinates":[1.0,-1.0]}}"""
// encodedFeature: String = "{\"type\":\"Feature\",\"geometry\": {\"type\":\"Point\",\"coordinates\":[1.0,-1.0]}}"
GeoJson.parse(encodedFeature)
// res1: Either[io.circe.Error, GeoJson] = Right(
//   Feature(
//     None,
//     None,
//     Some(Point(Coordinate(1.0, -1.0, None, None), None, None)),
//     None,
//     None
//   )
// )

val encodedComplexFeature = """
{
  "type": "Feature",
  "id": 123,
  "properties": {"a": "b"},
  "bbox": [101.0,1.0,101.0,1.0],
  "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
}
"""
// encodedComplexFeature: String = """
// {
//   "type": "Feature",
//   "id": 123,
//   "properties": {"a": "b"},
//   "bbox": [101.0,1.0,101.0,1.0],
//   "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
// }
// """

GeoJson.parse(encodedComplexFeature)
// res2: Either[io.circe.Error, GeoJson] = Right(
//   Feature(
//     Some(Left(JsonLong(123L))),
//     Some(object[a -> "b"]),
//     Some(Point(Coordinate(101.0, 1.0, None, None), None, None)),
//     Some(
//       BBox(
//         Coordinate(101.0, 1.0, None, None),
//         Coordinate(101.0, 1.0, None, None)
//       )
//     ),
//     None
//   )
// )
```

### Creating and Encoding GeoJSON

```scala
import works.worace.geojson.{BBox, Coordinate, Feature, Point}
import io.circe.{Json, JsonObject}

val feature = Feature(
  id=Some(Right("String ID")),
  properties=Some(JsonObject("a" -> Json.fromString("b"))),
  geometry=Some(Point(Coordinate(101.0,1.0))),
  bbox=Some(BBox(Coordinate(101,1.0),Coordinate(101,1.0))),
  foreignMembers=Some(JsonObject("topLevel" -> Json.fromString("properties")))
)
// feature: Feature = Feature(
//   Some(Right("String ID")),
//   Some(object[a -> "b"]),
//   Some(Point(Coordinate(101.0, 1.0, None, None), None, None)),
//   Some(
//     BBox(Coordinate(101.0, 1.0, None, None), Coordinate(101.0, 1.0, None, None))
//   ),
//   Some(object[topLevel -> "properties"])
// )
feature.encode
// res3: Json = JObject(
//   object[topLevel -> "properties",type -> "Feature",id -> "String ID",properties -> {
//   "a" : "b"
// },bbox -> [
//   101.0,
//   1.0,
//   101.0,
//   1.0
// ],geometry -> {
//   "coordinates" : [
//     101.0,
//     1.0
//   ],
//   "bbox" : null,
//   "foreignMembers" : null,
//   "type" : "Point"
// }]
// )
feature.encode.spaces2
// res4: String = """{
//   "topLevel" : "properties",
//   "type" : "Feature",
//   "id" : "String ID",
//   "properties" : {
//     "a" : "b"
//   },
//   "bbox" : [
//     101.0,
//     1.0,
//     101.0,
//     1.0
//   ],
//   "geometry" : {
//     "coordinates" : [
//       101.0,
//       1.0
//     ],
//     "bbox" : null,
//     "foreignMembers" : null,
//     "type" : "Point"
//   }
// }"""
```
