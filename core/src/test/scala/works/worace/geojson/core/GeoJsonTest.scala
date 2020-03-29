package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

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
      encodeCase(FeatureCollectionCases.bbox)
    }
  }

  def decodeCase(c: Case) = {
    val decoded = GeoJson.parse(c.encoded)
    assert(decoded == Right(c.decoded))
  }

  def encodeCase(c: Case) = {
    val rt = GeoJson.fromJson(c.decoded.encode)
    assert(rt == Right(c.decoded))
  }
}
