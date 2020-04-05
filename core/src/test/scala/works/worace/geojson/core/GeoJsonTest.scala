package works.worace.geojson.core

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

class GeoJsonTest extends munit.FunSuite {
  import TestData._

  test("Decodes with appropriate Option value") {
    def parseCoord(s: String): Either[io.circe.Error, Coordinate] = {
      import CoordinateCodec.implicits._
      io.circe.parser.decode[Coordinate](s)
    }

    assert(parseCoord(coordXY) == Right(Coordinate(102.0, 0.5, None, None)))
    assert(parseCoord(coordXYZ) == Right(Coordinate(102.0, 0.5, Some(1.0), None)))
    assert(parseCoord(coordXYZM) == Right(Coordinate(102.0, 0.5, Some(1.0), Some(2.0))))
    assert(
      parseCoord(coordTooFew) == Left(DecodingFailure("Invalid GeoJson Coordinates", List()))
    )
    assert(
      parseCoord(coordTooMany) == Left(DecodingFailure("Invalid GeoJson Coordinates", List()))
    )
  }

  // feature("2-D Basic Geometries") {
  test("Point") {
    val decodedPoint = GeoJson.parse(BaseGeomCases.point.encoded)
    assert(decodedPoint == Right(BaseGeomCases.point.decoded))
  }

  test("LineString") {
    val decoded = GeoJson.parse(BaseGeomCases.lineString.encoded)
    assert(decoded == Right(BaseGeomCases.lineString.decoded))
  }

  test("Polygon") {
    val decoded = GeoJson.parse(BaseGeomCases.polygon.encoded)
    assert(decoded == Right(BaseGeomCases.polygon.decoded))
  }

  test("MultiPoint") {
    val decoded = GeoJson.parse(BaseGeomCases.multiPoint.encoded)
    assert(decoded == Right(BaseGeomCases.multiPoint.decoded))
  }

  test("MultiLineString") {
    val decoded = GeoJson.parse(BaseGeomCases.multiLineString.encoded)
    assert(decoded == Right(BaseGeomCases.multiLineString.decoded))
  }

  test("MultiPolygon") {
    val decoded = GeoJson.parse(BaseGeomCases.multiPolygon.encoded)
    assert(decoded == Right(BaseGeomCases.multiPolygon.decoded))
  }

  test("GeometryCollection") {
    val decoded = GeoJson.parse(BaseGeomCases.geometryCollection.encoded)
    assert(decoded == Right(BaseGeomCases.geometryCollection.decoded))
  }
  // }

  // feature("Feature") {
  test("No Properties") {
    val decoded = GeoJson.parse(FeatureCases.noProps.encoded)
    assert(decoded == Right(FeatureCases.noProps.decoded))
  }

  test("Null properties") {
    decodeCase(FeatureCases.nullProps)
  }

  test("Empty feature") {
    val decoded = GeoJson.parse(FeatureCases.empty.encoded)
    assert(decoded == Right(FeatureCases.empty.decoded))
  }

  test("Null geometry") {
    decodeCase(FeatureCases.nullGeom)
  }

  test("Properties") {
    val decoded = GeoJson.parse(FeatureCases.props.encoded)
    assert(decoded == Right(FeatureCases.props.decoded))
  }

  test("String ID") {
    val decoded = GeoJson.parse(FeatureCases.stringId.encoded)
    assert(decoded == Right(FeatureCases.stringId.decoded))
  }

  test("Numeric ID") {
    val decoded = GeoJson.parse(FeatureCases.intId.encoded)
    assert(decoded == Right(FeatureCases.intId.decoded))
  }

  test("BBox") {
    decodeCase(FeatureCases.bbox)
  }

  test("All attributes") {
    val decoded = GeoJson.parse(FeatureCases.intId.encoded)
    assert(decoded == Right(FeatureCases.intId.decoded))
  }

  test("xyz geom") {
    decodeCase(FeatureCases.xyz)
  }

  test("xyzm geom") {
    decodeCase(FeatureCases.xyzm)
  }
  // }

  // feature("FeatureCollection") {
  test("1 Feature, no Foreign Members") {
    val decoded = GeoJson.parse(FeatureCollectionCases.noForeignMembers.encoded)
    assert(decoded == Right(FeatureCollectionCases.noForeignMembers.decoded))
  }

  test("Foreign members") {
    decodeCase(FeatureCollectionCases.foreignMembers)
  }

  test("BBox") {
    decodeCase(FeatureCollectionCases.bbox)
    encodeCase(FeatureCollectionCases.bbox)
  }
  // }

  def decodeCase(c: Case) = {
    val decoded = GeoJson.parse(c.encoded)
    assert(decoded == Right(c.decoded))
  }

  def encodeCase(c: Case) = {
    val rt = GeoJson.fromJson(c.decoded.encode)
    assert(rt == Right(c.decoded))
  }
}
