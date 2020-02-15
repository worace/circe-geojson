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
      assert(decodedLineString == Right(LineString(Vector(Coordinate(1.0, 2.0), Coordinate(2.0, 3.0)))))
    }
  }
}
