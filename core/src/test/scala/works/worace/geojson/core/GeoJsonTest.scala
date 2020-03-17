package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder}

class GeoJsonTest extends FeatureSpec {
  feature("example") {
    scenario("decoding coords") {
      val xy = "[102.0, 0.5]"
      val xyz = "[102.0, 0.5, 1.0]"
      val xyzm = "[102.0, 0.5, 1.0, 2.0]"

      def parseCoord(s: String): Either[io.circe.Error, Coordinate] = {
        import CoordinateSerde._
        io.circe.parser.decode[Coordinate](s)
      }

      assert(parseCoord(xy) == Right(Coordinate(102.0, 0.5, None, None)))
      assert(parseCoord(xyz) == Right(Coordinate(102.0, 0.5, Some(1.0), None)))
      assert(parseCoord(xyzm) == Right(Coordinate(102.0, 0.5, Some(1.0), Some(2.0))))
      // val decXy = io.circe.parser.parse(xy)
      // val decXy = io.circe.parser.parse(xyz)
      // println(decXy)
    }

    scenario("Point") {
      val point = """
      {"type": "Point", "coordinates": [102.0, 0.5]}
      """
      val decodedPoint = GeoJson.parse(point)
      val exp = Right(Point(Coordinate(102.0, 0.5)))
      assert(decodedPoint == exp)
    }

    scenario("LineString") {
      val lineString = """
      {"type": "LineString", "coordinates": [[1.0, 2.0], [2.0, 3.0]]}
      """
      val decoded = GeoJson.parse(lineString)
      val expLs = Right(LineString(Vector(Coordinate(1.0, 2.0), Coordinate(2.0, 3.0))))
      assert(decoded == expLs)
    }

    scenario("basic polygon") {
      println("poly")
      val gj = """
      {"type": "Polygon", "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]}
      """
      val decoded = GeoJson.parse(gj)
      val exp = Polygon(
        Vector(
          Vector(
            Coordinate(100.0, 0.0),
            Coordinate(101.0, 0.0),
            Coordinate(101.0, 1.0),
            Coordinate(100.0, 1.0),
            Coordinate(100.0, 0.0)
          )
        )
      )
      assert(decoded == Right(exp))
    }

    scenario("multi point") {
      val gj = """
      {"type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0]]}
      """
      val decoded = GeoJson.parse(gj)
      val exp = MultiPoint(Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)))
      assert(decoded == Right(exp))
    }

    scenario("multi linestring") {
      val gj = """
      {"type": "MultiLineString",
       "coordinates": [
         [ [100.0, 0.0], [101.0, 1.0] ],
         [ [102.0, 2.0], [103.0, 3.0] ]
       ]
      }
      """
      val decoded = GeoJson.parse(gj)
      val exp = MultiLineString(
        Vector(
          Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)),
          Vector(Coordinate(102.0, 2.0), Coordinate(103.0, 3.0))
        )
      )
      assert(decoded == Right(exp))
    }

    scenario("multi polygon") {
      val gj = """
      {"type": "MultiPolygon",
       "coordinates": [
         [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
         [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
          [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
       ]
      }
      """
      val decoded = GeoJson.parse(gj)
      val exp = MultiPolygon(
        Vector(
          Vector(
            Vector(Coordinate(102.0, 2.0), Coordinate(103.0, 2.0), Coordinate(103.0, 3.0), Coordinate(102.0, 3.0), Coordinate(102.0, 2.0))
          ),
          Vector(
            Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 0.0), Coordinate(101.0, 1.0), Coordinate(100.0, 1.0), Coordinate(100.0, 0.0)),
            Vector(Coordinate(100.2, 0.2), Coordinate(100.8, 0.2), Coordinate(100.8, 0.8), Coordinate(100.2, 0.8), Coordinate(100.2, 0.2)
            )
          )
        )
      )
      assert(decoded == Right(exp))
    }

    scenario("geometry collection") {
      val gj = """
      { "type": "GeometryCollection",
        "geometries": [
          { "type": "Point", "coordinates": [101.0, 1.0]},
          { "type": "LineString", "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]}
        ]
      }
      """
      val decoded = GeoJson.parse(gj)
      val exp = GeometryCollection(
        Vector(
          Point(Coordinate(101.0, 1.0)),
          LineString(Vector(Coordinate(101.0, 0.0), Coordinate(102.0, 1.0)))
        )
      )
      assert(decoded == Right(exp))
    }

    scenario("feature no props") {
      val gj = """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """
      val decoded = GeoJson.parse(gj)
      val exp = Feature(
        Point(Coordinate(101.0, 1.0))
      )
      assert(decoded == Right(exp))
    }

    scenario("feature with props") {
      val gj = """
      { "type": "Feature",
        "properties": {"a": "b"},
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """
      val decoded = GeoJson.parse(gj)
      val exp = Feature(
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
      assert(decoded == Right(exp))
    }

    scenario("feature with numeric id") {
      val gj = """
      { "type": "Feature",
        "id": 123,
        "properties": {"a": "b"},
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """
      val decoded = GeoJson.parse(gj)
      val exp = Feature(
        123,
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
      assert(decoded == Right(exp))
    }

    scenario("feature collection") {
      val gj = """
      {"type": "FeatureCollection",
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """
      val decoded = GeoJson.parse(gj)
      val f = Feature(
        "pizza",
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
      val exp = FeatureCollection(Vector(f))
      assert(decoded == Right(exp))
    }
  }
}
