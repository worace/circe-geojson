package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

object TestData {
  val coordXY = "[102.0, 0.5]"
  val coordXYZ = "[102.0, 0.5, 1.0]"
  val coordXYZM = "[102.0, 0.5, 1.0, 2.0]"
  val coordTooFew = "[102.0]"
  val coordTooMany = "[102.0, 0.5, 1.0, 2.0, 3.0]"

  case class Case(
    encoded: String,
    decoded: GeoJson
  )

  object BaseGeomCases {
    val point = Case(
      """{"type": "Point", "coordinates": [102.0, 0.5]}""",
      Point(Coordinate(102.0, 0.5))
    )
    val lineString = Case(
      """{"type": "LineString", "coordinates": [[1.0, 2.0], [2.0, 3.0]]}""",
      LineString(Vector(Coordinate(1.0, 2.0), Coordinate(2.0, 3.0)))
    )
    val polygon = Case(
      """
        {"type": "Polygon", "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]}
      """,
      Polygon(
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
    )

    val multiPoint = Case(
      """
      {"type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0]]}
    """,
      MultiPoint(Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)))
    )
    val multiLineString = Case(
      """
      {"type": "MultiLineString",
       "coordinates": [
         [ [100.0, 0.0], [101.0, 1.0] ],
         [ [102.0, 2.0], [103.0, 3.0] ]
       ]
      }
    """,
      MultiLineString(
        Vector(
          Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)),
          Vector(Coordinate(102.0, 2.0), Coordinate(103.0, 3.0))
        )
      )
    )
    val multiPolygon = Case(
      """
      {"type": "MultiPolygon",
       "coordinates": [
         [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
         [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
          [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
       ]
      }
    """,
      MultiPolygon(
        Vector(
          Vector(
            Vector(
              Coordinate(102.0, 2.0),
              Coordinate(103.0, 2.0),
              Coordinate(103.0, 3.0),
              Coordinate(102.0, 3.0),
              Coordinate(102.0, 2.0)
            )
          ),
          Vector(
            Vector(
              Coordinate(100.0, 0.0),
              Coordinate(101.0, 0.0),
              Coordinate(101.0, 1.0),
              Coordinate(100.0, 1.0),
              Coordinate(100.0, 0.0)
            ),
            Vector(
              Coordinate(100.2, 0.2),
              Coordinate(100.8, 0.2),
              Coordinate(100.8, 0.8),
              Coordinate(100.2, 0.8),
              Coordinate(100.2, 0.2)
            )
          )
        )
      )
    )
    val geometryCollection = Case(
      """
      { "type": "GeometryCollection",
        "geometries": [
          { "type": "Point", "coordinates": [101.0, 1.0]},
          { "type": "LineString", "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]}
        ]
      }
      """,
      GeometryCollection(
        Vector(
          Point(Coordinate(101.0, 1.0)),
          LineString(Vector(Coordinate(101.0, 0.0), Coordinate(102.0, 1.0)))
        )
      )
    )
  }

  object FeatureCases {
    val noProps = Case(
      """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0))
      )
    )

    val xyz = Case(
      """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0, 2.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0, 2.0))
      )
    )

    val xyzm = Case(
      """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0, 2.0, 3.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0, 2.0, 3.0))
      )
    )

    val nullProps = Case(
      """
      { "type": "Feature", "properties": null, "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0))
      )
    )

    val empty = Case(
      """
      { "type": "Feature"}
      """,
      Feature(None, None, None, None, None)
    )

    val nullGeom = Case(
      """
      { "type": "Feature", "geometry": null}
      """,
      Feature(None, None, None, None, None)
    )

    val props = Case(
      """
      { "type": "Feature", "properties": {"a": "b", "c": 1}, "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """,
      Feature(
        JsonObject("a" -> Json.fromString("b"), "c" -> Json.fromInt(1)),
        Point(Coordinate(101.0, 1.0))
      )
    )

    val stringId = Case(
      """ {"type": "Feature",
        "id": "123",
        "properties": {"a": "b"},
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """,
      Feature(
        "123",
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
    )

    val intId = Case(
      """ {"type": "Feature",
        "id": 123,
        "properties": {"a": "b"},
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """,
      Feature(
        123,
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
    )

    val bbox = Case(
      """{"type": "Feature",
        "bbox": [101.0, 1.0, 101.0, 1.0],
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """,
      Feature(
        None,
        None,
        Some(Point(Coordinate(101.0, 1.0))),
        Some(BBox(Coordinate(101.0, 1.0), Coordinate(101.0, 1.0))),
        None
      )
    )
  }

  object FeatureCollectionCases {
    val noForeignMembers = Case(
      """
      {"type": "FeatureCollection",
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """,
      FeatureCollection(
        Vector(
          Feature(
            "pizza",
            JsonObject("a" -> Json.fromString("b")),
            Point(Coordinate(101.0, 1.0))
          )
        )
      )
    )

    val foreignMembers = Case(
      """
      {"type": "FeatureCollection",
       "some": "prop",
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """,
      FeatureCollection(
        Vector(
          Feature(
            "pizza",
            JsonObject("a" -> Json.fromString("b")),
            Point(Coordinate(101.0, 1.0))
          )
        ),
        None,
        Some(JsonObject("some" -> Json.fromString("prop")))
      )
    )

    val bbox = Case(
      """
      {"type": "FeatureCollection",
       "bbox": [101.0, 1.0, 101.0, 1.0],
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """,
      FeatureCollection(
        Vector(
          Feature(
            "pizza",
            JsonObject("a" -> Json.fromString("b")),
            Point(Coordinate(101.0, 1.0))
          )
        ),
        Some(BBox(Coordinate(101.0, 1.0), Coordinate(101.0, 1.0))),
        None
      )
    )
  }
}

