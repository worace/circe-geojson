package works.worace.geojson.core

import org.scalatest.FeatureSpec

class GeoJsonTest extends FeatureSpec {
  feature("example") {
    scenario("hello world") {
      val point = """
      {"type": "Point", "coordinates": [102.0, 0.5]}
      """
      val decodedPoint = GeoJson.parse(point)
      assert(decodedPoint == Right(Point(Coordinate(102.0, 0.5))))

      val lineString = """
      {"type": "LineString", "coordinates": [[1.0, 2.0], [2.0, 3.0]]}
      """
      val decodedLineString = GeoJson.parse(lineString)
      assert(
        decodedLineString == Right(LineString(Vector(Coordinate(1.0, 2.0), Coordinate(2.0, 3.0))))
      )
    }

    scenario("basic polygon") {
      val gj = """
      {"type": "Polygon", "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]}
      """
      val decodedLineString = GeoJson.parse(gj)
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
      assert(decodedLineString == Right(exp))
    }

    scenario("multi point") {
      val gj = """
      {"type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0]]}
      """
      val decodedLineString = GeoJson.parse(gj)
      val exp = MultiPoint(Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)))
      assert(decodedLineString == Right(exp))
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
      val decodedLineString = GeoJson.parse(gj)
      val exp = MultiLineString(
        Vector(
          Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)),
          Vector(Coordinate(102.0, 2.0), Coordinate(103.0, 3.0))
        )
      )
      assert(decodedLineString == Right(exp))
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
      val decodedLineString = GeoJson.parse(gj)
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
      assert(decodedLineString == Right(exp))
    }
  }
}
