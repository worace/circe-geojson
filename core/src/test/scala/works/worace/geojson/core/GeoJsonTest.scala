package works.worace.geojson.core

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

class GeoJsonTest extends munit.FunSuite {
  import TestData._

  test("Decodes with appropriate Option value") {
    def parseCoord(s: String): Either[io.circe.Error, Coordinate] = {
      import CoordinateCodec.implicits._
      io.circe.parser.decode[Coordinate](s)
    }

    assertEquals(parseCoord(coordXY), Right(Coordinate(102.0, 0.5, None, None)))
    assertEquals(parseCoord(coordXYZ), Right(Coordinate(102.0, 0.5, Some(1.0), None)))
    assertEquals(parseCoord(coordXYZM), Right(Coordinate(102.0, 0.5, Some(1.0), Some(2.0))))
    assertEquals(
      parseCoord(coordTooFew), Left(DecodingFailure("Invalid GeoJson Coordinates", List()))
    )
    assertEquals(
      parseCoord(coordTooMany), Left(DecodingFailure("Invalid GeoJson Coordinates", List()))
    )
  }

  // feature("2-D Basic Geometries") {
  test("Point") {
    val decodedPoint = GeoJson.parse(BaseGeomCases.point.encoded)
    assertEquals(decodedPoint, Right(BaseGeomCases.point.decoded))
  }

  test("LineString") {
    val decoded = GeoJson.parse(BaseGeomCases.lineString.encoded)
    assertEquals(decoded, Right(BaseGeomCases.lineString.decoded))
  }

  test("Polygon") {
    val decoded = GeoJson.parse(BaseGeomCases.polygon.encoded)
    assertEquals(decoded, Right(BaseGeomCases.polygon.decoded))
  }

  test("MultiPoint") {
    val decoded = GeoJson.parse(BaseGeomCases.multiPoint.encoded)
    assertEquals(decoded, Right(BaseGeomCases.multiPoint.decoded))
  }

  test("MultiLineString") {
    val decoded = GeoJson.parse(BaseGeomCases.multiLineString.encoded)
    assertEquals(decoded, Right(BaseGeomCases.multiLineString.decoded))
  }

  test("MultiPolygon") {
    val decoded = GeoJson.parse(BaseGeomCases.multiPolygon.encoded)
    assertEquals(decoded, Right(BaseGeomCases.multiPolygon.decoded))
  }

  test("GeometryCollection") {
    val decoded = GeoJson.parse(BaseGeomCases.geometryCollection.encoded)
    assertEquals(decoded, Right(BaseGeomCases.geometryCollection.decoded))
  }
  // }

  // feature("Feature") {
  test("No Properties") {
    val decoded = GeoJson.parse(FeatureCases.noProps.encoded)
    assertEquals(decoded, Right(FeatureCases.noProps.decoded))
  }

  test("Null properties") {
    decodeCase(FeatureCases.nullProps)
  }

  test("Empty feature") {
    val decoded = GeoJson.parse(FeatureCases.empty.encoded)
    assertEquals(decoded, Right(FeatureCases.empty.decoded))
  }

  test("Null geometry") {
    decodeCase(FeatureCases.nullGeom)
  }

  test("Properties") {
    val decoded = GeoJson.parse(FeatureCases.props.encoded)
    assertEquals(decoded, Right(FeatureCases.props.decoded))
  }

  test("String ID") {
    val decoded = GeoJson.parse(FeatureCases.stringId.encoded)
    assertEquals(decoded, Right(FeatureCases.stringId.decoded))
  }

  test("Numeric ID") {
    decodeCase(FeatureCases.intId)
  }

  test("BBox") {
    decodeCase(FeatureCases.bbox)
  }

  test("All attributes") {
    decodeCase(FeatureCases.all)
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
    decodeCase(FeatureCollectionCases.noForeignMembers)
    // val decoded = GeoJson.parse(.encoded)
    // assert(decoded == Right(FeatureCollectionCases.noForeignMembers.decoded))
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
    assertEquals(decoded, Right(c.decoded))
  }

  def encodeCase(c: Case) = {
    val rt = GeoJson.fromJson(c.decoded.encode)
    assertEquals(rt,  Right(c.decoded))
  }
}