class GeoJsonTest extends FeatureSpec {
  import TestData._

  feature("Variable length coordinates") {
    scenario("Decodes with appropriate Option value") {
      def parseCoord(s: String): Either[io.circe.Error, Coordinate] = {
        import CoordinateSerde._
        io.circe.parser.decode[Coordinate](s)
      }

      assert(parseCoord(coordXY) == Right(Coordinate(102.0, 0.5, None, None)))
      assert(parseCoord(coordXYZ) == Right(Coordinate(102.0, 0.5, Some(1.0), None)))
      assert(parseCoord(coordXYZM) == Right(Coordinate(102.0, 0.5, Some(1.0), Some(2.0))))
      assert(parseCoord(coordTooFew) == Left(DecodingFailure("Invalid GeoJson Coordinates", List())))
      assert(parseCoord(coordTooMany) == Left(DecodingFailure("Invalid GeoJson Coordinates", List())))
    }
  }

  feature("2-D Basic Geometries") {
    scenario("Point") {
      val decodedPoint = GeoJson.parse(BaseGeomCases.point.encoded)
      assert(decodedPoint == Right(BaseGeomCases.point.decoded))
    }

    scenario("LineString") {
      val decoded = GeoJson.parse(BaseGeomCases.lineString.encoded)
      assert(decoded == Right(BaseGeomCases.lineString.decoded))
    }

    scenario("Polygon") {
      val decoded = GeoJson.parse(BaseGeomCases.polygon.encoded)
      assert(decoded == Right(BaseGeomCases.polygon.decoded))
    }

    scenario("MultiPoint") {
      val decoded = GeoJson.parse(BaseGeomCases.multiPoint.encoded)
      assert(decoded == Right(BaseGeomCases.multiPoint.decoded))
    }

    scenario("MultiLineString") {
      val decoded = GeoJson.parse(BaseGeomCases.multiLineString.encoded)
      assert(decoded == Right(BaseGeomCases.multiLineString.decoded))
    }

    scenario("MultiPolygon") {
      val decoded = GeoJson.parse(BaseGeomCases.multiPolygon.encoded)
      assert(decoded == Right(BaseGeomCases.multiPolygon.decoded))
    }

    scenario("GeometryCollection") {
      val decoded = GeoJson.parse(BaseGeomCases.geometryCollection.encoded)
      assert(decoded == Right(BaseGeomCases.geometryCollection.decoded))
    }
  }

  feature("Feature") {
    scenario("No Properties") {
      val decoded = GeoJson.parse(FeatureCases.noProps.encoded)
      assert(decoded == Right(FeatureCases.noProps.decoded))
    }

    scenario("Null properties") {
      decodeCase(FeatureCases.nullProps)
    }

    scenario("Empty feature") {
      val decoded = GeoJson.parse(FeatureCases.empty.encoded)
      assert(decoded == Right(FeatureCases.empty.decoded))
    }

    scenario("Null geometry") {
      decodeCase(FeatureCases.nullGeom)
    }

    scenario("Properties") {
      val decoded = GeoJson.parse(FeatureCases.props.encoded)
      assert(decoded == Right(FeatureCases.props.decoded))
    }

    scenario("String ID") {
      val decoded = GeoJson.parse(FeatureCases.stringId.encoded)
      assert(decoded == Right(FeatureCases.stringId.decoded))
    }

    scenario("Numeric ID") {
      val decoded = GeoJson.parse(FeatureCases.intId.encoded)
      assert(decoded == Right(FeatureCases.intId.decoded))
    }

    scenario("BBox") {
      decodeCase(FeatureCases.bbox)
    }

    scenario("All attributes") {
      val decoded = GeoJson.parse(FeatureCases.intId.encoded)
      assert(decoded == Right(FeatureCases.intId.decoded))
    }

    scenario("xyz geom") {
      decodeCase(FeatureCases.xyz)
    }

    scenario("xyzm geom") {
      decodeCase(FeatureCases.xyzm)
    }
  }

  feature("FeatureCollection") {
    scenario("1 Feature, no Foreign Members") {
      val decoded = GeoJson.parse(FeatureCollectionCases.noForeignMembers.encoded)
      assert(decoded == Right(FeatureCollectionCases.noForeignMembers.decoded))
    }

    scenario("Foreign members") {
      decodeCase(FeatureCollectionCases.foreignMembers)
    }

    scenario("BBox") {
      decodeCase(FeatureCollectionCases.bbox)
    }
  }

  def decodeCase(c: Case) = {
    val decoded = GeoJson.parse(c.encoded)
    assert(decoded == Right(c.decoded))
  }
}
